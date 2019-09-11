/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class Person {

	private String inum;
	private String firstName; // givenName
	private String lastName; // sn
	private String displayName;
	private String commonName;
	private String userId;
	private String password;
	private String email;
	private String telephoneNumber;
	private String faxNumber;
	private String employeeNumber;
	private String address;
	private String city;
	private String state;
	private String postalCode;
	private String mobileNumber;

	List<PersonAttribute> personAttrList = new ArrayList<PersonAttribute>();

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public List<PersonAttribute> getPersonAttrList() {
		return personAttrList;
	}

	public void setPersonAttrList(List<PersonAttribute> personAttrList) {
		this.personAttrList = personAttrList;
	}

	@Override
	public String toString() {
		return String
				.format("Person [inum=%s,firstName=%s, lastName=%s, displayName=%s, commonName=%s, userId=%s, password=%s, email=%s, telephoneNumber=%s, faxNumber=%s, employeeNumber=%s, address=%s, city=%s, state=%s, postalCode=%s, mobileNumber=%s, personAttrList=%s]",
						inum,firstName, lastName, displayName,
						commonName, userId, password, email, telephoneNumber,
						faxNumber, employeeNumber, address, city, state,
						postalCode, mobileNumber, personAttrList);
	}

}
