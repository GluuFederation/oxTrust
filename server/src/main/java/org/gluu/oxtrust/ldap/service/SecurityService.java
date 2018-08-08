/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.User;
import org.slf4j.Logger;
import org.xdi.model.user.UserRole;

/**
 * Provides operations with groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Stateless
@Named("securityService")
public class SecurityService implements Serializable {

	private static final long serialVersionUID = 1395327358942223005L;

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private IGroupService groupService;

	@Inject
	private OrganizationService organizationService;

	/**
	 * Get person user roles
	 * 
	 * @param user
	 *            Person
	 * @return List of roles
	 * @throws Exception
	 *             exception
	 */
	public UserRole[] getUserRoles(User user) {
		GluuOrganization organization = organizationService.getOrganization();
		// String ownerGroupDn = organization.getOwnerGroup();
		String managerGroupDn = organization.getManagerGroup();

		String personDN = user.getDn();

		Set<UserRole> userRoles = new HashSet<UserRole>();
		// if (groupService.isMemberOrOwner(ownerGroupDn, personDN)) {
		// userRoles.add(GluuUserRole.OWNER);
		// }

		if (groupService.isMemberOrOwner(managerGroupDn, personDN)) {
			userRoles.add(UserRole.MANAGER);
		}

		if ((userRoles.size() == 0) /*
									 * &&
									 * (GluuStatus.ACTIVE.equals(person.getStatus
									 * ()))
									 */) {
			userRoles.add(UserRole.USER);
		}

		return userRoles.toArray(new UserRole[userRoles.size()]);
	}

	public boolean isUseAdminUser(String userName) {
		try {
			User user = personService.getUserByUid(userName);
			UserRole[] roles = getUserRoles(user);
			
			for (UserRole role: roles) {
				if (UserRole.MANAGER.equals(role)) {
					return true;
				}
			}
		} catch (Exception ex) {
			log.error("Failed to find user '{}' in ldap", userName, ex);
		}
			
		return false;
	}

}
