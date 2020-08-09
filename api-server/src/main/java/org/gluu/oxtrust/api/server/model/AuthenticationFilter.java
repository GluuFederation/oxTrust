package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "filter", "bind", "bindPasswordAttribute", "baseDn" })
public class AuthenticationFilter implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6098795373123352276L;
	@JsonProperty("filter")
	private String filter;
	@JsonProperty("bind")
	private Boolean bind;
	@JsonProperty("bindPasswordAttribute")
	private String bindPasswordAttribute;
	@JsonProperty("baseDn")
	private String baseDn;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("filter")
	public String getFilter() {
		return filter;
	}

	@JsonProperty("filter")
	public void setFilter(String filter) {
		this.filter = filter;
	}

	@JsonProperty("bind")
	public Boolean getBind() {
		return bind;
	}

	@JsonProperty("bind")
	public void setBind(Boolean bind) {
		this.bind = bind;
	}

	@JsonProperty("bindPasswordAttribute")
	public String getBindPasswordAttribute() {
		return bindPasswordAttribute;
	}

	@JsonProperty("bindPasswordAttribute")
	public void setBindPasswordAttribute(String bindPasswordAttribute) {
		this.bindPasswordAttribute = bindPasswordAttribute;
	}

	@JsonProperty("baseDn")
	public String getBaseDn() {
		return baseDn;
	}

	@JsonProperty("baseDn")
	public void setBaseDn(String baseDn) {
		this.baseDn = baseDn;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
