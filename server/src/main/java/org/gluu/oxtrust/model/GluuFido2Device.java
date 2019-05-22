package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.BaseEntry;

@SuppressWarnings("serial")
@DataEntry
@ObjectClass(values = { "top", "oxFido2RegistrationEntry" })
public class GluuFido2Device extends BaseEntry implements Serializable {

	@AttributeName(ignoreDuringUpdate = true, name = "oxId")
	private String id;

	@AttributeName(name = "oxCodeChallenge")
	private String challange;

	@AttributeName(name = "oxCodeChallengeHash")
	private String challangeHash;

	@AttributeName(name = "creationDate")
	private Date creationDate;

	@AttributeName(name = "oxSessionStateId")
	private String sessionId;

	@AttributeName(name = "personInum")
	private String userInum;

	public GluuFido2Device() {
	}

	public GluuFido2Device(String dn) {
		super(dn);
	}

	public GluuFido2Device(String dn, String id, Date creationDate, String sessionId, String userInum,
			String challange) {
		super(dn);
		this.id = id;
		this.creationDate = creationDate;
		this.sessionId = sessionId;
		this.userInum = userInum;
		this.challange = challange;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChallange() {
		return challange;
	}

	public void setChallange(String challange) {
		this.challange = challange;
	}

	public String getChallangeHash() {
		return challangeHash;
	}

	public void setChallangeHash(String challangeHash) {
		this.challangeHash = challangeHash;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserInum() {
		return userInum;
	}

	public void setUserInum(String userInum) {
		this.userInum = userInum;
	}

}
