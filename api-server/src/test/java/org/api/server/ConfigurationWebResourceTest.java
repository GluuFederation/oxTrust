package org.api.server;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.gluu.oxauth.model.gluu.GluuConfiguration;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationWebResourceTest extends BaseApiTest {

	@Test
	public void getConfigurationTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			GluuConfiguration configuration = mapper.readValue(content, GluuConfiguration.class);
			Assert.assertNotNull(configuration);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
