package org.gluu.oxtrust.ldap.cache.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.model.CustomEntry;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapAttributesList;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;

/**
 * Person with custom attributes
 * 
 * @author Yuriy Movchan Date: 07.13.2011
 */
@LdapEntry
public class GluuSimplePerson extends CustomEntry implements Serializable {

	private static final long serialVersionUID = -2279582184398161100L;

	private String sourceServerName;

	@LdapAttributesList(name = "name", value = "values", sortByName = true)
	private List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();

	public List<GluuCustomAttribute> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<GluuCustomAttribute> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public String getSourceServerName() {
		return sourceServerName;
	}

	public void setSourceServerName(String sourceServerName) {
		this.sourceServerName = sourceServerName;
	}

}
