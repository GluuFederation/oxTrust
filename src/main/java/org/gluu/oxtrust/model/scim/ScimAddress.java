package org.gluu.oxtrust.model.scim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: Dejan Maric
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "address")
public class ScimAddress {

	private String streetAddress;
	private String city;
	private String state;
	private String postalCode;
	private String country;

	@XmlElement(name = "streetAddress")
	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	@XmlElement(name = "locality")
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@XmlElement(name = "region")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@XmlElement(name = "postalCode")
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	@XmlElement(name = "country")
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public ScimAddress() {
		streetAddress = "";
		city = "";
		state = "";
		postalCode = "";
		country = "";
	}
}
