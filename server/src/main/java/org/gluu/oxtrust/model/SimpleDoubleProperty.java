/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

/**
 * Simple property to hold value
 * 
 * @author Yuriy Movchan Date: 08.02.2011
 */
public class SimpleDoubleProperty implements Serializable {

	private static final long serialVersionUID = -1451889014702205980L;

	private String value1;
	private String value2;

	public SimpleDoubleProperty(String value1, String value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public final String getValue1() {
		return value1;
	}

	public final void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
		result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleDoubleProperty other = (SimpleDoubleProperty) obj;
		if (value1 == null) {
			if (other.value1 != null)
				return false;
		} else if (!value1.equals(other.value1))
			return false;
		if (value2 == null) {
			if (other.value2 != null)
				return false;
		} else if (!value2.equals(other.value2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleDoubleProperty [value1=" + value1 + ", value2=" + value2 + "]";
	}

}
