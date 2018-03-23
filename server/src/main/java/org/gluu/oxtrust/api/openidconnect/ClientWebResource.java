package org.gluu.oxtrust.api.openidconnect;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL+OxTrustApiConstants.CLIENTS)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ClientService clientService;

	
	@Inject
	public ClientWebResource() {
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@ApiOperation(value = "Get openidconnect clients")
	public String listClients(@Context HttpServletResponse response) {
		try {
			List<OxAuthClient> clientList = clientService.getAllClients();
			response.setStatus(HttpServletResponse.SC_OK);
			return mapper.writeValueAsString(clientList);
		} catch (Exception e) {
			logger.error("Exception when getting clients", e);
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
			} catch (Exception ex) {
			}
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a specific openidconnect client")
	public String getClientByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, @Context HttpServletResponse response) {
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return mapper.writeValueAsString(client);
		} catch (Exception e) {
			return handleError(logger, e, "Exception when retrieving client " + inum, response);
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Add an openidconnect client")
	public String createClient(OxAuthClient client, @Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(client, "Attempt to create null client");
			String inum = clientService.generateInumForNewClient();
			client.setInum(inum);
			clientService.addClient(client);
			response.setStatus(HttpServletResponse.SC_CREATED);
			return inum;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during client registration", response);
		}
	}

	@PUT
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Update openidconnect client")
	public String updateClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, OxAuthClient client,
			@Context HttpServletResponse response) {
		try {
			Objects.requireNonNull(client, "Attempt to update null client");
			OxAuthClient existingClient = clientService.getClientByInum(inum);
			if (existingClient != null) {
				client.setInum(existingClient.getInum());
				clientService.updateClient(client);
				response.setStatus(HttpServletResponse.SC_OK);
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return OxTrustConstants.RESULT_FAILURE;
			}
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs during client update", response);
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Delete an openidconnect client")
	public String deleteClient(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum, @Context HttpServletResponse response) {
		try {
			OxAuthClient client = clientService.getClientByInum(inum);
			if (client != null) {
				clientService.removeClient(client);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			return handleError(logger, e, "Error occurs when deleting client " + inum, response);
		}
	}
}
