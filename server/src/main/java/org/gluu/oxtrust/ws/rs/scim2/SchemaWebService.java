/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import com.wordnik.swagger.annotations.Api;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeLoadingFactory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
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

        List<SchemaType> schemas = SchemaTypeMapping.getSchemaInstances();

        SchemaTypeLoadingFactory factory = new SchemaTypeLoadingFactory();
        for (SchemaType schemaType : schemas) {
            factory.load(schemaType);
        }

        URI location = new URI("/v2/Schemas");

        return Response.ok(schemas).location(location).build();
    }

    /**
     * Retrieves the User Extension schema.
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
        SchemaType schemaType = factory.load(id);

        URI location = new URI("/v2/Schemas/" + id);

        if (schemaType == null) {
            log.info(" NOT FOUND: schema with id = '" + id + "'");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(schemaType).location(location).build();
    }
}
