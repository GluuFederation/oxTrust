/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import java.io.Serializable;

/**
 * A complex type that specifies ETag configuration options.
 */
public class ETagConfig implements Serializable {

	private final boolean supported;

	/**
	 * Create a <code>ETagConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether the ETag resource versions are supported.
	 */
	public ETagConfig(final boolean supported) {
		this.supported = supported;
	}

	/**
	 * Indicates whether the ETag resource versions are supported.
	 * 
	 * @return {@code true} if ETag resource versions are supported.
	 */
	public boolean isSupported() {
		return supported;
	}

}
