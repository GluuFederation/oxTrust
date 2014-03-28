package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person Addresses attributes
 * 
 * @author Reda Zerrad Date: 04.17.2012
 */

@JsonPropertyOrder({ "type", "streetAddress", "locality", "region", "postalCode", "country", "formatted", "primary" })
@XmlType(propOrder = { "type", "streetAddress", "locality", "region", "postalCode", "country", "formatted", "primary" })
public class ScimPersonAddresses {
	private String formatted;
	private String type;
	private String streetAddress; // street
	private String locality;// l
	private String region;// st
	private String postalCode;// postalCode
	private String country; // c
	private String primary;

	public ScimPersonAddresses() {
		formatted = "";
		type = "";
		streetAddress = "";
		locality = "";
		region = "";
		postalCode = "";
		country = "";
	}

	public String getFormatted() {
		return this.formatted;
	}

	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPrimary() {
		return this.primary;
	}

	public void setPrimary(String primary) {
		this.primary = primary;
	}
}
