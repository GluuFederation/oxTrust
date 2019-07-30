package org.gluu.oxtrust.model;

import java.io.Serializable;

public class SamlAcr implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1357084900401963003L;

	public SamlAcr() {
	}

	public SamlAcr(String parent, String classRef) {
		super();
		this.parent = parent;
		this.classRef = classRef;
	}

	private String parent;

	private String classRef;

	public String getClassRef() {
		return classRef;
	}

	public void setClassRef(String classRef) {
		this.classRef = classRef;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

}
