/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.security.SimplePrincipal;
import org.gluu.model.user.UserRole;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.service.el.ExpressionEvaluator;
import org.gluu.service.security.SecurityEvaluationException;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;

/**
 * @author Yuriy Movchan Date: 05/17/2017
 */
@ApplicationScoped
@Named
public class PermissionService implements Serializable {

    private static final long serialVersionUID = 8880839485161960537L;

    @Inject
    private Logger log;

    @Inject
    private Identity identity;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ExpressionEvaluator expressionEvaluator;

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
            {"passport", "access"},
            {"super-gluu", "access"},
            {"radius","access"}
    };

    public boolean hasPermission(Object target, String action) {
        log.trace("Checking permissions for target '{}' an 'action'. Identity: {}", target, action, identity);
        if (!identity.isLoggedIn()) {
            return false;
        }

        if (identity.hasRole(UserRole.MANAGER.getValue()) || identity.hasRole(UserRole.USER.getValue())) {
            if (StringHelper.equalsIgnoreCase("profile_management", action)) {
                GluuConfiguration configuration = configurationService.getConfiguration();
                GluuConfiguration targetConfiguration = (GluuConfiguration) target;
                if (configuration.isProfileManagment()
                        && StringHelper.equals(configurationService.getConfiguration().getInum(), targetConfiguration.getInum())) {
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

    public void requestPermission(Object target, String action) {
        boolean hasPermission = hasPermission(target, action);

        if (!hasPermission) {
            throw new SecurityEvaluationException();
        }
    }

    public void requestPermission(String constraint) {
        Boolean expressionValue = expressionEvaluator.evaluateValueExpression(constraint, Boolean.class, Collections.<String, Object>emptyMap());

        if ((expressionValue == null) || !expressionValue) {
            log.debug("constraint '{}' evaluation is null or false!", constraint);
            throw new SecurityEvaluationException();
        }
    }

}
