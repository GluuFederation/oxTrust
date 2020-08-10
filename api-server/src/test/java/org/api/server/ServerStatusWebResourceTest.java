package org.api.server;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class ServerStatusWebResourceTest extends BaseApiTest {
	@Test
	public void getServerStatusTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.STATUS);
		HttpResponse response = handle(request);
		int code = response.getStatusLine().getStatusCode();
		Assert.assertTrue(code == 200);
		HttpEntity entity = response.getEntity();
		try {
			GluuServerStatus server = mapper.readValue(EntityUtils.toString(entity), GluuServerStatus.class);
			Assert.assertNotNull(server);
			Assert.assertNotNull(server.getHostname());
			Assert.assertNotNull(server.getIpAddress());
			Assert.assertNotNull(server.getFreeMemory());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
