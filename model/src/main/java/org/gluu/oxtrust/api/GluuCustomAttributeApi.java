package org.gluu.oxtrust.api;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gluuperson")
public class GluuCustomAttributeApi {

	private String name;
	private String[] values;

	public GluuCustomAttributeApi(String name, String value) {
		this.name = name;
		setValue(value);
	}

	public String getValue() {
		if (this.values == null) {
			return null;
		}

		if (this.values.length > 0) {
			return this.values[0];
		}

		return null;
	}

	public void setValue(String value) {
		if (this.values == null) {
			this.values = new String[0];
		}

		if (this.values.length != 1) {
			this.values = new String[1];
		}
		this.values[0] = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "GluuCustomAttributeApi [name=" + name + ", values=" + Arrays.toString(values) + "]";
	}
}
