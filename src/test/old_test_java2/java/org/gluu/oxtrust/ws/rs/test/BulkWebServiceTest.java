package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.scim.BulkResponses;
import org.gluu.oxtrust.model.scim.ScimBulkOperation;
import org.gluu.oxtrust.model.scim.ScimBulkResponse;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * SCIM BulkWebService tests 
 *
 * @author Reda Zerrad Date: 05.01.2012
 */

public class BulkWebServiceTest extends ConfigurableTest {
	
	ScimBulkResponse bulkResponse = null;
	
	 final String REQUESTJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Operations\":[{\"method\":\"POST\",\"path\":\"/Users\",\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"externalId\":\"bulk\",\"userName\":\"bulk\",\"name\":{\"givenName\":\"bulk\",\"familyName\":\"bulk\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"bulk bulk\",\"nickName\":\"bulk\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"bulk@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"bulk2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"secret\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}},\"bulkId\":\"onebunk\"},{\"method\":\"PUT\",\"path\":\"/Users/@!1111!0000!C4C4\", \"version\":\"oneversion\",\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"displayName\":\"bulk person\",\"externalId\":\"bulk\"}},{\"method\":\"DELETE\",\"path\":\"/Users/@!1111!0000!C3C3\",\"version\":\"oneversion\"}]}";
     final String REQUESTXML ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Bulk xmlns=\"urn:scim:schemas:core:1.0\"><Operations><operation><bulkId>onebunk</bulkId><data><externalId>bulkxml</externalId><userName>bulkxml</userName><name><givenName>bulkxml</givenName><familyName>bulkxml</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>bulkxml bulkxml</displayName><nickName>bulkxml</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>bulkxml@gluu.org</value><type>work</type><primary>true</primary></email><email><value>bulkxml2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>US</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 US</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>secret</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>aversion</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></data><location></location><method>POST</method><path>/Users</path><version></version></operation><operation><bulkId></bulkId><data><externalId>bulk</externalId><userName></userName><name><givenName></givenName><familyName></familyName><middleName></middleName><honorificPrefix></honorificPrefix><honorificSuffix></honorificSuffix></name><displayName>bulk person</displayName><nickName></nickName><profileUrl></profileUrl><emails/><addresses/><PhoneNumbers/><ims/><photos/><userType></userType><title></title><locale></locale><password></password><groups/><roles/><entitlements/><x509Certificates/><meta><created></created><lastModified></lastModified><version></version><location></location></meta></data><location></location><method>PUT</method><path>/Users/@!1111!0000!C4C4</path><version>oneversion</version></operation><operation><bulkId></bulkId><data><externalId></externalId><userName></userName><name><givenName></givenName><familyName></familyName><middleName></middleName><honorificPrefix></honorificPrefix><honorificSuffix></honorificSuffix></name><nickName></nickName><profileUrl></profileUrl><emails/><addresses/><PhoneNumbers/><ims/><photos/><userType></userType><title></title><locale></locale><password></password><groups/><roles/><entitlements/><x509Certificates/><meta><created></created><lastModified></lastModified><version></version><location></location></meta></data><location></location><method>DELETE</method><path>/Users/@!1111!0000!C1C1</path><version>oneversion</version></operation></Operations></Bulk>";
	 final String REQUESTFAILJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"Operations\":[{\"method\":\"POST\",\"path\":\"/Users\",\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"externalId\":\"bulk\",\"userName\":\"mike\",\"name\":{\"givenName\":\"bulk\",\"familyName\":\"bulk\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"bulk bulk\",\"nickName\":\"bulk\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"bulk@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"bulk2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"secret\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}},\"bulkId\":\"onebunk\"},{\"method\":\"PUT\",\"path\":\"/Users/@!1111!0000!JJJJ\", \"version\":\"oneversion\",\"data\":{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"displayName\":\"bulk person\",\"externalId\":\"bulk\"}},{\"method\":\"DELETE\",\"path\":\"/Users/@!1111!0000!JJJJ\",\"version\":\"oneversion\"}]}";
     final String REQUESFAILXML ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Bulk xmlns=\"urn:scim:schemas:core:1.0\"><Operations><operation><bulkId>onebunk</bulkId><data><externalId>bulkxml</externalId><userName>mike</userName><name><givenName>bulkxml</givenName><familyName>bulkxml</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>bulkxml bulkxml</displayName><nickName>bulkxml</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>bulkxml@gluu.org</value><type>work</type><primary>true</primary></email><email><value>bulkxml2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>US</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 US</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>secret</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>aversion</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></data><location></location><method>POST</method><path>/Users</path><version></version></operation><operation><bulkId></bulkId><data><externalId>bulk</externalId><userName></userName><name><givenName></givenName><familyName></familyName><middleName></middleName><honorificPrefix></honorificPrefix><honorificSuffix></honorificSuffix></name><displayName>bulk person</displayName><nickName></nickName><profileUrl></profileUrl><emails/><addresses/><PhoneNumbers/><ims/><photos/><userType></userType><title></title><locale></locale><password></password><groups/><roles/><entitlements/><x509Certificates/><meta><created></created><lastModified></lastModified><version></version><location></location></meta></data><location></location><method>PUT</method><path>/Users/@!1111!0000!JJJJ</path><version>oneversion</version></operation><operation><bulkId></bulkId><data><externalId></externalId><userName></userName><name><givenName></givenName><familyName></familyName><middleName></middleName><honorificPrefix></honorificPrefix><honorificSuffix></honorificSuffix></name><nickName></nickName><profileUrl></profileUrl><emails/><addresses/><PhoneNumbers/><ims/><photos/><userType></userType><title></title><locale></locale><password></password><groups/><roles/><entitlements/><x509Certificates/><meta><created></created><lastModified></lastModified><version></version><location></location></meta></data><location></location><method>DELETE</method><path>/Users/@!1111!0000!JJJJ</path><version>oneversion</version></operation></Operations></Bulk>";
	 
	 @BeforeTest
	    public void initTestConfiguration() throws Exception {
	        initTest();
	        bulkResponse = null;
	        

	    }
     
	 @Test
	    public void bulkRequestJsonResponseJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Bulk") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in JSON, expect answer in JSON
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                	ScimBulkOperation testBulkRequest = (ScimBulkOperation) jsonToObject(REQUESTJSON,ScimBulkOperation.class); 
	                    request.setContent(getJSONBytes(testBulkRequest));
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
	                	bulkResponse = (ScimBulkResponse) jsonToObject(responseStr, ScimBulkResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal person from json");
	                }
	                 List<BulkResponses> operations = bulkResponse.getOperations();
	                 
	                assertNotNull(bulkResponse,"bulkResponse is null");
	                assertEquals(operations.get(0).getStatus().getCode(), "201", "Unexpected status code for adding a person");
	                assertEquals(operations.get(1).getStatus().getCode(), "200", "Unexpected status code for modifying a person");
	                assertEquals(operations.get(2).getStatus().getCode(), "200", "Unexpected status code for modifying a person");
	                
	            }
	        }.run();
	    }   
	 
	 @Test
	    public void bulkFailRequestJsonResponseJson() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Bulk") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in JSON, expect answer in JSON
	                request.setContentType(MediaType.APPLICATION_JSON);
	                request.addHeader("Accept", MediaType.APPLICATION_JSON);

	                try {
	                	ScimBulkOperation testBulkRequest = (ScimBulkOperation) jsonToObject(REQUESTFAILJSON,ScimBulkOperation.class); 
	                    request.setContent(getJSONBytes(testBulkRequest));
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
	                	bulkResponse = (ScimBulkResponse) jsonToObject(responseStr, ScimBulkResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal person from json");
	                }
	                 List<BulkResponses> operations = bulkResponse.getOperations();
	                 
	                assertNotNull(bulkResponse,"bulkResponse is null");
	                assertEquals(operations.get(0).getStatus().getCode(), "400", "Unexpected status code for adding a person");
	                assertEquals(operations.get(1).getStatus().getCode(), "400", "Unexpected status code for modifying a person");
	                assertEquals(operations.get(2).getStatus().getCode(), "400", "Unexpected status code for modifying a person");
	                
	            }
	        }.run();
	    }   
	 
	 
	 @Test
	    public void bulkRequestXMLResponseXML() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Bulk") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in JSON, expect answer in JSON
	                request.setContentType(MediaType.APPLICATION_XML);
	                request.addHeader("Accept", MediaType.APPLICATION_XML);

	                try {
	                	ScimBulkOperation testBulkRequest = (ScimBulkOperation) xmlToObject(REQUESTXML,ScimBulkOperation.class); 
	                    request.setContent(getXMLBytes(testBulkRequest));
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
	                	bulkResponse = (ScimBulkResponse) xmlToObject(responseStr, ScimBulkResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal person from XML");
	                }
	                 List<BulkResponses> operations = bulkResponse.getOperations();
	                 
	                assertNotNull(bulkResponse,"bulkResponse is null");
	                assertEquals(operations.get(0).getStatus().getCode(), "201", "Unexpected status code for adding a person");
	                assertEquals(operations.get(1).getStatus().getCode(), "200", "Unexpected status code for modifying a person");
	                assertEquals(operations.get(2).getStatus().getCode(), "200", "Unexpected status code for modifying a person");
	                
	            }
	        }.run();
	    }   
	 
	 @Test
	    public void bulkFailRequestXMLResponseXML() throws Exception {
	        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Bulk") {

	            @Override
	            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
	                super.prepareRequest(request);
	                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
	                //send in XML, expect answer in XML
	                request.setContentType(MediaType.APPLICATION_XML);
	                request.addHeader("Accept", MediaType.APPLICATION_XML);

	                try {
	                	ScimBulkOperation testBulkRequest = (ScimBulkOperation) xmlToObject(REQUESFAILXML,ScimBulkOperation.class); 
	                    request.setContent(getXMLBytes(testBulkRequest));
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
	                	bulkResponse = (ScimBulkResponse) xmlToObject(responseStr, ScimBulkResponse.class);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Failed to unmarshal person from XML");
	                }
	                 List<BulkResponses> operations = bulkResponse.getOperations();
	                 
	                assertNotNull(bulkResponse,"bulkResponse is null");
	                assertEquals(operations.get(0).getStatus().getCode(), "400", "Unexpected status code for adding a person");
	                assertEquals(operations.get(1).getStatus().getCode(), "400", "Unexpected status code for modifying a person");
	                assertEquals(operations.get(2).getStatus().getCode(), "400", "Unexpected status code for modifying a person");
	                
	            }
	        }.run();
	    }   
	 
	 
	    private byte[] getXMLBytes(ScimBulkOperation testBulkRequest) throws JAXBException {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        JAXBContext context = JAXBContext.newInstance(ScimBulkOperation.class);
	        context.createMarshaller().marshal(testBulkRequest, bos);
	        return bos.toByteArray();
	    }
	    
	private byte[] getJSONBytes(ScimBulkOperation testBulkRequest) throws Exception {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	ObjectMapper mapper = new ObjectMapper();
	    	 mapper.writeValue(bos, testBulkRequest);
	    	return bos.toByteArray();
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
}
