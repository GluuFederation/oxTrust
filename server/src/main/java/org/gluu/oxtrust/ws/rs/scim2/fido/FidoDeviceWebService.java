/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ws.rs.scim2.fido;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.ldap.service.IFidoDeviceService;
import org.gluu.oxtrust.ldap.service.FidoDeviceService;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseFidoDeviceSerializer;
import org.gluu.oxtrust.service.scim2.Scim2FidoDeviceService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.ws.rs.scim2.BaseScimWebService;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.gluu.oxtrust.util.OxTrustConstants.INTERNAL_SERVER_ERROR_MESSAGE;

/**
 * @author Val Pecaoco
 */
@Named("scim2FidoDeviceEndpoint")
@Path("/scim/v2/FidoDevices")
public class FidoDeviceWebService extends BaseScimWebService {

	@Logger
	private Log log;

	@Inject
	private IFidoDeviceService fidoDeviceService;

	@Inject
	private Scim2FidoDeviceService scim2FidoDeviceService;

	@GET
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Search devices", notes = "Returns a list of devices (https://tools.ietf.org/html/rfc7644#section-3.4.2.2)", response = ListResponse.class)
	public Response searchDevices(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@QueryParam("userId") final String userId,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_FILTER) final String filterString,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_START_INDEX) final int startIndex,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_COUNT) final int count,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_BY) final String sortBy,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_SORT_ORDER) final String sortOrder,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			if (count > getMaxCount()) {

				String detail = "Too many results (=" + count + ") would be returned; max is " + getMaxCount() + " only.";
				return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, detail);

			} else {

				log.info(" Searching devices from LDAP ");

				fidoDeviceService = FidoDeviceService.instance();

				String baseDn = fidoDeviceService.getDnForFidoDevice(userId, null);
				log.info("##### baseDn = " + baseDn);

				VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

				List<GluuCustomFidoDevice> gluuCustomFidoDevices = search(baseDn, GluuCustomFidoDevice.class, filterString, startIndex, count, sortBy, sortOrder, vlvResponse, attributesArray);

				ListResponse devicesListResponse = new ListResponse();

				List<String> schema = new ArrayList<String>();
				schema.add(Constants.LIST_RESPONSE_SCHEMA_ID);

				log.info(" setting schema");
				devicesListResponse.setSchemas(schema);

				// Set total
				devicesListResponse.setTotalResults(vlvResponse.getTotalResults());

				if (count > 0 && gluuCustomFidoDevices != null && !gluuCustomFidoDevices.isEmpty()) {

					for (GluuCustomFidoDevice gluuCustomFidoDevice : gluuCustomFidoDevices) {

						FidoDevice fidoDevice = CopyUtils2.copy(gluuCustomFidoDevice, new FidoDevice());

						devicesListResponse.getResources().add(fidoDevice);
					}

					// Set the rest of results info
					devicesListResponse.setItemsPerPage(vlvResponse.getItemsPerPage());
					devicesListResponse.setStartIndex(vlvResponse.getStartIndex());
				}

				// Serialize to JSON
				String json = serializeToJson(devicesListResponse, attributesArray);

				URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/FidoDevices");

				return Response.ok(json).location(location).build();
			}

		} catch (Exception e) {

			log.error("Error in searchDevices", e);
			e.printStackTrace();
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("{id}")
	@GET
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Find device by id", notes = "Returns a device by id as path param (https://tools.ietf.org/html/rfc7644#section-3.4.1)", response = FidoDevice.class)
	public Response getDeviceById(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@QueryParam("userId") final String userId,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			fidoDeviceService = FidoDeviceService.instance();

			String baseDn = fidoDeviceService.getDnForFidoDevice(userId, id);
			log.info("##### baseDn = " + baseDn);

			String filterString = "id eq \"" + id + "\"";
			VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

			List<GluuCustomFidoDevice> gluuCustomFidoDevices = search(baseDn, GluuCustomFidoDevice.class, filterString, 1, 1, "id", SortOrder.ASCENDING.getValue(), vlvResponse, attributesArray);

			if (gluuCustomFidoDevices == null || gluuCustomFidoDevices.isEmpty() || vlvResponse.getTotalResults() == 0) {
				// sets HTTP status code 404 Not Found
				return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");
			} else {
				log.info(" Resource " + id + " found ");
			}

			GluuCustomFidoDevice gluuCustomFidoDevice = gluuCustomFidoDevices.get(0);

			FidoDevice fidoDevice = CopyUtils2.copy(gluuCustomFidoDevice, new FidoDevice());

			// Serialize to JSON
			String json = serializeToJson(fidoDevice, attributesArray);

			URI uriLocation = new URI(fidoDevice.getMeta().getLocation());

			return Response.ok(json).location(uriLocation).build();

		} catch (EntryPersistenceException epe) {

			log.error("Error in getDeviceById", epe);
			epe.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

		} catch (Exception e) {

			log.error("Error in getDeviceById", e);
			e.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@POST
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Create device", notes = "Create device (https://tools.ietf.org/html/rfc7644#section-3.3)", response = FidoDevice.class)
	public Response createDevice() {
		return getErrorResponse(501, "Not implemented; device registration happens via the FIDO API.");
	}

	@Path("{id}")
	@PUT
	@Consumes({Constants.MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Update device", notes = "Update device (https://tools.ietf.org/html/rfc7644#section-3.5.1)", response = FidoDevice.class)
	public Response updateDevice(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id,
		@ApiParam(value = "FidoDevice", required = true) FidoDevice fidoDevice,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_ATTRIBUTES) final String attributesArray) throws Exception {

		Response authorizationResponse;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			if (!id.equalsIgnoreCase(fidoDevice.getId())) {

				String detail = "Path param id does not match with device id";
				return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, detail);

			} else {

				FidoDevice updatedFidoDevice = scim2FidoDeviceService.updateFidoDevice(id, fidoDevice);

				// Serialize to JSON
				String json = serializeToJson(updatedFidoDevice, attributesArray);

				URI location = new URI(updatedFidoDevice.getMeta().getLocation());

				return Response.ok(json).location(location).build();
			}

		} catch (EntryPersistenceException epe) {

			log.error("Failed to update device", epe);
			epe.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

		} catch (DuplicateEntryException dee) {

			log.error("DuplicateEntryException", dee);
			dee.printStackTrace();
			return getErrorResponse(Response.Status.CONFLICT, ErrorScimType.UNIQUENESS, dee.getMessage());

		} catch (Exception e) {

			log.error("Failed to update device", e);
			e.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("{id}")
	@DELETE
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Delete device", notes = "Delete device (https://tools.ietf.org/html/rfc7644#section-3.6)")
	public Response deleteDevice(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@PathParam("id") String id) throws Exception {

		Response authorizationResponse;
		if (jsonConfigurationService.getOxTrustApplicationConfiguration().isScimTestMode()) {
			log.info(" ##### SCIM Test Mode is ACTIVE");
			authorizationResponse = processTestModeAuthorization(token);
		} else {
			authorizationResponse = processAuthorization(authorization);
		}
		if (authorizationResponse != null) {
			return authorizationResponse;
		}

		try {

			scim2FidoDeviceService.deleteFidoDevice(id);

			return Response.noContent().build();

		} catch (EntryPersistenceException epe) {

			log.error("Failed to delete device", epe);
			epe.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");

		} catch (Exception e) {

			log.error("Failed to delete device", e);
			e.printStackTrace();
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("/.search")
	@POST
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "Search devices POST /.search", notes = "Returns a list of devices (https://tools.ietf.org/html/rfc7644#section-3.4.3)", response = ListResponse.class)
	public Response searchDevicesPost(
		@HeaderParam("Authorization") String authorization,
		@QueryParam(OxTrustConstants.QUERY_PARAMETER_TEST_MODE_OAUTH2_TOKEN) final String token,
		@QueryParam("userId") final String userId,
		@ApiParam(value = "SearchRequest", required = true) SearchRequest searchRequest) throws Exception {

		try {

			log.info("IN FidoDeviceWebService.searchDevicesPost()...");

			// Authorization check is done in searchDevices()
			Response response = searchDevices(
				authorization,
				token,
				userId,
				searchRequest.getFilter(),
				searchRequest.getStartIndex(),
				searchRequest.getCount(),
				searchRequest.getSortBy(),
				searchRequest.getSortOrder(),
				searchRequest.getAttributesArray()
			);

			URI location = new URI(applicationConfiguration.getBaseEndpoint() + "/scim/v2/FidoDevices/.search");

			log.info("LEAVING FidoDeviceWebService.searchDevicesPost()...");

			return Response.fromResponse(response).location(location).build();

		} catch (EntryPersistenceException epe) {

			log.error("Error in searchDevicesPost", epe);
			epe.printStackTrace();
			return getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource not found");

		} catch (Exception e) {

			log.error("Error in searchDevicesPost", e);
			e.printStackTrace();
			return getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, INTERNAL_SERVER_ERROR_MESSAGE);
		}
	}

	@Path("/Me")
	@GET
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "GET \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
	public Response meGet() {
		return getErrorResponse(501, "Not Implemented");
	}

	@Path("/Me")
	@POST
	@Produces({Constants.MEDIA_TYPE_SCIM_JSON + "; charset=utf-8", MediaType.APPLICATION_JSON + "; charset=utf-8"})
	@HeaderParam("Accept") @DefaultValue(Constants.MEDIA_TYPE_SCIM_JSON)
	@ApiOperation(value = "POST \"/Me\"", notes = "\"/Me\" Authenticated Subject Alias (https://tools.ietf.org/html/rfc7644#section-3.11)")
	public Response mePost() {
		return getErrorResponse(501, "Not Implemented");
	}

	private String serializeToJson(Object object, String attributesArray) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
		SimpleModule customScimFilterModule = new SimpleModule("CustomScim2FidoDeviceFilterModule", new Version(1, 0, 0, ""));
		ListResponseFidoDeviceSerializer serializer = new ListResponseFidoDeviceSerializer();
		serializer.setAttributesArray(attributesArray);
		customScimFilterModule.addSerializer(FidoDevice.class, serializer);
		mapper.registerModule(customScimFilterModule);

		return mapper.writeValueAsString(object);
	}
}
