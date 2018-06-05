package org.gluu.oxtrust.model.log;

import org.gluu.oxtrust.api.logs.LogFilesConfigApi;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.oxauth.model.configuration.AppConfiguration;
import org.xdi.service.JsonService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogFilesConfig {

    private final GluuAppliance updateAppliance;
    private final AppConfiguration appConfiguration;
    private final JsonService jsonService;

    public LogFilesConfig(GluuAppliance updateAppliance, AppConfiguration appConfiguration, JsonService jsonService) {
        this.updateAppliance = updateAppliance;
        this.appConfiguration = appConfiguration;
        this.jsonService = jsonService;
    }

    public void updateWith(LogFilesConfigApi config) throws IOException {
        LogViewerConfig logViewerConfiguration = logViewerConfiguration(config);
        updateAppliance.setOxLogViewerConfig(jsonService.objectToJson(logViewerConfiguration));
        updateAppliance.setOxLogConfigLocation(config.getOxTrustLocation());
        appConfiguration.setExternalLoggerConfiguration(config.getOxAuthLocation());
    }

    private LogViewerConfig logViewerConfiguration(LogFilesConfigApi config) {
        LogViewerConfig logViewerConfig = new LogViewerConfig();
        List<SimpleExtendedCustomProperty> properties = new ArrayList<SimpleExtendedCustomProperty>();
        for (Map.Entry<String, String> allowedTemplate : config.getAllowedTemplates().entrySet()) {
            properties.add(new SimpleExtendedCustomProperty(allowedTemplate.getKey(), allowedTemplate.getValue()));
        }
        logViewerConfig.setLogTemplates(properties);

        return logViewerConfig;
    }

}
