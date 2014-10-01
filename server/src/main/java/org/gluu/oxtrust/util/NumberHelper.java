/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

/**
 * @author: Yuriy Movchan Date: 12.06.2010
 */
public final class NumberHelper {

	private NumberHelper() {
	}

	public static double round(double value, int fractionDigits) {
		int multiplicator = (int) Math.pow(10, fractionDigits);
		return (double) Math.round(value * multiplicator) / multiplicator;
	}

}
