package org.gluu.oxtrust.service.uma;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaRptIntrospectionService;
import org.xdi.oxauth.model.uma.PermissionTicket;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.oxauth.model.uma.UmaMetadata;
import org.xdi.oxauth.model.uma.UmaPermission;
import org.xdi.oxauth.model.uma.UmaPermissionList;
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

	private static final long serialVersionUID = -3347131971095468866L;

	@Inject
	private Logger log;

	@Inject
	private UmaMetadata umaMetadata;

	@Inject
	protected AppConfiguration appConfiguration;
		
	@Inject
	private JsonService jsonService;
	
	@Inject
	private AppInitializer appInitializer;

	private org.xdi.oxauth.client.uma.UmaPermissionService permissionService;
	private UmaRptIntrospectionService rptStatusService;

	private final Pair<Boolean, Response> authenticationFailure = new Pair<Boolean, Response>(false, null);
	private final Pair<Boolean, Response> authenticationSuccess = new Pair<Boolean, Response>(true, null);

	@PostConstruct
	public void init() {
		if (this.umaMetadata != null) {
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

				this.permissionService = UmaClientFactory.instance().createPermissionService(this.umaMetadata, clientExecutor);
				this.rptStatusService = UmaClientFactory.instance().createRptStatusService(this.umaMetadata, clientExecutor);

			} else {
				this.permissionService = UmaClientFactory.instance().createPermissionService(this.umaMetadata);
				this.rptStatusService = UmaClientFactory.instance().createRptStatusService(this.umaMetadata);
			}
		}
	}

	public Pair<Boolean, Response> validateRptToken(Token patToken, String authorization, String umaResourceId, String scopeId) {
		return validateRptToken(patToken, authorization, umaResourceId, Arrays.asList(scopeId));
	}

	public Pair<Boolean, Response> validateRptToken(Token patToken, String authorization, String resourceId, List<String> scopeIds) {
	    /* //caller of this method never pass null patToken
		if (patToken == null) {
	        return authenticationFailure;
		} */

		if (StringHelper.isNotEmpty(authorization) && authorization.startsWith("Bearer ")) {
			String rptToken = authorization.substring(7);
	
	        RptIntrospectionResponse rptStatusResponse = getStatusResponse(patToken, rptToken);
			if ((rptStatusResponse == null) || !rptStatusResponse.getActive()) {
				log.error("Status response for RPT token: '{}' is invalid", rptToken);
				//return authenticationFailure;
			} else{
                boolean rptHasPermissions = isRptHasPermissions(rptStatusResponse);

                if (rptHasPermissions) {
                	// Collect all scopes
                	List<String> returnScopeIds = new LinkedList<String>();
                    for (UmaPermission umaPermission : rptStatusResponse.getPermissions()) {
                        if (umaPermission.getScopes() != null) {
                        	returnScopeIds.addAll(umaPermission.getScopes());
                        }
                    }
                    
                    if (returnScopeIds.containsAll(scopeIds)) {
                        return authenticationSuccess;
                    }

                    log.error("Status response for RPT token: '{}' not contains right permissions", rptToken);
                }
            }
		}

		Response registerPermissionsResponse = prepareRegisterPermissionsResponse(patToken, resourceId, scopeIds);
        if (registerPermissionsResponse == null) {
        	return authenticationFailure;
        }

        return new Pair<Boolean, Response>(true, registerPermissionsResponse);
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

	public String registerResourcePermission(Token patToken, String resourceId, List<String> scopeIds) {

        UmaPermission permission = new UmaPermission();
        permission.setResourceId(resourceId);
        permission.setScopes(scopeIds);

        PermissionTicket ticket = permissionService.registerPermission(
                "Bearer " + patToken.getAccessToken(), UmaPermissionList.instance(permission));
        
        if (ticket == null) {
        	return null;
        }

        return ticket.getTicket();
    }

	private Response prepareRegisterPermissionsResponse(Token patToken, String resourceId, List<String> scopeIds) {
		String ticket = registerResourcePermission(patToken, resourceId, scopeIds);
		if (StringHelper.isEmpty(ticket)) {
			return null;
		}

    	log.debug("Construct response: HTTP 401 (Unauthorized), ticket: '{}'",  ticket);
        Response response = null;
		try {
			String authHeaderValue = String.format("UMA realm=\"Authorization required\", host_id=%s, as_uri=%s, ticket=%s",
					getHost(appConfiguration.getIdpUrl()),  appInitializer.getUmaConfigurationEndpoint(), ticket);
			response = Response.status(Response.Status.UNAUTHORIZED).
			        header("WWW-Authenticate", authHeaderValue).
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