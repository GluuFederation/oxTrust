package org.gluu.oxtrust.model.scim2.provider;


/**
 * A complex type that specifies ETag configuration options.
 */
public class ETagConfig {
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
