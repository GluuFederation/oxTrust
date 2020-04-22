/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.service.secure;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.model.user.UserRole;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

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
	private ConfigurationService configurationService;

	private String[][] managerActions = new String[][] { { "attribute", "access" }, { "person", "access" },
			{ "person", "import" }, { "group", "access" }, { "sectorIdentifier", "access" }, { "trust", "access" },
			{ "configuration", "access" }, { "log", "access" }, { "import", "access" }, { "profile", "access" },
			{ "registrationLinks", "access" }, { "scim", "access" }, { "scim_test", "access" }, { "client", "access" },
			{ "scope", "access" }, { "oxauth", "access" }, { "uma", "access" }, { "super-gluu", "access" }, };

	public boolean hasPermission(Object target, String action) {
		log.trace("Checking permissions for target '{}' an 'action'. Identity: {}", target, action, identity);
		if (!identity.isLoggedIn()) {
			return false;
		}

		if (identity.hasRole(UserRole.MANAGER.getValue()) || identity.hasRole(UserRole.USER.getValue())) {
			if (StringHelper.equalsIgnoreCase("profile_management", action)) {
				GluuConfiguration targetConfiguration = (GluuConfiguration) target;
				if (configurationService.getConfiguration().isProfileManagment() && StringHelper
						.equals(configurationService.getConfiguration().getInum(), targetConfiguration.getInum())) {
					return true;
				} else {
					return false;
				}
			}
		}

		if (identity.hasRole(UserRole.MANAGER.getValue())) {
			for (String[] managerAction : managerActions) {
				String targetString = (String) target;
				if (StringHelper.equals(managerAction[0], targetString)
						&& StringHelper.equals(managerAction[1], action)) {
					return true;
				}
			}
		}

		if (identity.hasRole(UserRole.USER.getValue())) {
			for (String[] managerAction : managerActions) {
				String targetString = (String) target;
				if (StringHelper.equals("profile", targetString) && StringHelper.equals(managerAction[0], targetString)
						&& StringHelper.equals(managerAction[1], action)) {
					return true;
				}
			}
		}

		return false;
	}
}
