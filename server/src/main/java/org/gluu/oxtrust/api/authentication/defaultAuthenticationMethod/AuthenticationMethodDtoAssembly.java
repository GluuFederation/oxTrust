package org.gluu.oxtrust.api.authentication.defaultAuthenticationMethod;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.AuthenticationMethodDTO;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.PassportAuthenticationMethodDTO;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.PassportConfigurationDTO;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethod;
import org.gluu.oxtrust.service.config.authentication.PassportAuthenticationMethod;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.model.passport.PassportConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gluu.oxtrust.util.CollectionsUtil.trimToEmpty;

class AuthenticationMethodDtoAssembly {

    AuthenticationMethodDTO toDto(AuthenticationMethod authenticationMethod) {
        AuthenticationMethodDTO authenticationMethodDTO = new AuthenticationMethodDTO();
        authenticationMethodDTO.setAuthenticationMode(authenticationMethod.getAuthenticationMode());
        authenticationMethodDTO.setOxTrustAuthenticationMode(authenticationMethod.getOxTrustAuthenticationMode());
        authenticationMethodDTO.setPassportAuthenticationMethod(toDto(authenticationMethod.getPassportAuthenticationMethod()));
        return authenticationMethodDTO;
    }

    private PassportAuthenticationMethodDTO toDto(PassportAuthenticationMethod passportAuthenticationMethod) {
        PassportAuthenticationMethodDTO passportAuthenticationMethodDTO = new PassportAuthenticationMethodDTO();
        passportAuthenticationMethodDTO.setEnabled(passportAuthenticationMethod.isEnabled());
        passportAuthenticationMethodDTO.setPassportConfigurations(toDto(passportAuthenticationMethod.getLdapOxPassportConfiguration().getPassportConfigurations()));
        return passportAuthenticationMethodDTO;
    }

    private List<PassportConfigurationDTO> toDto(List<PassportConfiguration> passportConfigurations) {
        return FluentIterable.from(trimToEmpty(passportConfigurations))
                .transform(new Function<PassportConfiguration, PassportConfigurationDTO>() {
                    @Override
                    public PassportConfigurationDTO apply(PassportConfiguration input) {
                        PassportConfigurationDTO passportConfigurationDTO = new PassportConfigurationDTO();
                        passportConfigurationDTO.setStrategy(input.getStrategy());
                        passportConfigurationDTO.setProperties(toMap(input.getFieldset()));
                        return passportConfigurationDTO;
                    }
                }).toList();
    }

    private Map<String, String> toMap(List<SimpleExtendedCustomProperty> fieldset) {
        Map<String, String> properties = new HashMap<String, String>();
        for (SimpleExtendedCustomProperty property : fieldset) {
            properties.put(property.getValue1(), property.getValue2());
        }
        return properties;
    }

    AuthenticationMethod fromDto(AuthenticationMethodDTO authenticationMethodDto) {
        AuthenticationMethod authenticationMethod = new AuthenticationMethod();
        authenticationMethod.setAuthenticationMode(authenticationMethodDto.getAuthenticationMode());
        authenticationMethod.setOxTrustAuthenticationMode(authenticationMethodDto.getOxTrustAuthenticationMode());
        authenticationMethod.setPassportAuthenticationMethod(fromDto(authenticationMethodDto.getPassportAuthenticationMethod()));
        return authenticationMethod;
    }

    private PassportAuthenticationMethod fromDto(PassportAuthenticationMethodDTO dto) {
        PassportAuthenticationMethod passportAuthenticationMethod = new PassportAuthenticationMethod();
        passportAuthenticationMethod.setEnabled(dto.isEnabled());
        passportAuthenticationMethod.setLdapOxPassportConfiguration(toLdapOxPassportConfiguration(dto.getPassportConfigurations()));
        return passportAuthenticationMethod;
    }

    private LdapOxPassportConfiguration toLdapOxPassportConfiguration(List<PassportConfigurationDTO> passportConfigurations) {
        List<PassportConfiguration> configurations = FluentIterable.from(passportConfigurations)
                .transform(new Function<PassportConfigurationDTO, PassportConfiguration>() {
                    @Override
                    public PassportConfiguration apply(PassportConfigurationDTO input) {
                        PassportConfiguration passportConfiguration = new PassportConfiguration();
                        passportConfiguration.setStrategy(input.getStrategy());
                        passportConfiguration.setFieldset(toFieldSet(input.getProperties()));
                        return passportConfiguration;
                    }
                }).toList();

        LdapOxPassportConfiguration ldapOxPassportConfiguration = new LdapOxPassportConfiguration();
        ldapOxPassportConfiguration.setPassportConfigurations(configurations);
        ldapOxPassportConfiguration.setStatus("");
        ldapOxPassportConfiguration.setDn("");
        ldapOxPassportConfiguration.setBaseDn("");
        return ldapOxPassportConfiguration;
    }

    private List<SimpleExtendedCustomProperty> toFieldSet(Map<String, String> properties) {
        List<SimpleExtendedCustomProperty> fieldset = new ArrayList<SimpleExtendedCustomProperty>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            SimpleExtendedCustomProperty customProperty = new SimpleExtendedCustomProperty();
            customProperty.setHideValue(false);
            customProperty.setDescription("");
            customProperty.setValue1(property.getKey());
            customProperty.setValue2(property.getValue());
            fieldset.add(customProperty);
        }
        return fieldset;
    }
}
