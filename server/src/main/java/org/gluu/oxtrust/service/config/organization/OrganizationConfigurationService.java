package org.gluu.oxtrust.service.config.organization;

import org.gluu.oxtrust.api.organization.ManagerGroup;
import org.gluu.oxtrust.api.organization.OrganizationConfiguration;
import org.gluu.oxtrust.api.organization.OrganizationOxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.GluuBoolean;
import org.xdi.config.oxauth.WebKeysSettings;
import org.xdi.model.DisplayNameEntry;

import javax.inject.Inject;

public class OrganizationConfigurationService {

    @Inject
    private ApplianceService applianceService;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private PersistenceEntryManager persistenceEntryManager;
    @Inject
    private WebKeySettingsService webKeySettingsService;

    public OrganizationConfiguration find() {
        GluuAppliance appliance = applianceService.getAppliance();

        return new OrganizationConfiguration(toBoolean(appliance.getPasswordResetAllowed()),
                toBoolean(appliance.getScimEnabled()), toBoolean(appliance.getPassportEnabled()),
                appliance.getApplianceDnsServer(), Long.parseLong(appliance.getMaxLogSize()),
                toBoolean(appliance.getProfileManagment()), appliance.getContactEmail(), organizationOxTrustConfiguration(),
                webKeySettingsService.find().getServerIP());
    }

    private boolean toBoolean(GluuBoolean gluuBoolean) {
        return gluuBoolean == null ? false : gluuBoolean.isBooleanValue();
    }

    private OrganizationOxTrustConfiguration organizationOxTrustConfiguration() {
        GluuOrganization organization = organizationService.getOrganization();
        return new OrganizationOxTrustConfiguration(organization.getDisplayName(), managerGroup(organization));
    }

    private ManagerGroup managerGroup(GluuOrganization organization) {
        DisplayNameEntry displayNameEntry = persistenceEntryManager.find(DisplayNameEntry.class, organization.getManagerGroup(), null);
        return new ManagerGroup(displayNameEntry.getInum(), displayNameEntry.getDisplayName());
    }

    public void save(OrganizationConfiguration organizationConfiguration) {
        GluuAppliance appliance = applianceService.getAppliance();
        WebKeysSettings webKeysSettings = webKeySettingsService.find();

        organizationConfiguration.populate(appliance);
        organizationConfiguration.populate(webKeysSettings);

        GluuOrganization organization = organizationService.getOrganization();
        organizationConfiguration.getOrganizationOxTrustConfiguration().populate(organization);

        applianceService.updateAppliance(appliance);
        webKeySettingsService.save(webKeysSettings);
        organizationService.updateOrganization(organization);
    }

}

