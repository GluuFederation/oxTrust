/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.custom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.util.StringHelper;

import java.util.List;

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
	private PersistenceEntryManager persistenceEntryManager;

	private static final long serialVersionUID = -5283102477313448031L;

	public String baseDn() {
		String orgDn = organizationService.getDnForOrganization();
		return String.format("ou=scripts,%s", orgDn);
	}

	public CustomScript getScriptByInum(String inum) {
		CustomScript result = null;
		try {
			result = persistenceEntryManager.find(CustomScript.class, buildDn(inum));
		} catch (Exception ex) {
		}
		return result;
	}

	public List<CustomScript> findCustomAuthScripts(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.DESCRIPTION, null, targetArray, null);
		Filter scriptTypeFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE,
				CustomScriptType.PERSON_AUTHENTICATION);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(descriptionFilter, displayNameFilter);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class,
				Filter.createANDFilter(searchFilter, scriptTypeFilter), sizeLimit);
	}

	public List<CustomScript> findCustomAuthScripts(int sizeLimit) {
		Filter searchFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE,
				CustomScriptType.PERSON_AUTHENTICATION.getValue());
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class, searchFilter,
				sizeLimit);
	}

	public List<CustomScript> findOtherCustomScripts(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.DESCRIPTION, null, targetArray, null);
		Filter scriptTypeFilter = Filter.createNOTFilter(
				Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, CustomScriptType.PERSON_AUTHENTICATION));
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(descriptionFilter, displayNameFilter);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class,
				Filter.createANDFilter(searchFilter, scriptTypeFilter), sizeLimit);
	}

	public List<CustomScript> findScriptByType(CustomScriptType type, int sizeLimit) {
		Filter searchFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, type);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class, searchFilter,
				sizeLimit);
	}

	public List<CustomScript> findScriptByType(CustomScriptType type) {
		Filter searchFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, type);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class, searchFilter, null);
	}

	public List<CustomScript> findScriptByPatternAndType(String pattern, CustomScriptType type, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.DESCRIPTION, null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(descriptionFilter, displayNameFilter);
		Filter typeFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, type);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class,
				Filter.createANDFilter(searchFilter, typeFilter), sizeLimit);
	}

	public List<CustomScript> findScriptByPatternAndType(String pattern, CustomScriptType type) {
		String[] targetArray = new String[] { pattern };
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.DESCRIPTION, null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(descriptionFilter, displayNameFilter);
		Filter typeFilter = Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, type);
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class,
				Filter.createANDFilter(searchFilter, typeFilter), null);
	}

	public List<CustomScript> findOtherCustomScripts(int sizeLimit) {
		Filter searchFilter = Filter.createNOTFilter(
				Filter.createEqualityFilter(OxTrustConstants.SCRYPT_TYPE, CustomScriptType.PERSON_AUTHENTICATION));
		return persistenceEntryManager.findEntries(getDnForCustomScript(null), CustomScript.class, searchFilter,
				sizeLimit);
	}

	public String getDnForCustomScript(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=scripts,%s", orgDn);
		}
		return String.format("inum=%s,ou=scripts,%s", inum, orgDn);
	}
}
