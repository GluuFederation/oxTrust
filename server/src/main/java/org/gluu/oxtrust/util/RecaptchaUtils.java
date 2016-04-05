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
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.web.ServletContexts;

/**
 * User: Dejan Maric
 */
public class RecaptchaUtils {

	private static final String PUBLIC_KEY = "6Ld9oM0SAAAAAHnAjeCniZz6FJ3REl5ImpvKcRqU";
	private static final String PRIVATE_KEY = "6Ld9oM0SAAAAAFBuuYMTXGr-Y3OSAFsbUmIZ0-lE";
	
	public static final String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";
    
    public static final String SITE_KEY = "6LcYpRsTAAAAAFqRBoHmjJEE72-0Ey-GbZV-6vqC";
    public static final String SECRET_KEY = "6LcYpRsTAAAAAEnaFe4KFkwQ6GzyZqEZ2fcs4SNq";
	
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
	
	public static boolean getGoogleRecaptchaFromServletContext() {
		HttpServletRequest httpServletRequest = ServletContexts.instance().getRequest();
		String gRecaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");
		if((gRecaptchaResponse != null) && !(gRecaptchaResponse.trim().equals("")))
			return verifyGoogleRecaptcha(gRecaptchaResponse);
		
		return false;
	}

	public static boolean verifyGoogleRecaptcha(String gRecaptchaResponse) {
		boolean result = false;
		try {
			ClientRequest request = new ClientRequest(CAPTCHA_URL);
			request.formParameter("secret", SECRET_KEY);
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
			e.printStackTrace();
			return result;
		}
	}
	
	
	
}
