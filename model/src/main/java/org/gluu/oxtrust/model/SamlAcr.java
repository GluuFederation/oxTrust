package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@DataEntry
@ObjectClass(value = "samlAcr")
@JsonInclude(Include.NON_NULL)
public class SamlAcr extends Entry implements Serializable {

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
	@AttributeName
	private String parent;

	@AttributeName
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
