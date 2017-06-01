package org.gluu.oxtrust.service;

import org.gluu.oxtrust.exception.PermissionException;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.security.Identity;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuUserRole;
import org.xdi.util.StringHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Yuriy Movchan Date: 05/17/2017
 */
@ApplicationScoped
@Named
public class PermissionService {

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
            {"oxpush", "access"},
            {"linktrack", "access"},
    };

    public boolean hasPermission(Object target, String action) {
        log.trace("Checking permissions for target '{}' an 'action'. Identity: {}", target, action, identity);
        if (!identity.isLoggedIn()) {
            return false;
        }

        if (identity.hasRole(GluuUserRole.MANAGER.getValue()) || identity.hasRole(GluuUserRole.USER.getValue())) {
            if (StringHelper.equals("profile_management", action)) {
                GluuAppliance appliance = applianceService.getAppliance();
                GluuAppliance targetAppliance = (GluuAppliance) target;
                if (((appliance.getProfileManagment() != null) && appliance.getProfileManagment().isBooleanValue())
                        && StringHelper.equals(applianceService.getAppliance().getInum(), targetAppliance.getInum())) {
                    return true;
                } else {
                    return false;
                }
            }

            if (StringHelper.equals("whitePagesEnabled", action)) {
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

        if (identity.hasRole(GluuUserRole.MANAGER.getValue())) {
            for (String[] managerAction : managerActions) {
                String targetString = (String) target;
                if (StringHelper.equals(managerAction[0], targetString) && StringHelper.equals(managerAction[1], action)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void throwOnLackOfPermissions(Object target, String action) throws PermissionException {
        if (!hasPermission(target, action))
            throw new PermissionException("Target has no permission.");
    }

}
