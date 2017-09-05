/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleExtendedCustomProperty;

/**
 * Log viewer configuration model
 * 
 * @author Yuriy Movchan Date: 07/08/2013
 */

@XmlRootElement
@JsonPropertyOrder({ "logs" })
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
