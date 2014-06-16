package org.gluu.oxtrust.ws.rs.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.GluuCustomPersonList;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonAddresses;
import org.gluu.oxtrust.model.scim.ScimPersonSearch;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * SCIM UserWebService tests 
 *
 * @author Reda Zerrad Date: 04.29.2012
 */
public class UserWebServiceTest extends ConfigurableTest {
	final String USER_PATH = "restv1/Users";
   

    ScimPerson person = null;
    GluuCustomPersonList personList = null;
    String inum = "@!1111!0000!D4E7";
    String uid = "mike";
    
    final String RESPONSEXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><User xmlns=\"urn:scim:schemas:core:1.0\"><id>@!1111!0000!D4E7</id><externalId>mike</externalId><userName>mike</userName><name><givenName>Michael</givenName><familyName>Schwartz</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>Micheal Schwartz</displayName><nickName>Sensei</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>mike@gluu.org</value><type>work</type><primary>true</primary></email><email><value>mike2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>US</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 US</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>Hiden for Privacy Reasons</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>W\\&quot;b431af54f0671a2&quot;</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></User>";
    final String RESPONSEJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"externalId\":\"json\",\"userName\":\"mike\",\"name\":{\"givenName\":\"json\",\"familyName\":\"json\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"json json\",\"nickName\":\"json\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"json@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"json2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"secret\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}}" ;//"{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0000!D4E7\",\"externalId\":\"mike\",\"userName\":\"mike\",\"name\":{\"givenName\":\"Michael\",\"familyName\":\"Schwartz\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"Micheal Schwartz\",\"nickName\":\"Sensei\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"mike@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"mike2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"Hiden for Privacy Reasons\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"W\\\"b431af54f0671a2\"\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}}";
    final String CREATEXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><User xmlns=\"urn:scim:schemas:core:1.0\"><externalId>newxml</externalId><userName>newxml</userName><name><givenName>newxml</givenName><familyName>newxml</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>newxml newxml</displayName><nickName>newxml</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>newxml@gluu.org</value><type>work</type><primary>true</primary></email><email><value>newxml2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>US</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 US</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>Hiden for Privacy Reasons</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>aversion</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></User>";
    final String EDITXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><User xmlns=\"urn:scim:schemas:core:1.0\"><externalId>max</externalId><userName>max</userName><name><givenName>max</givenName><familyName>max</familyName><middleName>N/A</middleName><honorificPrefix>N/A</honorificPrefix><honorificSuffix>N/A</honorificSuffix></name><displayName>max max</displayName><nickName>max</nickName><profileUrl>http://www.gluu.org/</profileUrl><emails><email><value>max@gluu.org</value><type>work</type><primary>true</primary></email><email><value>max2@gluu.org</value><type>home</type><primary>false</primary></email></emails><addresses><address><type>work</type><streetAddress>621 East 6th Street Suite 200</streetAddress><locality>Austin</locality><region>TX</region><postalCode>78701</postalCode><country>FR</country><formatted>621 East 6th Street Suite 200  Austin , TX 78701 FR</formatted><primary>true</primary></address></addresses><PhoneNumbers><PhoneNumber><value>646-345-2346</value><type>work</type></PhoneNumber></PhoneNumbers><ims><im><value>nynymike</value><type>Skype</type></im></ims><photos><photo><value>http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png</value><type>gluu photo</type></photo></photos><userType>CEO</userType><title>CEO</title><preferredLanguage>en-us</preferredLanguage><locale>en_US</locale><timezone>America/Chicago</timezone><active>true</active><password>secret</password><groups><group><display>Gluu Manager Group</display><value>@!1111!0003!B2C6</value></group><group><display>Gluu Owner Group</display><value>@!1111!0003!D9B4</value></group></groups><roles><role><value>Owner</value></role></roles><entitlements><entitlement><value>full access</value></entitlement></entitlements><x509Certificates><x509Certificate><value>MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=</value></x509Certificate></x509Certificates><meta><created>2010-01-23T04:56:22Z</created><lastModified>2011-05-13T04:42:34Z</lastModified><version>W\\&quot;b431af54f0671a2&quot;</version><location>http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7</location></meta></User>";
    final String EDITJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"@!1111!0000!D4E7\",\"externalId\":\"max\",\"userName\":\"max\",\"name\":{\"givenName\":\"max\",\"familyName\":\"max\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"max max\",\"nickName\":\"max\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"max@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"max2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"FR\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 FR\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"secret\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}}";
    final String CREATEJSON = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"externalId\":\"json\",\"userName\":\"json\",\"name\":{\"givenName\":\"json\",\"familyName\":\"json\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"json json\",\"nickName\":\"json\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"json@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"json2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynymike\",\"type\":\"Skype\"}],\"photos\":[{\"value\":\"http://www.gluu.org/wp-content/themes/SaaS-II/images/logo.png\",\"type\":\"gluu photo\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"timezone\":\"America/Chicago\",\"active\":\"true\",\"password\":\"secret\",\"groups\":[{\"display\":\"Gluu Manager Group\",\"value\":\"@!1111!0003!B2C6\"},{\"display\":\"Gluu Owner Group\",\"value\":\"@!1111!0003!D9B4\"}],\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/oxTrust/seam/resource/restv1/Users/@!1111!0000!D4E7\"}}";
    final String XMLSEARCH = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><SearchService xmlns=\"urn:scim:schemas:core:1.0\"><attribute>oxTrustCustAttrA</attribute><value>some random value1</value></SearchService>";
    final String JSONSEARCH = "{\"attribute\":\"oxTrustCustAttrA\",\"value\":\"some random value1\"}";
   
    @BeforeTest
    public void initTestConfiguration() throws Exception {
        initTest();
        person = null;
        personList = null;
        

    }
    
    @Test
    public void getUserByUidXml() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users/" + inum) {

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
                    person = (ScimPerson) xmlToObject(responseStr,ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from xml");
                }

                assertEquals(response.getStatus(), 200, "Unexpected response code.");
                assertNotNull(person,"The person is Null");
                assertEquals("mike", person.getUserName()," username's arent the same");
                assertEquals("Michael", person.getName().getGivenName(),"GibvenNames arent the same");
                assertEquals("Schwartz", person.getName().getFamilyName(),"Familynames arent the same");
            }
        }.run();
    }
    
    @Test
    public void getUserByUidJson() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users/" + inum) {

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
                    person = (ScimPerson) jsonToObject(responseStr,ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from json");
                }

                assertEquals(response.getStatus(), 200, "Unexpected response code.");
                assertNotNull(person);
                assertEquals("mike", person.getUserName());
                assertEquals("Michael", person.getName().getGivenName());
                assertEquals("Schwartz", person.getName().getFamilyName());
            }
        }.run();
    }
    
    @Test
    public void listUsersXmlTest() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users/") {

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
                	personList = (GluuCustomPersonList) xmlToObject(responseStr,GluuCustomPersonList.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person list from xml");
                }
                long totalResult = personList.getTotalResults();
                int numberOfResults = personList.getResources().size();
                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
                assertNotNull(personList,"Person List is null");
                assertEquals(totalResult, (long) numberOfResults );
                
                
                
            }
        }.run();
    }
    
    
    @Test
    public void listUsersJsonTest() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1/Users/") {

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
                	personList = (GluuCustomPersonList) jsonToObject(responseStr,GluuCustomPersonList.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person list from JSON");
                }
                long totalResult = personList.getTotalResults();
                int numberOfResults = personList.getResources().size();
                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
                assertNotNull(personList,"Person List is null");
                assertEquals(totalResult, (long) numberOfResults );
                
                
                
            }
        }.run();
    }
    
    
    @Test
    public void createUserXmlResponseXml() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in XML, expect answer in XML
                request.setContentType(MediaType.APPLICATION_XML);
                request.addHeader("Accept", MediaType.APPLICATION_XML);

                try {
                    ScimPerson testPerson = (ScimPerson) xmlToObject(CREATEXML,ScimPerson.class); 
                    
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
                    person = (ScimPerson) xmlToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from xml");
                }
                String id = person.getId();
                
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + id), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "newxml", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "newxml", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "newxml", "Unexpected last name");
                
            }
        }.run();
    }  
    
    @Test
    public void createUserJsonResponseJson() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in JSON, expect answer in JSON
                request.setContentType(MediaType.APPLICATION_JSON);
                request.addHeader("Accept", MediaType.APPLICATION_JSON);

                try {
                    ScimPerson testPerson = (ScimPerson) jsonToObject(CREATEJSON,ScimPerson.class); 
                    System.out.println(testPerson.getUserName());
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
                    person = (ScimPerson) jsonToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from json");
                }
                String id = person.getId();
                
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + id), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "json", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "json", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "json", "Unexpected last name");
                
            }
        }.run();
    }   
    
    @Test
    public void createExistedUserXmlResponseXml() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in XML, expect answer in XML
                request.setContentType(MediaType.APPLICATION_XML);
                request.addHeader("Accept", MediaType.APPLICATION_XML);

                try {
                    ScimPerson testPerson = (ScimPerson) xmlToObject(RESPONSEXML,ScimPerson.class); 
                    
                    request.setContent(getXMLBytes(testPerson));
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
    public void createExistedUserJSONResponseJSON() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in JSON, expect answer in JSON
                request.setContentType(MediaType.APPLICATION_JSON);
                request.addHeader("Accept", MediaType.APPLICATION_JSON);

                try {
                    ScimPerson testPerson = (ScimPerson) jsonToObject(RESPONSEJSON,ScimPerson.class); 
                    
                    request.setContent(getJSONBytes(testPerson));
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
    public void updateUserXmlResponsXml() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, "/restv1/Users/" + "@!1111!0000!C4C4") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                request.setContentType(MediaType.APPLICATION_XML);
                request.addHeader("Accept", MediaType.APPLICATION_XML);

                try {
                	 ScimPerson testPerson = (ScimPerson) xmlToObject(EDITXML,ScimPerson.class); 
                     request.setContent(getXMLBytes(testPerson));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onResponse(EnhancedMockHttpServletResponse response) {
                super.onResponse(response);
//				showResponse("requestTest", response);
                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
                String responseStr = response.getContentAsString();
                try {
                    person = (ScimPerson) xmlToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from xml");
                }
                //String id = person.getId();
                List<ScimPersonAddresses> addresses = person.getAddresses();
                ScimPersonAddresses address = addresses.get(0);  
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + "@!1111!0000!C4C4"), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "max", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "max", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "max", "Unexpected last name");
                assertEquals(address.getCountry(), "FR", "Unexpected Country");
                assertEquals(address.getFormatted(), "621 East 6th Street Suite 200  Austin , TX 78701 FR", "Unexpected Formatted Address");

            }
        }.run();
    }
    
    @Test
    public void updateUserJsonResponsJson() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.PUT, "/restv1/Users/" + "@!1111!0000!C4C4") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                request.setContentType(MediaType.APPLICATION_JSON);
                request.addHeader("Accept", MediaType.APPLICATION_JSON);

                try {
                	 ScimPerson testPerson = (ScimPerson) jsonToObject(EDITJSON,ScimPerson.class); 
                     request.setContent(getJSONBytes(testPerson));
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
                    person = (ScimPerson) jsonToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from JSON");
                }
                //String id = person.getId();
                List<ScimPersonAddresses> addresses = person.getAddresses();
                ScimPersonAddresses address = addresses.get(0);  
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + "@!1111!0000!C4C4"), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "max", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "max", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "max", "Unexpected last name");
                assertEquals(address.getCountry(), "FR", "Unexpected Country");
                assertEquals(address.getFormatted(), "621 East 6th Street Suite 200  Austin , TX 78701 FR", "Unexpected Formatted Address");

            }
        }.run();
    }
    @Test
    public void deleteUserXML() throws Exception {
    	
    	
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/restv1/Users/@!1111!0000!C6C6") {

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
    public void deleteUserJSON() throws Exception {
    	
    	
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.DELETE, "/restv1/Users/@!1111!0000!C5C5") {

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
    
    @Test
    public void searchPersonXmlResponseXml() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users/Search") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in XML, expect answer in XML
                request.setContentType(MediaType.APPLICATION_XML);
                request.addHeader("Accept", MediaType.APPLICATION_XML);

                try {
                	ScimPersonSearch searchPattern = (ScimPersonSearch) xmlToObject(XMLSEARCH,ScimPersonSearch.class); 
                    
                    request.setContent(getXMLBytes(searchPattern,ScimPersonSearch.class));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onResponse(EnhancedMockHttpServletResponse response) {
                super.onResponse(response);
//                
                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
                
                String responseStr = response.getContentAsString();
                try {
                    person = (ScimPerson) xmlToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from xml");
                }
                String id = person.getId();
                
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + id), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "mike", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "Michael", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "Schwartz", "Unexpected last name");
                
            }
        }.run();
    }  
    
    @Test
    public void searchPersonJSONResponseJSON() throws Exception {
        new ResourceRequest(new ResourceRequestEnvironment(this), Method.POST, "/restv1/Users/Search") {

            @Override
            protected void prepareRequest(EnhancedMockHttpServletRequest request) {
                super.prepareRequest(request);
                request.addHeader("Authorization", "Basic bWlrZTpzZWNyZXQ=");
                //send in JSON, expect answer in JSON
                request.setContentType(MediaType.APPLICATION_JSON);
                request.addHeader("Accept", MediaType.APPLICATION_JSON);

                try {
                	ScimPersonSearch searchPattern = (ScimPersonSearch) jsonToObject(JSONSEARCH,ScimPersonSearch.class); 
                    
                    request.setContent(getJSONBytes(searchPattern));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void onResponse(EnhancedMockHttpServletResponse response) {
                super.onResponse(response);
//                
                assertEquals(response.getStatus(), 200,	"Unexpected response code.");
                
                String responseStr = response.getContentAsString();
                try {
                    person = (ScimPerson) xmlToObject(responseStr, ScimPerson.class);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to unmarshal person from xml");
                }
                String id = person.getId();
                
                assertNotNull(person,"Person is null");
                assertTrue(((String)response.getHeader("Location")).contains("/restv1/Users/" + id), "Unexpected value of Location header" );
                assertEquals(person.getUserName(), "mike", "Unexpected user id");
                assertEquals(person.getName().getGivenName(), "Michael", "Unexpected first name");
                assertEquals(person.getName().getFamilyName(), "Schwartz", "Unexpected last name");
                
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
    
    private byte[] getXMLBytes(ScimPerson person) throws JAXBException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JAXBContext context = JAXBContext.newInstance(ScimPerson.class);
        context.createMarshaller().marshal(person, bos);
        return bos.toByteArray();
    }
    
private byte[] getJSONBytes(ScimPerson person) throws Exception {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectMapper mapper = new ObjectMapper();
    	 mapper.writeValue(bos, person);
    	return bos.toByteArray();
    }

private byte[] getXMLBytes(Object ob, Class<?> clazz) throws JAXBException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    JAXBContext context = JAXBContext.newInstance(clazz);
    context.createMarshaller().marshal(ob, bos);
    return bos.toByteArray();
}

private byte[] getJSONBytes(Object ob) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	ObjectMapper mapper = new ObjectMapper();
	 mapper.writeValue(bos, ob);
	return bos.toByteArray();
}
    
   
}
