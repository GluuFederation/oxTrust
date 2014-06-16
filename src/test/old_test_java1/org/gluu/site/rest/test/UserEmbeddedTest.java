package org.gluu.site.rest.test;

import static org.testng.Assert.assertEquals;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.gluu.site.model.Person;
import org.gluu.site.test.BaseTest;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.xml.sax.InputSource;
import org.testng.annotations.Test;


public class UserEmbeddedTest extends BaseTest {
	
	Person person = null;
//	String inum = "@!DA85.5F98.95A1.CA3B!9E7D.4BF5!4FEB";
//	String inum = "@!DA85.5F98.95A1.CA3B!9E7D.4BF5!A1F9";
	String inum = "@!DA85.5F98.95A1.CA3B!9E7D.4BF5!0000!D038.DB72"; // GU-1
	
	@Test
	public void getUserTest() throws Exception {
		getAllUsers();
	}
	
//	@Test
	public void updateUserTest() throws Exception {
		getUserByInum();
		updateUser();
	}
	
//	@Test
	public void userCrudTest() throws Exception {
		createUser();
		updateUser();
		deleteUser();
	}
	
	private void initTestData(EnhancedMockHttpServletRequest request) {
		request.addHeader("Accept", MediaType.APPLICATION_XML);
		request.setAuthType("shibboleth");
//		request.addHeader("REMOTE_USER", "mike");
		request.addHeader("REMOTE_USER", "test12345");
	}

//	@Test
	public void getAllUsers() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/users/") {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
//				showResponse("requestTest", response);
//				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
				assertEquals(response.getStatus(), 403,	"Unexpected response code."); // Assuming the user doesn't exist.
			}
		}.run();
	}
	
//	@Test
	public void getUserByInum() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/users/" + inum) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				String responseStr = response.getContentAsString();
				person = null;
				try {
					person = (Person) xmlToObject(responseStr);
				} catch (Exception ex) {
					// Do Nothing
				}
//				showResponse("requestTest", response);
				if (person != null) {
					System.out.println("UserId: " + person.getUserId());
					System.out.println("Email: " + person.getEmail());
					System.out.println("AttributeSize: " + person.getPersonAttrList().size());
				}
				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
//	@Test
	public void getUserById() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/service/users/uid/pankaj") {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestTest", response);
				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
//	@Test
	public void deleteUser() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/service/users/" + inum) {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);				
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestTest", response);
				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
//	@Test
	public void createUser() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/service/users/dummy/createpost") {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
				request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				
				try {
			        Person person = new Person();
			        person.setUserId("GU-1");
			        person.setFirstName("gFirst");
			        person.setDisplayName("Gluu One");
			        person.setLastName("gLast");
			        person.setEmail("gu1@email.com");
			        person.setPassword("test123");
			        String xml = objectToString(person);
			        request.addParameter("person_data", xml);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestTest", response);
				assertEquals(response.getStatus(), 201,	"Unexpected response code.");
			}
		}.run();
	}
	
//	@Test
	public void updateUser() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/service/users/" + inum + "/updatepost") {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
				request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				
				try {
					person.setEmail("abc@def.com"); 
					String xml = objectToString(person);
			        request.addParameter("person_data", xml);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
//				showResponse("requestTest", response);
				assertEquals(response.getStatus(), 200,	"Unexpected response code.");
			}
		}.run();
	}
	
//	@Test
	public void updatePassword() throws Exception {
		new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/service/users/" + inum + "/passwordpost") {

			@Override
			protected void prepareRequest(EnhancedMockHttpServletRequest request) {
				super.prepareRequest(request);
				initTestData(request);
				request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				request.addParameter("password", "test123");
			}

			@Override
			protected void onResponse(EnhancedMockHttpServletResponse response) {
				super.onResponse(response);
				showResponse("requestTest", response);
				assertEquals(response.getStatus(), 204,	"Unexpected response code.");
			}
		}.run();
	}
	
	private Object xmlToObject(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(xml)));
        DOMReader reader = new DOMReader();
        Document doc = reader.read(document); 
        
        JAXBContext context = JAXBContext.newInstance(Person.class);
        DocumentSource source = new DocumentSource(doc);  			        
        context = JAXBContext.newInstance(Person.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(source);
	}
	
	private String objectToString(Person person) throws Exception {
		JAXBContext context = JAXBContext.newInstance(Person.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        DocumentResult dr = new DocumentResult();
        marshaller.marshal(person, dr );
        return dr.getDocument().asXML();
	}
}
