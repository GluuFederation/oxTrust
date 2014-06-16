package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.InumResponse;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * InumGenerationWebService tests 
 *
 * @author Reda Zerrad Date: 08.24.2012
 */
public class InumGenerationWebServiceTest extends ConfigurableTest {
	
	 InumResponse inumResponse = null;
	
	 @BeforeTest
	    public void initTestConfiguration() throws Exception {
	        initTest();
	        inumResponse = null;
	        
	    }
	 
	 @Test
	    public void generateInumXml() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users//InumGenerator?entityPrefix=person") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                
	                
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.addHeader("Accept", MediaType.APPLICATION_XML);
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
	                String responseStr = response.getContentAsString();
	                
	                try {
	                	inumResponse = (InumResponse) xmlToObject(responseStr,InumResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal inumResponse from xml");
	                }

	                assertEquals(response.getStatus(), 200, "Unexpected response code.");
	                assertNotNull(inumResponse,"The inumResponse is Null");
	            }
	        }.run();
	    }
	 
	 @Test
	    public void generateInumJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users//InumGenerator?entityPrefix=person") {

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
	                	inumResponse = (InumResponse) jsonToObject(responseStr,InumResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal inumResponse from JSON");
	                }

	                assertEquals(response.getStatus(), 200, "Unexpected response code.");
	                assertNotNull(inumResponse,"The inumResponse is Null");
	            }
	        }.run();
	    }
	 
	 private Object xmlToObject(String xml, Class<?> clazz) throws Exception {
	    	
		 ByteArrayInputStream input = new ByteArrayInputStream (xml.getBytes()); 
		     	
		     	JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		     	 
		          Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		         
		 		Object clazzObject = jaxbUnmarshaller.unmarshal(input);
		 		return clazzObject;
		         
		     }
	 
	 private Object jsonToObject(String json, Class<?> clazz) throws Exception {
	    	
	    	ObjectMapper mapper = new ObjectMapper();
	    	Object clazzObject = mapper.readValue(json, clazz);
	    	return clazzObject;
	    }
	   

}
