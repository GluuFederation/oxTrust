package org.api.server;

import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.gluu.model.passport.Provider;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class PassportProviderWebResourceTest extends BaseApiTest {

	@Test
	public void listProviderTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			Provider[] goups = mapper.readValue(content, Provider[].class);
			Assert.assertTrue(goups.length >= 0);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getPassportProviderByIdTest() {
		String inum = "UNKNOWPROVIDER";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void createProviderTest() {
		String name = "ApiLinkedin";
		Provider provider = getProvider(name);
		HttpPost request = new HttpPost(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS);

		try {
			HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsString(provider).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			Provider myProvider = mapper.readValue(EntityUtils.toString(result), Provider.class);
			Assert.assertEquals(myProvider.getDisplayName(), name);
			Assert.assertEquals(myProvider.isEnabled(), false);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateProviderTest() {
		String name = "ApiLinkedInUpdated";
		Provider provider = getProvider(name);
		HttpPost request = new HttpPost(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS);
		try {
			HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsString(provider).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			Provider myGroup = mapper.readValue(EntityUtils.toString(result), Provider.class);
			Assert.assertEquals(myGroup.getDisplayName(), name);

			myGroup.setDisplayName(myGroup.getDisplayName() + " Updated");
			HttpPut second = new HttpPut(
					BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS);
			entity = new ByteArrayEntity(mapper.writeValueAsString(myGroup).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			second.setEntity(entity);
			second.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			response = handle(second);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	private Provider getProvider(String name) {
		Provider provider = new Provider();
		provider.setEmailLinkingSafe(true);
		provider.setEnabled(false);
		provider.setOptions(new HashMap<>());
		provider.setDisplayName(name);
		provider.setPassportStrategyId("passport-saml");
		provider.setType("saml");
		provider.setMapping("saml_ldap_profile");
		provider.setRequestForEmail(true);
		return provider;
	}

	@Test
	public void deleteAllProvidersTest() {
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.PROVIDERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

}
