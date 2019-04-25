package org.api.server;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.gluu.oxtrust.api.server.model.AuthenticationMethod;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class AuthenticationMethodWebResourceTest extends BaseApiTest {

	@Test
	public void getCurrentAuthenticationTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ACRS);
		try {
			HttpResponse response = handle(request);
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			AuthenticationMethod method = mapper.readValue(EntityUtils.toString(result), AuthenticationMethod.class);
			Assert.assertNotNull(method.getDefaultAcr());
			Assert.assertNotNull(method.getOxtrustAcr());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateAuthenticationMethodTest() {
		HttpPut request = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ACRS);
		AuthenticationMethod method = new AuthenticationMethod();
		String defaultAcr = "auth_ldap_server";
		method.setDefaultAcr(defaultAcr);
		method.setOxtrustAcr(defaultAcr);
		try {
			String json = mapper.writeValueAsString(method);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
			request.setEntity(entity);
			request.setHeader("Content-Type", MediaType.APPLICATION_JSON);
			HttpResponse response = handle(request);
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			AuthenticationMethod value = mapper.readValue(EntityUtils.toString(result), AuthenticationMethod.class);
			Assert.assertEquals(value.getOxtrustAcr(), defaultAcr);
			Assert.assertEquals(value.getDefaultAcr(), defaultAcr);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
