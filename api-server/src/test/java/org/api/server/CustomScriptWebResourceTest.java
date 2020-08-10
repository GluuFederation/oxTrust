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
import org.gluu.model.ProgrammingLanguage;
import org.gluu.model.ScriptLocationType;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class CustomScriptWebResourceTest extends BaseApiTest {

	@Test
	public void listCustomScriptsTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			CustomScript[] scripts = mapper.readValue(content, CustomScript[].class);
			Assert.assertTrue(scripts.length > 5);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void listUpdateUserCustomScriptsTest() {
		String type = "update_user";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
				+ ApiConstants.SCRIPTS + ApiConstants.TYPE_PATH + "/" + type);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			CustomScript[] scripts = mapper.readValue(content, CustomScript[].class);
			Assert.assertTrue(scripts.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void listUnknownCustomScriptsTest() {
		String type = "unknown";
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
				+ ApiConstants.SCRIPTS + ApiConstants.TYPE_PATH + "/" + type);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getScriptByInumTest() {
		String inum = "5018-AF9C";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			CustomScript script = mapper.readValue(content, CustomScript.class);
			Assert.assertNotNull(script);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createScriptTest() {
		String name = "MyScript";
		CustomScript script = getCustomScript(name);
		HttpPost request = new HttpPost(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS);
		try {
			String json = mapper.writeValueAsString(script);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			CustomScript myScript = mapper.readValue(EntityUtils.toString(result), CustomScript.class);
			Assert.assertEquals(myScript.getName(), name);
			Assert.assertEquals(myScript.getDescription(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}
	
	
	@Test
	public void updateScriptTest() {
		String name = "AnotherMyScript";
		CustomScript script = getCustomScript(name);
		HttpPost request = new HttpPost(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS);
		try {
			String json = mapper.writeValueAsString(script);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			String CONTENT_TYPE = "Content-Type";
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			CustomScript myScript = mapper.readValue(EntityUtils.toString(result), CustomScript.class);
			Assert.assertEquals(myScript.getName(), name);
			
			
			myScript.setDescription(myScript.getDescription() + " Updated");
			HttpPut second = new HttpPut(
					BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS);
			json = mapper.writeValueAsString(myScript);
			entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			second.setEntity(entity);
			second.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);
			response =handle(second);
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	private CustomScript getCustomScript(String name) {
		CustomScript customScript = new CustomScript();
		customScript.setName(name);
		customScript.setDescription(name);
		customScript.setEnabled(false);
		customScript.setLevel(20);
		customScript.setConfigurationProperties(new ArrayList<>());
		customScript.setModuleProperties(new ArrayList<>());
		customScript.setLocationType(ScriptLocationType.LDAP);
		customScript.setProgrammingLanguage(ProgrammingLanguage.PYTHON);
		customScript.setScript("I'm a custom script added via oxtrust api");
		customScript.setScriptType(CustomScriptType.PERSON_AUTHENTICATION);
		return customScript;
	}

	@Test
	public void deleteCustomScriptTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SCRIPTS + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}

	@Test
	public void deleteAllAttributeTest() {
		HttpUriRequest request = new HttpDelete(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}

}
