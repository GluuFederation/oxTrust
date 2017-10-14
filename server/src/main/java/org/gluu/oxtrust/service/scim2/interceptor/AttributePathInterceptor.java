package org.gluu.oxtrust.service.scim2.interceptor;

import org.gluu.oxtrust.model.scim2.SearchRequest;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * This class checks if filter, attributes or excludedAttributes query param contains resource references ($ref) and if so
 * drops the dollar sign of occurrences. This is required for introspection utilities to make their work more accurately.
 * Ideally this could have been implemented with a decorator, but pollutes the code a lot, so this way is more concise
 * Created by jgomer on 2017-10-10.
 */
@RefAdjusted
@Interceptor
@Priority(Interceptor.Priority.APPLICATION+1)
public class AttributePathInterceptor {

    @Inject
    private Logger log;

    private static String dropDollar(Object param){
        return (param!=null && param instanceof String) ? param.toString().replaceAll("\\$ref", "ref") : null;
    }

    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {

        Object[] params=ctx.getParameters();
        Annotation[][] annotations=ctx.getMethod().getParameterAnnotations();

        for (int i = 0; i<annotations.length; i++){
            //Iterate over annotations found at every parameter
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof QueryParam) {
                    String paramName=((QueryParam)annotation).value();

                    if ((paramName.equals(QUERY_PARAM_FILTER) || paramName.equals(QUERY_PARAM_ATTRIBUTES) ||
                            paramName.equals(QUERY_PARAM_EXCLUDED_ATTRS))){
                        log.debug("Removing '$' char (if any) from {} param", paramName);
                        params[i]=dropDollar(params[i]);
                    }
                }
            }
            if (params[i]!=null && params[i] instanceof SearchRequest){
                log.debug("Removing '$' char (if any) from {} SearchRequest object");
                SearchRequest sr=(SearchRequest) params[i];
                sr.setAttributes(dropDollar(sr.getAttributes()));
                sr.setExcludedAttributes(dropDollar(sr.getExcludedAttributes()));
                sr.setFilter(dropDollar(sr.getFilter()));
            }
        }
        return ctx.proceed();
    }

}
