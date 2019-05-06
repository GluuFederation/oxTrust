/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.cert;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Trust Store configuration
 * 
 * @author Yuriy Movchan Date: 03/04/2014
 */
public class TrustStoreCertificate implements Serializable {

	private static final long serialVersionUID = 1332826184937052508L;

	private String name;
	private String certificate;
	private String version;

	@JsonProperty("added_by")
	private String addedBy;

	@JsonProperty("added_at")
	private Date addedAt;

	@JsonProperty("modified_by")
	private String modifiedBy;

	@JsonProperty("modified_at")
	private Date modifiedAt;

	private boolean enabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addedAt == null) ? 0 : addedAt.hashCode());
		result = prime * result + ((addedBy == null) ? 0 : addedBy.hashCode());
		result = prime * result + ((certificate == null) ? 0 : certificate.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((modifiedAt == null) ? 0 : modifiedAt.hashCode());
		result = prime * result + ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrustStoreCertificate other = (TrustStoreCertificate) obj;
		if (addedAt == null) {
			if (other.addedAt != null)
				return false;
		} else if (!addedAt.equals(other.addedAt))
			return false;
		if (addedBy == null) {
			if (other.addedBy != null)
				return false;
		} else if (!addedBy.equals(other.addedBy))
			return false;
		if (certificate == null) {
			if (other.certificate != null)
				return false;
		} else if (!certificate.equals(other.certificate))
			return false;
		if (enabled != other.enabled)
			return false;
		if (modifiedAt == null) {
			if (other.modifiedAt != null)
				return false;
		} else if (!modifiedAt.equals(other.modifiedAt))
			return false;
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrustStoreCertificate [name=").append(name).append(", certificate=").append(certificate).append(", version=")
				.append(version).append(", addedBy=").append(addedBy).append(", addedAt=").append(addedAt).append(", modifiedBy=")
				.append(modifiedBy).append(", modifiedAt=").append(modifiedAt).append(", enabled=").append(enabled).append("]");
		return builder.toString();
	}

}
