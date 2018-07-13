package org.gluu.oxtrust.api.authentication.defaultAuthenticationMethod;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.AuthenticationMethodDTO;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.PassportAuthenticationMethodDTO;
import org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod.PassportConfigurationDTO;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethod;
import org.gluu.oxtrust.service.config.authentication.AuthenticationMethodService;
import org.gluu.oxtrust.service.config.authentication.PassportAuthenticationMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.model.passport.PassportConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;

public class DefaultAuthenticationMethodResourceTest {

    private DefaultAuthenticationMethodResource defaultAuthenticationMethodResource;
    private AuthenticationMethod existingAuthenticationMethod;
    private Function<AuthenticationMethod, Void> saveCallback = new Function<AuthenticationMethod, Void>() {
        @Override
        public Void apply(AuthenticationMethod input) {
            return null;
        }
    };

    @BeforeTest
    public void init() {
        List<PassportConfiguration> passportConfigurations = Arrays.asList(
                passportConfiguration("strategy_0", Arrays.asList(
                        property("value_1", "value_2"),
                        property("value_3", "value_4")
                )), passportConfiguration("strategy_1", Arrays.asList(
                        property("value_5", "value_6"),
                        property("value_7", "value_8")
                )), passportConfiguration("strategy_2", Arrays.asList(
                        property("value_9", "value_10"),
                        property("value_11", "value_12")
                ))
        );

        LdapOxPassportConfiguration ldapOxPassportConfiguration = new LdapOxPassportConfiguration();
        ldapOxPassportConfiguration.setStatus("status");
        ldapOxPassportConfiguration.setBaseDn("dn");
        ldapOxPassportConfiguration.setDn("dn");
        ldapOxPassportConfiguration.setPassportConfigurations(passportConfigurations);
        PassportAuthenticationMethod passportAuthenticationMethod = new PassportAuthenticationMethod();
        passportAuthenticationMethod.setEnabled(true);
        passportAuthenticationMethod.setLdapOxPassportConfiguration(ldapOxPassportConfiguration);
        existingAuthenticationMethod = new AuthenticationMethod("authentication_method", "oxtrust_authentication_method", passportAuthenticationMethod);

        AuthenticationMethodDtoAssembly authenticationMethodDtoAssembly = new AuthenticationMethodDtoAssembly();
        AuthenticationMethodService authenticationMethodServiceStub = new AuthenticationMethodService() {
            @Override
            public AuthenticationMethod findAuthenticationMode() {
                return existingAuthenticationMethod;
            }

            @Override
            public void save(AuthenticationMethod authenticationMethod) {
                saveCallback.apply(authenticationMethod);
            }
        };

        defaultAuthenticationMethodResource = new DefaultAuthenticationMethodResource();
        try {
            writeField(defaultAuthenticationMethodResource, "authenticationMethodService", authenticationMethodServiceStub, true);
            writeField(defaultAuthenticationMethodResource, "authenticationMethodDtoAssembly", authenticationMethodDtoAssembly, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private SimpleExtendedCustomProperty property(String value1, String value2) {
        SimpleExtendedCustomProperty property = new SimpleExtendedCustomProperty();
        property.setValue1(value1);
        property.setValue2(value2);
        return property;
    }

    private PassportConfiguration passportConfiguration(String strategy, List<SimpleExtendedCustomProperty> fields) {
        PassportConfiguration passportConfiguration = new PassportConfiguration();
        passportConfiguration.setStrategy(strategy);
        passportConfiguration.setFieldset(fields);
        return passportConfiguration;
    }

    @Test
    public void should_override_existing_properties() {
        AuthenticationMethodDTO newAuthenticationMethodDTO = new AuthenticationMethodDTO();
        newAuthenticationMethodDTO.setAuthenticationMode("new_auth_method");
        newAuthenticationMethodDTO.setOxTrustAuthenticationMode("new_oxtrust_auth_method");

        PassportAuthenticationMethodDTO passportConfigurationDto = new PassportAuthenticationMethodDTO();
        passportConfigurationDto.setEnabled(false);
        passportConfigurationDto.setPassportConfigurations(Arrays.asList(
                passportConfigurationDTO("strategy_x", ImmutableMap.<String, String>builder()
                        .put("value_100", "value_101")
                        .put("value_102", "value_103")
                        .put("value_104", "value_105")
                        .build())
        ));

        newAuthenticationMethodDTO.setPassportAuthenticationMethod(passportConfigurationDto);

        saveCallback = new Function<AuthenticationMethod, Void>() {
            @Override
            public Void apply(AuthenticationMethod input) {
                Assert.assertEquals("new_auth_method", input.getAuthenticationMode());
                Assert.assertEquals("new_oxtrust_auth_method", input.getOxTrustAuthenticationMode());
                Assert.assertFalse(input.getPassportAuthenticationMethod().isEnabled());
                Assert.assertEquals("status", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration().getStatus());
                Assert.assertEquals("dn", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration().getBaseDn());
                Assert.assertEquals("dn", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration().getDn());
                Assert.assertEquals(1, input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration().getPassportConfigurations().size());
                Assert.assertEquals("strategy_x", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getStrategy());
                Assert.assertEquals(3, input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().size());
                Assert.assertEquals("value_100", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(0).getValue1());
                Assert.assertEquals("value_101", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(0).getValue2());
                Assert.assertEquals("value_102", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(1).getValue1());
                Assert.assertEquals("value_103", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(1).getValue2());
                Assert.assertEquals("value_104", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(2).getValue1());
                Assert.assertEquals("value_105", input.getPassportAuthenticationMethod().getLdapOxPassportConfiguration()
                        .getPassportConfigurations().get(0).getFieldset().get(2).getValue2());
                return null;
            }
        };

        defaultAuthenticationMethodResource.update(newAuthenticationMethodDTO);
    }

    private PassportConfigurationDTO passportConfigurationDTO(String strategy, Map<String, String> properties) {
        PassportConfigurationDTO passportConfigurationDTO = new PassportConfigurationDTO();
        passportConfigurationDTO.setStrategy(strategy);
        passportConfigurationDTO.setProperties(properties);
        return passportConfigurationDTO;
    }

}