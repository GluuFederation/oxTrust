package org.gluu.oxtrust.service.logger.log;

import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.service.logger.log.LogFilesService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.service.JsonService;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class LogFilesServiceTest {

    private LogFilesService logFilesService;
    private GluuAppliance appliance;
    private JsonService jsonService;

    @BeforeMethod
    public void setUp() {
        appliance = new GluuAppliance();
        jsonService = new JsonService();
        jsonService.init();

        logFilesService = new LogFilesService(appliance, jsonService);
    }

    @Test
    public void testConfig() {
        appliance.setOxLogViewerConfig(createOxLogViewerConfig());
        LogViewerConfig config = logFilesService.config();
        assertEquals(config.getLogTemplates().size(), 2);
        assertEquals(config.getLogTemplates().get(0).getValue2(), "/opt/gluu/jetty/oxauth/logs/*.log");
        assertEquals(config.getLogTemplates().get(1).getValue2(), "/opt/gluu/jetty/identity/logs/*.log");

        // When conf is empty.
        appliance.setOxLogViewerConfig("");
        config = logFilesService.config();
        assertEquals(config.getLogTemplates().size(), 0);
    }

    private String createOxLogViewerConfig() {
        try {
            LogViewerConfig config = new LogViewerConfig();
            config.setLogTemplates(asList(
                    new SimpleExtendedCustomProperty("oxAuth logs", "/opt/gluu/jetty/oxauth/logs/*.log"),
                    new SimpleExtendedCustomProperty("oxTrust logs", "/opt/gluu/jetty/identity/logs/*.log")));
            return jsonService.objectToJson(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}