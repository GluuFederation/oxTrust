package org.gluu.site.rest.test;

// Consumer Key: gluu 
// Consumer Secret: 4zqhyP93hsg
// Public API methods: http://api.poken.com
// Non-Published API methods: http://api.poken.com/internal/index.html

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class PokenTest {
	public static String apibaseURL = "http://api.poken.com/rest081";
	private static String basicAuth = "?applicationId=gluu&applicationSecret=4zqhyP93hsg";

	public static void main(String[] args) {
		testVerifyCredentials("mike@gluu.org", "password123");
		testVerifyProfile("mike@gluu.org", "password123");
	}

	private static void testVerifyCredentials(String userId, String password) {
		// http://api.poken.com/rest081/account/credentials/verify
		String path = "/account/credentials/verify";
		PostMethod post = new PostMethod(apibaseURL + path + basicAuth);
		NameValuePair[] data = { new NameValuePair("aliasOrEmail", userId), new NameValuePair("password", password) };
		post.setRequestBody(data);
		post.setRequestHeader("Accept", "application/xml");
		post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		try {
			HttpClient client = new HttpClient();
			int status = client.executeMethod(post);
			System.out.println("Status: " + new Integer(status).toString());
			System.out.println(post.getResponseBodyAsString());
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
	}

	private static void testVerifyProfile(String userId, String password) {
		// http://api.poken.com/rest081/account/profile
		HttpClient client = createHttpClientWithBasicAuth(userId, password);
		String path = "/account/profile";
		GetMethod get = new GetMethod(apibaseURL + path + basicAuth);
		get.setRequestHeader("Accept", "application/xml");
		try {
			client.executeMethod(get);
			int status = client.executeMethod(get);
			System.out.println("Status: " + new Integer(status).toString());
			System.out.println(get.getResponseBodyAsString());
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static HttpClient createHttpClientWithBasicAuth(String userid, String password) {
		Credentials credentials = new UsernamePasswordCredentials(userid, password);
		HttpClient httpClient = new HttpClient();
		httpClient.getState().setCredentials(AuthScope.ANY, credentials);
		httpClient.getParams().setAuthenticationPreemptive(true);
		return httpClient;
	}

}
