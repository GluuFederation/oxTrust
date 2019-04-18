package org.api.server;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Test;

public class AttributeWebResourceTest extends BaseApiTest {

	@Test
	public void getAllAttributesTest() {
		HttpUriRequest request = new HttpGet("https://gluu.gasmyr.com/identity/restv1/api/v1/attributes");
		HttpResponse response = handle(request);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
	}

}
