/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.push;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Push application configuration for platforms
 * 
 * @author Yuriy Movchan Date: 02/03/2014
 */
@JsonPropertyOrder({ "name", "description", "platforms" })
public class PushApplicationConfiguration implements Serializable {

	private static final long serialVersionUID = 2208826784937052508L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("platforms")
	private ArrayList<HashMap<String, String>> platforms;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<HashMap<String, String>> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(ArrayList<HashMap<String, String>> platforms) {
		this.platforms = platforms;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushApplicationConfiguration [name=").append(name).append(", description=").append(description)
				.append(", platforms=").append(platforms).append("]");
		return builder.toString();
	}

}
