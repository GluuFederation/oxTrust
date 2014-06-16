package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.GluuGroupList;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
/**
 * SCIM GroupWebService tests 
 *
 * @author Reda Zerrad Date: 05.10.2012
 */
public class GroupWebServiceTest extends ConfigurableTest {

	ScimGroup group = null;
	GluuGroupList groupList = null;
	String inum = "@!1111!0003!B2C6";
	
	final String RESPONSEXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Group xmlns=\"urn:scim:schemas:core:1.0\"><id>@!1111!0003!B2C6</id><displayName>Gluu Manager Group</displayName><members><member><display>Micheal Schwartz</display><value>@!1111!0000!D4E7</value></member></members></Group>";
	final String RESPONSEJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0003!B2C6\",\"displayName\":\"Gluu Manager Group\",\"members\":[{\"value\":\"@!1111!0000!D4E7\",\"display\":\"Micheal Schwartz\"}]}";
	final String CREATEXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Group xmlns=\"urn:scim:schemas:core:1.0\"><displayName>Gluu Testing Group</displayName><members><member><display>Micheal Schwartz</display><value>@!1111!0000!D4E7</value></member></members></Group>";
	final String CREATEJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0003!B2C6\",\"displayName\":\"Gluu Testing GroupJSON\",\"members\":[{\"value\":\"@!1111!0000!D4E7\",\"display\":\"Micheal Schwartz\"}]}";
	final String EDITXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Group xmlns=\"urn:scim:schemas:core:1.0\"><displayName>Gluu Testing Group1</displayName><members><member><display>Micheal Schwartz</display><value>@!1111!0000!D4E7</value></member></members></Group>";
	final String EDITJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0003!B2C6\",\"displayName\":\"Gluu Testing Group1\",\"members\":[{\"value\":\"@!1111!0000!D4E7\",\"display\":\"Micheal Schwartz\"}]}";
	
	@BeforeTest
    public void initTestConfiguration() throws Exception {
        initTest();
        group = null;
        groupList = null;
        
    }
	
	 @Test
	    public void getGroupByUidXml() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Groups/" + inum) {

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
	                    group = (ScimGroup) xmlToObject(responseStr,ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from xml");
	                }

