package org.api.server;

import java.io.IOException;
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
import org.gluu.model.GluuStatus;
import org.gluu.oxtrust.api.server.model.GluuPersonApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class PeopleWebResourceTest extends BaseApiTest {

	@Test
	public void listPeopleTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuPersonApi[] users = mapper.readValue(content, GluuPersonApi[].class);
			Assert.assertTrue(users.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchUserTest() {
		String searchPattern = "admin";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuPersonApi[] users = mapper.readValue(content, GluuPersonApi[].class);
			Assert.assertTrue(users.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getUserByInumTest() {
		String inum = "A8F2-DE1E.D7FB";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuPersonApi user = mapper.readValue(content, GluuPersonApi.class);
			Assert.assertNotNull(user);
			Assert.assertTrue(user.getUserName().equalsIgnoreCase("admin"));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createUserTest() {
		String name = "ApiUser";
		GluuPersonApi group = getUser(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS);

		try {
			String json = mapper.writeValueAsString(group);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			GluuPersonApi myUser = mapper.readValue(EntityUtils.toString(result), GluuPersonApi.class);
			Assert.assertEquals(myUser.getUserName(), name);
			Assert.assertEquals(myUser.getDisplayName(), name);
			Assert.assertEquals(myUser.getStatus(), GluuStatus.ACTIVE);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void updateUserTest() {
		String name = "ApiUserUpdate";
		GluuPersonApi user = getUser(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS);
		try {
			String json = mapper.writeValueAsString(user);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			GluuPersonApi myUser = mapper.readValue(EntityUtils.toString(result), GluuPersonApi.class);
			Assert.assertEquals(myUser.getDisplayName(), name);

			myUser.setDisplayName(myUser.getDisplayName() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS);
			json = mapper.writeValueAsString(myUser);
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
	public void deleteUserTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteAllUsersTest() {
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.USERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	private GluuPersonApi getUser(String name) {
		GluuPersonApi user = new GluuPersonApi();
		user.setUserName(name);
		user.setDisplayName(name);
		user.setEmail(name + "@yahoo.fr");
		user.setGivenName(name);
		user.setStatus(GluuStatus.ACTIVE);
		user.setPassword(name);
		user.setCreationDate(new Date());
		user.setSurName(name);
		return user;
	}

}
