/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.gluu.oxtrust.service.uma.BaseUmaProtectionService;
import org.gluu.oxtrust.service.uma.BindingUrls;
import org.gluu.oxtrust.service.uma.ScimUmaProtectionService;
import org.jboss.weld.inject.WeldInstance;
import org.slf4j.Logger;

/**
 * A RestEasy filter to centralize protection of APIs with UMA based on path pattern.
 * Created by jgomer on 2017-11-25.
 * @author Yuriy Movchan Date: 02/14/2017
 */
//To protect methods with this filter just add the @ProtectedApi annotation to them and ensure there is a proper subclass
//of {@link BaseUmaProtectionService} that can handle specific protection logic for your particular case
@Provider
@ProtectedApi
public class AuthorizationProcessingFilter implements ContainerRequestFilter {

    @Inject
    private Logger log;

    @Inject
    private ScimUmaProtectionService scimUmaProtectionService;

    @Context
    private HttpHeaders httpHeaders;

    @Context
	private ResourceInfo resourceInfo;

    @Inject
    private WeldInstance<BaseUmaProtectionService> protectionServiceInstance;

    private Map<String, Class<BaseUmaProtectionService>> protectionMapping;

    /**
     * This method performs the protection check of service invocations: it provokes returning an early error response if
     * the underlying protection logic does not succeed, otherwise, makes the request flow to its destination service object
     * @param requestContext The ContainerRequestContext associated to filter execution
     * @throws IOException In practice no exception is thrown here. It's present to conform to interface implemented.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path=requestContext.getUriInfo().getPath();
        //This is a path relative to the resteasy base URI (the application path marked with @ApplicationPath)

        log.trace("REST call to '{}' intercepted", path);
        BaseUmaProtectionService protectionService=null;

        for (String prefix : protectionMapping.keySet()){
            if (path.startsWith(prefix)){
                protectionService=protectionServiceInstance.select(protectionMapping.get(prefix)).get();
                break;
            }
        }

        if (protectionService==null){
            log.warn("No concrete UMA protection mechanism is associated to this path (resource will be accessed anonymously)");
        }
        else{
            log.info("Path is protected, proceeding with authorization processing...");

            Response authorizationResponse=protectionService.processAuthorization(httpHeaders, resourceInfo);
            if (authorizationResponse == null)
                log.info("Authorization passed");   //If authorization passed, proceed with actual processing of request
            else
                requestContext.abortWith(authorizationResponse);
        }

    }

    /**
     * Builds a map around url patterns and service beans that are aimed to perform actual protection
     */
    @PostConstruct
    private void init() {
        protectionMapping=new HashMap<String, Class<BaseUmaProtectionService>>();
        for (WeldInstance.Handler<BaseUmaProtectionService> handler : protectionServiceInstance.handlers()){

        	Class<BaseUmaProtectionService> beanClass = (Class<BaseUmaProtectionService>) handler.getBean().getBeanClass();
            BindingUrls annotation=beanClass.getAnnotation(BindingUrls.class);
            if (annotation!=null){
                //annotation.value() is never null, at most, it's empty array
                for (String pattern : annotation.value()){
                    if (pattern.length()>0) {
                        //pattern, can never be null
                        protectionMapping.put(pattern, beanClass);
                        //If two beans pretend to protect the same url, only the last in the list will take effect
                    }
                }
            }
        }
    }

}
