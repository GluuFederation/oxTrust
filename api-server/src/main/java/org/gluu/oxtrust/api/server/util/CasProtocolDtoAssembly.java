package org.gluu.oxtrust.api.server.util;

import org.gluu.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.gluu.oxtrust.api.server.model.CasProtocolDTO;
import org.gluu.oxtrust.api.server.model.ShibbolethCASProtocolConfigurationDTO;
import org.gluu.oxtrust.util.CASProtocolConfiguration;

public class CasProtocolDtoAssembly {

	public CasProtocolDTO toDto(CASProtocolConfiguration casProtocolConfiguration) {
		CasProtocolDTO casProtocolDTO = new CasProtocolDTO();
		casProtocolDTO.setCasBaseURL(casProtocolConfiguration.getCasBaseURL());
		casProtocolDTO.setShibbolethCASProtocolConfiguration(toDto(casProtocolConfiguration.getConfiguration()));
		return casProtocolDTO;
	}

	private ShibbolethCASProtocolConfigurationDTO toDto(ShibbolethCASProtocolConfiguration configuration) {
		ShibbolethCASProtocolConfigurationDTO shibbolethCASProtocolConfigurationDTO = new ShibbolethCASProtocolConfigurationDTO();
		shibbolethCASProtocolConfigurationDTO.setInum(configuration.getInum());
		shibbolethCASProtocolConfigurationDTO.setEnabled(configuration.isEnabled());
		shibbolethCASProtocolConfigurationDTO.setExtended(configuration.isExtended());
		shibbolethCASProtocolConfigurationDTO.setEnableToProxyPatterns(configuration.isEnableToProxyPatterns());
		shibbolethCASProtocolConfigurationDTO.setAuthorizedToProxyPattern(configuration.getAuthorizedToProxyPattern());
		shibbolethCASProtocolConfigurationDTO
				.setUnauthorizedToProxyPattern(configuration.getUnauthorizedToProxyPattern());
		shibbolethCASProtocolConfigurationDTO
				.setSessionStorageType(SessionStorageType.from(configuration.getSessionStorageType()));
		return shibbolethCASProtocolConfigurationDTO;
	}

	public CASProtocolConfiguration fromDto(CasProtocolDTO casProtocol) {
		return new CASProtocolConfiguration(casProtocol.getCasBaseURL(),
				fromDto(casProtocol.getShibbolethCASProtocolConfiguration()));
	}

	private ShibbolethCASProtocolConfiguration fromDto(ShibbolethCASProtocolConfigurationDTO dto) {
		ShibbolethCASProtocolConfiguration shibbolethCASProtocolConfiguration = new ShibbolethCASProtocolConfiguration();
		shibbolethCASProtocolConfiguration.setInum(dto.getInum());
		shibbolethCASProtocolConfiguration.setEnabled(dto.isEnabled());
		shibbolethCASProtocolConfiguration.setExtended(dto.isExtended());
		shibbolethCASProtocolConfiguration.setEnableToProxyPatterns(dto.isEnableToProxyPatterns());
		shibbolethCASProtocolConfiguration.setAuthorizedToProxyPattern(dto.getAuthorizedToProxyPattern());
		shibbolethCASProtocolConfiguration.setUnauthorizedToProxyPattern(dto.getUnauthorizedToProxyPattern());
		shibbolethCASProtocolConfiguration.setSessionStorageType(dto.getSessionStorageType().getName());
		return shibbolethCASProtocolConfiguration;
	}
}