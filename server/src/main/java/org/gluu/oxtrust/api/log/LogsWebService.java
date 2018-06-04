package org.gluu.oxtrust.api.log;


import com.wordnik.swagger.annotations.ApiOperation;
import org.gluu.oxtrust.api.logs.LogFileApi;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.model.log.LogFiles;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.service.JsonService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.LOGS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// Secured ?
public class LogsWebService {

    @Inject
    private Logger log;

    @Inject
    private ApplianceService applianceService;
    @Inject
    private JsonService jsonService;

    @GET
    @ApiOperation(value = "Get all logs")
    public Response logs() {
        try {
            LogFiles logFiles = new LogFiles();
            LogViewerConfig config = logFiles.config(applianceService.getAppliance(), jsonService);
            return Response.ok(toDto(logFiles.filesIndexedById(config))).build();
        } catch (IOException e) {
            log.error("Error loading logs", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error loading logs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<LogFileApi> toDto(Map<Integer, String> filesById) {
        List<LogFileApi> result = new ArrayList<LogFileApi>();
        for (Map.Entry<Integer, String> fileById : filesById.entrySet()) {
            result.add(new LogFileApi(fileById.getKey(), fileById.getValue()));
        }
        return result;
    }

}
