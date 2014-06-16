package org.gluu.oxtrust.scim.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;



/**
* Marshalling test
*
* @author Reda Zerrad Date: 04.20.2012
*/
public class PersonMarshallingTest extends SeamTest {
    final String XML_MARSHALLING_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><User xmlns=\"urn:scim:schemas:core:1.0\"><id>@!1111!0000!D4E7</id><externalId>mike</externalId><userName>mike</userName><name><givenName>Michael</givenName><familyName>Schwartz</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>Micheal Schwartz</displayName><nickName>Sensei</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>mike@gluu.org</value><type>work</type><primary>true</primary></email><email><value>mike2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>US</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 US</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>Hiden for Privacy Reasons</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>2012GlUUGLUU</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></User>";
    final String JSON_MARSHALLING_RESPONSE = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0000!D4E7\",\"externalId\":\"mike\",\"userName\":\"mike\",\"name\":{\"givenName\":\"Michael\",\"familyName\":\"Schwartz\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"Micheal Schwartz\",\"nickName\":\"Sensei\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"mike@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"mike2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"Hiden for Privacy Reasons\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"2012GlUUGLUU\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"},\"customAttributes\":[]}";

    @Test
    public void testXmlToPersonUnMarshalling() throws Exception {
    	ScimPerson person = null;
    	
    	try {
        	person = (ScimPerson) xmlToObject(XML_MARSHALLING_RESPONSE,ScimPerson.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to unmarshal person from xml");
        }
        
        assertNotNull(person,"The person is Null");
        assertEquals("mike", person.getUserName()," username's arent the same");
        assertEquals("Michael", person.getName().getGivenName(),"GibvenNames arent the same");
        assertEquals("Schwartz", person.getName().getFamilyName(),"Familynames arent the same");
    }
    
    @Test
    public void testPersonToXmlMarshalling() throws Exception {
    	
    	ScimPerson person = createTestPersonXml();
    	String xml = null;
    	try {
        	 xml =  getXMLString(person);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to marshal person to xml");
        }
        
        assertNotNull(xml,"xml is Null");
        assertEquals(xml,XML_MARSHALLING_RESPONSE,"the generating XML does not match XML_MARSHALLING_RESPONSE");
    	
    }
    
    
    @Test
    public void testJsonToPersonUnMarshalling() throws Exception {
    	ScimPerson person = null;
    	
    	try {
        	person = (ScimPerson) jsonToObject(JSON_MARSHALLING_RESPONSE,ScimPerson.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to unmarshal person from JSON");
        }
        
        assertNotNull(person,"The person is Null");
        assertEquals("mike", person.getUserName()," username's arent the same");
        assertEquals("Michael", person.getName().getGivenName(),"GibvenNames arent the same");
        assertEquals("Schwartz", person.getName().getFamilyName(),"Familynames arent the same");
    } 
    
    @Test
    public void testPersonToJsonMarshalling() throws Exception {
    	
    	ScimPerson person = createTestPersonJson();
    	String json = null;
    	try {
        	 json =  getJSONString(person);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to marshal person to JSON");
        }
        
        assertNotNull(json,"json is Null");
        assertEquals(json,JSON_MARSHALLING_RESPONSE,"the generating json does not match JSON_MARSHALLING_RESPONSE");
    	
    }
    
    
    private ScimPerson createTestPersonXml(){
    	ScimPerson person = null;
    	try {
        	person = (ScimPerson) xmlToObject(XML_MARSHALLING_RESPONSE,ScimPerson.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create TestPersonXML");
        }
        
        return person;
    }
    
    private ScimPerson createTestPersonJson(){
    	ScimPerson person = null;
    	try {
        	person = (ScimPerson) jsonToObject(JSON_MARSHALLING_RESPONSE,ScimPerson.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create TestPersonJSON");
        }
        
        return person;
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
 
 private String getJSONString(ScimPerson person) throws  JsonGenerationException, JsonMappingException, IOException {
 	StringWriter sw = new StringWriter();
 	ObjectMapper mapper = new ObjectMapper();
 	mapper.writeValue(sw, person);
     return sw.toString();
 }
 
 private String getXMLString(ScimPerson person) throws JAXBException {
	 StringWriter sw = new StringWriter();
	 JAXBContext context = JAXBContext.newInstance(ScimPerson.class);
     context.createMarshaller().marshal(person, sw);
     return sw.toString();
 }
 
}
