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
import org.gluu.model.SmtpConfiguration;
import org.gluu.model.SmtpConnectProtectionType;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.junit.Assert;
import org.junit.Test;

public class SMTPWebResourceTest extends BaseApiTest {

	@Test
	public void getSmtpServerConfigurationTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SMTP);
		HttpResponse response = handle(request);
		int code = response.getStatusLine().getStatusCode();
		Assert.assertTrue(code == 404 || code == 200);
	}

	@Test
	public void updateSmtpServerConfigurationTest() {
		SmtpConfiguration configuration = getStmp();
		HttpPut request = new HttpPut(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.SMTP);
		try {
			String json = mapper.writeValueAsString(configuration);
			HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(entity);
			request.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);
			HttpResponse response = handle(request);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			HttpEntity result = response.getEntity();
			configuration = mapper.readValue(EntityUtils.toString(result), SmtpConfiguration.class);
			Assert.assertNotNull(configuration);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testSmtpConfigurationTest() {
		HttpUriRequest request = new HttpGet(BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION
				+ ApiConstants.SMTP + ApiConstants.TEST);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
	}

	private SmtpConfiguration getStmp() {
		SmtpConfiguration configuration = new SmtpConfiguration();
		configuration.setHost("smtp.gmail.com");
		configuration.setPort(587);
		configuration.setPassword("XoLsnJdMZ4EQnWaqkvBSBA==");
		configuration.setPasswordDecrypted("GluuTestMail");
		configuration.setFromEmailAddress("gluutestmail@gmail.com");
		configuration.setFromName("Gluu Api");
		configuration.setRequiresAuthentication(true);
		configuration.setConnectProtection(SmtpConnectProtectionType.START_TLS);
		configuration.setUserName("gluutestmail@gmail.com");
		return configuration;
	}

}
