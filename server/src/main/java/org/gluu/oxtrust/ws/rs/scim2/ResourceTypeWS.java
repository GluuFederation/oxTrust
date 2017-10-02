package org.gluu.oxtrust.ws.rs.scim2;

import com.wordnik.swagger.annotations.Api;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.provider.SchemaExtensionHolder;
import org.gluu.oxtrust.model.scim2.user.Meta;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.gluu.oxtrust.service.scim2.interceptor.RejectFilterParam;
import org.gluu.oxtrust.service.scim2.serialization.ListResponseJsonSerializer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.*;
import static org.gluu.oxtrust.util.OxTrustConstants.QUERY_PARAMETER_FILTER;

/**
 * @author Rahat Ali Date: 05.08.2015
 * Updated by jgomer on 2017-09-25.
 */
@Named("resourceTypesWs")
@Path("/scim/v2/ResourceTypes")
@Api(value = "/v2/ResourceTypes", description = "SCIM 2.0 ResourceType Endpoint (https://tools.ietf.org/html/rfc7643#section-6)")
public class ResourceTypeWS extends BaseScimWebService {

    @Inject
    private UserWebService userService;

    @Inject
    private ExtensionService extService;

    private String location;

    private ResourceType getUserResourceType(){

        Class<? extends BaseScimResource> cls=UserResource.class;
        Schema schemaAnnot=cls.getAnnotation(Schema.class);

        ResourceType usrRT=new ResourceType();
        usrRT.setId(schemaAnnot.name());
        usrRT.setName(schemaAnnot.name());
        usrRT.setDescription(schemaAnnot.description());
        usrRT.setEndpoint(userService.getEndpointUrl());
        usrRT.setSchema(schemaAnnot.id());

        List<Extension> usrExtensions=extService.getResourceExtensions(cls);
        List<SchemaExtensionHolder> schemaExtensions=new ArrayList<SchemaExtensionHolder>();

        for (Extension extension : usrExtensions){
            SchemaExtensionHolder userExtensionSchema = new SchemaExtensionHolder();
            userExtensionSchema.setSchema(extension.getUrn());
            userExtensionSchema.setRequired(false);

            schemaExtensions.add(userExtensionSchema);
        }
        usrRT.setSchemaExtensions(schemaExtensions);

        Meta userMeta = new Meta();
        userMeta.setLocation(location + "/User");
        userMeta.setResourceType("ResourceType");
        usrRT.setMeta(userMeta);

        return usrRT;
    }

    @GET
    @Produces(MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT)
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @RejectFilterParam
    public Response serve(@QueryParam(QUERY_PARAMETER_FILTER) String filter) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module=new SimpleModule("ListResponseModule", Version.unknownVersion());
        //why not to inject the resource serializer inside the list response serializer? weld simply does not like it!
        module.addSerializer(ListResponse.class, new ListResponseJsonSerializer(resourceSerializer));
        mapper.registerModule(module);

        ListResponse listResponse=new ListResponse(1,1);
        listResponse.addResource(getUserResourceType());
        //TODO: add all other resource types

        String json=mapper.writeValueAsString(listResponse);
        return Response.ok(json).location(new URI(location)).build();

    }

    @Path("User")
    @GET
    @Produces(MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT)
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @RejectFilterParam
    public Response userResourceType(@QueryParam(QUERY_PARAMETER_FILTER) String filter) throws Exception {
        ResourceType usrRT = getUserResourceType();
        return Response.ok(resourceSerializer.serialize(usrRT)).build();
    }

    @PostConstruct
    public void setup(){
        //weld makes you cry if using getClass() here
        location=appConfiguration.getBaseEndpoint() + ResourceTypeWS.class.getAnnotation(Path.class).value();
    }

}
