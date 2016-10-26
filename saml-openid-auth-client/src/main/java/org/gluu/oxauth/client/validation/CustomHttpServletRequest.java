/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxauth.client.validation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class CustomHttpServletRequest extends HttpServletRequestWrapper {

	private Map<String, String> customParameters = new HashMap<String, String>();

	public CustomHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	public void addCustomParameter(String name, String value) {
		customParameters.put(name,	value);
	}

	@Override
	public String getParameter(String name) {

		String originalParameter = super.getParameter(name);

		if(originalParameter != null) {
			return originalParameter;
		} else {
			return customParameters.get(name);
		}
	}
}
