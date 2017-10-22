/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;

/**
 * A complex type that specifies SORT configuration options.
 * Updated by jgomer on 2017-10-21
 */
public class SortConfig {

    @Attribute(description = "A Boolean value specifying whether or not sorting is supported.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.BOOLEAN)
	private boolean supported;

    public SortConfig(){ }

	/**
	 * Create a <code>SortConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether sorting is supported.
	 */
	public SortConfig(boolean supported) {
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

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

}
