/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.schema.SchemaExtensionHolder;
import org.gluu.oxtrust.service.scim2.schema.SchemaTypeMapping;
import org.gluu.oxtrust.ws.rs.scim.BaseScimWebService;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/**
 * @author Rahat Ali Date: 05.08.2015
 */
@Name("resourceTypesWs")
@Path("/scim/v2/ResourceTypes")
@Api(value = "/v2/ResourceTypes", description = "SCIM 2.0 ResourceType Endpoint (https://tools.ietf.org/html/rfc7643#section-6)")
public class ResourceTypeWS extends BaseScimWebService {

	@Logger
	private Log log;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response listGroups(@HeaderParam("Authorization") String authorization) throws Exception {

		ListResponse resouceTypes = new ListResponse();
		ResourceType resource = new ResourceType();
		resource.setDescription("User Endpoint");
		resource.setEndpoint("/Users");
		resource.setName("User");
		resource.setId("User");
		resource.setSchema(Constants.USER_EXT_SCHEMA_ID);

		List<SchemaExtensionHolder> schemaExtensions = new ArrayList<SchemaExtensionHolder>();
		SchemaExtensionHolder userExtensionSchema = new SchemaExtensionHolder();
		userExtensionSchema.setSchema(Constants.USER_EXT_SCHEMA_ID);
		userExtensionSchema.setRequired(false);
		schemaExtensions.add(userExtensionSchema);
		resource.setSchemaExtensions(schemaExtensions);

		resouceTypes.getResources().add(resource);
		
		resource = new ResourceType();
		resource.setDescription("Group Endpoint");
		resource.setEndpoint("/Groups");
		resource.setName("Group");
		resource.setId("Group");
		resource.setSchema(Constants.GROUP_CORE_SCHEMA_ID);

		resouceTypes.getResources().add(resource);

		URI location = new URI("/v2/ResourceTypes");
		return Response.ok(resouceTypes).location(location).build();
	}
}
