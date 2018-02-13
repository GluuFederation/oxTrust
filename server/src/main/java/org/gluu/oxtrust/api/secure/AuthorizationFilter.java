/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.secure;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.model.ErrorResponse;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.uma.ApiUmaProtectionService;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.ClientInfoClient;
import org.xdi.oxauth.client.ClientInfoResponse;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;

/**
 * This class checks whether authorization header is present and is valid before
 * API service methods are actually called. To protect methods with this filter
 * just add the UmaSecure annotation to them
 *
 * @author Yuriy Movchan Date: 02/13/2017
 */
@Provider
@UmaSecure
public class AuthorizationFilter implements ContainerRequestFilter {

	@Inject
	private Logger log;

	@Context
	private ResourceInfo resourceInfo;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private OpenIdService openIdService;

	@Inject
	private ApiUmaProtectionService apiUmaProtectionService;

	@Inject
	private UmaPermissionService umaPermissionService;

	/**
	 * This method performs the protection check of API invocations: it searches for
	 * the "Authorization" Header param and does the respective processing by
	 * calling test mode or UMA validation token logic. If successful, the request
	 * follows to its destination service object. If not, a Response object is
	 * returned immediately signaling an authorization error
	 * 
	 * @param requestContext
	 *            The ContainerRequestContext associated to filter execution
	 * @throws IOException
	 *             Whenever checking the authorization throws an exception as well
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// log.warn("Bypassing protection TEMPORARILY");
		List<String> scopes = getRequestedScopes();
		log.info("Requested UMA scopes: '{}'", scopes);

		Response authorizationResponse;
		log.info("==== API Service call intercepted ====");
		String authorization = requestContext.getHeaderString("Authorization");
		log.info("Authorization header {} found", StringUtils.isEmpty(authorization) ? "not" : "");

		try {
			if (appConfiguration.isScimTestMode()) {
				log.info("API Test Mode is ACTIVE");
				authorizationResponse = processTestModeAuthorization(authorization);
			} else if (apiUmaProtectionService.isEnabled()) {
				log.info("API is protected by UMA");
				authorizationResponse = processUmaAuthorization(requestContext, authorization);
			} else {
				log.info(
						"Please activate UMA or test mode to protect your API endpoints. Read the Gluu API docs to learn more");
				authorizationResponse = getErrorResponse(Status.UNAUTHORIZED, "API API not protected");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			authorizationResponse = getErrorResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		if (authorizationResponse != null) {
			log.info("Authorization passed"); // If authorization passed, proceed with actual processing of request
		} else {
			requestContext.abortWith(authorizationResponse);
		}
	}

	private Response processTestModeAuthorization(String token) throws Exception {
		Response response = null;

		if (StringUtils.isNotEmpty(token)) {
			token = token.replaceFirst("Bearer\\s+", "");
			log.debug("Validating token {}", token);

			String clientInfoEndpoint = openIdService.getOpenIdConfiguration().getClientInfoEndpoint();
			ClientInfoClient clientInfoClient = new ClientInfoClient(clientInfoEndpoint);
			ClientInfoResponse clientInfoResponse = clientInfoClient.execClientInfo(token);

			if (clientInfoResponse.getErrorType() != null) {
				response = getErrorResponse(Status.UNAUTHORIZED, "Invalid token " + token);
				log.debug("Error validating access token: {}", clientInfoResponse.getErrorDescription());
			}
		} else {
			log.info("Request is missing authorization header");
			// see section 3.12 RFC 7644
			response = getErrorResponse(Status.INTERNAL_SERVER_ERROR, "No authorization header found");
		}
		return response;

	}

	private Response processUmaAuthorization(ContainerRequestContext requestContext, String authorization) throws Exception {
		List<String> scopes = getRequestedScopes();
		if (scopes == null) {
			return getErrorResponse(Status.UNAUTHORIZED, "Invalid API method security restrictions");
		}

		log.debug("Requested access to '{}' with scopes '{}'", requestContext.getUriInfo().getPath(), scopes);

		Token patToken;
		try {
			patToken = apiUmaProtectionService.getPatToken();
		} catch (UmaProtectionException ex) {
			return getErrorResponse(Status.INTERNAL_SERVER_ERROR, "Failed to obtain PAT token");
		}

		Pair<Boolean, Response> rptTokenValidationResult = umaPermissionService.validateRptToken(patToken,
				authorization, apiUmaProtectionService.getUmaResourceId(), scopes);
		if (rptTokenValidationResult.getFirst()) {
			if (rptTokenValidationResult.getSecond() != null) {
				return rptTokenValidationResult.getSecond();
			}
		} else {
			return getErrorResponse(Status.UNAUTHORIZED, "Invalid RPT token");
		}

		return null;
	}

	private List<String> getRequestedScopes() {
		Class<?> resourceClass = resourceInfo.getResourceClass();
		UmaSecure typeAnnotation = resourceClass.getAnnotation(UmaSecure.class);
		if (typeAnnotation == null) {
			return Collections.emptyList();
		}

		List<String> scopes = new ArrayList<String>();
		scopes.addAll(getResourceScopes(typeAnnotation.scopes()));

		Method resourceMethod = resourceInfo.getResourceMethod();
		UmaSecure methodAnnotation = resourceMethod.getAnnotation(UmaSecure.class);
		if (methodAnnotation != null) {
			scopes.addAll(getResourceScopes(methodAnnotation.scopes()));
		}

		return scopes;
	}
	
	private List<String> getResourceScopes(String[] scopes) {
		List<String> result = new ArrayList<String>();
		if ((scopes == null) || (scopes.length == 0)) {
			return result;
		}
		
		String baseEndpoint = appConfiguration.getBaseEndpoint();
		if (!baseEndpoint.endsWith("/")) {
			baseEndpoint += "/";
		}

		for (String scope : scopes) {
			String umaIssuerScope = baseEndpoint + scope;
			result.add(umaIssuerScope);
		}
		
		return result;
	}

    public static Response getErrorResponse(Response.Status status, String detail) {
        return getErrorResponse(status.getStatusCode(), detail);
    }

    public static Response getErrorResponse(int statusCode, String detail) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(String.valueOf(statusCode));
        errorResponse.setDetail(detail);

        return Response.status(statusCode).entity(errorResponse).build();
    }

}
