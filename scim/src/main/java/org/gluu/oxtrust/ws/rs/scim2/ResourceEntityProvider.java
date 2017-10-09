package org.gluu.oxtrust.ws.rs.scim2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.user.UserResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Created by jgomer on 2017-09-04.
 *
 * A RESTeasy entity provider. It helps the server side code to take the incoming Json content, and convert it to objects
 * deriving from BaseScimResource, in other words, convert Json string to SCIM resource
 * Likewise, it helps the Java client code to do the converse (sending a SCIM resource object to the service by applying
 * the Json conversion)
 */
//@Provider
//@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
//@Produces({Constants.MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
public class ResourceEntityProvider implements MessageBodyReader<UserResource>, MessageBodyWriter<UserResource> {

    private Logger logger = LogManager.getLogger(getClass());

    private ObjectMapper mapper=new ObjectMapper();

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(UserResource.class);
    }

    public UserResource readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

        //Uncomment the following block to be able to see the "string" the client is actually sending
        /*
        BufferedReader br = new BufferedReader(new InputStreamReader(entityStream, Charset.forName("UTF-8")));
        StringBuilder sb=new StringBuilder();
        String tmp="";
        while (tmp!=null) {
            sb.append(tmp);
            tmp = br.readLine();
        }
        tmp=sb.toString();
        logger.debug("got user {}", tmp);
        return mapper.readValue(tmp, UserResource.class);
        */
        InputStreamReader ins = new InputStreamReader(entityStream, Charset.forName("UTF-8"));
        return mapper.readValue(ins, UserResource.class);

    }

    /*
     * =====================================================================
     *  The following methods are used by client side code (not server code)
     * =====================================================================
     */

    public long getSize(UserResource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType){
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType){
        return type.equals(UserResource.class);
    }

    public void writeTo(UserResource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String,Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException{
        entityStream.write(mapper.writeValueAsString(t).getBytes());
    }

}
