/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.xdi.util.StringHelper;

/**
 * User: Dejan Maric
 */
@Stateless
@Named
public class RecaptchaUtil {
	
	@Inject
	private Logger log;
	
    @Inject
    private ExternalContext externalContext;

	public boolean verifyGoogleRecaptchaFromServletContext(String secretKey) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
		String gRecaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");
		if (StringHelper.isNotEmpty(gRecaptchaResponse)) {
			return verifyGoogleRecaptcha(gRecaptchaResponse, secretKey);
		}

		return false;
	}

	public boolean verifyGoogleRecaptcha(String gRecaptchaResponse, String secretKey) {
		boolean result = false;
		try {
			ClientRequest request = new ClientRequest("https://www.google.com/recaptcha/api/siteverify");
			request.formParameter("secret", secretKey);
			request.formParameter("response", gRecaptchaResponse);
			request.accept("application/json");

			ClientResponse<String> response = request.post(String.class);

			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> map = mapper.readValue(new ByteArrayInputStream(
					response.getEntity().getBytes()), new TypeReference<Map<String, String>>() {});

			return Boolean.parseBoolean(map.get("success"));
		} catch (Exception e) {
			log.error("Exception happened while verifying recaptcha ", e);
			return result;
		}
	}	

}
