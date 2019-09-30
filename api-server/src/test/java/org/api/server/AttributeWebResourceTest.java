package org.api.server;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.model.attribute.AttributeDataType;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class AttributeWebResourceTest extends BaseApiTest {
	@Test
	public void getAllAttributesTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuAttribute[] gluuAttributes = mapper.readValue(content, GluuAttribute[].class);
			Assert.assertTrue(gluuAttributes.length > 10);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getActiveAttributesTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES + ApiConstants.ACTIVE);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuAttribute[] gluuAttributes = mapper.readValue(content, GluuAttribute[].class);
			Assert.assertTrue(gluuAttributes.length > 10);
			for (GluuAttribute gluuAttribute : gluuAttributes) {
				Assert.assertTrue(gluuAttribute.getStatus().getValue().equalsIgnoreCase("active"));
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getInActiveAttributesTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES + ApiConstants.INACTIVE);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuAttribute[] gluuAttributes = mapper.readValue(content, GluuAttribute[].class);
			Assert.assertTrue(gluuAttributes.length > 10);
			for (GluuAttribute gluuAttribute : gluuAttributes) {
				Assert.assertTrue(gluuAttribute.getStatus().getValue().equalsIgnoreCase("inactive"));
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void getAttributeByInumTest() {
		String inum = "CAE3";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES + "/" + inum);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			System.out.println("Content:"+content);
			GluuAttribute gluuAttribute = mapper.readValue(content, GluuAttribute.class);
			Assert.assertNotNull(gluuAttribute);
			Assert.assertTrue(gluuAttribute.getInum().equalsIgnoreCase(inum));
			System.out.println("Name:" + gluuAttribute.getDisplayName());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void searchAttributesTest() {
		String searchPattern = "country";
		String SEARCH_QUERY = "?" + ApiConstants.SEARCH_PATTERN + "=" + searchPattern + "&size=5";
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES + ApiConstants.SEARCH + SEARCH_QUERY);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuAttribute[] gluuAttributes = mapper.readValue(content, GluuAttribute[].class);
			Assert.assertTrue(gluuAttributes.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void createAttributeTest() {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		String name = "customTest";
		GluuAttribute attribute = getGluuAtrribute(name);
		HttpPost request = new HttpPost(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES);
		try {
			String json = mapper.writeValueAsString(attribute);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader("Content-Type",MediaType.APPLICATION_JSON);
			
			HttpResponse response = handle(request);
			
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			GluuAttribute gluuAttribute = mapper.readValue(EntityUtils.toString(result), GluuAttribute.class);
			Assert.assertEquals(gluuAttribute.getName(), name);
			Assert.assertEquals(gluuAttribute.getDisplayName(), name);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	private GluuAttribute getGluuAtrribute(String name) {
		GluuAttribute attribute = new GluuAttribute();
		attribute.setName(name);
		attribute.setDisplayName(name);
		attribute.setDescription("custom attribute");
		attribute.setDataType(AttributeDataType.STRING);
		attribute.setStatus(GluuStatus.ACTIVE);
		attribute.setOxMultiValuedAttribute(Boolean.FALSE);
		return attribute;
	}

	@Test
	public void deleteAttributeTest() {
		String inum = "53536GGEGEJE";
		HttpUriRequest request = new HttpDelete(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.ATTRIBUTES + "/" + inum);
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
