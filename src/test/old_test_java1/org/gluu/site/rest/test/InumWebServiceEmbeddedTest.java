package org.gluu.site.rest.test;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.core.MediaType;

import org.gluu.site.test.BaseTest;
import org.gluu.site.util.Configuration;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;


public class InumWebServiceEmbeddedTest extends BaseTest {
	
	@Test
	public void requestInumForPeople() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/inum/" + Configuration.INUM_TYPE_PEOPLE_SLUG) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				request.addHeader("Accept", MediaType.TEXT_PLAIN);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestInum", response);

				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
	@Test
	public void requestInumForGroup() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/inum/" + Configuration.INUM_TYPE_GROUP_SLUG) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				request.addHeader("Accept", MediaType.TEXT_PLAIN);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestInum", response);

				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
	@Test
	public void requestInumForAttribute() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/inum/" + Configuration.INUM_TYPE_ATTRIBUTE_SLUG) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				request.addHeader("Accept", MediaType.TEXT_PLAIN);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestInum", response);

				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
	@Test
	public void requestInumForTrustRelatnShip() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/inum/" + Configuration.INUM_TYPE_TRUST_RELATNSHIP_SLUG) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				request.addHeader("Accept", MediaType.TEXT_PLAIN);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestInum", response);

				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
}
