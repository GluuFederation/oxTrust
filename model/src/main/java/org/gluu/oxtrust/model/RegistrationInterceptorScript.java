/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.xdi.model.SimpleCustomProperty;

/**
 * @author "Oleksiy Tataryn"
 *
 */
@JsonPropertyOrder({ "customAuthenticationScript", "enabled", "type", "priority", "customAuthenticationAttributes" })
public class RegistrationInterceptorScript implements Comparable<RegistrationInterceptorScript>{
	
	public RegistrationInterceptorScript(){
		this.customAttributes = new ArrayList<SimpleCustomProperty>();
	}
	
	@JsonProperty
	private boolean enabled;
	
	@JsonProperty
	private String customScript;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String priority;
	
	@JsonProperty
	private List<SimpleCustomProperty> customAttributes;

	@Override
	public int compareTo(RegistrationInterceptorScript o) {
		return Integer.parseInt(priority) - Integer.parseInt(o.getPriority());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getCustomScript() {
		return customScript;
	}

	public void setCustomScript(String customScript) {
		this.customScript = customScript;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public List<SimpleCustomProperty> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<SimpleCustomProperty> customAttributes) {
		this.customAttributes = customAttributes;
	}

	@Override
	public String toString() {
		return String
				.format("RegistrationInterceptorScript [enabled=%s, customScript=%s, type=%s, priority=%s, customAttributes=%s]",
						enabled, customScript, type, priority, customAttributes);
	}

}
