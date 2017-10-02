/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.external;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.conf.CustomScriptConfiguration;
import org.xdi.model.custom.script.type.scim.ScimType;
import org.xdi.service.custom.script.ExternalScriptService;

/**
 * @author Val Pecaoco
 */
@ApplicationScoped
@Named
public class ExternalScimService extends ExternalScriptService {

    public ExternalScimService() {
        super(CustomScriptType.SCIM);
        //System.out.println(">>>>> Initializing ExternalScimService()...");
    }

    public boolean executeScimCreateUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Create User' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.createUser(user, configurationAttributes);

            log.debug("executeScimCreateUserMethod result = " + result);

            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimUpdateUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Update User' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.updateUser(user, configurationAttributes);

            log.debug("executeScimUpdateUserMethod result = " + result);

            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimDeleteUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Delete User' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.deleteUser(user, configurationAttributes);

            log.debug("executeScimDeleteUserMethod result = " + result);

            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimCreateGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Create Group' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.createGroup(group, configurationAttributes);

            log.debug("executeScimCreateGroupMethod result = " + result);

            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimUpdateGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Update Group' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.updateGroup(group, configurationAttributes);

            log.debug("executeScimUpdateGroupMethod result = " + result);

            return  result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimDeleteGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {

            log.debug("Executing python 'SCIM Delete Group' method");

            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();

            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.deleteGroup(group, configurationAttributes);

            log.debug("executeScimDeleteGroupMethod result = " + result);

            return  result;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean executeScimCreateUserMethods(GluuCustomPerson user) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimCreateUserMethod(user, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

    public boolean executeScimUpdateUserMethods(GluuCustomPerson user) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimUpdateUserMethod(user, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

    public boolean executeScimDeleteUserMethods(GluuCustomPerson user) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimDeleteUserMethod(user, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

    public boolean executeScimCreateGroupMethods(GluuGroup group) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimCreateGroupMethod(group, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

    public boolean executeScimUpdateGroupMethods(GluuGroup group) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimUpdateGroupMethod(group, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

    public boolean executeScimDeleteGroupMethods(GluuGroup group) {

        boolean result = true;

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {

            result &= executeScimDeleteGroupMethod(group, customScriptConfiguration);

            if (!result) {
                return result;
            }
        }

        return result;
    }

}
