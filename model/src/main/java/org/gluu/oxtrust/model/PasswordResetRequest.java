/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;

import org.gluu.persist.model.base.Entry;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.Expiration;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry(sortBy = "creationDate")
@ObjectClass(value = "gluuPasswordResetRequest")
public class PasswordResetRequest extends Entry implements Serializable {

	private static final long serialVersionUID = -3360077330096416826L;
	@AttributeName
	private String oxGuid;
	@AttributeName
	private String personInum;
	@AttributeName
	private Date creationDate;

    @AttributeName(name = "exp")
    private Date expirationDate;

    @Expiration
    private Integer ttl;

    @AttributeName(name = "del")
    private boolean deletable = true;

	public String getOxGuid() {
		return oxGuid;
	}

	public void setOxGuid(String oxGuid) {
		this.oxGuid = oxGuid;
	}

	public String getPersonInum() {
		return personInum;
	}

	public void setPersonInum(String personInum) {
		this.personInum = personInum;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	@Override
	public String toString() {
		return "PasswordResetRequest [oxGuid=" + oxGuid + ", personInum=" + personInum + ", creationDate="
				+ creationDate + ", expirationDate=" + expirationDate + ", ttl=" + ttl + ", deletable=" + deletable
				+ "]";
	}

}
