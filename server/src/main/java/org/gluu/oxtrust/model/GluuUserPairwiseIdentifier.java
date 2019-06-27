package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.gluu.persist.model.base.BaseEntry;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry(sortBy = { "oxId" })
@ObjectClass(value = "pairwiseIdentifier")
public class GluuUserPairwiseIdentifier extends BaseEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -449401585533639948L;

	@AttributeName(ignoreDuringUpdate = true)
	private String oxId;
	@AttributeName(name = "oxAuthClientId")
	private String clientId;
	@AttributeName(name = "oxSectorIdentifier")
	private String sp;

	public String getOxId() {
		return oxId;
	}

	public void setOxId(String oxId) {
		this.oxId = oxId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSp() {
		return sp;
	}

	public void setSp(String sp) {
		this.sp = sp;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getOxId() != null && obj instanceof GluuUserPairwiseIdentifier) {
			result = getOxId().equals(((GluuUserPairwiseIdentifier) obj).getOxId());
		}
		return result;
	}
}
