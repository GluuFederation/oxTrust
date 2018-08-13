package org.gluu.oxtrust.api.organization;

import org.gluu.oxtrust.api.organization.ManagerGroup;

import org.gluu.oxtrust.util.OxTrustApiConstants;

class OrganizationConfigurationDtoAssembly {

    OrganizationConfigurationDTO toDto(OrganizationConfiguration organizationConfiguration) {
        OrganizationConfigurationDTO organizationConfigurationDTO = new OrganizationConfigurationDTO();
        organizationConfigurationDTO.setPasswordResetEnabled(organizationConfiguration.isPasswordResetEnabled());
        organizationConfigurationDTO.setScimEnabled(organizationConfiguration.isScimEnabled());
        organizationConfigurationDTO.setPassportEnabled(organizationConfiguration.isPassportEnabled());
        organizationConfigurationDTO.setApplianceDnsServer(organizationConfiguration.getApplianceDnsServer());
        organizationConfigurationDTO.setMaxLogSize(organizationConfiguration.getMaxLogSize());
        organizationConfigurationDTO.setProfileManagementEnabled(organizationConfiguration.isProfileManagementEnabled());
        organizationConfigurationDTO.setContactEmail(organizationConfiguration.getContactEmail());
        organizationConfigurationDTO.setOrganizationOxTrustConfiguration(toDto(organizationConfiguration.getOrganizationOxTrustConfiguration()));
        organizationConfigurationDTO.setOxAuthServerIP(organizationConfiguration.getOxAuthServerIP());
        return organizationConfigurationDTO;
    }

    private OrganizationOxTrustConfigurationDTO toDto(OrganizationOxTrustConfiguration organizationOxTrustConfiguration) {
        OrganizationOxTrustConfigurationDTO organizationOxTrustConfigurationDTO = new OrganizationOxTrustConfigurationDTO();
        organizationOxTrustConfigurationDTO.setName(organizationOxTrustConfiguration.getName());
        organizationOxTrustConfigurationDTO.setGroupId(organizationOxTrustConfiguration.getManagerGroup().getId());
        organizationOxTrustConfigurationDTO.setGroupName(organizationOxTrustConfiguration.getManagerGroup().getName());
        organizationOxTrustConfigurationDTO.setGroupUrl(groupUrl(organizationOxTrustConfiguration));
        return organizationOxTrustConfigurationDTO;
    }

    private String groupUrl(OrganizationOxTrustConfiguration organizationOxTrustConfiguration) {
        return OxTrustApiConstants.GROUPS + "/" + organizationOxTrustConfiguration.getManagerGroup().getId();
    }

    OrganizationConfiguration fromDto(OrganizationConfigurationDTO organizationConfigurationDto) {
        OrganizationConfiguration organizationConfiguration = new OrganizationConfiguration();
        organizationConfiguration.setPasswordResetEnabled(organizationConfigurationDto.isPasswordResetEnabled());
        organizationConfiguration.setScimEnabled(organizationConfigurationDto.isScimEnabled());
        organizationConfiguration.setPassportEnabled(organizationConfigurationDto.isPassportEnabled());
        organizationConfiguration.setApplianceDnsServer(organizationConfigurationDto.getApplianceDnsServer());
        organizationConfiguration.setMaxLogSize(organizationConfigurationDto.getMaxLogSize());
        organizationConfiguration.setProfileManagementEnabled(organizationConfigurationDto.isProfileManagementEnabled());
        organizationConfiguration.setContactEmail(organizationConfigurationDto.getContactEmail());
        organizationConfiguration.setOrganizationOxTrustConfiguration(fromDto(organizationConfigurationDto.getOrganizationOxTrustConfiguration()));
        organizationConfiguration.setOxAuthServerIP(organizationConfigurationDto.getOxAuthServerIP());
        return organizationConfiguration;
    }

    private OrganizationOxTrustConfiguration fromDto(OrganizationOxTrustConfigurationDTO dto) {
        OrganizationOxTrustConfiguration organizationOxTrustConfiguration = new OrganizationOxTrustConfiguration();
        organizationOxTrustConfiguration.setName(dto.getName());
        organizationOxTrustConfiguration.setManagerGroup(managerGroup(dto));
        return organizationOxTrustConfiguration;
    }

    private ManagerGroup managerGroup(OrganizationOxTrustConfigurationDTO dto) {
        return new ManagerGroup(dto.getGroupId(), dto.getGroupName());
    }
}
