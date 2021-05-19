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
import org.gluu.oxauth.model.gluu.GluuConfiguration;
import org.gluu.oxtrust.api.server.model.OxtrustSetting;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.model.GluuBoolean;
import org.junit.Assert;
import org.junit.Test;

public class OxtrustSettingWebResourceTest extends BaseApiTest {

	@Test
	public void getOxtrustSettingsTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXTRUST_SETTINGS);
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

	@Test
	public void updateOxtrustSettingTest() {
		OxtrustSetting oxtrustSetting = getOxTrustSetting();
		try {
			HttpPut second = new HttpPut(
					BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.OXTRUST_SETTINGS);
			HttpEntity entity = new ByteArrayEntity(
					mapper.writeValueAsString(oxtrustSetting).toString().getBytes("UTF-8"),
					ContentType.APPLICATION_FORM_URLENCODED);
			second.setEntity(entity);
			second.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);

			HttpResponse response = handle(second);

			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	private OxtrustSetting getOxTrustSetting() {
		OxtrustSetting oxtrustSetting = new OxtrustSetting();
		oxtrustSetting.setAllowPasswordReset(GluuBoolean.ENABLED.getValue());
		oxtrustSetting.setAllowProfileManagement(GluuBoolean.ENABLED.getValue());
		oxtrustSetting.setEnablePassport(GluuBoolean.ENABLED.getValue());
		oxtrustSetting.setEnableScim(GluuBoolean.ENABLED.getValue());
		return oxtrustSetting;
	}

}
