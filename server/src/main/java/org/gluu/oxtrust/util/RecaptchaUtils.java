package org.gluu.oxtrust.util;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.jboss.seam.web.ServletContexts;

/**
 * User: Dejan Maric
 */
public class RecaptchaUtils {

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
}
