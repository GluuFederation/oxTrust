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

import org.gluu.oxtrust.ldap.cache.model.GluuInumMap;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * Provides cache refresh related operations
 * 
 * @author Yuriy Movchan Date: 07.04.2011
 */
@Scope(ScopeType.STATELESS)
@Name("cacheRefreshService")
@AutoCreate
public class CacheRefreshService implements Serializable {

	private static final long serialVersionUID = -2225880517520443390L;

	@Logger
	private Log log;

	@In
	private InumService inumService;

	public Filter createFilter(String customLdapFilter) {
		if (StringHelper.isEmpty(customLdapFilter)) {
			return null;
		}

		try {
			return Filter.create(customLdapFilter);
		} catch (LDAPException e) {
			log.error("Failed to create filter: {0}", customLdapFilter);

			return null;
		}
	}

	public Filter createFilter(String[] keyAttributes, String[] keyObjectClasses, String keyAttributeStart, Filter customFilter) {
		if ((keyAttributes == null) || (keyObjectClasses == null)) {
			return null;
		}

		List<Filter> filters = new ArrayList<Filter>();
		for (int i = 0; i < keyAttributes.length; i++) {
			String filterString = keyAttributes[i];

			try {
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
			} catch (LDAPException ex) {
				log.error("Failed to create filter: {0}", keyAttributes[i]);
				return null;
			}
		}

		for (String keyObjectClass : keyObjectClasses) {
			filters.add(Filter.createEqualityFilter(OxTrustConstants.objectClass, keyObjectClass));
		}

		if (customFilter != null) {
			filters.add(customFilter);
		}

		return Filter.createANDFilter(filters);
	}

	public Filter createObjectClassPresenceFilter() {
		return Filter.createPresenceFilter(OxTrustConstants.objectClass);
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
			String sourceKeyAttributeName = targetServerAttributeEntry.getValue();
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
