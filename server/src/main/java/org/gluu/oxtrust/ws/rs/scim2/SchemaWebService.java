/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import com.wordnik.swagger.annotations.Api;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.Resource;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeLoadingFactory;
import org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeAbstractSerializer;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Web service for the /Schemas endpoint.
 *
 * @author Val Pecaoco
 */
@Name("scim2SchemaEndpoint")
@Path("/scim/v2/Schemas")
@Api(value = "/v2/Schemas", description = "SCIM 2.0 Schema Endpoint (https://tools.ietf.org/html/rfc7643#section-4)")
public class SchemaWebService extends BaseScimWebService {

    @Logger
    private Log log;

    /**
     * Retrieves the complete schema.
     *
     * @param authorization
     * @return
     * @throws Exception
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listSchemas(@HeaderParam("Authorization") String authorization) throws Exception {

        log.info(" listSchemas() ");

        ListResponse listResponse = new ListResponse();

        List<String> schemas = new ArrayList<String>();
        schemas.add(Constants.LIST_RESPONSE_SCHEMA_ID);
        listResponse.setSchemas(schemas);

        List<SchemaType> schemaTypes = SchemaTypeMapping.getSchemaInstances();
        List<Resource> resources = new ArrayList<Resource>();

        SchemaTypeLoadingFactory factory = new SchemaTypeLoadingFactory();
        for (SchemaType schemaType : schemaTypes) {
            factory.load(super.applicationConfiguration, schemaType);
            resources.add(schemaType);
        }

        listResponse.setResources(resources);
        listResponse.setTotalResults(schemaTypes.size());
        listResponse.setItemsPerPage(10);
        listResponse.setStartIndex(1);

        URI location = new URI(super.applicationConfiguration.getBaseEndpoint() + "/scim/v2/Schemas");

        // Serialize to JSON
        String json = serialize(listResponse);

        return Response.ok(json).location(location).build();
    }

    /**
     * Retrieves a schema via its id/urn.
     *
     * @param authorization
     * @param id
     * @return
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getSchemaById(@HeaderParam("Authorization") String authorization, @PathParam("id") String id) throws Exception {

        log.info(" getSchemaById(), id = '" + id + "'");

        SchemaTypeLoadingFactory factory = new SchemaTypeLoadingFactory();
        SchemaType schemaType = factory.load(super.applicationConfiguration, id);

        if (schemaType == null) {
            log.info(" NOT FOUND: schema with id = '" + id + "'");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        URI location = new URI(super.applicationConfiguration.getBaseEndpoint() + "/scim/v2/Schemas/" + id);

        // Serialize to JSON
        String json = serialize(schemaType);

        return Response.ok(json).location(location).build();
    }

    private String serialize(Serializable serializable) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleModule customSchemaTypeAstractModule = new SimpleModule("CustomSchemaTypeAbstractModule", new Version(1, 0, 0, ""));
        SchemaTypeAbstractSerializer serializer = new SchemaTypeAbstractSerializer();
        customSchemaTypeAstractModule.addSerializer(SchemaType.class, serializer);
        mapper.registerModule(customSchemaTypeAstractModule);

        return mapper.writeValueAsString(serializable);
    }
}
