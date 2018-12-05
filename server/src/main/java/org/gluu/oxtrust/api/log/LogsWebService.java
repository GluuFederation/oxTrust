package org.gluu.oxtrust.api.log;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.gluu.oxtrust.api.logs.LogFileApi;
import org.gluu.oxtrust.api.logs.LogFileDefApi;
import org.gluu.oxtrust.api.logs.LogFilesConfigApi;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.logger.log.LogFilesService;
import org.gluu.oxtrust.service.logger.log.LogFilesConfigService;
import org.gluu.oxtrust.service.logger.LoggerService;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.oxauth.model.configuration.AppConfiguration;
import org.xdi.service.JsonService;
import org.xdi.service.security.Secure;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path(OxTrustApiConstants.BASE_API_URL + "/logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = OxTrustApiConstants.BASE_API_URL + "/logs", description = "Logs webservice")
public class LogsWebService {

    @Inject
    private Logger log;
    @Inject
    private ApplianceService applianceService;
    @Inject
    private JsonService jsonService;
    @Inject
    private LoggerService loggerService;
    @Inject
    private JsonConfigurationService jsonConfigurationService;

    @GET
    @ApiOperation(value = "Get all logs")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = LogFileDefApi[].class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response logs() {
        try {
            LogFilesService logFilesService = new LogFilesService(applianceService.getAppliance(), jsonService);
            return Response.ok(toDto(logFilesService.filesIndexedById())).build();
        } catch (Exception e) {
            log.error("Error loading logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<LogFileDefApi> toDto(Map<Integer, String> filesById) {
        List<LogFileDefApi> result = new ArrayList<LogFileDefApi>();
        for (Map.Entry<Integer, String> fileById : filesById.entrySet()) {
            result.add(new LogFileDefApi(fileById.getKey(), fileById.getValue()));
        }
        return result;
    }

    @GET
    @Path("/{id}/{numberOfLines}")
    @ApiOperation(value = "Get #numberOfLines log by id")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, response = LogFileDefApi.class, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response log(@PathParam("id") int id, @PathParam("numberOfLines") @DefaultValue("400") int numberOfLines) {
        try {
            LogFilesService logFilesService = new LogFilesService(applianceService.getAppliance(), jsonService);
            return Response.ok(toDto(id, logFilesService.logTailById(id, numberOfLines))).build();
        } catch (IOException e) {
            log.error("Error loading log", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error loading logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private LogFileApi toDto(int id, String content) {
        return new LogFileApi(id, content);
    }

    @PUT
    @ApiOperation(value = "Update log configuration")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "Success"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    public Response update(LogFilesConfigApi config) {
        try {
            AppConfiguration appConfiguration = jsonConfigurationService.getOxauthAppConfiguration();
            GluuAppliance appliance = applianceService.getAppliance();
            LogFilesConfigService logFilesConfigService = new LogFilesConfigService(appliance, appConfiguration, jsonService);
            logFilesConfigService.updateWith(config);

            jsonConfigurationService.saveOxAuthAppConfiguration(appConfiguration);
            applianceService.updateAppliance(appliance);
            loggerService.updateLoggerConfigLocation();

            return Response.noContent().build();
        } catch (IOException e) {
            log.error("Error loading logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
