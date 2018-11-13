package org.gluu.oxtrust.api.organization;

import org.xdi.model.SmtpConfiguration;

import static org.apache.commons.lang3.StringUtils.EMPTY;

class SmtpConfigurationDtoAssembly {

    SmtpConfigurationDTO toDto(SmtpConfiguration smtpConfiguration) {
        SmtpConfigurationDTO smtpConfigurationDTO= new SmtpConfigurationDTO();
        smtpConfigurationDTO.setHost(smtpConfiguration.getHost());
        smtpConfigurationDTO.setPort(smtpConfiguration.getPort());
        smtpConfigurationDTO.setRequiresSsl(smtpConfiguration.isRequiresSsl());
        smtpConfigurationDTO.setTrustHost(smtpConfiguration.isServerTrust());
        smtpConfigurationDTO.setFromName(smtpConfiguration.getFromName());
        smtpConfigurationDTO.setFromEmailAddress(smtpConfiguration.getFromEmailAddress());
        smtpConfigurationDTO.setRequiresAuthentication(smtpConfiguration.isRequiresAuthentication());
        smtpConfigurationDTO.setUserName(smtpConfiguration.getUserName());
        smtpConfigurationDTO.setPassword(EMPTY);
        return smtpConfigurationDTO;
    }

    public SmtpConfiguration fromDto(SmtpConfigurationDTO smtpConfigurationDTO) {
        SmtpConfiguration smtpConfiguration= new SmtpConfiguration();
        smtpConfiguration.setHost(smtpConfigurationDTO.getHost());
        smtpConfiguration.setPort(smtpConfigurationDTO.getPort());
        smtpConfiguration.setRequiresSsl(smtpConfigurationDTO.isRequiresSsl());
        smtpConfiguration.setServerTrust(smtpConfigurationDTO.isTrustHost());
        smtpConfiguration.setFromName(smtpConfigurationDTO.getFromName());
        smtpConfiguration.setFromEmailAddress(smtpConfigurationDTO.getFromEmailAddress());
        smtpConfiguration.setRequiresAuthentication(smtpConfigurationDTO.isRequiresAuthentication());
        smtpConfiguration.setUserName(smtpConfigurationDTO.getUserName());
        smtpConfiguration.setPasswordDecrypted(smtpConfigurationDTO.getPassword());
        return smtpConfiguration;
    }
}
