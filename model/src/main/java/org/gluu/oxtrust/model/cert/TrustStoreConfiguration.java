/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.cert;

import java.io.Serializable;

/**
 * SSL configuration
 * 
 * @author Yuriy Movchan Date: 03/04/2014
 */
public class TrustStoreConfiguration implements Serializable {

	private static final long serialVersionUID = 1332826784937032508L;

	private boolean useJreCertificates;

	public boolean isUseJreCertificates() {
		return useJreCertificates;
	}

	public void setUseJreCertificates(boolean useJreCertificates) {
		this.useJreCertificates = useJreCertificates;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrustStoreConfiguration [useJreCertificates=").append(useJreCertificates).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (useJreCertificates ? 1231 : 1237);
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
		TrustStoreConfiguration other = (TrustStoreConfiguration) obj;
		if (useJreCertificates != other.useJreCertificates)
			return false;
		return true;
	}

}
