package org.gluu.oxtrust.service.uma;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.uma.PermissionRegistrationService;
import org.xdi.oxauth.client.uma.RptStatusService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.model.uma.PermissionTicket;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.UmaPermission;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.service.JsonService;
import org.xdi.util.Pair;
import org.xdi.util.StringHelper;

/**
 * Provide methods to work with permissions and RPT tokens
 * 
 * @author Yuriy Movchan Date: 12/06/2016
 */
@ApplicationScoped
@Named("umaPermissionService")
public class UmaPermissionService implements Serializable {

	private static final long serialVersionUID = -3347131971095468865L;

	@Inject
	private Logger log;

	@Inject
	private UmaConfiguration umaMetadataConfiguration;

	@Inject
	protected AppConfiguration appConfiguration;
		
	@Inject
	private JsonService jsonService;
	
	@Inject
	private AppInitializer appInitializer;

	private PermissionRegistrationService resourceSetPermissionRegistrationService;
	private RptStatusService rptStatusService;

	private final Pair<Boolean, Response> authenticationFailure = new Pair<Boolean, Response>(false, null);
	private final Pair<Boolean, Response> authenticationSuccess = new Pair<Boolean, Response>(true, null);

	@PostConstruct
	public void init() {
		if (this.umaMetadataConfiguration != null) {
			if (appConfiguration.isRptConnectionPoolUseConnectionPooling()) {

				// For more information about PoolingHttpClientConnectionManager, please see:
				// http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/index.html?org/apache/http/impl/conn/PoolingHttpClientConnectionManager.html

				log.info("##### Initializing custom ClientExecutor...");
				PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
				connectionManager.setMaxTotal(appConfiguration.getRptConnectionPoolMaxTotal());
				connectionManager.setDefaultMaxPerRoute(appConfiguration.getRptConnectionPoolDefaultMaxPerRoute());
				connectionManager.setValidateAfterInactivity(appConfiguration.getRptConnectionPoolValidateAfterInactivity() * 1000);
				CloseableHttpClient client = HttpClients.custom()
					.setKeepAliveStrategy(connectionKeepAliveStrategy)
					.setConnectionManager(connectionManager)
					.build();
				ClientExecutor clientExecutor = new ApacheHttpClient4Executor(client);
				log.info("##### Initializing custom ClientExecutor DONE");

				this.resourceSetPermissionRegistrationService = UmaClientFactory.instance().createResourceSetPermissionRegistrationService(this.umaMetadataConfiguration, clientExecutor);
				this.rptStatusService = UmaClientFactory.instance().createRptStatusService(this.umaMetadataConfiguration, clientExecutor);

			} else {
				this.resourceSetPermissionRegistrationService = UmaClientFactory.instance().createResourceSetPermissionRegistrationService(this.umaMetadataConfiguration);
				this.rptStatusService = UmaClientFactory.instance().createRptStatusService(this.umaMetadataConfiguration);
			}
		}
	}

	public Pair<Boolean, Response> validateRptToken(Token patToken, String authorization, String resourceSetId, String scopeId) {
		if ((patToken == null) || (authorization == null) || !authorization.startsWith("Bearer ")) {
			return authenticationFailure;
		}

		String rptToken = authorization.substring(7);
		boolean isGat = rptToken.startsWith("gat_");

        RptIntrospectionResponse rptStatusResponse = getStatusResponse(patToken, rptToken);
		if ((rptStatusResponse == null) || !rptStatusResponse.getActive()) {
			log.error("Status response for RPT token: '{0}' is invalid", rptToken);
			return authenticationFailure;
		}
		
		boolean rptHasPermissions = isRptHasPermissions(rptStatusResponse);
		if (rptHasPermissions) {
			for (UmaPermission umaPermission : rptStatusResponse.getPermissions()) {
				if ((umaPermission.getScopes() != null) && umaPermission.getScopes().contains(scopeId) &&
						(isGat || StringHelper.equals(resourceSetId, umaPermission.getResourceSetId()))) {
					return authenticationSuccess;
				}
			}

			log.error("Status response for RPT token: '{0}' not contains right permission", rptToken);
			return authenticationFailure;
		}

		// If the RPT is valid but has insufficient authorization data for the type of access sought,
        // the resource server SHOULD register a requested permission with the authorization server
        // that would suffice for that scope of access (see Section 3.2),
        // and then respond with the HTTP 403 (Forbidden) status code,
        // along with providing the authorization server's URI in an "as_uri" property in the header,
        // and the permission ticket it just received from the AM in the body in a JSON-encoded "ticket" property.
		
        final String ticket = registerUmaPermissions(patToken, resourceSetId, scopeId);
        if (StringHelper.isEmpty(ticket)) {
        	return authenticationFailure;
        }
        
        Response registerUmaPermissionsResponse = prepareRegisterUmaPermissionsResponse(patToken, resourceSetId, scopeId);
        if (registerUmaPermissionsResponse == null) {
        	return authenticationFailure;
        }

		return new Pair<Boolean, Response>(true, registerUmaPermissionsResponse);
	}

	private boolean isRptHasPermissions(RptIntrospectionResponse umaRptStatusResponse) {
        return !((umaRptStatusResponse.getPermissions() == null) || umaRptStatusResponse.getPermissions().isEmpty());
    }

	private RptIntrospectionResponse getStatusResponse(Token patToken, String rptToken) {
		String authorization = "Bearer " + patToken.getAccessToken();

		// Determine RPT token to status
        RptIntrospectionResponse rptStatusResponse = null;
		try {
			rptStatusResponse = this.rptStatusService.requestRptStatus(authorization, rptToken, "");
		} catch (Exception ex) {
			log.error("Failed to determine RPT status", ex);
			ex.printStackTrace();
		}

		// Validate RPT status response
		if ((rptStatusResponse == null) || !rptStatusResponse.getActive()) {
			return null;
		}

		return rptStatusResponse;
	}

	private String registerUmaPermissions(Token patToken, String resourceSetId, String umaScope) {
		String authorization = "Bearer " + patToken.getAccessToken();

		// Register permissions for resource set
        UmaPermission resourceSetPermissionRequest = new UmaPermission();
        resourceSetPermissionRequest.setResourceSetId(resourceSetId);

        resourceSetPermissionRequest.setScopes(Arrays.asList(umaScope));

        PermissionTicket resourceSetPermissionTicket = null;
        try {
        	resourceSetPermissionTicket = this.resourceSetPermissionRegistrationService.registerResourceSetPermission(
        			authorization,
                    getHost(umaMetadataConfiguration.getIssuer()),
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

	private Response prepareRegisterUmaPermissionsResponse(Token patToken, String resourceSetId, String umaScope) {
		String ticket = registerUmaPermissions(patToken, resourceSetId, umaScope);
		if (StringHelper.isEmpty(ticket)) {
			return null;
		}

    	String entity = null;
		try {
			entity = jsonService.objectToJson(new PermissionTicket(ticket));
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
			        header("host_id", getHost(appConfiguration.getIdpUrl())).
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

	private ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
		@Override
		public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {

			HeaderElementIterator headerElementIterator = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));

			while (headerElementIterator.hasNext()) {

				HeaderElement headerElement = headerElementIterator.nextElement();

				String name = headerElement.getName();
				String value = headerElement.getValue();

				if (value != null && name.equalsIgnoreCase("timeout")) {
					return Long.parseLong(value) * 1000;
				}
			}

			// Set own keep alive duration if server does not have it
			return appConfiguration.getRptConnectionPoolCustomKeepAliveTimeout() * 1000;
		}
	};

}