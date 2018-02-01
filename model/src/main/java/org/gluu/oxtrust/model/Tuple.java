/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

/**
 * 
 */
package org.gluu.oxtrust.model;

/**
 * @author "Oleksiy Tataryn"
 *
 */
public class Tuple<A, B> {

	private A value0;
	private B value1;

	public A getValue0() {
		return value0;
	}

	public void setValue0(A value0) {
		this.value0 = value0;
	}

	public B getValue1() {
		return value1;
	}

	public void setValue1(B value1) {
		this.value1 = value1;
	}

	@Override
	public String toString() {
		return String.format("Tuple [value0=%s, value1=%s]", value0, value1);
	}

}
