package org.gluu.oxtrust.api.server.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.slf4j.Logger;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES, description = "Attributes webservice")
@ApplicationScoped
public class AttributeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private AttributeService attributeService;

	public AttributeWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all attributes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllAttributes() {
		log(logger, "Processing getAllAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes();
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.ACTIVE)
	@ApiOperation(value = "Get all actives attributes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllActivesAttributes() {
		log(logger, "Processing getAllActivesAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes().stream()
					.filter(e -> e.getStatus() == GluuStatus.ACTIVE).collect(Collectors.toList());
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INACTIVE)
	@ApiOperation(value = "Get all inactives attributes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAllInActivesAttributes() {
		log(logger, "Processing getAllInActivesAttributes()");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes().stream()
					.filter(e -> e.getStatus() == GluuStatus.INACTIVE).collect(Collectors.toList());
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get attribute by inum")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAttributeByInum(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Processing getAttributeByInum()");
		try {
			Preconditions.checkNotNull(inum);
			return Response.ok(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(ApiConstants.SEARCH)
	@ApiOperation(value = "Search attributes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response searchAttributes(@QueryParam(ApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(ApiConstants.SIZE) int size) {
		log(logger, "Processing searchAttributes()");
		try {
			List<GluuAttribute> attributes = attributeService.searchAttributes(pattern, size);
			return Response.ok(attributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add new attribute")
	@ApiResponses(value = { @ApiResponse(code = 200, response = GluuAttribute.class, message = "Success"),
			@ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response createAttribute(GluuAttribute gluuAttribute) {
		log(logger, "Processing createAttribute()");
		try {
			Preconditions.checkNotNull(gluuAttribute, "Attempt to create null attribute");
			String inum = attributeService.generateInumForNewAttribute();
			gluuAttribute.setInum(inum);
			gluuAttribute.setDn(attributeService.getDnForAttribute(inum));
			attributeService.addAttribute(gluuAttribute);
			return Response.ok(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update new attribute")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute.class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateAttribute(GluuAttribute gluuAttribute) {
		log(logger, "Processing updateAttribute()");
		try {
			Preconditions.checkNotNull(gluuAttribute, "Attempt to update null attribute");
			String inum = gluuAttribute.getInum();
			GluuAttribute existingAttribute = attributeService.getAttributeByInum(inum);
			if (existingAttribute != null) {
				gluuAttribute.setInum(existingAttribute.getInum());
				attributeService.updateAttribute(gluuAttribute);
				return Response.ok(attributeService.getAttributeByInum(existingAttribute.getInum())).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(ApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an attribute")
	@ApiResponses(value = {
			@ApiResponse(code = 200, response = GluuAttribute[].class, message = Constants.RESULT_SUCCESS),
			@ApiResponse(code = 404, message = "Not found"), @ApiResponse(code = 500, message = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAttribute(@PathParam(ApiConstants.INUM) @NotNull String inum) {
		log(logger, "Processing deleteAttribute()");
		try {
			Preconditions.checkNotNull(inum);
			GluuAttribute gluuAttribute = attributeService.getAttributeByInum(inum);
			if (gluuAttribute != null) {
				attributeService.removeAttribute(gluuAttribute);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response deleteAttributes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

}