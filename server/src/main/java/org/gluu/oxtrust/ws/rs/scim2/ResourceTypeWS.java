package org.gluu.oxtrust.ws.rs.scim2;

import com.wordnik.swagger.annotations.Api;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.provider.SchemaExtensionHolder;
import org.gluu.oxtrust.model.scim2.user.Meta;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.xdi.config.oxtrust.AppConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * @author Rahat Ali Date: 05.08.2015
 * Updated by jgomer on 2017-09-25.
 */
@Named("resourceTypesWs")
@Path("/scim/v2/ResourceTypes")
@Api(value = "/v2/ResourceTypes", description = "SCIM 2.0 ResourceType Endpoint (https://tools.ietf.org/html/rfc7643#section-6)")
public class ResourceTypeWS extends BaseScimWebService {

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private UserWebService userService;

    private String location;

    private ResourceType getUserResourceType(){

        ResourceType usrRT=new ResourceType();
        usrRT.setSchemas(Collections.singletonList(RESOURCE_TYPE_SCHEMA_ID));
        usrRT.setId(ResourceType.getType(UserResource.class));
        usrRT.setName(ResourceType.getType(UserResource.class));
        usrRT.setDescription(USER_CORE_SCHEMA_DESCRIPTION);
        usrRT.setEndpoint(userService.getEndpointUrl());
        usrRT.setSchema(UserResource.class.getAnnotation(Schema.class).id());

        SchemaExtensionHolder userExtensionSchema = new SchemaExtensionHolder();
        userExtensionSchema.setSchema(USER_EXT_SCHEMA_ID);
        userExtensionSchema.setRequired(false);

        usrRT.setSchemaExtensions(Collections.singletonList(userExtensionSchema));

        Meta userMeta = new Meta();
        userMeta.setLocation(location + "/User");
        userMeta.setResourceType("ResourceType");
        usrRT.setMeta(userMeta);

        return usrRT;
    }

    @GET
    @Produces(MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT)
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    public Response serve() throws Exception {
        ResourceType usrRT = getUserResourceType();
        return Response.ok(Arrays.asList(usrRT)).location(new URI(location)).build();
    }

    @Path("User")
    @GET
    @Produces(MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT)
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    public Response userResourceType(@HeaderParam("Authorization") String authorization) throws Exception {
        ResourceType usrRT = getUserResourceType();
        return Response.ok(serializeToJson(usrRT)).build();
    }

    @PostConstruct
    public void setup(){
        location=appConfiguration.getBaseEndpoint() + ResourceTypeWS.class.getAnnotation(Path.class).value();
    }

}
