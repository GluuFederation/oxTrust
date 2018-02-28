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
 * @author jgomer
 */
@ApplicationScoped
@Named
public class ExternalScimService extends ExternalScriptService {

    public ExternalScimService() {
        super(CustomScriptType.SCIM);
    }

    private int executeExternalGetApiVersion(CustomScriptConfiguration customScriptConfiguration) {

        int version=-1;
        try{
            log.debug("Executing python 'getApiVersion' method");
            version=customScriptConfiguration.getExternalType().getApiVersion();
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return version;

    }

    public boolean executeScimCreateUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {
            log.debug("Executing python 'SCIM Create User' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.createUser(user, configurationAttributes);
            log.debug("executeScimCreateUserMethod result = " + result);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostCreateUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Create User' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postCreateUser(user, configurationAttributes);
            log.debug("executeScimPostCreateUserMethod result = " + result);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostUpdateUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Update User' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postUpdateUser(user, configurationAttributes);
            log.debug("executeScimPostUpdateUserMethod result = " + result);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostDeleteUserMethod(GluuCustomPerson user, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Delete User' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postDeleteUser(user, configurationAttributes);
            log.debug("executeScimPostDeleteUserMethod result = " + result);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostCreateGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Create Group' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postCreateGroup(group, configurationAttributes);
            log.debug("executeScimPostCreateGroupMethod result = " + result);
            return result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostUpdateGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Update Group' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postUpdateGroup(group, configurationAttributes);
            log.debug("executeScimPostUpdateGroupMethod result = " + result);
            return  result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimPostDeleteGroupMethod(GluuGroup group, CustomScriptConfiguration customScriptConfiguration) {

        try {
            if (executeExternalGetApiVersion(customScriptConfiguration) < 2)
                return true;

            log.debug("Executing python 'SCIM Post Delete Group' method");
            ScimType externalType = (ScimType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();

            boolean result = externalType.postDeleteGroup(group, configurationAttributes);
            log.debug("executeScimPostDeleteGroupMethod result = " + result);
            return  result;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public boolean executeScimCreateUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimCreateUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostCreateUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostCreateUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimUpdateUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimUpdateUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostUpdateUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostUpdateUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimDeleteUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimDeleteUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostDeleteUserMethods(GluuCustomPerson user) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostDeleteUserMethod(user, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimCreateGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimCreateGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostCreateGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostCreateGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimUpdateGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimUpdateGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostUpdateGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostUpdateGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimDeleteGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimDeleteGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

    public boolean executeScimPostDeleteGroupMethods(GluuGroup group) {

        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            if (!executeScimPostDeleteGroupMethod(group, customScriptConfiguration)) {
                return false;
            }
        }
        return true;

    }

}
