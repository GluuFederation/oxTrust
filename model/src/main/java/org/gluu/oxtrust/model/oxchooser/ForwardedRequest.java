/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.oxchooser;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * oxChooser response
 * 
 * @author Reda Zerrad Date: 07.04.2012
 */
@XmlRootElement(name = "ForwardedRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "receivingURL", "parameterMap" })
@XmlType(propOrder = { "receivingURL", "parameterMap" })
public class ForwardedRequest {

	private String receivingURL;
	private Map<String, String[]> parameterMap;
	private String queryString;
	private StringBuffer requestURL;

	public ForwardedRequest() {
		this.receivingURL = "";
		this.queryString = "";
		this.requestURL = new StringBuffer();
		this.parameterMap = new HashMap<String, String[]>();
	}

	public String getReceivingURL() {
		return this.receivingURL;
	}

	public void setReceivingURL(String receivingURL) {
		this.receivingURL = receivingURL;
	}

	public Map<String, String[]> getParameterMap() {
		return this.parameterMap;
	}

	public void setParameterMap(Map<String, String[]> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getQueryString() {
		return this.queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public StringBuffer getRequestURL() {
		return this.requestURL;
	}

	public void setRequestURL(StringBuffer requestURL) {
		this.requestURL = requestURL;
	}
}
