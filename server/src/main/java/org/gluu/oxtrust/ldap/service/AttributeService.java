/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.service.cdi.event.EventType;
import org.gluu.oxtrust.service.cdi.event.EventTypeQualifier;
import org.gluu.oxtrust.service.cdi.event.Events;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.search.filter.Filter;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.attribute.AttributeDataType;
import org.xdi.model.attribute.AttributeUsageType;
import org.xdi.model.attribute.Multivalued;
import org.xdi.model.scim.ScimCustomAtribute;
import org.xdi.model.user.UserRole;
import org.xdi.service.SchemaService;
import org.xdi.util.INumGenerator;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.LDAPException;

/**
 * Provides operations with attributes
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Stateless
@Named
public class AttributeService extends org.xdi.service.AttributeService {

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private SchemaService schemaService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	@Any
	private Event<Events> event;

	public static final String CUSTOM_ATTRIBUTE_OBJECTCLASS_PREFIX = "ox-";

	/**
	 * Get all person attributes
	 * 
	 * @param gluuUserRole
	 *            User role
	 * @return List of person attributes
	 */
	@SuppressWarnings("unchecked")
	public List<GluuAttribute> getAllPersonAttributes(UserRole gluuUserRole) {
		String key = OxTrustConstants.CACHE_ATTRIBUTE_PERSON_KEY_LIST + "_" + gluuUserRole.getValue();
		List<GluuAttribute> attributeList = (List<GluuAttribute>) cacheService.get(OxConstants.CACHE_ATTRIBUTE_NAME,
				key);
		if (attributeList == null) {
			attributeList = getAllPersonAtributesImpl(gluuUserRole, getAllAttributes());
			cacheService.put(OxConstants.CACHE_ATTRIBUTE_NAME, key, attributeList);
		}

		return attributeList;
	}

	/**
	 * Get all organization attributes
	 * 
	 * @param attributes
	 *            List of attributes
	 * @return List of organization attributes
	 */
	private List<GluuAttribute> getAllPersonAtributesImpl(UserRole gluuUserRole,
			Collection<GluuAttribute> attributes) {
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();

		String[] objectClassTypes = appConfiguration.getPersonObjectClassTypes();
		log.debug("objectClassTypes={}", Arrays.toString(objectClassTypes));
		for (GluuAttribute attribute : attributes) {
			if (StringHelper.equalsIgnoreCase(attribute.getOrigin(), appConfiguration.getPersonCustomObjectClass())
					&& (UserRole.ADMIN == gluuUserRole)) {
				attribute.setCustom(true);
				returnAttributeList.add(attribute);
				continue;
			}

			for (String objectClassType : objectClassTypes) {
				if (attribute.getOrigin().equals(objectClassType)
						&& ((attribute.allowViewBy(gluuUserRole) || attribute.allowEditBy(gluuUserRole)))) {
					returnAttributeList.add(attribute);
					break;
				}
			}
		}

		return returnAttributeList;
	}

	/**
	 * Get all contact attributes
	 * 
	 * @return List of contact attributes
	 */
	@SuppressWarnings("unchecked")
	public List<GluuAttribute> getAllContactAttributes(UserRole gluuUserRole) {
		String key = OxTrustConstants.CACHE_ATTRIBUTE_CONTACT_KEY_LIST + "_" + gluuUserRole.getValue();
		List<GluuAttribute> attributeList = (List<GluuAttribute>) cacheService.get(OxConstants.CACHE_ATTRIBUTE_NAME,
				key);
		if (attributeList == null) {
			attributeList = getAllContactAtributesImpl(gluuUserRole, getAllAttributes());
			cacheService.put(OxConstants.CACHE_ATTRIBUTE_NAME, key, attributeList);
		}

		return attributeList;
	}

	/**
	 * Get all contact attributes
	 * 
	 * @param attributes
	 *            List of attributes
	 * @return List of contact attributes
	 */
	private List<GluuAttribute> getAllContactAtributesImpl(UserRole gluuUserRole,
			Collection<GluuAttribute> attributes) {
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();

		String[] objectClassTypes = appConfiguration.getContactObjectClassTypes();
		for (GluuAttribute attribute : attributes) {
			if (StringHelper.equalsIgnoreCase(attribute.getOrigin(), appConfiguration.getPersonCustomObjectClass())
					&& (UserRole.ADMIN == gluuUserRole)) {
				attribute.setCustom(true);
				returnAttributeList.add(attribute);
				continue;
			}

			for (String objectClassType : objectClassTypes) {
				if (attribute.getOrigin().equals(objectClassType)
						&& (attribute.allowViewBy(gluuUserRole) || attribute.allowEditBy(gluuUserRole))) {
					returnAttributeList.add(attribute);
					break;
				}
			}
		}

		return returnAttributeList;
	}

	/**
	 * Get all origins
	 * 
	 * @return List of origins
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllAttributeOrigins() {
		List<String> attributeOriginList = (List<String>) cacheService.get(OxConstants.CACHE_ATTRIBUTE_NAME,
				OxTrustConstants.CACHE_ATTRIBUTE_ORIGIN_KEY_LIST);
		if (attributeOriginList == null) {
			attributeOriginList = getAllAttributeOrigins(getAllAttributes());
			cacheService.put(OxConstants.CACHE_ATTRIBUTE_NAME, OxTrustConstants.CACHE_ATTRIBUTE_ORIGIN_KEY_LIST,
					attributeOriginList);
		}

		return attributeOriginList;
	}

	/**
	 * Get all origins
	 * 
	 * @param attributes
	 *            List of attributes
	 * @return List of origins
	 */
	public List<String> getAllAttributeOrigins(Collection<GluuAttribute> attributes) {
		List<String> attributeOriginList = new ArrayList<String>();

		for (GluuAttribute attribute : attributes) {
			String origin = attribute.getOrigin();
			if (!attributeOriginList.contains(origin)) {
				attributeOriginList.add(attribute.getOrigin());
			}
		}

		String customOrigin = getCustomOrigin();
		if (!attributeOriginList.contains(customOrigin)) {
			attributeOriginList.add(customOrigin);
		}

		return attributeOriginList;
	}

	/**
	 * Get origin display names
	 * 
	 * @param attributes
	 *            List of origins
	 * @param objectClassTypes
	 *            List of objectClasses
	 * @param objectClassDisplayNames
	 *            List of display names for objectClasses
	 * @return Map with key = origin and value = display name
	 */
	public Map<String, String> getAllAttributeOriginDisplayNames(List<String> attributeOriginList,
			String[] objectClassTypes, String[] objectClassDisplayNames) {
		// Put displayNames = origins if user don't specify right mapping in
		// properties file
		Map<String, String> attributeOriginDisplayNameMap = new HashMap<String, String>();
		for (String origin : attributeOriginList) {
			attributeOriginDisplayNameMap.put(origin, origin);
		}

		if (objectClassTypes.length == objectClassDisplayNames.length) {
			for (int i = 0; i < objectClassTypes.length; i++) {
				String objectClass = objectClassTypes[i];
				if (attributeOriginDisplayNameMap.containsKey(objectClass)) {
					attributeOriginDisplayNameMap.put(objectClass, objectClassDisplayNames[i]);
				}
			}
		}

		return attributeOriginDisplayNameMap;
	}

	/**
	 * Get custom attributes
	 * 
	 * @return List of cusomt attributes
	 */
	@SuppressWarnings("unchecked")
	public List<GluuAttribute> getCustomAttributes() {
		List<GluuAttribute> attributeList = (List<GluuAttribute>) cacheService.get(OxConstants.CACHE_ATTRIBUTE_NAME,
				OxTrustConstants.CACHE_ATTRIBUTE_CUSTOM_KEY_LIST);
		if (attributeList == null) {
			attributeList = new ArrayList<GluuAttribute>();
			for (GluuAttribute attribute : getAllAttributes()) {
				if (attribute.isCustom()) {
					attributeList.add(attribute);
				}
			}

			cacheService.put(OxConstants.CACHE_ATTRIBUTE_NAME, OxTrustConstants.CACHE_ATTRIBUTE_CUSTOM_KEY_LIST,
					attributeList);
		}

		return attributeList;
	}

	/**
	 * Get attribute by inum
	 * 
	 * @param inum
	 *            Inum
	 * @return Attribute
	 */
	public GluuAttribute getAttributeByInum(String inum) {
		return getAttributeByInum(inum, getAllAtributesImpl(getDnForAttribute(null)));
	}

	public GluuAttribute getAttributeByInum(String inum, List<GluuAttribute> attributes) {
		for (GluuAttribute attribute : attributes) {
			if (attribute.getInum().equals(inum)) {
				return attribute;
			}
		}

		return null;
	}

	/**
	 * Get SCIM related attributes
	 * 
	 * @return Attribute
	 */
	public List<GluuAttribute> getSCIMRelatedAttributes() throws Exception {
		return getSCIMRelatedAttributesImpl(getAllAttributes());
	}

	/**
	 * Get SCIMAttributes
	 * 
	 * @param attributes
	 *            List of attributes
	 * @return list of Attributes
	 */
	public List<GluuAttribute> getSCIMRelatedAttributesImpl(List<GluuAttribute> attributes) throws Exception {

		List<GluuAttribute> result = new ArrayList<GluuAttribute>();

		for (GluuAttribute attribute : attributes) {

			boolean isEmpty = attribute.getOxSCIMCustomAttribute() == null;

			if (!isEmpty) {

				if (attribute.getOxSCIMCustomAttribute().getValue().equalsIgnoreCase("true")) {
					result.add(attribute);
				}
			}
		}

		return result;
	}

	/**
	 * Add new custom attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public void addAttribute(GluuAttribute attribute) {
		ldapEntryManager.persist(attribute);

		event.select(new EventTypeQualifier(Events.EVENT_CLEAR_ATTRIBUTES)).fire(Events.EVENT_CLEAR_ATTRIBUTES);
	}

	/**
	 * Remove attribute with specified Inum
	 * 
	 * @param inum
	 *            Inum
	 */
	public void removeAttribute(String inum) {
		GluuAttribute attribute = new GluuAttribute();
		attribute.setDn(getDnForAttribute(inum));

		removeAttribute(attribute);
	}

	/**
	 * Remove attribute with specified Inum
	 * 
	 * @param inum
	 *            Inum
	 */
	public void removeAttribute(GluuAttribute attribute) {
		log.trace("Removing attribute {}", attribute.getDisplayName());
		ldapEntryManager.remove(attribute);

		event.select(new EventTypeQualifier(Events.EVENT_CLEAR_ATTRIBUTES)).fire(Events.EVENT_CLEAR_ATTRIBUTES);
	}

	/**
	 * Update specified attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public void updateAttribute(GluuAttribute attribute) {
		ldapEntryManager.merge(attribute);

		event.select(new EventTypeQualifier(Events.EVENT_CLEAR_ATTRIBUTES)).fire(Events.EVENT_CLEAR_ATTRIBUTES);
	}

	/**
	 * Clear attributes cache after receiving event that attributes were changed
	 */
	public void clearAttributesCache(@Observes @EventType(Events.EVENT_CLEAR_ATTRIBUTES) Events event) {
		log.debug("Removing attributes from cache");
		cacheService.removeAll(OxConstants.CACHE_ATTRIBUTE_NAME);
	}

	/**
	 * Get all available data types
	 * 
	 * @return Array of data types
	 */
	public AttributeDataType[] getDataTypes() {
		return AttributeDataType.values();
	}

	/**
	 * Get all available Scim Custom Atributes
	 * 
	 * @return Array of data types
	 */
	public ScimCustomAtribute[] getScimCustomAttribute() {
		return ScimCustomAtribute.values();
	}

	/**
	 * Get all available oxMultivalued attributes
	 * 
	 * @return Array of data types
	 */
	public Multivalued[] getOxMultivalued() {
		return Multivalued.values();
	}

	/**
	 * Get all available attribute user roles
	 * 
	 * @return Array of attribute user roles
	 */
	public UserRole[] getAttributeUserRoles() {
		return new UserRole[] { UserRole.ADMIN, UserRole.USER };
	}

	/**
	 * Get all available attribute view types
	 * 
	 * @return Array of attribute user roles
	 */
	public UserRole[] getViewTypes() {
		if (applianceService.getAppliance().getWhitePagesEnabled() != null
				&& applianceService.getAppliance().getWhitePagesEnabled().isBooleanValue()) {
			return new UserRole[] { UserRole.ADMIN, UserRole.USER, UserRole.WHITEPAGES };
		}
		return new UserRole[] { UserRole.ADMIN, UserRole.USER };
	}

	/**
	 * Get all usage types
	 * 
	 * @return Array of Usage types
	 */
	public AttributeUsageType[] getAttributeUsageTypes() {
		return new AttributeUsageType[] { AttributeUsageType.OPENID };
	}

	/**
	 * Check if LDAP server contains attribute with specified attributes
	 * 
	 * @return True if attribute with specified attributes exist
	 */
	public boolean containsAttribute(GluuAttribute attribute) {
		return ldapEntryManager.contains(attribute);
	}

	/**
	 * Generate new inum for attribute
	 * 
	 * @return New inum for attribute
	 */
	public String generateInumForNewAttribute() {
		GluuAttribute attribute = null;
		String newInum = null;
		do {
			newInum = generateInumForNewAttributeImpl();
			String newDn = getDnForAttribute(newInum);
			attribute = new GluuAttribute();
			attribute.setDn(newDn);
		} while (containsAttribute(attribute));

		return newInum;
	}

	/**
	 * Convert inum to string without delimiters
	 * 
	 * @return Inum string without delimiters
	 */
	public String toInumWithoutDelimiters(String inum) {
		return inum.replace(".", "").replace(OxTrustConstants.inumDelimiter, "").replace("@", "");
	}

	public String generateRandomOid() {
		return Long.toString(System.currentTimeMillis());
	}

	/**
	 * Generate new inum for attribute
	 * 
	 * @return New inum for attribute
	 * @throws Exception
	 */
	private String generateInumForNewAttributeImpl() {
		String orgInum = organizationService.getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + "0005" + OxTrustConstants.inumDelimiter + generateInum();
	}

	private String generateInum() {
		String inum = "";
		int value;
		while (true) {
			inum = INumGenerator.generate(1);
			try {
				value = Integer.parseInt(inum, 16);
				if (value < 7) {
					continue;
				}
			} catch (Exception ex) {
				log.error("Error generating inum: ", ex);

			}
			break;
		}
		return inum;
	}

	/**
	 * Build DN string for attribute
	 * 
	 * @param inum
	 *            Inum
	 * @return DN string for specified attribute or DN for attributes branch if inum
	 *         is null
	 */
	public String getDnForAttribute(String inum) {
		String organizationDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=attributes,%s", organizationDn);
		}

		return String.format("inum=%s,ou=attributes,%s", inum, organizationDn);
	}

	/**
	 * Return current custom origin
	 * 
	 * @return Current custom origin
	 */
	public String getCustomOrigin() {
		return appConfiguration.getPersonCustomObjectClass();
	}

	@Override
	protected List<GluuAttribute> getAllAtributesImpl(String baseDn) {
		List<GluuAttribute> attributeList = ldapEntryManager.findEntries(baseDn, GluuAttribute.class, null);
		String customOrigin = getCustomOrigin();
		for (GluuAttribute attribute : attributeList) {
			attribute.setCustom(customOrigin.equals(attribute.getOrigin()));
		}

		return attributeList;
	}

	/**
	 * Set metadata for every custom attribute
	 * 
	 * @param customAttributes
	 *            List of custom attributes
	 * @param attributes
	 *            List of attributes
	 */
	public void setAttributeMetadata(List<GluuCustomAttribute> customAttributes, List<GluuAttribute> attributes) {
		if ((customAttributes == null) || (attributes == null)) {
			return;
		}

		for (GluuCustomAttribute personAttribute : customAttributes) {
			GluuAttribute tmpAttribute = getAttributeByName(personAttribute.getName(), attributes);
			if (tmpAttribute == null) {
				log.error("Failed to find attribute '{}' metadata", personAttribute.getName());
			}

			personAttribute.setMetadata(tmpAttribute);
		}
	}

	/**
	 * Get custom attributes by attribute DNs
	 * 
	 * @param customAttributes
	 *            List of attribute DNs
	 * @param attributes
	 *            List of custom attributes
	 */
	public List<GluuCustomAttribute> getCustomAttributesByAttributeDNs(List<String> attributeDNs,
			HashMap<String, GluuAttribute> attributesByDNs) {
		List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();
		if (attributeDNs == null) {
			return customAttributes;
		}

		for (String releasedAttributeDn : attributeDNs) {
			GluuAttribute attribute = attributesByDNs.get(releasedAttributeDn);
			if (attribute != null) {
				GluuCustomAttribute customAttribute = new GluuCustomAttribute(attribute.getName(), releasedAttributeDn);
				customAttribute.setMetadata(attribute);
				customAttributes.add(customAttribute);
			}
		}

		return customAttributes;
	}

	public HashMap<String, GluuAttribute> getAttributeMapByDNs(List<GluuAttribute> attributes) {
		HashMap<String, GluuAttribute> attributeDns = new HashMap<String, GluuAttribute>();
		for (GluuAttribute attribute : attributes) {
			attributeDns.put(attribute.getDn(), attribute);
		}

		return attributeDns;
	}

	public void sortCustomAttributes(List<GluuCustomAttribute> customAttributes, String sortByProperties) {
		ldapEntryManager.sortListByProperties(GluuCustomAttribute.class, customAttributes, false, sortByProperties);
	}

	/**
	 * Build DN string for group
	 * 
	 * @param inum
	 *            Group Inum
	 * @return DN string for specified group or DN for groups branch if inum is null
	 */
	public String getDnForGroup(String inum) throws Exception {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=groups,%s", orgDn);
		}

		return String.format("inum=%s,ou=groups,%s", inum, orgDn);
	}

	/**
	 * @param admin
	 * @return
	 */
	public List<GluuAttribute> getAllActivePersonAttributes(UserRole admin) {
		@SuppressWarnings("unchecked")
		List<GluuAttribute> activeAttributeList = (List<GluuAttribute>) cacheService
				.get(OxConstants.CACHE_ACTIVE_ATTRIBUTE_NAME, OxConstants.CACHE_ACTIVE_ATTRIBUTE_KEY_LIST);
		if (activeAttributeList == null) {
			activeAttributeList = getAllActiveAtributesImpl(admin);
			cacheService.put(OxConstants.CACHE_ACTIVE_ATTRIBUTE_NAME, OxConstants.CACHE_ATTRIBUTE_KEY_LIST,
					activeAttributeList);
		}

		return activeAttributeList;
	}

	/**
	 * @return
	 * @throws LDAPException
	 */
	private List<GluuAttribute> getAllActiveAtributesImpl(UserRole gluuUserRole) {
		Filter filter = Filter.createEqualityFilter("gluuStatus", "active");
		List<GluuAttribute> attributeList = ldapEntryManager.findEntries(getDnForAttribute(null), GluuAttribute.class,
				filter);
		String customOrigin = getCustomOrigin();
		String[] objectClassTypes = appConfiguration.getPersonObjectClassTypes();
		log.debug("objectClassTypes={}", Arrays.toString(objectClassTypes));
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();
		for (GluuAttribute attribute : attributeList) {
			if (StringHelper.equalsIgnoreCase(attribute.getOrigin(), appConfiguration.getPersonCustomObjectClass())
					&& (UserRole.ADMIN == gluuUserRole)) {
				attribute.setCustom(true);
				returnAttributeList.add(attribute);
				continue;
			}

			for (String objectClassType : objectClassTypes) {
				if (attribute.getOrigin().equals(objectClassType)
						&& ((attribute.allowViewBy(gluuUserRole) || attribute.allowEditBy(gluuUserRole)))) {
					attribute.setCustom(customOrigin.equals(attribute.getOrigin()));
					returnAttributeList.add(attribute);
					break;
				}
			}
		}

		return returnAttributeList;
	}

	/**
	 * Search groups by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of groups
	 * @throws Exception
	 */
	public List<GluuAttribute> searchAttributes(String pattern, int sizeLimit) throws Exception {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter nameFilter = Filter.createSubstringFilter(OxTrustConstants.attributeName, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter, nameFilter);

		List<GluuAttribute> result = ldapEntryManager.findEntries(getDnForAttribute(null), GluuAttribute.class,
				searchFilter, sizeLimit);
		String customOrigin = getCustomOrigin();
		for (GluuAttribute attribute : result) {
			attribute.setCustom(customOrigin.equals(attribute.getOrigin()));
		}

		return result;
	}

	public List<GluuAttribute> searchPersonAttributes(String pattern, int sizeLimit) throws Exception {
		String[] objectClassTypes = appConfiguration.getPersonObjectClassTypes();
		String[] targetArray = new String[] { pattern };
		List<Filter> originFilters = new ArrayList<Filter>();
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		for (String objectClassType : objectClassTypes) {
			Filter originFilter = Filter.createEqualityFilter(OxTrustConstants.origin, objectClassType);
			originFilters.add(originFilter);
		}
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);
		Filter originFilter = Filter.createORFilter(originFilters.toArray(new Filter[0]));
		Filter filter = Filter.createANDFilter(searchFilter, originFilter);

		List<GluuAttribute> result = ldapEntryManager.findEntries(getDnForAttribute(null), GluuAttribute.class, filter,
				sizeLimit);

		return result;
	}

	public GluuAttribute getAttributeByDn(String Dn) throws Exception {
		return ldapEntryManager.find(GluuAttribute.class, Dn);
	}

}
