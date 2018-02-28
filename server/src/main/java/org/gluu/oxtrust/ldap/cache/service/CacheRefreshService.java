/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.cache.model.GluuInumMap;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.search.filter.Filter;
import org.slf4j.Logger;
import org.xdi.util.ArrayHelper;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;

/**
 * Provides cache refresh related operations
 * 
 * @author Yuriy Movchan Date: 07.04.2011
 */
@Stateless
@Named("cacheRefreshService")
public class CacheRefreshService implements Serializable {

	private static final long serialVersionUID = -2225880517520443390L;

	@Inject
	private Logger log;

	@Inject
	private InumService inumService;

	public Filter createFilter(String customLdapFilter) {
		if (StringHelper.isEmpty(customLdapFilter)) {
			return null;
		}

		return Filter.create(customLdapFilter);
	}

	public Filter createFilter(String[] keyAttributes, String[] keyObjectClasses, String keyAttributeStart, Filter customFilter) {
		if ((keyAttributes == null) || (keyObjectClasses == null)) {
			return null;
		}

		List<Filter> filters = new ArrayList<Filter>();
		for (int i = 0; i < keyAttributes.length; i++) {
			String filterString = keyAttributes[i];

			if (filterString.contains("=")) {
				filters.add(Filter.create(filterString));
				// } else {
				// filters.add(Filter.createPresenceFilter(filterString));
			}

			// Limit result list
			if ((i == 0) && (keyAttributeStart != null)) {
				int index = filterString.indexOf('=');
				if (index != -1) {
					filterString = filterString.substring(0, index);
				}

				filterString = String.format("%s=%s*", filterString, keyAttributeStart);
				filters.add(Filter.create(filterString));
			}
		}

		for (String keyObjectClass : keyObjectClasses) {
			filters.add(Filter.createEqualityFilter(OxConstants.OBJECT_CLASS, keyObjectClass));
		}

		if (customFilter != null) {
			filters.add(customFilter);
		}

		return Filter.createANDFilter(filters);
	}

	public Filter createObjectClassPresenceFilter() {
		return Filter.createPresenceFilter(OxConstants.OBJECT_CLASS);
	}

	public void addInumMap(LdapEntryManager ldapEntryManager, GluuInumMap inumMap) {
		ldapEntryManager.persist(inumMap);
	}

	public boolean containsInumMap(LdapEntryManager ldapEntryManager, GluuInumMap inumMap) {
		return ldapEntryManager.contains(inumMap);
	}

	public String generateInumForNewInumMap(String inumbBaseDn, LdapEntryManager ldapEntryManager) {
		String newInum = generateInumForNewInumMapImpl();
		String newDn = getDnForInum(inumbBaseDn, newInum);

		GluuInumMap inumMap = new GluuInumMap();
		inumMap.setDn(newDn);

		while (containsInumMap(ldapEntryManager, inumMap)) {
			newInum = generateInumForNewInumMapImpl();
			newDn = getDnForInum(inumbBaseDn, newInum);
			inumMap.setDn(newDn);
		}

		return newInum;
	}

	public String getDnForInum(String baseDn, String inum) {
		return String.format("inum=%s,%s", inum, baseDn);
	}

	private String generateInumForNewInumMapImpl() {
		String inum = inumService.generateInums(OxTrustConstants.INUM_TYPE_PEOPLE_SLUG);
		return inum;
	}

	public void setTargetEntryAttributes(GluuSimplePerson sourcePerson, Map<String, String> targetServerAttributesMapping,
			GluuCustomPerson targetPerson) {
		// Collect all attributes to single map
		Map<String, GluuCustomAttribute> customAttributesMap = new HashMap<String, GluuCustomAttribute>();
		for (GluuCustomAttribute sourceCustomAttribute : sourcePerson.getCustomAttributes()) {
			customAttributesMap.put(StringHelper.toLowerCase(sourceCustomAttribute.getName()), sourceCustomAttribute);
		}

		List<GluuCustomAttribute> resultAttributes = new ArrayList<GluuCustomAttribute>();

		// Add attributes configured via mapping
		Set<String> processedAttributeNames = new HashSet<String>();
		for (Entry<String, String> targetServerAttributeEntry : targetServerAttributesMapping.entrySet()) {
			String sourceKeyAttributeName = StringHelper.toLowerCase(targetServerAttributeEntry.getValue());
			String targetKeyAttributeName = targetServerAttributeEntry.getKey();

			processedAttributeNames.add(sourceKeyAttributeName);

			GluuCustomAttribute gluuCustomAttribute = customAttributesMap.get(sourceKeyAttributeName);
			if (gluuCustomAttribute != null) {
				String[] values = gluuCustomAttribute.getValues();
				String[] clonedValue = ArrayHelper.arrayClone(values);
				
				GluuCustomAttribute gluuCustomAttributeCopy = new GluuCustomAttribute(targetKeyAttributeName, clonedValue);
				gluuCustomAttributeCopy.setName(targetKeyAttributeName);
				resultAttributes.add(gluuCustomAttributeCopy);
			}
		}

		// Set destination entry attributes
		for (Entry<String, GluuCustomAttribute> sourceCustomAttributeEntry : customAttributesMap.entrySet()) {
			if (!processedAttributeNames.contains(sourceCustomAttributeEntry.getKey())) {
				targetPerson.setAttribute(sourceCustomAttributeEntry.getValue());
			}
		}

		for (GluuCustomAttribute resultAttribute : resultAttributes) {
			targetPerson.setAttribute(resultAttribute);
		}
	}

}
