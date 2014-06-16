package org.gluu.oxtrust.oxchooser.test;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.oxchooser.IdentityRequest;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
* Marshalling test
*
* @author Reda Zerrad Date: 07.06.2012
*/
public class oxChooserMarshallingTest extends SeamTest {
	
	@Test
    public void identityRequestToJSONMarshalling() throws Exception {
		IdentityRequest idRequest = new IdentityRequest();
		idRequest = new IdentityRequest();
    	idRequest.setAxschema("openid");
    	idRequest.setIdentifier("https://www.google.com/accounts/o8/id");
    	idRequest.setRealm("oxTrust");
    	idRequest.setReturnToUrl( "http://www.gluu.org");
		String idRequestToString = null;
    	
    	try {
        	 idRequestToString = getJSONString(idRequest);
        	 System.out.println(idRequestToString);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to marshal IdentityRequest to JSON");
        }
        
        assertNotNull(idRequestToString,"The person is Null");
        
    }
	
	private String getJSONString(IdentityRequest idRequest) throws  JsonGenerationException, JsonMappingException, IOException {
	 	StringWriter sw = new StringWriter();
	 	ObjectMapper mapper = new ObjectMapper();
	 	mapper.writeValue(sw, idRequest);
	     return sw.toString();
	 }

}
