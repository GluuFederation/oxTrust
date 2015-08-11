package org.gluu.oxtrust.model.scim2.provider;

/**
 * A complex type that specifies SORT configuration options.
 */
public class SortConfig {
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
