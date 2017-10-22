/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;

/**
 * A complex type that specifies BULK configuration options.
 * Updated by jgomer on 2017-10-21
 */
public class BulkConfig {

    @Attribute(description = "A Boolean value specifying whether or not the operation is supported.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.BOOLEAN)
	private boolean supported;

    @Attribute(description = "An integer value specifying the maximum number of operations.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.INTEGER)
	private long maxOperations;

    @Attribute(description = " An integer value specifying the maximum payload size in bytes",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.INTEGER)
	private long maxPayloadSize;

    public BulkConfig(){ }
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
	public BulkConfig(boolean supported, long maxOperations, long maxPayloadSize) {
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

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public void setMaxOperations(long maxOperations) {
        this.maxOperations = maxOperations;
    }

    public void setMaxPayloadSize(long maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
    }

}
