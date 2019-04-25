package org.api.server;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.gluu.model.passport.config.Configuration;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class PassportBasicConfigWebResourceTest extends BaseApiTest {
	private Configuration configuration;

	@Test
	public void getPassportBasicConfigTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.CONFIG);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			configuration = mapper.readValue(content, Configuration.class);
			Assert.assertNotNull(configuration.getLogging().getLevel());
			Assert.assertNotNull(configuration.getServerWebPort());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void updatePassportBasicConfigTest() {
		getPassportBasicConfigTest();
		String level = "warn";
		configuration.getLogging().setLevel(level);
		HttpPut request = new HttpPut(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.PASSPORT + ApiConstants.CONFIG);
		try {
			String json = mapper.writeValueAsString(configuration);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			configuration = mapper.readValue(EntityUtils.toString(result), Configuration.class);
			Assert.assertTrue(configuration.getLogging().getLevel().equalsIgnoreCase(level));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
