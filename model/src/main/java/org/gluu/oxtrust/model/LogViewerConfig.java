/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.SimpleExtendedCustomProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Log viewer configuration model
 * 
 * @author Yuriy Movchan Date: 07/08/2013
 */

@XmlRootElement
@JsonPropertyOrder({ "logs" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogViewerConfig {

	@JsonProperty("log_template")
	private List<SimpleExtendedCustomProperty> logTemplates;

	public LogViewerConfig() {
		this.logTemplates = new ArrayList<SimpleExtendedCustomProperty>();
	}

	public List<SimpleExtendedCustomProperty> getLogTemplates() {
		return logTemplates;
	}

	public void setLogTemplates(List<SimpleExtendedCustomProperty> logTemplates) {
		this.logTemplates = logTemplates;
	}

}
