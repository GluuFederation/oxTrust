package org.gluu.oxtrust.model.scim2.provider;

/**
 * A complex type that specifies BULK configuration options.
 */
public class BulkConfig {
	private final boolean supported;
	private final long maxOperations;
	private final long maxPayloadSize;

	/**
	 * Create a <code>BulkConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether the BULK operation is supported.
	 * @param maxOperations
	 *            Specifies the maximum number of operations.
	 * @param maxPayloadSize
	 *            Specifies the maximum payload size in bytes.
	 */
	public BulkConfig(final boolean supported, final long maxOperations,
			final long maxPayloadSize) {
		this.supported = supported;
		this.maxOperations = maxOperations;
		this.maxPayloadSize = maxPayloadSize;
	}

	/**
	 * Indicates whether the PATCH operation is supported.
	 * 
	 * @return {@code true} if the PATCH operation is supported.
	 */
	public boolean isSupported() {
		return supported;
	}

	/**
	 * Retrieves the maximum number of operations.
	 * 
	 * @return The maximum number of operations.
	 */
	public long getMaxOperations() {
		return maxOperations;
	}

	/**
	 * Retrieves the maximum payload size in bytes.
	 * 
	 * @return The maximum payload size in bytes.
	 */
	public long getMaxPayloadSize() {
		return maxPayloadSize;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final BulkConfig that = (BulkConfig) o;

		if (maxOperations != that.maxOperations) {
			return false;
		}
		if (maxPayloadSize != that.maxPayloadSize) {
			return false;
		}
		if (supported != that.supported) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = (supported ? 1 : 0);
		result = 31 * result + (int) (maxOperations ^ (maxOperations >>> 32));
		result = 31 * result + (int) (maxPayloadSize ^ (maxPayloadSize >>> 32));
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BulkConfig");
		sb.append("{supported=").append(supported);
		sb.append(", maxOperations=").append(maxOperations);
		sb.append(", maxPayloadSize=").append(maxPayloadSize);
		sb.append('}');
		return sb.toString();
	}
}
