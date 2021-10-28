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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.gluu.net.ProxyUtil;
import org.gluu.util.StringHelper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
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

			ResteasyClient resteasyClient;
			if (ProxyUtil.isProxyRequied()) {
				URLConnectionEngine engine = new URLConnectionEngine();
				resteasyClient = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(engine).build();
			} else {
				resteasyClient = (ResteasyClient) ResteasyClientBuilder.newClient();
			}

			WebTarget webTarget = resteasyClient.target(uriTemplate);
	        Builder clientRequest = webTarget.request();
	        clientRequest.accept("application/json");

	        Form requestForm = new Form();
	        requestForm.param("secret", secretKey);
	        requestForm.param("response", gRecaptchaResponse);

			Response response = clientRequest.buildPost(Entity.form(requestForm)).invoke();
			try {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> map = mapper.readValue(new ByteArrayInputStream(response.readEntity(String.class).getBytes()),
						new TypeReference<Map<String, String>>() {
						});

				return Boolean.parseBoolean(map.get("success"));
			} finally {
				response.close();

				if (resteasyClient.httpEngine() != null) {
					resteasyClient.httpEngine().close();
				}
			}
		} catch (Exception e) {
			log.warn("Exception happened while verifying recaptcha, check your internet connection", e);
			return result;
		}
	}

}
