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
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.junit.Assert;
import org.junit.Test;

public class SectorIdentifierWebResourceTest extends BaseApiTest {

	@Test
	public void getAllSectorIdentifiersTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			OxAuthSectorIdentifier[] sectors = mapper.readValue(EntityUtils.toString(entity),
					OxAuthSectorIdentifier[].class);
			Assert.assertNotNull(sectors);
			Assert.assertTrue(sectors.length >= 0);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchSectorIdentifierTest() {
		String searchPattern = "unknow";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			OxAuthSectorIdentifier[] resources = mapper.readValue(EntityUtils.toString(entity),
					OxAuthSectorIdentifier[].class);
			Assert.assertTrue(resources.length >= 0);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createSectorIdentifierTest() {
		String name = "ApiSector";
		OxAuthSectorIdentifier sector = getSector(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS);

		try {
			String json = mapper.writeValueAsString(sector);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			OxAuthSectorIdentifier mySector = mapper.readValue(EntityUtils.toString(result),
					OxAuthSectorIdentifier.class);
			Assert.assertNotNull(mySector);
			Assert.assertEquals(mySector.getDescription(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateUmaResourceTest() {
		String name = "ApiSectorUpdate";
		OxAuthSectorIdentifier scope = getSector(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS);
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
			OxAuthSectorIdentifier mySector = mapper.readValue(EntityUtils.toString(result),
					OxAuthSectorIdentifier.class);
			Assert.assertEquals(mySector.getDescription(), name);

			mySector.setDescription(mySector.getDescription() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS);
			json = mapper.writeValueAsString(mySector);
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

	private OxAuthSectorIdentifier getSector(String name) {
		OxAuthSectorIdentifier sector = new OxAuthSectorIdentifier();
		sector.setDescription(name);
		sector.setRedirectUris(new ArrayList<>());
		sector.setId(UUID.randomUUID().toString());
		sector.setClientIds(new ArrayList<>());
		return sector;
	}

	@Test
	public void getSectorIdentifierByInumTest() {
		String inum = "40040404fyyy";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteSectorIdentifierTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.SECTORS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

}
