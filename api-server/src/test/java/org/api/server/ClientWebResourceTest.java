package org.api.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthClient;
import org.junit.Assert;
import org.junit.Test;
import org.oxauth.persistence.model.Scope;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientWebResourceTest extends BaseApiTest {
	private ObjectMapper mapper = new ObjectMapper();
	String CONTENT_TYPE = "Content-Type";

	@Test
	public void listClientsTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			OxAuthClient[] goups = mapper.readValue(content, OxAuthClient[].class);
			Assert.assertTrue(goups.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getClientByInumTest() {
		String inum = "800-b526-43a0-b5e5-e39c7a970386";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			OxAuthClient client = mapper.readValue(EntityUtils.toString(entity), OxAuthClient.class);
			Assert.assertNotNull(client);
			Assert.assertTrue(client.getDisplayName().contains("API"));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createClientTest() {
		String name = "OxTrustApiClient";
		OxAuthClient group = getClient(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS);

		try {
			String json = mapper.writeValueAsString(group);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			OxAuthClient myClient = mapper.readValue(EntityUtils.toString(result), OxAuthClient.class);
			Assert.assertEquals(myClient.getDisplayName(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateClientTest() {
		String name = "EditedApiClient";
		OxAuthClient group = getClient(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS);
		try {
			HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsString(group).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			OxAuthClient myClient = mapper.readValue(EntityUtils.toString(result), OxAuthClient.class);
			Assert.assertEquals(myClient.getDisplayName(), name);

			myClient.setDescription(myClient.getDescription() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS);
			entity = new ByteArrayEntity(mapper.writeValueAsString(myClient).toString().getBytes("UTF-8"),
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

	private OxAuthClient getClient(String name) {
		OxAuthClient client = new OxAuthClient();
		client.setDescription(name + " description");
		client.setDisplayName(name);
		client.setExp(new Date());
		client.setDeletable(true);
		client.setOxAuthScopes(new ArrayList<>());
		client.setRequestUris(new String[] {});
		client.setDisabled(false);
		client.setOxAuthAppType(OxAuthApplicationType.WEB);
		return client;
	}

	@Test
	public void getClientScopesTest() {
		String inum = "800-b526-43a0-b5e5-e39c7a970386";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS + "/" + inum + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			Scope[] scopes = mapper.readValue(EntityUtils.toString(entity), Scope[].class);
			Assert.assertNotNull(scopes);
			Assert.assertTrue(scopes.length >= 0);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchClientsTest() {
		String searchPattern = "api";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			OxAuthClient[] clients = mapper.readValue(content, OxAuthClient[].class);
			Assert.assertTrue(clients.length >= 2);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void deleteClientTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteClientScopesTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS + "/" + inum + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteAllClientsTest() {
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CLIENTS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

}
