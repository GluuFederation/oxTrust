package org.api.server;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.util.X509CertificateShortInfo;
import org.junit.Assert;
import org.junit.Test;

public class CertificatesWebResourceTest extends BaseApiTest {

	@Test
	public void listCertificatesTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CERTIFICATES);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			X509CertificateShortInfo[] certs = mapper.readValue(content, X509CertificateShortInfo[].class);
			Assert.assertTrue(certs.length >= 2);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
