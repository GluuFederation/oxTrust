package org.gluu.oxtrust.service.logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.xdi.config.oxtrust.AppConfiguration;

/**
 * Logger service
 *
 * @author Yuriy Movchan Date: 08/19/2018
 */
@ApplicationScoped
@Named
public class LoggerService extends org.xdi.service.logger.LoggerService {

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private ApplianceService applianceService;

    @Override
    public boolean isDisableJdkLogger() {
        return (appConfiguration.getDisableJdkLogger() != null) && appConfiguration.getDisableJdkLogger();
    }

    @Override
    public String getLoggingLevel() {
        return appConfiguration.getLoggingLevel();
    }

    @Override
    public String getExternalLoggerConfiguration() {
        GluuAppliance appliance = applianceService.getAppliance();
        return appliance.getOxLogConfigLocation();
    }

}
