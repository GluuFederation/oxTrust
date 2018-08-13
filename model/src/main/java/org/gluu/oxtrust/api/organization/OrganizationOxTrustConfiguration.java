package org.gluu.oxtrust.api.organization;

import org.gluu.oxtrust.model.GluuOrganization;

public class OrganizationOxTrustConfiguration {

    private String name;
    private ManagerGroup managerGroup;

    public OrganizationOxTrustConfiguration() {
    }

    public OrganizationOxTrustConfiguration(String name, ManagerGroup managerGroup) {
        this.name = name;
        this.managerGroup = managerGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ManagerGroup getManagerGroup() {
        return managerGroup;
    }

    public void setManagerGroup(ManagerGroup managerGroup) {
        this.managerGroup = managerGroup;
    }

    public void populate(GluuOrganization organization) {
        organization.setDisplayName(name);
    }

}