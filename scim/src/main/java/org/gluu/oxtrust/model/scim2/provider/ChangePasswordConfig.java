package org.gluu.oxtrust.model.scim2.provider;

/**
 * A complex type that specifies Change Password configuration options.
 */
public class ChangePasswordConfig {
	private final boolean supported;

	/**
	 * Create a <code>ChangePasswordConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether the Change Password operation is supported.
	 */
	public ChangePasswordConfig(final boolean supported) {
		this.supported = supported;
	}

	/**
	 * Indicates whether the Change Password operation is supported.
	 * 
	 * @return {@code true} if the Change Password operation is supported.
	 */
	public boolean isSupported() {
		return supported;
	}

}
