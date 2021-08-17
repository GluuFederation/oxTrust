package org.gluu.oxtrust.service.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.gluu.config.oxtrust.ScimMode;
import org.gluu.oxtrust.auth.uma.BindingUrls;
import org.gluu.oxtrust.auth.uma.ScimUmaProtectionService;
import org.gluu.oxtrust.auth.oauth.OAuthProtectionService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.service.JsonConfigurationService;

import org.slf4j.Logger;

import static org.gluu.oxtrust.util.OxTrustConstants.API_PROTECTION_BYPASS_PROPERTY;

@Provider
@ProtectedApi
@Priority(Priorities.AUTHENTICATION - 1)
@RequestScoped
public class OAuthAuthorizationProcessingFilter implements ContainerRequestFilter {

	private static final List<String> SCIM_PATHS;
	
	@Inject
	private Logger log;

    @Inject
    private JsonConfigurationService jsonConfigurationService;

	@Inject
	private OAuthProtectionService protectionService;

	@Context
	private HttpHeaders httpHeaders;

	@Context
	private ResourceInfo resourceInfo;
	
	static {
		SCIM_PATHS = Arrays.asList(optAnnnotation(ScimUmaProtectionService.class, BindingUrls.class)
			.map(BindingUrls::value).orElse(new String[0])); 
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		String path = requestContext.getUriInfo().getPath();
		log.debug("REST call to '{}' intercepted", path);
		
		// TODO: in 5.0 these scim checks will not be required. OAuth will be the only protection mechanism for
		// SCIM, Passport, etc. with ProtectedApi annotation regardless of path URI
		ScimMode mode = null;
		if (SCIM_PATHS.stream().filter(path::startsWith).findFirst().isPresent()) {
            log.info("==== SCIM Service call intercepted ====");  
			// it's an SCIM request!
			mode = jsonConfigurationService.getOxTrustappConfiguration().getScimProperties().getProtectionMode();
		}
		
		if (ScimMode.OAUTH.equals(mode)) {
            log.info("SCIM is protected by OAuth");
			List<String> oauthScopes = getRequestedScopes(resourceInfo);

			log.debug("Path is protected, proceeding with authorization processing...");
			Response authorizationResponse = protectionService.processAuthorization(httpHeaders, oauthScopes);
			
			if (authorizationResponse == null) {
				// prevent UMA authorization check to happen
				requestContext.setProperty(API_PROTECTION_BYPASS_PROPERTY, "");
				log.debug("Authorization passed");
				// Actual processing of request proceeds
			} else {
				requestContext.abortWith(authorizationResponse);
			}			
		}
		
	}

	private List<String> getRequestedScopes(ResourceInfo resourceInfo) {
		List<String> scopes = new ArrayList<>();
		scopes.addAll(getScopesFromAnnotation(resourceInfo.getResourceClass()));
		scopes.addAll(getScopesFromAnnotation(resourceInfo.getResourceMethod()));
		return scopes;
	}

	private List<String> getScopesFromAnnotation(AnnotatedElement elem) {		
		return optAnnnotation(elem, ProtectedApi.class).map(ProtectedApi::oauthScopes)
		    .map(Arrays::asList).orElse(Collections.emptyList());
	}	
	
	private static <T extends Annotation> Optional<T> optAnnnotation(AnnotatedElement elem, Class<T> cls) {
		return Optional.ofNullable(elem.getAnnotation(cls));
	}
	
}
