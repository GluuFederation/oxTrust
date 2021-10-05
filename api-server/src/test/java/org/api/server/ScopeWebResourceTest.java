package org.api.server;

import java.io.IOException;
import java.util.ArrayList;

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
import org.gluu.model.GluuAttribute;
import org.gluu.oxauth.model.common.ScopeType;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;
import org.oxauth.persistence.model.Scope;

public class ScopeWebResourceTest extends BaseApiTest {

	@Test
	public void getAllScopesTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			Scope[] users = mapper.readValue(content, Scope[].class);
			Assert.assertTrue(users.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getScopeByInumTest() {
		String inum = "F0C4";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			Scope scope = mapper.readValue(content, Scope.class);
			Assert.assertNotNull(scope);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getScopeClaimsTest() {
		String inum = "C17A";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES + "/" + inum + ApiConstants.CLAIMS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			GluuAttribute[] claims = mapper.readValue(EntityUtils.toString(entity), GluuAttribute[].class);
			Assert.assertNotNull(claims);
			Assert.assertTrue(claims.length > 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchScopeTest() {
		String searchPattern = "openid";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			Scope[] scopes = mapper.readValue(content, Scope[].class);
			Assert.assertTrue(scopes.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createScopeTest() {
		String name = "ApiScope";
		Scope scope = getScope(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES);

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
			Assert.assertEquals(myScope.getScopeType(), ScopeType.OPENID);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateScopeTest() {
		String name = "ApiScopeUpdate";
		Scope scope = getScope(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES);
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
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES);
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
	public void deleteScopeTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteAllScopesTest() {
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	private Scope getScope(String name) {
		Scope scope = new Scope();
		scope.setDescription(name + " description");
		scope.setDisplayName(name);
		scope.setOxAuthClaims(new ArrayList<>());
		scope.setScopeType(ScopeType.OPENID);
		scope.setDynamicScopeScripts(new ArrayList<>());
		scope.setDefaultScope(Boolean.FALSE);
		return scope;
	}

}
