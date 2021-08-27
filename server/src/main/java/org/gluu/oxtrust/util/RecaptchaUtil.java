/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.gluu.net.ProxyUtil;
import org.gluu.util.StringHelper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.URLConnectionClientExecutor;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public boolean verify(String response, String secretKey) {
		if (StringHelper.isNotEmpty(response)) {
			return verifyGoogleRecaptcha(response, secretKey);
		}
		return false;
	}

	public boolean isCaptchaValid(String secretKey, String response) {
		try {
			String url = "https://www.google.com/recaptcha/api/siteverify?" + "secret=" + secretKey + "&response="
					+ response;
			InputStream res = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(res, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			String jsonText = sb.toString();
			res.close();
			JSONObject json = new JSONObject(jsonText);
			return json.getBoolean("success");
		} catch (Exception e) {
			return false;
		}
	}

	public boolean verifyGoogleRecaptcha(String gRecaptchaResponse, String secretKey) {
		boolean result = false;
		try {
			String uriTemplate = "https://www.google.com/recaptcha/api/siteverify";
			ClientRequest request;
			if (ProxyUtil.isProxyRequied()) {
				URLConnectionClientExecutor executor = new URLConnectionClientExecutor();
				request = new ClientRequest(uriTemplate, executor);
			} else {
				request = new ClientRequest(uriTemplate);
			}
			request.formParameter("secret", secretKey);
			request.formParameter("response", gRecaptchaResponse);
			request.accept("application/json");

			ClientResponse<String> response = request.post(String.class);

			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> map = mapper.readValue(new ByteArrayInputStream(response.getEntity().getBytes()),
					new TypeReference<Map<String, String>>() {
					});

			return Boolean.parseBoolean(map.get("success"));
		} catch (Exception e) {
			log.warn("Exception happened while verifying recaptcha, check your internet connection", e);
			return result;
		}
	}

}