	                assertEquals(response.getStatus(), 200, "Unexpected response code.");
	                assertNotNull(group,"The group is Null");
	                assertEquals("Gluu Manager Group",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");
	            }
	        }.run();
	    }
	 
	    @Test
	    public void getGroupByUidJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Groups/" + inum) {

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
	                    group = (ScimGroup) jsonToObject(responseStr,ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from json");
	                }

	                assertEquals(response.getStatus(), 200, "Unexpected response code.");
	                assertNotNull(group,"The group is Null");
	                assertEquals("Gluu Manager Group",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");
	            }
	        }.run();
	    }
	
	    @Test
	    public void listGroupsXmlTest() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Groups/") {

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
	                	groupList = (GluuGroupList) xmlToObject(responseStr,GluuGroupList.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group list from xml");
	                }
	                long totalResult = groupList.getTotalResults();
	                int numberOfResults = groupList.getResources().size();
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                assertNotNull(groupList,"group List is null");
	                assertEquals(totalResult, (long) numberOfResults,"number of results arent the same" );           
	            }
	        }.run();
	    }
	    
	    @Test
	    public void listGroupsJsonTest() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Groups/") {

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
	                	groupList = (GluuGroupList) jsonToObject(responseStr,GluuGroupList.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group list from JSON");
	                }
	                long totalResult = groupList.getTotalResults();
	                int numberOfResults = groupList.getResources().size();
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                assertNotNull(groupList,"group List is null");
	                assertEquals(totalResult, (long) numberOfResults,"number of results arent the same" );
	                
	                
	                
	            }
	        }.run();
	    }
	    
	    @Test
	    public void createGroupXmlResponseXml() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Groups") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in XML, expect answer in XML
	                request.setContentType(MediaType.APPLICATION_XML);
	                request.addHeader("Accept", MediaType.APPLICATION_XML);

	                try {
	                    ScimGroup testGroup = (ScimGroup) xmlToObject(CREATEXML,ScimGroup.class); 
	                    
	                    request.setContent(getXMLBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//	                
	                assertEquals(response.getStatus(), 201,	"Unexpected response code.");
	                
	                String responseStr = response.getContentAsString();
	                try {
	                    group = (ScimGroup) xmlToObject(responseStr, ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from xml");
	                }
	                String id = group.getId();
	                
	                assertNotNull(group,"group is null");
	                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Groups/" + id), "Unexpected value of Location header" );
	                assertEquals("Gluu Testing Group",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");
	                
	            }
	        }.run();
	    }  
	    
	    @Test
	    public void createGroupJsonResponseJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Groups") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in JSON, expect answer in JSON
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                    ScimGroup testGroup = (ScimGroup) jsonToObject(CREATEJSON,ScimGroup.class); 
	                    request.setContent(getJSONBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//	                
	                assertEquals(response.getStatus(), 201,	"Unexpected response code.");
	                
	                String responseStr = response.getContentAsString();
	                try {
	                    group = (ScimGroup) jsonToObject(responseStr, ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from json");
	                }
	                String id = group.getId();
	                
	                assertNotNull(group,"group is null");
	                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Groups/" + id), "Unexpected value of Location header" );
	                assertEquals("Gluu Testing GroupJSON",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");
	                
	            }
	        }.run();
	    }   
	 
	    
	    @Test
	    public void createExistedGroupXmlResponseXml() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Groups") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in XML, expect answer in XML
	                request.setContentType(MediaType.APPLICATION_XML);
	                request.addHeader("Accept", MediaType.APPLICATION_XML);

	                try {
	                    ScimGroup testGroup = (ScimGroup) xmlToObject(RESPONSEXML,ScimGroup.class); 
	                    
	                    request.setContent(getXMLBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//	                
	                assertEquals(response.getStatus(), 400,	"Unexpected response code.");
	                
	                
	            }
	        }.run();
	    }   
	    
	    
	    @Test
	    public void createExistedGroupJSONResponseJSON() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Groups") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in JSON, expect answer in JSON
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                    ScimGroup testGroup = (ScimGroup) jsonToObject(RESPONSEJSON,ScimGroup.class); 
	                    
	                    request.setContent(getJSONBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//	                
	                assertEquals(response.getStatus(), 400,	"Unexpected response code.");
	                
	                
	            }
	        }.run();
	    }   
	    
	    
	    @Test
	    public void updateGroupXmlResponsXml() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, "/restv1/Groups/" + "@!1111!0003!C4C4") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.setContentType(MediaType.APPLICATION_XML);
	                request.addHeader("Accept", MediaType.APPLICATION_XML);

	                try {
	                	 ScimGroup testGroup = (ScimGroup) xmlToObject(EDITXML,ScimGroup.class); 
	                     request.setContent(getXMLBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//					showResponse("requestTest", response);
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                String responseStr = response.getContentAsString();
	                try {
	                    group = (ScimGroup) xmlToObject(responseStr, ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from xml");
	                }
                    String id = group.getId();
	                
	                assertNotNull(group,"group is null");
	                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Groups/" + id), "Unexpected value of Location header" );
	                assertEquals("Gluu Testing Group1",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");

	            }
	        }.run();
	    }
	    
	    @Test
	    public void updateGroupJsonResponsJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, "/restv1/Groups/" + "@!1111!0003!C4C4") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                	 ScimGroup testGroup = (ScimGroup) jsonToObject(EDITJSON,ScimGroup.class); 
	                     request.setContent(getJSONBytes(testGroup));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);			
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                String responseStr = response.getContentAsString();
	                try {
	                    group = (ScimGroup) jsonToObject(responseStr, ScimGroup.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal group from JSON");
	                }
                    String id = group.getId();
	                
	                assertNotNull(group,"group is null");
	                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Groups/" + id), "Unexpected value of Location header" );
	                assertEquals("Gluu Testing Group1",group.getDisplayName()," Displaynames arent the same");
	                assertEquals("@!1111!0000!D4E7",group.getMembers().get(0).getValue(),"inums arent the same");

	            }
	        }.run();
	    }
	    
	    @Test
	    public void deleteGroupXML() throws Exception {
	    	
	    	
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/restv1/Groups/@!1111!0003!C6C6") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.addHeader("Accept", MediaType.APPLICATION_XML);
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
	               
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                
	            }
	        }.run();
	    }
	    
	    @Test
	    public void deleteGroupJSON() throws Exception {
	    	
	    	
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/restv1/Groups/@!1111!0003!C5C5") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);
	            }

	            @Override
	            protected void onResponse(EnhancedMockHttpServletResponse response) {
	                super.onResponse(response);
//	               
	                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
	                
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
	 
	 private byte[] getXMLBytes(ScimGroup group) throws JAXBException {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        JAXBContext context = JAXBContext.newInstance(ScimGroup.class);
	        context.createMarshaller().marshal(group, bos);
	        return bos.toByteArray();
	    }
	 private byte[] getJSONBytes(ScimGroup group) throws Exception {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	ObjectMapper mapper = new ObjectMapper();
	    	 mapper.writeValue(bos, group);
	    	return bos.toByteArray();
	    }
}
