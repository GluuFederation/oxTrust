package org.gluu.oxtrust.service.scim2.interceptor;

import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;

import static org.gluu.oxtrust.model.scim2.Constants.QUERY_PARAM_FILTER;

/**
 * Created by jgomer on 2017-09-28.
 */
@RejectFilterParam
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ResourceTypeEndpointInterceptor {

    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {

        Object[] params=ctx.getParameters();
        Annotation[][] annotations=ctx.getMethod().getParameterAnnotations();
        int j=-1;

        for (int i = 0; i<annotations.length && j<0; i++){
            //Iterate over annotations found at every parameter
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType().equals(QueryParam.class)) {
                    //Verifies this is an authz header
                    if (((QueryParam)annotation).value().equals(QUERY_PARAM_FILTER)) {
                        j=i;
                        break;
                    }
                }
            }
        }

        if (j>=0 && params[j]!=null && params[j] instanceof String) {
            BaseScimWebService service=(BaseScimWebService)ctx.getTarget();
            return service.getErrorResponse(Response.Status.FORBIDDEN, ErrorScimType.INVALID_VALUE, "No filter allowed here");
        }
        else
            return ctx.proceed();

    }

}