package org.gluu.oxtrust.service.config.smtp;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.xdi.model.SmtpConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SmtpConfigurationService {

    @Inject
    private ApplianceService applianceService;

    public SmtpConfiguration findSmtpConfiguration() {
        GluuAppliance appliance = applianceService.getAppliance();
        SmtpConfiguration smtpConfiguration = appliance.getSmtpConfiguration();
        if (smtpConfiguration == null) {
            return new SmtpConfiguration();
        }

        applianceService.decryptSmtpPassword(smtpConfiguration);

        return smtpConfiguration;
    }

    public void save(SmtpConfiguration smtpConfiguration) {
        applianceService.encryptedSmtpPassword(smtpConfiguration);

        GluuAppliance appliance = applianceService.getAppliance();
        appliance.setSmtpConfiguration(smtpConfiguration);
        applianceService.updateAppliance(appliance);
    }

}