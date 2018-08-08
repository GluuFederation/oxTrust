/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.service.uma.annotations;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.security.Identity;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.user.UserRole;
import org.xdi.util.StringHelper;

/**
 * Provides service to protect Rest service endpoints with UMA scope.
 * 
 * @author Dmitry Ognyannikov
 */
@ApplicationScoped
@Named
public class ApiPermissionService implements Serializable {
    
    private static final long serialVersionUID = 8290321709004847387L;

    @Inject
    private Logger log;

    @Inject
    private Identity identity;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private ApplianceService applianceService;
    
        private String[][] managerActions = new String[][]{
            {"attribute", "access"},
            {"person", "access"},
            {"person", "import"},
            {"group", "access"},
            {"sectorIdentifier", "access"},
            {"trust", "access"},
            {"configuration", "access"},
            {"log", "access"},
            {"import", "access"},
            {"profile", "access"},
            {"registrationLinks", "access"},
            {"scim", "access"},
            {"scim_test", "access"},
            {"client", "access"},
            {"scope", "access"},
            {"oxauth", "access"},
            {"uma", "access"},
            {"super-gluu", "access"},
            {"linktrack", "access"},
    };

    public boolean hasPermission(Object target, String action) {
        log.trace("Checking permissions for target '{}' an 'action'. Identity: {}", target, action, identity);
        if (!identity.isLoggedIn()) {
            return false;
        }

        if (identity.hasRole(UserRole.MANAGER.getValue()) || identity.hasRole(UserRole.USER.getValue())) {
            if (StringHelper.equalsIgnoreCase("profile_management", action)) {
                GluuAppliance appliance = applianceService.getAppliance();
                GluuAppliance targetAppliance = (GluuAppliance) target;
                if (((appliance.getProfileManagment() != null) && appliance.getProfileManagment().isBooleanValue())
                        && StringHelper.equals(applianceService.getAppliance().getInum(), targetAppliance.getInum())) {
                    return true;
                } else {
                    return false;
                }
            }

            if (StringHelper.equalsIgnoreCase("whitePagesEnabled", action)) {
                GluuAppliance appliance = applianceService.getAppliance();
                GluuAppliance targetAppliance = (GluuAppliance) target;
                if (((appliance.getWhitePagesEnabled() != null) && appliance.getWhitePagesEnabled().isBooleanValue())
                        && StringHelper.equals(applianceService.getAppliance().getInum(), targetAppliance.getInum())) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        if (identity.hasRole(UserRole.MANAGER.getValue())) {
            for (String[] managerAction : managerActions) {
                String targetString = (String) target;
                if (StringHelper.equals(managerAction[0], targetString) && StringHelper.equals(managerAction[1], action)) {
                    return true;
                }
            }
        }
        
        if (identity.hasRole(UserRole.USER.getValue())) {
            for (String[] managerAction : managerActions) {
                String targetString = (String) target;
                if (StringHelper.equals("profile", targetString) && StringHelper.equals(managerAction[0], targetString) && StringHelper.equals(managerAction[1], action)) {
                    return true;
                }
            }
        }


        return false;
    }
}
