package org.api.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.gluu.oxauth.model.uma.UmaResource;
import org.gluu.oxauth.model.uma.UmaScopeDescription;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.model.OxAuthClient;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UmaResourceWebServiceTest extends BaseApiTest {

	@Test
	public void listUmaResourcesTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaResource[] resources = mapper.readValue(content, UmaResource[].class);
			Assert.assertTrue(resources.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchUmaResourcesTest() {
		String searchPattern = "api";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA
				+ ApiConstants.RESOURCES + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaResource[] resources = mapper.readValue(content, UmaResource[].class);
			Assert.assertTrue(resources.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getUmaResourceByIdTest() {
		String id = "0f963ecc-93f0-49c1-beae-ad2006abbb99";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES + "/" + id);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaResource resource = mapper.readValue(content, UmaResource.class);
			Assert.assertNotNull(resource);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getUmaResourceClientTest() {
		String id = "0f963ecc-93f0-49c1-beae-ad2006abbb99";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA
				+ ApiConstants.RESOURCES + "/" + id + ApiConstants.CLIENTS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			OxAuthClient[] clients = new ObjectMapper().readValue(content, OxAuthClient[].class);
			Assert.assertNotNull(clients);
			Assert.assertEquals(clients.length, 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getUmaResourceScopesTest() {
		String id = "0f963ecc-93f0-49c1-beae-ad2006abbb99";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA
				+ ApiConstants.RESOURCES + "/" + id + ApiConstants.SCOPES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			UmaScopeDescription[] scopes = mapper.readValue(content, UmaScopeDescription[].class);
			Assert.assertNotNull(scopes);
			Assert.assertEquals(scopes.length, 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void deleteUmaResourceTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void createUmaResourceTest() {
		String name = "ApiUmaResource";
		org.gluu.oxauth.model.uma.persistence.UmaResource resource = getUmaResource(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES);

		try {
			String json = mapper.writeValueAsString(resource);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			org.gluu.oxauth.model.uma.persistence.UmaResource myResource = mapper.readValue(
					EntityUtils.toString(result), org.gluu.oxauth.model.uma.persistence.UmaResource.class);
			Assert.assertNotNull(myResource);
			Assert.assertEquals(myResource.getName(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void updateUmaResourceTest() {
		String name = "ApiUmaResourceUpdate";
		org.gluu.oxauth.model.uma.persistence.UmaResource scope = getUmaResource(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES);
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
			org.gluu.oxauth.model.uma.persistence.UmaResource myResource = mapper.readValue(EntityUtils.toString(result), org.gluu.oxauth.model.uma.persistence.UmaResource.class);
			Assert.assertEquals(myResource.getName(), name);

			myResource.setName(myResource.getName() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.UMA + ApiConstants.RESOURCES);
			json = mapper.writeValueAsString(myResource);
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


	private org.gluu.oxauth.model.uma.persistence.UmaResource getUmaResource(String name) {
		org.gluu.oxauth.model.uma.persistence.UmaResource resource = new org.gluu.oxauth.model.uma.persistence.UmaResource();
		resource.setDescription(name + " description");
		resource.setName(name);
		resource.setCreationDate(new Date());
		resource.setIconUri("https://api.gluu.org/icon/35353");
		resource.setScopes(new ArrayList<>());
		resource.setResources(new ArrayList<>());
		resource.setId(UUID.randomUUID().toString());
		resource.setClients(new ArrayList<>());
		return resource;
	}

}
