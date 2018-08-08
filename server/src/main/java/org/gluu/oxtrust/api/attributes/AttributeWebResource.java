package org.gluu.oxtrust.api.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import org.gluu.oxtrust.api.openidconnect.BaseWebResource;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.ATTRIBUTES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AttributeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private AttributeService attributeService;

	public AttributeWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all attributes")
	public Response getAllAttributes() {
		log("Get all attributes");
		try {
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes();
			return Response.ok(gluuAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.ACTIVE)
	@ApiOperation(value = "Get all actives attributes")
	public Response getAllActivesAttributes() {
		log("Get all actives attributes");
		try {
			List<GluuAttribute> gluuActivesAttributes = new ArrayList<GluuAttribute>();
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes();
			for (GluuAttribute gluuAttribute : gluuAttributes) {
				if (gluuAttribute.getStatus() == GluuStatus.ACTIVE) {
					gluuActivesAttributes.add(gluuAttribute);
				}
			}
			return Response.ok(gluuActivesAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INACTIVE)
	@ApiOperation(value = "Get all inactives attributes")
	public Response getAllInActivesAttributes() {
		log("Get all inactives attributes");
		try {
			List<GluuAttribute> gluuInActivesAttributes = new ArrayList<GluuAttribute>();
			List<GluuAttribute> gluuAttributes = attributeService.getAllAttributes();
			for (GluuAttribute gluuAttribute : gluuAttributes) {
				if (gluuAttribute.getStatus() == GluuStatus.INACTIVE) {
					gluuInActivesAttributes.add(gluuAttribute);
				}
			}
			return Response.ok(gluuInActivesAttributes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get attribute by inum")
	public Response getAttributeByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		try {
			Objects.requireNonNull(inum);
			log("Get attribute by inum : " + inum);
			return Response.ok(attributeService.getAttributeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search attributes")
	public Response searchAttributes(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) @NotNull String pattern,
			@DefaultValue("1") @QueryParam(OxTrustApiConstants.SIZE) int size) {
		log("Search attributes with pattern = " + pattern + " and size " + size);
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
	public Response createAttribute(GluuAttribute gluuAttribute) {
		log("Add new attribute");
		try {
			Objects.requireNonNull(gluuAttribute, "Attempt to create null attribute");
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
	public Response updateAttribute(GluuAttribute gluuAttribute) {
		try {
			Objects.requireNonNull(gluuAttribute, "Attempt to update null attribute");
			String inum = gluuAttribute.getInum();
			log("Update new attribute " + inum);
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
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an attribute")
	public Response deleteAttribute(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Delete an attribute " + inum);
		try {
			Objects.requireNonNull(inum);
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

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

}
