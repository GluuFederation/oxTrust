package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.Response;

import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.oxauth.client.uma.ResourceSetPermissionRegistrationService;
import org.xdi.oxauth.client.uma.ResourceSetRegistrationService;
import org.xdi.oxauth.client.uma.RptStatusService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.wrapper.UmaClient;
import org.xdi.oxauth.model.uma.MetadataConfiguration;
import org.xdi.oxauth.model.uma.ResourceSetPermissionRequest;
import org.xdi.oxauth.model.uma.ResourceSetPermissionTicket;
import org.xdi.oxauth.model.uma.RptStatusRequest;
import org.xdi.oxauth.model.uma.RptStatusResponse;
import org.xdi.oxauth.model.uma.VersionedResourceSet;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;
import org.xdi.util.security.PropertiesDecrypter;

/**
 * Provide methods to simplify work with UMA Rest services
 * 
 * @author Yuriy Movchan Date: 08/20/2013
 */
@Scope(ScopeType.APPLICATION)
@Name("umaProtectionService")
@AutoCreate
public class UmaProtectionService implements Serializable {

	private static final long serialVersionUID = -1147131971095468865L;

	@Logger
	private Log log;

	@In(required = false)
	private MetadataConfiguration umaMetadataConfiguration;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In
	private JsonService jsonService;
	
	@In
	private AppInitializer appInitializer;

	private Token umaPat;
	private long umaPatAccessTokenExpiration = 0l; // When the "accessToken" will expire;

	private final ReentrantLock lock = new ReentrantLock();

	private ResourceSetPermissionRegistrationService resourceSetPermissionRegistrationService;
	private ResourceSetRegistrationService resourceSetRegistrationService;
	private RptStatusService rptStatusService;
	
	@Create
	public void init() {
		if (this.umaMetadataConfiguration != null) {
	        this.resourceSetPermissionRegistrationService = UmaClientFactory.instance().createResourceSetPermissionRegistrationService(this.umaMetadataConfiguration);
	        this.resourceSetRegistrationService = UmaClientFactory.instance().createResourceSetRegistrationService(this.umaMetadataConfiguration);
			this.rptStatusService = UmaClientFactory.instance().createRptStatusService(this.umaMetadataConfiguration);
		}
	}

	public Token getPatToken() throws UmaProtectionException {
		if (isValidPatToken(this.umaPat, this.umaPatAccessTokenExpiration)) {
			return this.umaPat;
		}

		lock.lock();
		try {
			if (isValidPatToken(this.umaPat, this.umaPatAccessTokenExpiration)) {
				return this.umaPat;
			}

			retrievePatToken();
		} finally {
		  lock.unlock();
		}


		return this.umaPat;
	}

	public boolean isEnabledUmaAuthentication() {
		boolean enabled = (umaMetadataConfiguration != null) && isExistPatToken();
		
		return enabled;
	}

	public boolean isExistPatToken() {
		try {
			return getPatToken() != null;
		} catch (UmaProtectionException ex) {
			log.error("Failed to check UMA PAT token status", ex);
		}

		return false;
	}

	public boolean isRptHasPermissions(RptStatusResponse umaRptStatusResponse) {
		if ((umaRptStatusResponse.getPermissions() == null) || umaRptStatusResponse.getPermissions().isEmpty()) {
			return false;
		}

		return true;
	}

	public RptStatusResponse getStatusResponse(Token patToken, String rptToken) {
		String authorization = "Bearer " + patToken.getAccessToken();

		// Determine RPT token to status
		RptStatusResponse rptStatusResponse = null;
		try {
			RptStatusRequest tokenStatusRequest = new RptStatusRequest(rptToken);
			rptStatusResponse = rptStatusService.requestRptStatus(authorization, tokenStatusRequest);
		} catch (Exception ex) {
			log.error("Failed to determine RPT status", ex);
		}

		// Validate RPT status response
		if ((rptStatusResponse == null) || !rptStatusResponse.getActive()) {
			return null;
		}

		return rptStatusResponse;
	}

