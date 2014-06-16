package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.association.ClientAssociation;
import org.gluu.oxtrust.model.association.PersonAssociation;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * ClientAssociationWebSevice tests 
 *
 * @author Reda Zerrad Date: 07.23.2012
 */
public class ClientAssociationWebServiceTest extends ConfigurableTest  {
	
   final String JSONREQUEST = "{\"userAssociation\":\"@!1111!0000!38DE\",\"entryAssociations\":[\"inum=@!1111!0008!683B\",\"@!1111!0008!A6A6\"]}";
   final String XMLREQUEST ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><AssociationService xmlns=\"urn:oxtrust:schemas:core:1.0\"><userAssociation>@!1111!0000!38DE</userAssociation><entryAssociations><entryAssociation>@!1111!0008!683B</entryAssociation><entryAssociation>@!1111!0008!A6A6</entryAssociation></entryAssociations></AssociationService>";
   
   String uid = "@!1111!0000!D4E7";
   String cid = "@!1111!0008!683B";
   ClientAssociation  clientAssociation;
   PersonAssociation personAssociation;
   
   
   @BeforeTest
   public void initTestConfiguration() throws Exception {
       initTest();
       clientAssociation = null;
       personAssociation = null;
   }
   
   @Test
   public void getAssociationByUidXml() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/ClientAssociation/User/" + uid) {

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
            	   personAssociation = (PersonAssociation) xmlToObject(responseStr,PersonAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal person from xml");
               }

