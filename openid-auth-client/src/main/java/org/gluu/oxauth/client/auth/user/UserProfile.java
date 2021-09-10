/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.auth.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User profile retrieved from oxAuth after successful authentication
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public class UserProfile implements Serializable {

	private static final long serialVersionUID = 4570636703916315314L;

	private String id;

	private String usedAcr;

	private final Map<String, Object> attributes = new HashMap<String, Object>();

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getUsedAcr() {
		return usedAcr;
	}

	public void setUsedAcr(String usedAcr) {
		this.usedAcr = usedAcr;
	}

	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(this.attributes);
	}

	public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

	public void addAttribute(final String key, final Object value) {
		if (value != null) {
			this.attributes.put(key, value);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserProfile [id=").append(id).append(", attributes=").append(attributes).append("]");
		return builder.toString();
	}

}
