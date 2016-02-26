/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.test;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.action.test.AbstractAuthorizationTest;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.scim2.jackson.custom.UserDeserializer;
import org.gluu.oxtrust.util.CopyUtils2;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.seam.Component;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.*;
import org.xdi.service.SchemaService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Val Pecaoco
 */
@RunWith(Arquillian.class)
public class UserExtensionsTest extends AbstractAuthorizationTest {

    @Test
    @InSequence(1)
    public void testCreatePersonFromJsonString() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {

                System.out.println(" testCreatePersonFromJsonString() ");

                AttributeService attributeService = AttributeService.instance();
                SchemaService schemaService = SchemaService.instance();
                ApplicationConfiguration applicationConfiguration = ((OxTrustConfiguration)Component.getInstance(OxTrustConfiguration.class)).getApplicationConfiguration();

                // Create custom attributes
                GluuAttribute customFirst = null;  // String, not multi-valued
                if (attributeService.getAttributeByName("customFirst") == null) {
                    customFirst = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customFirst", "Custom First", "First custom attribute", GluuAttributeDataType.STRING, OxMultivalued.FALSE);
                }
                GluuAttribute customSecond = null;  // Date, multi-valued
                if (attributeService.getAttributeByName("customSecond") == null) {
                    customSecond = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customSecond", "Custom Second", "Second custom attribute", GluuAttributeDataType.DATE, OxMultivalued.TRUE);
                }
                GluuAttribute customThird = null;  // Numeric, not multi-valued
                if (attributeService.getAttributeByName("customThird") == null) {
                    customThird = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customThird", "Custom Third", "Third custom attribute", GluuAttributeDataType.NUMERIC, OxMultivalued.FALSE);
                }

                String CREATEJSON = "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\",\"urn:ietf:params:scim:schemas:extension:gluu:2.0:User\"],\"urn:ietf:params:scim:schemas:extension:gluu:2.0:User\": {\"customFirst\":\"[1000,2000]\",\"customSecond\":[\"2016-02-23T15:35:22Z\"],\"customThird\":3000},\"externalId\":\"scimclient\",\"userName\":\"userjson.add.username\",\"name\":{\"givenName\":\"json\",\"familyName\":\"json\",\"middleName\":\"N/A\",\"honorificPrefix\":\"N/A\",\"honorificSuffix\":\"N/A\"},\"displayName\":\"json json\",\"nickName\":\"json\",\"profileUrl\":\"http://www.gluu.org/\",\"emails\":[{\"value\":\"json@gluu.org\",\"type\":\"work\",\"primary\":\"true\"},{\"value\":\"json2@gluu.org\",\"type\":\"home\",\"primary\":\"false\"}],\"addresses\":[{\"type\":\"work\",\"streetAddress\":\"621 East 6th Street Suite 200\",\"locality\":\"Austin\",\"region\":\"TX\",\"postalCode\":\"78701\",\"country\":\"US\",\"formatted\":\"621 East 6th Street Suite 200  Austin , TX 78701 US\",\"primary\":\"true\"}],\"phoneNumbers\":[{\"value\":\"646-345-2346\",\"type\":\"work\"}],\"ims\":[{\"value\":\"nynytest_user\",\"type\":\"Skype\"}],\"userType\":\"CEO\",\"title\":\"CEO\",\"preferredLanguage\":\"en-us\",\"locale\":\"en_US\",\"active\":\"true\",\"password\":\"secret\",\"roles\":[{\"value\":\"Owner\"}],\"entitlements\":[{\"value\":\"full access\"}],\"x509Certificates\":[{\"value\":\"MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtleGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIwIAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5iPSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZzidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDrSGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAUdGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJtNg5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1RC4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1+GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=\"}],\"meta\":{\"created\":\"2010-01-23T04:56:22Z\",\"lastModified\":\"2011-05-13T04:42:34Z\",\"version\":\"aversion\",\"location\":\"http://localhost:8080/identity/seam/resource/restv1/Users/8c4b6c26-efaf-4840-bddf-c0146a8eb2a9\"}}";

                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
                SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, ""));
                simpleModule.addDeserializer(User.class, new UserDeserializer());
                mapper.registerModule(simpleModule);

                User user = mapper.readValue(CREATEJSON, User.class);

                Extension extension = user.getExtension(Constants.USER_EXT_SCHEMA_ID);
                Assert.assertNotNull("(Deserialization) Custom extension not deserialized.", extension);

                Extension.Field customFirstField = extension.getFields().get("customFirst");
                Assert.assertNotNull("(Deserialization) \"customFirst\" field not deserialized.", customFirstField);
                Assert.assertEquals("[1000,2000]", customFirstField.getValue());
                System.out.println("##### (Deserialization) customFirstField.getValue() = " + customFirstField.getValue());

                Extension.Field customSecondField = extension.getFields().get("customSecond");
                Assert.assertNotNull("(Deserialization) \"customSecond\" field not deserialized.", customSecondField);
                List<Date> dateList = Arrays.asList(mapper.readValue(customSecondField.getValue(), Date[].class));
                Assert.assertEquals(1, dateList.size());
                System.out.println("##### (Deserialization) dateList.get(0) = " + dateList.get(0));

                Extension.Field customThirdField = extension.getFields().get("customThird");
                Assert.assertNotNull("(Deserialization) \"customThird\" field not deserialized.", customThirdField);
                Assert.assertEquals(new BigDecimal(3000), new BigDecimal(customThirdField.getValue()));
                System.out.println("##### (Deserialization) customThirdField.getValue() = " + customThirdField.getValue());

                // Create Person
                GluuCustomPerson gluuPerson = CopyUtils2.copy(user, null, false);
                IPersonService personService = PersonService.instance();
                String inum = personService.generateInumForNewPerson();
                String dn = personService.getDnForPerson(inum);
                String iname = personService.generateInameForNewPerson(user.getUserName());
                gluuPerson.setDn(dn);
                gluuPerson.setInum(inum);
                gluuPerson.setIname(iname);
                gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

                personService.addPerson(gluuPerson);

                // Retrieve Person
                GluuCustomPerson retrievedPerson = personService.getPersonByUid(gluuPerson.getUid());
                Assert.assertNotNull("Failed to find person.", retrievedPerson);

                User newPerson = CopyUtils2.copy(gluuPerson, null);

                extension = newPerson.getExtension(Constants.USER_EXT_SCHEMA_ID);
                Assert.assertNotNull("(Persistence) Custom extension not persisted.", extension);

                customFirstField = extension.getFields().get("customFirst");
                Assert.assertNotNull("(Persistence) \"customFirst\" field not persisted.", customFirstField);
                Assert.assertEquals("[1000,2000]", customFirstField.getValue());
                System.out.println("##### (Persistence) customFirstField.getValue() = " + customFirstField.getValue());

                customSecondField = extension.getFields().get("customSecond");
                Assert.assertNotNull("(Persistence) \"customSecond\" field not persisted.", customSecondField);
                dateList = Arrays.asList(mapper.readValue(customSecondField.getValue(), Date[].class));
                Assert.assertEquals(1, dateList.size());
                System.out.println("##### (Persistence) dateList.get(0) = " + dateList.get(0));

                customThirdField = extension.getFields().get("customThird");
                Assert.assertNotNull("(Persistence) \"customThird\" field not persisted.", customThirdField);
                Assert.assertEquals(new BigDecimal(3000), new BigDecimal(customThirdField.getValue()));
                System.out.println("##### (Persistence) customThirdField.getValue() = " + customThirdField.getValue());

                // Remove Person
                personService.removePerson(retrievedPerson);

                // Remove custom attributes
                // schemaService.removeAttributeTypeFromObjectClass(customFirst.getOrigin(), customFirst.getName());
                // schemaService.removeStringAttribute(customFirst.getName());
                // attributeService.removeAttribute(customFirst);
                // schemaService.removeAttributeTypeFromObjectClass(customSecond.getOrigin(), customSecond.getName());
                // schemaService.removeStringAttribute(customSecond.getName());
                // attributeService.removeAttribute(customSecond);
                // schemaService.removeAttributeTypeFromObjectClass(customThird.getOrigin(), customThird.getName());
                // schemaService.removeStringAttribute(customThird.getName());
                // attributeService.removeAttribute(customThird);
            }
        }.run();
    }

    @Test
    @InSequence(2)
    public void testCreatePersonFromUserObject() throws Exception {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {

                System.out.println(" testCreatePersonFromUserObject() ");

                AttributeService attributeService = AttributeService.instance();
                SchemaService schemaService = SchemaService.instance();
                ApplicationConfiguration applicationConfiguration = ((OxTrustConfiguration)Component.getInstance(OxTrustConfiguration.class)).getApplicationConfiguration();

                // Create custom attributes
                GluuAttribute customFirst = null;  // String, not multi-valued
                if (attributeService.getAttributeByName("customFirst") == null) {
                    customFirst = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customFirst", "Custom First", "First custom attribute", GluuAttributeDataType.STRING, OxMultivalued.FALSE);
                }
                GluuAttribute customSecond = null;  // Date, multi-valued
                if (attributeService.getAttributeByName("customSecond") == null) {
                    customSecond = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customSecond", "Custom Second", "Second custom attribute", GluuAttributeDataType.DATE, OxMultivalued.TRUE);
                }
                GluuAttribute customThird = null;  // Numeric, not multi-valued
                if (attributeService.getAttributeByName("customThird") == null) {
                    customThird = createCustomAttribute(attributeService, schemaService, applicationConfiguration, "customThird", "Custom Third", "Third custom attribute", GluuAttributeDataType.NUMERIC, OxMultivalued.FALSE);
                }

                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

                User user = createUserObject();

                // Create Person
                GluuCustomPerson gluuPerson = CopyUtils2.copy(user, null, false);
                IPersonService personService = PersonService.instance();
                String inum = personService.generateInumForNewPerson();
                String dn = personService.getDnForPerson(inum);
                String iname = personService.generateInameForNewPerson(user.getUserName());
                gluuPerson.setDn(dn);
                gluuPerson.setInum(inum);
                gluuPerson.setIname(iname);
                gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

                personService.addPerson(gluuPerson);

                // Retrieve Person
                GluuCustomPerson retrievedPerson = personService.getPersonByUid(gluuPerson.getUid());
                Assert.assertNotNull("Failed to find person.", retrievedPerson);

                User newPerson = CopyUtils2.copy(gluuPerson, null);

                Extension extension = newPerson.getExtension(Constants.USER_EXT_SCHEMA_ID);
                Assert.assertNotNull("(Persistence) Custom extension not persisted.", extension);

                Extension.Field customFirstField = extension.getFields().get("customFirst");
                Assert.assertNotNull("(Persistence) \"customFirst\" field not persisted.", customFirstField);
                Assert.assertEquals("customFirstValue", customFirstField.getValue());
                System.out.println("##### (Persistence) customFirstField.getValue() = " + customFirstField.getValue());

                Extension.Field customSecondField = extension.getFields().get("customSecond");
                Assert.assertNotNull("(Persistence) \"customSecond\" field not persisted.", customSecondField);
                List<Date> dateList = Arrays.asList(mapper.readValue(customSecondField.getValue(), Date[].class));
                Assert.assertEquals(2, dateList.size());
                System.out.println("##### (Persistence) dateList.get(0) = " + dateList.get(0));
                System.out.println("##### (Persistence) dateList.get(1) = " + dateList.get(1));

                Extension.Field customThirdField = extension.getFields().get("customThird");
                Assert.assertNotNull("(Persistence) \"customThird\" field not persisted.", customThirdField);
                Assert.assertEquals(new BigDecimal(3000), new BigDecimal(customThirdField.getValue()));
                System.out.println("##### (Persistence) customThirdField.getValue() = " + customThirdField.getValue());

                // Remove Person
                personService.removePerson(retrievedPerson);

                // Remove custom attributes
                // schemaService.removeAttributeTypeFromObjectClass(customFirst.getOrigin(), customFirst.getName());
                // schemaService.removeStringAttribute(customFirst.getName());
                // attributeService.removeAttribute(customFirst);
                // schemaService.removeAttributeTypeFromObjectClass(customSecond.getOrigin(), customSecond.getName());
                // schemaService.removeStringAttribute(customSecond.getName());
                // attributeService.removeAttribute(customSecond);
                // schemaService.removeAttributeTypeFromObjectClass(customThird.getOrigin(), customThird.getName());
                // schemaService.removeStringAttribute(customThird.getName());
                // attributeService.removeAttribute(customThird);
            }
        }.run();
    }

    private User createUserObject() throws Exception {

        User user = new User();

        user.setUserName("userjson.add.username");
        user.setPassword("test");
        user.setDisplayName("Scim2DisplayName");

        Email email = new Email();
        email.setValue("scim@gluu.org");
        email.setType(org.gluu.oxtrust.model.scim2.Email.Type.WORK);
        email.setPrimary(true);
        user.getEmails().add(email);

        PhoneNumber phone = new PhoneNumber();
        phone.setType(org.gluu.oxtrust.model.scim2.PhoneNumber.Type.WORK);
        phone.setValue("654-6509-263");
        user.getPhoneNumbers().add(phone);

        org.gluu.oxtrust.model.scim2.Address address = new org.gluu.oxtrust.model.scim2.Address();
        address.setCountry("US");
        address.setStreetAddress("random street");
        address.setLocality("Austin");
        address.setPostalCode("65672");
        address.setRegion("TX");
        address.setPrimary(true);
        address.setType(org.gluu.oxtrust.model.scim2.Address.Type.WORK);
        address.setFormatted(address.getStreetAddress() + " " + address.getLocality() + " " + address.getPostalCode() + " " + address.getRegion() + " "
                + address.getCountry());
        user.getAddresses().add(address);

        user.setPreferredLanguage("US_en");

        org.gluu.oxtrust.model.scim2.Name name = new  org.gluu.oxtrust.model.scim2.Name();
        name.setFamilyName("SCIM");
        name.setGivenName("SCIM");
        user.setName(name);

        // User Extensions
        Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setField("customFirst", "customFirstValue");
        extensionBuilder.setFieldAsList("customSecond", Arrays.asList(new String[]{"2016-02-23T03:35:22Z", "2016-02-24T01:52:05Z"}));
        extensionBuilder.setField("customThird", new BigDecimal(3000));
        user.addExtension(extensionBuilder.build());

        return user;
    }

    private GluuAttribute createCustomAttribute(AttributeService attributeService,
                                                SchemaService schemaService,
                                                ApplicationConfiguration applicationConfiguration,
                                                String name, String displayName, String description, GluuAttributeDataType gluuAttributeDataType, OxMultivalued oxMultivalued) throws Exception {

        System.out.println(" createCustomAttribute() ");

        String objectClassName = attributeService.getCustomOrigin();
        String ldapAttributedName = attributeService.generateRandomOid();
        String inum = attributeService.generateInumForNewAttribute();
        String dn = attributeService.getDnForAttribute(inum);

        GluuAttribute gluuAttribute = new GluuAttribute();
        gluuAttribute.setRequred(false);
        gluuAttribute.setName(name);
        gluuAttribute.setDisplayName(displayName);
        gluuAttribute.setDescription(description);
        gluuAttribute.setOrigin(objectClassName);
        gluuAttribute.setStatus(GluuStatus.ACTIVE);
        gluuAttribute.setEditType(new GluuUserRole[] { GluuUserRole.ADMIN, GluuUserRole.MANAGER, GluuUserRole.OWNER, GluuUserRole.USER });
        gluuAttribute.setDataType(gluuAttributeDataType);
        gluuAttribute.setCustom(true);
        gluuAttribute.setOxSCIMCustomAttribute(ScimCustomAtribute.TRUE);
        gluuAttribute.setOxMultivaluedAttribute(oxMultivalued);
        gluuAttribute.setInum(inum);
        gluuAttribute.setDn(dn);
        if (gluuAttribute.getSaml1Uri() == null || gluuAttribute.getSaml1Uri().equals("")) {
            gluuAttribute.setSaml1Uri("urn:gluu:dir:attribute-def:" + gluuAttribute);
        }
        if (gluuAttribute.getSaml2Uri() == null || gluuAttribute.getSaml2Uri().equals("")) {
            gluuAttribute.setSaml2Uri("urn:oid:" + gluuAttribute);
        }

        schemaService.addStringAttribute(ldapAttributedName, name, applicationConfiguration.getSchemaAddAttributeDefinition());
        schemaService.addAttributeTypeToObjectClass(objectClassName, name);

        attributeService.addAttribute(gluuAttribute);

        return gluuAttribute;
    }
}
