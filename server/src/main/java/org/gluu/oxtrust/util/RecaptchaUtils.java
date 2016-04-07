/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.ServletContexts;

/**
 * User: Dejan Maric
 */
public class RecaptchaUtils {
	private final static Log log = Logging.getLog(RecaptchaUtils.class);

	private static final String PUBLIC_KEY = "6Ld9oM0SAAAAAHnAjeCniZz6FJ3REl5ImpvKcRqU";
	private static final String PRIVATE_KEY = "6Ld9oM0SAAAAAFBuuYMTXGr-Y3OSAFsbUmIZ0-lE";
	
	private static ReCaptcha reCaptcha = ReCaptchaFactory.newReCaptcha(PUBLIC_KEY, PRIVATE_KEY, false);

	public static String createRecaptchaHtml(String error) {
		Properties options = new Properties();
		options.setProperty("theme", "white");
		options.setProperty("tabindex", "8");

		// todo: is there a better solutions to set https url?
		((ReCaptchaImpl) reCaptcha).setRecaptchaServer("https://www.google.com/recaptcha/api");
		String html = reCaptcha.createRecaptchaHtml(error, options);
		return html;
	}

	public static ReCaptchaResponse getRecaptchaResponse(String challenge, String response, String remoteAddress) {
		ReCaptchaResponse answer = reCaptcha.checkAnswer(remoteAddress, challenge, response);
		return answer;
	}

	public static ReCaptchaResponse getRecaptchaResponseFromServletContext() {
		HttpServletRequest request = ServletContexts.instance().getRequest();
		return getRecaptchaResponse(request.getParameter("recaptcha_challenge_field"), request.getParameter("recaptcha_response_field"),
				request.getRemoteAddr());
	}
	
	public boolean getGoogleRecaptchaFromServletContext(String url , String secretKey) {
		HttpServletRequest httpServletRequest = ServletContexts.instance().getRequest();
		String gRecaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");
		if((gRecaptchaResponse != null) && !(gRecaptchaResponse.trim().equals("")))
			return verifyGoogleRecaptcha(gRecaptchaResponse,url , secretKey);
		
		return false;
	}

	public boolean verifyGoogleRecaptcha(String gRecaptchaResponse,String url , String secretKey) {
		boolean result = false;
		try {
			ClientRequest request = new ClientRequest(url);
			request.formParameter("secret", secretKey);
			request.formParameter("response", gRecaptchaResponse);
			request.accept("application/json");

			ClientResponse<String> response = request.post(String.class);

			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> map = mapper.readValue(new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(response
							.getEntity().getBytes()))),
					new TypeReference<Map<String, String>>() {
					});
			return Boolean.parseBoolean(map.get("success"));
		} catch (Exception e) {
			log.error("Exception happened while verifying recaptcha ", e);
			return result;
		}
	}
	
	
	
}
