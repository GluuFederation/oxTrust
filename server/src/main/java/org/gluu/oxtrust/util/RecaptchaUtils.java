/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.ServletContexts;
import org.xdi.util.StringHelper;

/**
 * User: Dejan Maric
 */
public class RecaptchaUtils {
	private final static Log log = Logging.getLog(RecaptchaUtils.class);

	public static boolean verifyGoogleRecaptchaFromServletContext(String secretKey) {
		HttpServletRequest httpServletRequest = ServletContexts.instance().getRequest();

		String gRecaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");
		if (StringHelper.isNotEmpty(gRecaptchaResponse)) {
			return verifyGoogleRecaptcha(gRecaptchaResponse, secretKey);
		}

		return false;
	}

	public static boolean verifyGoogleRecaptcha(String gRecaptchaResponse, String secretKey) {
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
