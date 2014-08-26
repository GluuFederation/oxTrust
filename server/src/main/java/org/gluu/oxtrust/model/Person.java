package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement(name = "user")
public @Data class Person {

	private String inum;
	private String iname;
	private String firstName; // givenName
	private String lastName; // sn
	// private String commonName;
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
}
