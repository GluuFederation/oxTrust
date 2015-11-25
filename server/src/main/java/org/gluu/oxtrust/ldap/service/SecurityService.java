/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.User;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuUserRole;

/**
 * Provides operations with groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Scope(ScopeType.STATELESS)
@Name("securityService")
@AutoCreate
public class SecurityService implements Serializable {

	private static final long serialVersionUID = 1395327358942223005L;

	@Logger
	private Log log;

	@In
	private PersonService personService;

	@In
	private GroupService groupService;

	@In
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
	public GluuUserRole[] getUserRoles(User user) {
		GluuOrganization organization = organizationService.getOrganization();
		// String ownerGroupDn = organization.getOwnerGroup();
		String managerGroupDn = organization.getManagerGroup();

		String personDN = user.getDn();

		Set<GluuUserRole> userRoles = new HashSet<GluuUserRole>();
		// if (groupService.isMemberOrOwner(ownerGroupDn, personDN)) {
		// userRoles.add(GluuUserRole.OWNER);
		// }

		if (groupService.isMemberOrOwner(managerGroupDn, personDN)) {
			userRoles.add(GluuUserRole.MANAGER);
		}

		if ((userRoles.size() == 0) /*
									 * &&
									 * (GluuStatus.ACTIVE.equals(person.getStatus
									 * ()))
									 */) {
			userRoles.add(GluuUserRole.USER);
		}

		return userRoles.toArray(new GluuUserRole[userRoles.size()]);
	}

	public boolean isUseAdminUser(String userName) {
		try {
			User user = personService.getUserByUid(userName);
			GluuUserRole[] roles = getUserRoles(user);
			
			for (GluuUserRole role: roles) {
				if (GluuUserRole.MANAGER.equals(role)) {
					return true;
				}
			}
		} catch (Exception ex) {
			log.error("Failed to find user '{0}' in ldap", ex, userName);
		}
			
		return false;
	}

	public static SecurityService instance() {
		return (SecurityService) Component.getInstance(SecurityService.class);
	}

}
