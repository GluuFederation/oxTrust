/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import java.io.Serializable;

/**
 * A complex type that specifies SORT configuration options.
 */
public class SortConfig implements Serializable {

	private final boolean supported;

	/**
	 * Create a <code>SortConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether sorting is supported.
	 */
	public SortConfig(final boolean supported) {
		this.supported = supported;
	}

	/**
	 * Indicates whether sorting is supported.
	 * 
	 * @return {@code true} if sorting is supported.
	 */
	public boolean isSupported() {
		return supported;
	}
}
