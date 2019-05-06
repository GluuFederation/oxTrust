/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.oxchooser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@XmlRootElement(name = "nitialID")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "userID", "passWord" })
@XmlType(propOrder = { "userID", "passWord" })
public class InitialID {

	private String userID;
	private String passWord;

	public InitialID() {
		this.userID = "";
		this.passWord = "";
	}

	public String getUserID() {
		return this.userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getPassWord() {
		return this.passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

}
