/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.custom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.service.custom.script.AbstractCustomScriptService;

/**
 * Operations with custom scripts
 *
 * @author Yuriy Movchan Date: 12/03/2014
 */
@ApplicationScoped
public class CustomScriptService extends AbstractCustomScriptService {

	@Inject
	private OrganizationService organizationService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	private static final long serialVersionUID = -5283102477313448031L;

	public String baseDn() {
		String orgDn = organizationService.getDnForOrganization();
		return String.format("ou=scripts,%s", orgDn);
	}

	public CustomScript getScriptByInum(String inum) {
		CustomScript result = null;
		try {
			result = ldapEntryManager.find(CustomScript.class, buildDn(inum));
		} catch (Exception ex) {
		}
		return result;
	}

}
