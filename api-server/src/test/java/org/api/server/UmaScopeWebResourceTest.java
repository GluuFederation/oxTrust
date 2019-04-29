package org.api.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

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
import org.gluu.oxauth.model.uma.UmaScopeDescription;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;
import org.oxauth.persistence.model.Scope;

public class UmaScopeWebResourceTest extends BaseApiTest {

	@Test
	public void getUmaScopesTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaScopeDescription[] scopes = mapper.readValue(content, UmaScopeDescription[].class);
			Assert.assertTrue(scopes.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getUmaScopeByInumTest() {
		String inum = "8CAD-B06D";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaScopeDescription scope = mapper.readValue(content, UmaScopeDescription.class);
			Assert.assertNotNull(scope);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchUmaScopeTest() {
		String searchPattern = "SCIM";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA
				+ ApiConstants.SCOPES + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaScopeDescription[] scopes = mapper.readValue(content, UmaScopeDescription[].class);
			Assert.assertTrue(scopes.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createUmaScopeTest() {
		String name = "ApiUmaScope";
		Scope scope = getUmaScope(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES);

		try {
			String json = mapper.writeValueAsString(scope);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			Scope myScope = mapper.readValue(
					EntityUtils.toString(result), Scope.class);
			Assert.assertNotNull(myScope);
			Assert.assertEquals(myScope.getDisplayName(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateUmaScopeTest() {
		String name = "ApiUmaScopeUpdate";
		Scope scope = getUmaScope(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES);
		try {
			String json = mapper.writeValueAsString(scope);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			Scope myScope = mapper.readValue(EntityUtils.toString(result), Scope.class);
			Assert.assertEquals(myScope.getDisplayName(), name);

			myScope.setDisplayName(myScope.getDisplayName() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES);
			json = mapper.writeValueAsString(myScope);
			entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
			second.setEntity(entity);
			second.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			response = handle(second);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void deleteUmaScopeTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteAllUmaScopesTest() {
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	private Scope getUmaScope(String name) {
		Scope scope = new Scope();
		scope.setDescription(name + " description");
		scope.setDisplayName(name);
		scope.setIconUrl("https://api.gluu.org/icon/jl25");
		scope.setId(UUID.randomUUID().toString());
		scope.setUmaAuthorizationPolicies(new ArrayList<>());
		return scope;
	}

}
