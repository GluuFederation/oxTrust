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
import org.gluu.oxtrust.util.LdapConfigurationDTO;
import org.junit.Assert;
import org.junit.Test;

public class LDAPAuthenticationWebResourceTest extends BaseApiTest {

	@Test
	public void readTest() {
		HttpUriRequest request = new HttpGet(
				BASE_URL + ApiConstants.BASE_API_URL + ApiConstants.CONFIGURATION + ApiConstants.LDAP);
		HttpResponse response = handle(request);
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		try {
			String content = EntityUtils.toString(entity);
			LdapConfigurationDTO[] configurationDTOs = mapper.readValue(content, LdapConfigurationDTO[].class);
			Assert.assertTrue(configurationDTOs.length >= 1);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

}
