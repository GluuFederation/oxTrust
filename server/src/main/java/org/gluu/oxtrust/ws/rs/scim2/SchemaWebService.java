/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.Resource;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeLoadingFactory;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeAbstractSerializer;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

import com.wordnik.swagger.annotations.Api;

/**
 * Web service for the /Schemas endpoint.
 *
 * @author Val Pecaoco
 */
@Named("scim2SchemaEndpoint")
@Path("/scim/v2/Schemas")
@Api(value = "/v2/Schemas", description = "SCIM 2.0 Schema Endpoint (https://tools.ietf.org/html/rfc7643#section-4)")
public class SchemaWebService extends BaseScimWebService {

    @Inject
    private Logger log;

	@Inject
	private AppConfiguration appConfiguration;

    /**
     * Retrieves the complete schema.
     *
     * @param authorization
     * @return
     * @throws Exception
     */
    @GET
    @Produces(Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8")
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    public Response listSchemas(@HeaderParam("Authorization") @DefaultValue("") String authorization) throws Exception {

        log.info(" listSchemas() ");

        ListResponse listResponse = new ListResponse();

        List<String> schemas = new ArrayList<String>();
        schemas.add(Constants.LIST_RESPONSE_SCHEMA_ID);
        listResponse.setSchemas(schemas);

        List<SchemaType> schemaTypes = SchemaTypeMapping.getSchemaInstances();
        List<Resource> resources = new ArrayList<Resource>();

        SchemaTypeLoadingFactory factory = new SchemaTypeLoadingFactory();
        for (SchemaType schemaType : schemaTypes) {
            factory.load(appConfiguration, schemaType);
            resources.add(schemaType);
        }

        listResponse.setResources(resources);
        listResponse.setTotalResults(schemaTypes.size());
        listResponse.setItemsPerPage(10);
        listResponse.setStartIndex(1);

        URI location = new URI(appConfiguration.getBaseEndpoint() + "/scim/v2/Schemas");

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
    @Produces(Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8")
    @HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
    public Response getSchemaById(@HeaderParam("Authorization") @DefaultValue("") String authorization, @PathParam("id") String id) throws Exception {

        log.info(" getSchemaById(), id = '" + id + "'");

        SchemaTypeLoadingFactory factory = new SchemaTypeLoadingFactory();
        SchemaType schemaType = factory.load(appConfiguration, id);

        if (schemaType == null) {
            log.info(" NOT FOUND: schema with id = '" + id + "'");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        URI location = new URI(appConfiguration.getBaseEndpoint() + "/scim/v2/Schemas/" + id);

        // Serialize to JSON
        String json = serialize(schemaType);

        return Response.ok(json).location(location).build();
    }

    private String serialize(Serializable serializable) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        SimpleModule customSchemaTypeAstractModule = new SimpleModule("CustomSchemaTypeAbstractModule", new Version(1, 0, 0, ""));
        SchemaTypeAbstractSerializer serializer = new SchemaTypeAbstractSerializer();
        customSchemaTypeAstractModule.addSerializer(SchemaType.class, serializer);
        mapper.registerModule(customSchemaTypeAstractModule);

        return mapper.writeValueAsString(serializable);
    }
}