               assertEquals(response.getStatus(), 200, "Unexpected response code.");
               assertNotNull(personAssociation,"The association is Null");
               assertEquals("@!1111!0000!D4E7", personAssociation.getUserAssociation()," UserAssociations arent the same");
               assertEquals("@!1111!0008!683B", personAssociation.getEntryAssociations().get(0),"EntryAssociations arent the same");
           }
       }.run();
   }
   
   @Test
   public void getAssociationByUidJson() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/ClientAssociation/User/" + uid) {

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
            	   personAssociation = (PersonAssociation) jsonToObject(responseStr,PersonAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal person from JSON");
               }

               assertEquals(response.getStatus(), 200, "Unexpected response code.");
               assertNotNull(personAssociation,"The association is Null");
               assertEquals("@!1111!0000!D4E7", personAssociation.getUserAssociation()," UserAssociations arent the same");
               assertEquals("@!1111!0008!683B", personAssociation.getEntryAssociations().get(0),"EntryAssociations arent the same");
           }
       }.run();
   }
   
   @Test
   public void getAssociationByCidXml() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/ClientAssociation/Client/" + cid) {

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
            	   clientAssociation = (ClientAssociation) xmlToObject(responseStr,ClientAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal client from xml");
               }

               assertEquals(response.getStatus(), 200, "Unexpected response code.");
               assertNotNull(clientAssociation,"The association is Null");
               assertEquals("@!1111!0008!683B", clientAssociation.getEntryAssociation()," EntryAssociations arent the same");
               assertEquals("@!1111!0000!D4E7", clientAssociation.getUserAssociations().get(0),"UserAssociations arent the same");
           }
       }.run();
   }
   
   
   @Test
   public void getAssociationByCidJson() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/ClientAssociation/Client/" + cid) {

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
            	   clientAssociation = (ClientAssociation) jsonToObject(responseStr,ClientAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal client from JSON");
               }

               assertEquals(response.getStatus(), 200, "Unexpected response code.");
               assertNotNull(clientAssociation,"The association is Null");
               assertEquals("@!1111!0008!683B", clientAssociation.getEntryAssociation()," EntryAssociations arent the same");
               assertEquals("@!1111!0000!D4E7", clientAssociation.getUserAssociations().get(0),"UserAssociations arent the same");
           }
       }.run();
   }
   
   
   @Test
   public void createAssociationXmlResponseXml() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/ClientAssociation/Associate") {

           @Override
           protected void prepareRequest(EnhancedMockHttpServletRequest request) {
               super.prepareRequest(request);
               request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
               //send in XML, expect answer in XML
               request.setContentType(MediaType.APPLICATION_XML);
               request.addHeader("Accept", MediaType.APPLICATION_XML);

               try {
                   PersonAssociation testPerson = (PersonAssociation) xmlToObject(XMLREQUEST,PersonAssociation.class); 
                   
                   request.setContent(getXMLBytes(testPerson));
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
                   personAssociation = (PersonAssociation) xmlToObject(responseStr, PersonAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal person from xml");
               }
               
               
               assertNotNull(personAssociation,"PersonAssociation is null");
               assertNotNull(personAssociation,"The association is Null");
               assertEquals("@!1111!0000!38DE", personAssociation.getUserAssociation()," UserAssociations arent the same");
               assertEquals("@!1111!0008!683B", personAssociation.getEntryAssociations().get(0),"EntryAssociations arent the same");
               
           }
       }.run();
   }  
   
   
   @Test
   public void createAssociationJsonResponseJson() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/ClientAssociation/Associate") {

           @Override
           protected void prepareRequest(EnhancedMockHttpServletRequest request) {
               super.prepareRequest(request);
               request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
               //send in XML, expect answer in XML
               request.setContentType(MediaType.APPLICATION_JSON);
               request.addHeader("Accept", MediaType.APPLICATION_JSON);

               try {
                   PersonAssociation testPerson = (PersonAssociation) jsonToObject(JSONREQUEST,PersonAssociation.class); 
                   
                   request.setContent(getJSONBytes(testPerson));
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
                   personAssociation = (PersonAssociation) jsonToObject(responseStr, PersonAssociation.class);
               } catch (Exception ex) {
                   throw new RuntimeException("Failed to unmarshal person from JSON");
               }
               
               
               assertNotNull(personAssociation,"PersonAssociation is null");
               assertNotNull(personAssociation,"The association is Null");
               assertEquals("@!1111!0000!38DE", personAssociation.getUserAssociation()," UserAssociations arent the same");
               assertEquals("@!1111!0008!683B", personAssociation.getEntryAssociations().get(0),"EntryAssociations arent the same");
               
           }
       }.run();
   }  
   
   
   @Test
   public void removeAssociationXmlResponseXml() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/ClientAssociation") {

           @Override
           protected void prepareRequest(EnhancedMockHttpServletRequest request) {
               super.prepareRequest(request);
               request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
               //send in XML, expect answer in XML
               request.setContentType(MediaType.APPLICATION_XML);
               request.addHeader("Accept", MediaType.APPLICATION_XML);

               try {
                   PersonAssociation testPerson = (PersonAssociation) xmlToObject(XMLREQUEST,PersonAssociation.class); 
                   
                   request.setContent(getXMLBytes(testPerson));
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }

           @Override
           protected void onResponse(EnhancedMockHttpServletResponse response) {
               super.onResponse(response);
//               
               assertEquals(response.getStatus(), 200,	"Unexpected response code.");            
               
           }
       }.run();
   }  
   
   
   @Test
   public void removeAssociationJsonResponseJson() throws Exception {
       new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/ClientAssociation") {

           @Override
           protected void prepareRequest(EnhancedMockHttpServletRequest request) {
               super.prepareRequest(request);
               request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
               //send in XML, expect answer in XML
               request.setContentType(MediaType.APPLICATION_JSON);
               request.addHeader("Accept", MediaType.APPLICATION_JSON);

               try {
                   PersonAssociation testPerson = (PersonAssociation) jsonToObject(JSONREQUEST,PersonAssociation.class); 
                   
                   request.setContent(getJSONBytes(testPerson));
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }

           @Override
           protected void onResponse(EnhancedMockHttpServletResponse response) {
               super.onResponse(response);
//               
               assertEquals(response.getStatus(), 200,	"Unexpected response code.");            
               
           }
       }.run();
   }  
   
   private Object jsonToObject(String json, Class<?> clazz) throws Exception {
   	
   	ObjectMapper mapper = new ObjectMapper();
   	Object clazzObject = mapper.readValue(json, clazz);
   	return clazzObject;
   }
  
   
   private Object xmlToObject(String xml, Class<?> clazz) throws Exception {
   	
ByteArrayInputStream input = new ByteArrayInputStream (xml.getBytes()); 
   	
   	JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
   	 
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
       
		Object clazzObject = jaxbUnmarshaller.unmarshal(input);
		return clazzObject;
       
   }
   
   private byte[] getXMLBytes(PersonAssociation person) throws JAXBException {
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       JAXBContext context = JAXBContext.newInstance(ScimPerson.class);
       context.createMarshaller().marshal(person, bos);
       return bos.toByteArray();
   }
   
private byte[] getJSONBytes(PersonAssociation person) throws Exception {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
   	ObjectMapper mapper = new ObjectMapper();
   	 mapper.writeValue(bos, person);
   	return bos.toByteArray();
   }
   
}
