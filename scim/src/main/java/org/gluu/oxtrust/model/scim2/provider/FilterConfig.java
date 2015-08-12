package org.gluu.oxtrust.model.scim2.provider;


/**
 * A complex type that specifies FILTER configuration options.
 */
public class FilterConfig {
	private final boolean supported;
	private final long maxResults;

	/**
	 * Create a <code>FilterConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether the FILTER operation is supported.
	 * @param maxResults
	 *            Specifies the maximum number of resources returned in a
	 *            response.
	 */
	public FilterConfig(final boolean supported, final long maxResults) {
		this.supported = supported;
		this.maxResults = maxResults;
	}

	/**
	 * Indicates whether the FILTER operation is supported.
	 * 
	 * @return {@code true} if the FILTER operation is supported.
	 */
	public boolean isSupported() {
		return supported;
	}

}
