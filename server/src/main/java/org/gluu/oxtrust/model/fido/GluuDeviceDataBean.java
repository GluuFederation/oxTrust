package org.gluu.oxtrust.model.fido;

import java.io.Serializable;

public class GluuDeviceDataBean implements Serializable {

	private String id;
	private String creationDate;
	private String nickName;
	private String modality;

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	public final String getCreationDate() {
		return creationDate;
	}

	public final void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public final String getNickName() {
		return nickName;
	}

	public final void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public final String getModality() {
		return modality;
	}

	public final void setModality(String modality) {
		this.modality = modality;
	}

}