	public String registerUmaPermissions(Token patToken, String resourceSetId, String umaScope) {
		String authorization = "Bearer " + patToken.getAccessToken();

		// Load resource set. The idea is to get proper scope from resource set. But currently there is no such information in resource set
		VersionedResourceSet resourceSet = this.resourceSetRegistrationService.getResourceSet(authorization, resourceSetId);

		// Register permissions for resource set
        ResourceSetPermissionRequest resourceSetPermissionRequest = new ResourceSetPermissionRequest();
        resourceSetPermissionRequest.setResourceSetId(resourceSetId);

        resourceSetPermissionRequest.setScopes(Arrays.asList(umaScope));

        ResourceSetPermissionTicket resourceSetPermissionTicket = null;
        try {
        	resourceSetPermissionTicket = this.resourceSetPermissionRegistrationService.registerResourceSetPermission(
        			authorization,
                    getHost(umaMetadataConfiguration.getIssuer()),
                    getHost(applicationConfiguration.getIdpUrl()),
                    resourceSetPermissionRequest);
		} catch (MalformedURLException ex) {
        	log.error("Failed to determine host by URI", ex);
        } catch (ClientResponseFailure ex) {
        	log.error("Failed to register permissions for resource set: '{0}'", ex, resourceSetId);
        }

        if ((resourceSetPermissionTicket == null) || StringHelper.isEmpty(resourceSetPermissionTicket.getTicket())) {
        	log.error("Resource set permission ticket is invalid");
        	return null;
        }

        return resourceSetPermissionTicket.getTicket();
	}
	
	public Response prepareRegisterUmaPermissionsResponse(Token patToken, String resourceSetId, String umaScope) {
		String ticket = registerUmaPermissions(patToken, resourceSetId, umaScope);
		if (StringHelper.isEmpty(ticket)) {
			return null;
		}

    	String entity = null;
		try {
			entity = jsonService.objectToJson(new ResourceSetPermissionTicket(ticket));
		} catch (Exception ex) {
        	log.error("Failed to prepare response", ex);
		}

		if (entity == null) {
			return null;
		}

    	log.debug("Construct response: HTTP 403 (Forbidden), entity: '{0}'",  entity);
        Response response = null;
		try {
			response = Response.status(Response.Status.FORBIDDEN).
			        header("host_id", getHost(applicationConfiguration.getIdpUrl())).
			        header("as_uri",  appInitializer.getUmaConfigurationEndpoint()).
			        header("error", "insufficient_scope").
			        entity(entity).
			        build();
		} catch (MalformedURLException ex) {
        	log.error("Failed to determine host by URI", ex);
		}
                
         return response;
	}

	private String getHost(String uri) throws MalformedURLException {
		URL url = new URL(uri);

		return url.getHost();
	}

	private void retrievePatToken() throws UmaProtectionException {
		this.umaPat = null;
		if (umaMetadataConfiguration == null) {
			return;
		}

		String umaUserPassword = PropertiesDecrypter.decryptProperty(applicationConfiguration.getUmaUserPassword(), true);
		String umaClientPassword = PropertiesDecrypter.decryptProperty(applicationConfiguration.getUmaClientPassword(), true);
		try {
			this.umaPat = UmaClient.requestPat(umaMetadataConfiguration.getUserEndpoint(), umaMetadataConfiguration.getTokenEndpoint(),
					applicationConfiguration.getUmaUserId(), umaUserPassword,
					applicationConfiguration.getUmaClientId(), umaClientPassword,
					applicationConfiguration.getUmaRedirectUri());
			this.umaPatAccessTokenExpiration = computeAccessTokenExpirationTime(this.umaPat.getExpiresIn());
		} catch (Exception ex) {
			throw new UmaProtectionException("Failed to obtain valid UMA PAT token", ex);
		}
		
		if ((this.umaPat == null) || (this.umaPat.getAccessToken() == null) || (this.umaPat.getRefreshToken() == null)) {
			throw new UmaProtectionException("Failed to obtain valid UMA PAT token");
		}
	}

	protected long computeAccessTokenExpirationTime(Integer expiresIn) {
		// Compute "accessToken" expiration timestamp
		Calendar calendar = Calendar.getInstance();
		if (expiresIn != null) {
			calendar.add(Calendar.SECOND, expiresIn);
			calendar.add(Calendar.SECOND, -10); // Subtract 10 seconds to avoid expirations during executing request
		}

		return calendar.getTimeInMillis();
	}

	private boolean isValidPatToken(Token validatePatToken, long validatePatTokenExpiration) {
		final long now = System.currentTimeMillis();

		// Get new access token only if is the previous one is missing or expired
		if ((validatePatToken == null) || (validatePatToken.getAccessToken() == null) || (validatePatTokenExpiration <= now)) {
			return false;
		}

		return true;
	}


}
