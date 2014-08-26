/**
 * 
 */
package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.xdi.model.SimpleCustomProperty;

/**
 * @author "Oleksiy Tataryn"
 *
 */
@JsonPropertyOrder({ "customAuthenticationScript", "enabled", "type", "priority", "customAuthenticationAttributes" })
public @Data class RegistrationInterceptorScript implements Comparable<RegistrationInterceptorScript>{
	
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
}
