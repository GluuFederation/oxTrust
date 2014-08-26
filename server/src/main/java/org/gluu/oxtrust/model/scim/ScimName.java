package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * User: Dejan Maric Date: 4.4.12.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ScimName {

	private String givenName;
	private String familyName;
	private String middleName;
	private String honorificPrefix;
	private String honorificSuffix;

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getHonorificPrefix() {
		return this.honorificPrefix;
	}

	public void setHonorificPrefix(String honorificPrefix) {
		this.honorificPrefix = honorificPrefix;
	}

	public String getHonorificSuffix() {
		return this.honorificSuffix;
	}

	public void setHonorificSuffix(String honorificSuffix) {
		this.honorificSuffix = honorificSuffix;
	}

	public ScimName() {
		givenName = "";
		familyName = "";
		middleName = "";
		honorificPrefix = "";
		honorificSuffix = "";
	}

}
