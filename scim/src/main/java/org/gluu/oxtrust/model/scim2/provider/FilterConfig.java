/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;

/**
 * A complex type that specifies FILTER configuration options.
 * Updated by jgomer on 2017-10-21
 */
public class FilterConfig {

    @Attribute(description = "A Boolean value specifying whether or not the operation is supported",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.BOOLEAN)
	private boolean supported;

    @Attribute(description = "An integer value specifying the maximum number of resources returned in a response.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.INTEGER)
	private long maxResults;

    public FilterConfig(){ }

	/**
	 * Create a <code>FilterConfig</code> instance.
	 *
	 * @param supported
	 *            Specifies whether the FILTER operation is supported.
	 */
	public FilterConfig(boolean supported) {
		this.supported = supported;
	}

	/**
	 * Indicates whether the FILTER operation is supported.
	 * 
	 * @return {@code true} if the FILTER operation is supported.
	 */
	public boolean isSupported() {
		return supported;
	}

	public long getMaxResults() {
		return maxResults;
	}

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public void setMaxResults(long maxResults) {
        this.maxResults = maxResults;
    }

}
