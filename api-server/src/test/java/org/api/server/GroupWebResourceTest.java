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
import org.gluu.model.GluuStatus;
import org.gluu.oxtrust.api.server.model.GluuGroupApi;
import org.gluu.oxtrust.api.server.model.GluuPersonApi;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class GroupWebResourceTest extends BaseApiTest {
	@Test
	public void listGroupsTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuGroupApi[] goups = mapper.readValue(content, GluuGroupApi[].class);
			Assert.assertTrue(goups.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getGroupByInumTest() {
		String inum = "60B7";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuGroupApi group = mapper.readValue(content, GluuGroupApi.class);
			Assert.assertNotNull(group);
			Assert.assertTrue(group.getDisplayName().contains("Manager"));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchGroupTest() {
		String searchPattern = "manager";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuGroupApi[] groups = mapper.readValue(content, GluuGroupApi[].class);
			Assert.assertTrue(groups.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void deleteGroupTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void createGroupTest() {
		String name = "OxTrustApiGroup";
		GluuGroupApi group = getGroup(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS);

		try {
			HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsString(group).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			GluuGroupApi myGroup = mapper.readValue(EntityUtils.toString(result), GluuGroupApi.class);
			Assert.assertEquals(myGroup.getDisplayName(), name);
			Assert.assertEquals(myGroup.getStatus(), GluuStatus.ACTIVE);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updateGroupTest() {
		String name = "AnotherGroup";
		GluuGroupApi group = getGroup(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS);
		try {
			HttpEntity entity = new ByteArrayEntity(mapper.writeValueAsString(group).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			GluuGroupApi myGroup = mapper.readValue(EntityUtils.toString(response.getEntity()), GluuGroupApi.class);
			Assert.assertEquals(myGroup.getDisplayName(), name);

			myGroup.setDescription(myGroup.getDescription() + " Updated");
			HttpPut second = new HttpPut(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS);
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

	@Test
	public void getGroupMembersTest() {
		String inum = "60B7";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS + "/" + inum + ApiConstants.GROUP_MEMBERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			GluuPersonApi[] members = mapper.readValue(EntityUtils.toString(entity), GluuPersonApi[].class);
			Assert.assertNotNull(members);
			Assert.assertTrue(members.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void deleteAllGroupsTest() {
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteGroupMembersTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.GROUPS + "/" + inum + ApiConstants.GROUP_MEMBERS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

	private GluuGroupApi getGroup(String name) {
		GluuGroupApi groupApi = new GluuGroupApi();
		groupApi.setDescription(name + " description");
		groupApi.setDisplayName(name);
		groupApi.setStatus(GluuStatus.ACTIVE);
		groupApi.setMembers(new ArrayList<>());
		return groupApi;
	}

}
