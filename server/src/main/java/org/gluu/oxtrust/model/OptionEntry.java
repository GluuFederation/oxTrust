package org.gluu.oxtrust.model;

import java.io.Serializable;

public class OptionEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3645757672075149629L;
	private String key;
	private String value;

	public OptionEntry(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSecret() {
		return this.getKey().equalsIgnoreCase("client_secret") ||  this.getKey().equalsIgnoreCase("clientSecret");
	}
}
