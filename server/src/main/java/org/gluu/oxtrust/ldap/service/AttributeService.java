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

import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.GluuAttributePrivacyLevel;
import org.xdi.model.GluuAttributeUsageType;
import org.xdi.model.GluuUserRole;
import org.xdi.model.OxMultivalued;
import org.xdi.model.ScimCustomAtribute;
import org.xdi.service.SchemaService;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * Provides operations with attributes
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Scope(ScopeType.STATELESS)
@Name("attributeService")
@AutoCreate
public class AttributeService  extends org.xdi.service.AttributeService{

	@In
	private SchemaService schemaService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	public static final String CUSTOM_ATTRIBUTE_OBJECTCLASS_PREFIX = "ox-";

	/**
	 * Get all attributes
	 * 
	 * @return List of attributes
	 */



	/**
	 * Get all person attributes
	 * 
	 * @param gluuUserRole
	 *            User role
	 * @return List of person attributes
	 */
	@SuppressWarnings("unchecked")
	public List<GluuAttribute> getAllPersonAttributes(GluuUserRole gluuUserRole) {
		String key = OxTrustConstants.CACHE_ATTRIBUTE_PERSON_KEY_LIST + "_" + gluuUserRole.getValue();
		List<GluuAttribute> attributeList = (List<GluuAttribute>) getCacheService().get(OxTrustConstants.CACHE_ATTRIBUTE_NAME, key);
		if (attributeList == null) {
			attributeList = getAllPersonAtributesImpl(gluuUserRole, getAllAttributes());
			getCacheService().put(OxTrustConstants.CACHE_ATTRIBUTE_NAME, key, attributeList);
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
	private List<GluuAttribute> getAllPersonAtributesImpl(GluuUserRole gluuUserRole, Collection<GluuAttribute> attributes) {
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();

		String[] objectClassTypes = applicationConfiguration.getPersonObjectClassTypes();
		getLog().debug("objectClassTypes={0}", Arrays.toString(objectClassTypes));
		for (GluuAttribute attribute : attributes) {
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
	public List<GluuAttribute> getAllContactAttributes(GluuUserRole gluuUserRole) {
		String key = OxTrustConstants.CACHE_ATTRIBUTE_CONTACT_KEY_LIST + "_" + gluuUserRole.getValue();
		List<GluuAttribute> attributeList = (List<GluuAttribute>) getCacheService().get(OxTrustConstants.CACHE_ATTRIBUTE_NAME, key);
		if (attributeList == null) {
			attributeList = getAllContactAtributesImpl(gluuUserRole, getAllAttributes());
			getCacheService().put(OxTrustConstants.CACHE_ATTRIBUTE_NAME, key, attributeList);
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
	private List<GluuAttribute> getAllContactAtributesImpl(GluuUserRole gluuUserRole, Collection<GluuAttribute> attributes) {
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();

		String[] objectClassTypes = applicationConfiguration.getContactObjectClassTypes();
		for (GluuAttribute attribute : attributes) {
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
		List<String> attributeOriginList = (List<String>) getCacheService().get(OxTrustConstants.CACHE_ATTRIBUTE_NAME,
				OxTrustConstants.CACHE_ATTRIBUTE_ORIGIN_KEY_LIST);
		if (attributeOriginList == null) {
			attributeOriginList = getAllAttributeOrigins(getAllAttributes());
			getCacheService().put(OxTrustConstants.CACHE_ATTRIBUTE_NAME, OxTrustConstants.CACHE_ATTRIBUTE_ORIGIN_KEY_LIST, attributeOriginList);
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
	public Map<String, String> getAllAttributeOriginDisplayNames(List<String> attributeOriginList, String[] objectClassTypes,
			String[] objectClassDisplayNames) {
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
		List<GluuAttribute> attributeList = (List<GluuAttribute>) getCacheService().get(OxTrustConstants.CACHE_ATTRIBUTE_NAME,
				OxTrustConstants.CACHE_ATTRIBUTE_CUSTOM_KEY_LIST);
		if (attributeList == null) {
			attributeList = new ArrayList<GluuAttribute>();
			for (GluuAttribute attribute : getAllAttributes()) {
				if (attribute.isCustom()) {
					attributeList.add(attribute);
				}
			}

			getCacheService().put(OxTrustConstants.CACHE_ATTRIBUTE_NAME, OxTrustConstants.CACHE_ATTRIBUTE_CUSTOM_KEY_LIST, attributeList);
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
		return getAttributeByInum(inum, getAllAttributes());
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
	 * Get attribute by name
	 * 
	 * @param name
	 *            Name
	 * @return Attribute
	 */
	public GluuAttribute getAttributeByName(String name) {
		return getAttributeByName(name, getAllAttributes());
	}

	/**
	 * Get attribute by name
	 * 
	 * @param name
	 *            Name
	 * @param attributes
	 *            List of attributes
	 * @return Attribute
	 */
	public GluuAttribute getAttributeByName(String name, List<GluuAttribute> attributes) {
		for (GluuAttribute attribute : attributes) {
			if (attribute.getName().equals(name)) {
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
		getLdapEntryManager().persist(attribute);

		Events.instance().raiseEvent(OxTrustConstants.EVENT_CLEAR_ATTRIBUTES);
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
	 * Remove attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public boolean removeAttribute(GluuAttribute attribute) {
		getLog().info("Attribute removal started");
		getLog().trace("Getting attribute information");
		
		String objectClassName = getCustomOrigin();
		getLog().debug("objectClassName is " + objectClassName);
		
		String attributeName = attribute.getName();
		getLog().debug("attributeName is " + attributeName);
		
		getLog().trace("Removing attribute from people");
		List<GluuCustomPerson> people = PersonService.instance().searchPersons("", OxTrustConstants.searchPersonsSizeLimit);
		getLog().trace(String.format("Iterating %d people", people.size()));
		for (GluuCustomPerson person : people) {
			getLog().trace(String.format("Analyzing %s.", person.getUid()));
			List<GluuCustomAttribute> customAttrs = person.getCustomAttributes();
			for (GluuCustomAttribute attr : customAttrs) {
				getLog().trace(String.format("%s has custom attribute %s", person.getUid(), attr.getName()));
				if (attr.getName().equals(attributeName)) {
					getLog().trace(String.format("%s matches %s .  deleting it.", attr.getName(), attributeName));
					customAttrs.remove(attr);
					person.setCustomAttributes(customAttrs);
					PersonService.instance().updatePerson(person);
					break;
				}
			}
		}

		getLog().trace("Removing attribute from trustRelationships");
		List<GluuSAMLTrustRelationship> trustRelationships = TrustService.instance().getAllTrustRelationships();
		getLog().trace(String.format("Iterating %d trustRelationships", trustRelationships.size()));
		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			getLog().trace(String.format("Analyzing %s.", trustRelationship.getDisplayName()));
			List<String> customAttrs = trustRelationship.getReleasedAttributes();
			if (customAttrs != null) {
				for (String attrDN : customAttrs) {
					getLog().trace(String.format("%s has custom attribute %s", trustRelationship.getDisplayName(), attrDN));
					if (attrDN.equals(attribute.getDn())) {
						getLog().trace(String.format("%s matches %s .  deleting it.", attrDN, attribute.getDn()));
						List<String> updatedAttrs = new ArrayList<String>();
						updatedAttrs.addAll(customAttrs);
						updatedAttrs.remove(attrDN);
						if (updatedAttrs.size() == 0) {
							trustRelationship.setReleasedAttributes(null);
						} else {
							trustRelationship.setReleasedAttributes(updatedAttrs);
						}

						TrustService.instance().updateTrustRelationship(trustRelationship);
						break;
					}
				}
			}
		}
		getLog().trace("Removing attribute from objectclass");
		// Unregister new attribute type from custom object class
		try {
			schemaService.removeAttributeTypeFromObjectClass(objectClassName, attributeName);
		} catch (Exception ex) {
			getLog().error("Failed to remove attribute type from LDAP schema's object class", ex);
			return false;
		}
		
		getLog().trace("Removing attribute from schema");
		// Remove attribute type from LDAP schema
		try {
			schemaService.removeStringAttribute(attributeName);
		} catch (Exception ex) {
			getLog().error("Failed to remove attribute type from LDAP schema", ex);
			return false;
		}

		getLog().trace("Removing attribute for good.");
		getLdapEntryManager().remove(attribute);
		Events.instance().raiseEvent(OxTrustConstants.EVENT_CLEAR_ATTRIBUTES);
		return true;
	}

	/**
	 * Update specified attribute
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public void updateAttribute(GluuAttribute attribute) {
		getLdapEntryManager().merge(attribute);
		Events.instance().raiseEvent(OxTrustConstants.EVENT_CLEAR_ATTRIBUTES);
	}

	/**
	 * Clear attributes cache after receiving event that attributes were changed
	 */
	@Observer(OxTrustConstants.EVENT_CLEAR_ATTRIBUTES)
	public void clearAttributesCache() {
		getLog().debug("Removing attributes from cache");
		getCacheService().removeAll(OxTrustConstants.CACHE_ATTRIBUTE_NAME);
	}

	/**
	 * Get all available data types
	 * 
	 * @return Array of data types
	 */
	public GluuAttributeDataType[] getDataTypes() {
		return GluuAttributeDataType.values();
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
	public OxMultivalued[] getOxMultivalued() {
		return OxMultivalued.values();
	}

	/**
	 * Get all available attribute user roles
	 * 
	 * @return Array of attribute user roles
	 */
	public GluuUserRole[] getAttributeUserRoles() {
		return new GluuUserRole[] { GluuUserRole.ADMIN, GluuUserRole.USER };
	}

	/**
	 * Get all available attribute view types
	 * 
	 * @return Array of attribute user roles
	 */
	public GluuUserRole[] getViewTypes() {
		if (ApplianceService.instance().getAppliance().getWhitePagesEnabled() != null
				&& ApplianceService.instance().getAppliance().getWhitePagesEnabled().isBooleanValue()) {
			return new GluuUserRole[] { GluuUserRole.ADMIN, GluuUserRole.USER, GluuUserRole.WHITEPAGES };
		}
		return new GluuUserRole[] { GluuUserRole.ADMIN, GluuUserRole.USER };
	}

	/**
	 * Get all usage types
	 * 
	 * @return Array of Usage types
	 */
	public GluuAttributeUsageType[] getAttributeUsageTypes() {
		return GluuAttributeUsageType.values();
	}

	/**
	 * Get all available privacy levels
	 * 
	 * @return Array of Privacy levels
	 */
	public GluuAttributePrivacyLevel[] getPrivacyLevels() {
		return GluuAttributePrivacyLevel.values();
	}

	/**
	 * Check if LDAP server contains attribute with specified attributes
	 * 
	 * @return True if attribute with specified attributes exist
	 */
	public boolean containsAttribute(GluuAttribute attribute) {
		return getLdapEntryManager().contains(attribute);
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
		String orgInum = OrganizationService.instance().getInumForOrganization();
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
				getLog().error("Error generating inum: ", ex);

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
	 * @return DN string for specified attribute or DN for attributes branch if
	 *         inum is null
	 */
	public String getDnForAttribute(String inum) {
		String organizationDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=attributes,%s", organizationDn);
		}

		return String.format("inum=%s,ou=attributes,%s", inum, organizationDn);
	}
	
	public List<GluuAttribute> getAllAttributes() {
		return getAllAttributes(getDnForAttribute(null));
	}
	
	/**
	 * Return current custom origin
	 * 
	 * @return Current custom origin
	 */
	public String getCustomOrigin() {
		return applicationConfiguration.getPersonCustomObjectClass();
		// return CUSTOM_ATTRIBUTE_OBJECTCLASS_PREFIX +
		// toInumWithoutDelimiters(organizationService.getInumForOrganization());
	}
	
	@Override
	protected List<GluuAttribute> getAllAtributesImpl(String baseDn) {
		List<GluuAttribute> attributeList = getLdapEntryManager().findEntries(baseDn, GluuAttribute.class, null);
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
				getLog().error("Failed to find attribute '{0}' metadata", personAttribute.getName());
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
		getLdapEntryManager().sortListByProperties(GluuCustomAttribute.class, customAttributes, sortByProperties);
	}

	public static AttributeService instance() {
		return (AttributeService) Component.getInstance(AttributeService.class);
	}

	/**
	 * Build DN string for group
	 * 
	 * @param inum
	 *            Group Inum
	 * @return DN string for specified group or DN for groups branch if inum is
	 *         null
	 */
	public String getDnForGroup(String inum) throws Exception {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=groups,%s", orgDn);
		}

		return String.format("inum=%s,ou=groups,%s", inum, orgDn);
	}

	/**
	 * @param admin
	 * @return
	 */
	public List<GluuAttribute> getAllActivePersonAttributes(GluuUserRole admin) {
		@SuppressWarnings("unchecked")
		List<GluuAttribute> activeAttributeList = (List<GluuAttribute>) getCacheService().get(OxTrustConstants.CACHE_ACTIVE_ATTRIBUTE_NAME,
				OxTrustConstants.CACHE_ACTIVE_ATTRIBUTE_KEY_LIST);
		if (activeAttributeList == null) {
			try {
				activeAttributeList = getAllActiveAtributesImpl(admin);
			} catch (LDAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getCacheService().put(OxTrustConstants.CACHE_ACTIVE_ATTRIBUTE_NAME, OxTrustConstants.CACHE_ATTRIBUTE_KEY_LIST, activeAttributeList);
		}

		return activeAttributeList;
	}

	/**
	 * @return
	 * @throws LDAPException
	 */
	private List<GluuAttribute> getAllActiveAtributesImpl(GluuUserRole gluuUserRole) throws LDAPException {
		List<GluuAttribute> attributeList = getLdapEntryManager().findEntries(getDnForAttribute(null), GluuAttribute.class,
				Filter.create("gluuStatus=active"));
		String customOrigin = getCustomOrigin();
		String[] objectClassTypes = applicationConfiguration.getPersonObjectClassTypes();
		getLog().debug("objectClassTypes={0}", Arrays.toString(objectClassTypes));
		List<GluuAttribute> returnAttributeList = new ArrayList<GluuAttribute>();
		for (GluuAttribute attribute : attributeList) {
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
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);

		List<GluuAttribute> result = getLdapEntryManager().findEntries(getDnForAttribute(null), GluuAttribute.class, searchFilter, sizeLimit);
		String customOrigin = getCustomOrigin();
		for (GluuAttribute attribute : result) {
			attribute.setCustom(customOrigin.equals(attribute.getOrigin()));
		}
		
		return result;
	}

	public List<GluuAttribute> searchPersonAttributes(String pattern, int sizeLimit) throws Exception {
		String[] objectClassTypes = applicationConfiguration.getPersonObjectClassTypes();
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

		List<GluuAttribute> result = getLdapEntryManager().findEntries(getDnForAttribute(null), GluuAttribute.class, filter, sizeLimit);

		return result;
	}

	
}
