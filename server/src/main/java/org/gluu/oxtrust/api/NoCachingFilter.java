package org.gluu.oxtrust.api;

import org.gluu.oxtrust.util.OxTrustApiConstants;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;

@Provider
public class NoCachingFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        if (isApiRequest(req)) {
            res.getHeaders().add(CACHE_CONTROL, noCache());
        }
    }

    private boolean isApiRequest(ContainerRequestContext req) {
        return req.getUriInfo().getPath().startsWith(OxTrustApiConstants.BASE_API_URL);
    }

    private CacheControl noCache() {
        CacheControl cache = new CacheControl();
        cache.setMaxAge(0);
        cache.setSMaxAge(0);
        cache.setMustRevalidate(true);
        cache.setNoCache(true);
        cache.setNoStore(true);
        cache.setPrivate(true);
        cache.setProxyRevalidate(true);
        return cache;
    }
}
