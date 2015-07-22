/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for testing SCIM server
 * 
 * @author Yuriy Movchan Date: 03.28.2012
 */
@Scope(ScopeType.CONVERSATION)
@Name("testScimOperationAction")
@Restrict("#{identity.loggedIn}")
public class TestScimOperationAction implements Serializable {

	private static final long serialVersionUID = -5691758783531799744L;

	@Logger
	private Log log;

	private boolean isInitialized;

	private String userName, password, baseUrl;
	private String responseResult;

	private boolean isUseBaseAuth;

	@Restrict("#{s:hasPermission('scim_test', 'access')}")
	public String init() {
		if (this.isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.isInitialized = true;
		this.isUseBaseAuth = true;
		this.userName = "";
		this.password = "";
		this.baseUrl = "http://localhost:9090/oxTrust";

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scim_test', 'access')}")
	public String execute() {
		this.responseResult = getScimListUsers(this.userName, this.password, this.baseUrl, MediaType.TEXT_XML_TYPE);

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scim_test', 'access')}")
	public void cancel() {
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getResponseResult() {
		return responseResult;
	}

	private HttpClient createFormBasedHttpClient(String userId, String password, String base) {
		HttpClient httpClient = new HttpClient();

		httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

		GetMethod loginGetMethod = new GetMethod(base + "/login.htm");
		try {
			httpClient.executeMethod(loginGetMethod);
		} catch (Exception ex) {
			log.error("Failed to load login form", ex);
		} finally {
			loginGetMethod.releaseConnection();
		}

		if (loginGetMethod.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
			log.error("Failed to load login form");
			return null;
		}

		PostMethod logingPostMethod = new PostMethod(base + "/login.htm");

		// Prepare login parameters
		NameValuePair viewState = new NameValuePair("javax.faces.ViewState", "j_id1");
		NameValuePair action = new NameValuePair("loginForm:submit", "Login");
		NameValuePair form = new NameValuePair("loginForm", "loginForm");
		NameValuePair userid = new NameValuePair("loginForm:username", userId);
		NameValuePair userPassword = new NameValuePair("loginForm:password", password);
		logingPostMethod.setRequestBody(new NameValuePair[] { viewState, action, form, userid, userPassword });

		try {
			httpClient.executeMethod(logingPostMethod);
		} catch (Exception ex) {
			log.error("Failed to login", ex);
		} finally {
			logingPostMethod.releaseConnection();
		}

		if (logingPostMethod.getStatusLine().getStatusCode() != 302) {
			log.error("Failed to login");
			return null;
		}

		return httpClient;
	}

	private HttpClient createBasicHttpClient(String userId, String password, String base) {
		Credentials credentials = new UsernamePasswordCredentials(userId, password);

		HttpClient httpClient = new HttpClient();
		httpClient.getState().setCredentials(AuthScope.ANY, credentials);
		httpClient.getParams().setAuthenticationPreemptive(true);

		return httpClient;
	}

	private String getScimListUsers(String userId, String password, String base, MediaType returnMediaType) {
		String result = "Error";

		HttpClient httpClient;
		if (isUseBaseAuth) {
			httpClient = createBasicHttpClient(userId, password, base);
		} else {
			httpClient = createFormBasedHttpClient(userId, password, base);
		}
		if (httpClient == null) {
			return result;
		}

		ClientExecutor clientExecutor = new ApacheHttpClientExecutor(httpClient);
		ClientRequest request = clientExecutor.createRequest(base + "/Users/");
		// request.accept(returnMediaType);
		try {

			ClientResponse<String> response = request.get(String.class);
			result = response.getEntity();
			log.debug("Get reponse:\n {0}", result);

			response.releaseConnection();
		} catch (Exception ex) {
			log.error("Failed to get list of users", ex);
		}

		return result;
	}

}
