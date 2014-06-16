package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.oxchooser.IdentityRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * accountChooserWebService tests 
 *
 * @author Reda Zerrad Date: 07.05.2012
 */
public class OxChooserWebServiceTest extends ConfigurableTest {
     
	IdentityRequest idRequest = null;
	
	 @BeforeTest
	    public void initTestConfiguration() throws Exception {
	        initTest();
	        idRequest = null;
	    }
	 
	 
	 @Test
	    public void defaultResponseTest() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Chooser") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                
	               
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
	                

	                assertEquals(response.getStatus(), 200, "Unexpected response code.");
	                
	            }
	        }.run();
	    }
	 
	 @Test
	    public void serviceTest() throws Exception {
		 new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Chooser/TestHelper") {

			 @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                
	               
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);
	            }
			 
			 
			 @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
	                String responseStr = response.getContentAsString();
	                
	                try {
	                	String testo = "{\"identifier\":\"https://www.google.com/accounts/o8/id\",\"returnToUrl\":\"http://www.gluu.org\",\"axschema\":\"openid\",\"realm\":\"http://www.gluu.org\"}";
	                	idRequest = new IdentityRequest();
	                    idRequest = (IdentityRequest) jsonToObject(testo,IdentityRequest.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal IdentityRequest from json");
	                }
	                
	               assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	               assertEquals(idRequest.getAxschema(), "openid",	"Unexpected Ax Schema.");
	               assertEquals(idRequest.getIdentifier(), "https://www.google.com/accounts/o8/id",	"Unexpected Identifier.");
	               assertEquals(idRequest.getRealm(), "http://www.gluu.org",	"Unexpected Realm.");
	               assertEquals(idRequest.getReturnToUrl(), "http://www.gluu.org",	"Unexpected ReturnToUrl.");

	                
	            }
	        }.run();
	    }   
	 
	 /*
	 
	 @Test
	    public void identityRequestTest() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/oxChooser/Request") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	               
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                    idRequest = new IdentityRequest();
	                	idRequest.setAxschema("openid");
	                	idRequest.setIdentifier("https://www.google.com/accounts/o8/id");
	                	idRequest.setRealm("http://www.gluu.org");
	                	idRequest.setReturnToUrl("http://www.gluu.org");
	                    
	                    request.setContent(getJSONBytes(idRequest));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
	                
	               assertEquals(response.getStatus(), 404,	"Unexpected response code.");
	                
	                
	            }
	        }.run();
	    }   
	  */
	 private byte[] getJSONBytes(IdentityRequest idRequest) throws Exception {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	ObjectMapper mapper = new ObjectMapper();
	    	 mapper.writeValue(bos, idRequest);
	    	return bos.toByteArray();
	    }
	 
	 private Object jsonToObject(String json, Class<?> clazz) throws Exception {
	    	
	    	ObjectMapper mapper = new ObjectMapper();
	    	Object clazzObject = mapper.readValue(json, clazz);
	    	return clazzObject;
	    }
	   
	
}
