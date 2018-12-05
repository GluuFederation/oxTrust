/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.api.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.util.OxTrustApiConstants;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(OxTrustApiConstants.BASE_API_URL + "/inum")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + "/inum", description = "Inums webservice")
public class InumRestWebServiceV2 {

    @Inject
    private InumService inumService;

    @GET
    @Path("/{type}")
    @ApiOperation("Generate a new inum for the giving type")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = InumDTO.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response generateTextInum(@PathParam("type") String type) {
        return Response.ok(inum(type)).build();
    }

    private InumDTO inum(@PathParam("type") String type) {
        String inum = inumService.generateInums(type);
        InumDTO dto = new InumDTO();
        dto.setInum(inum);
        return dto;
    }

}
