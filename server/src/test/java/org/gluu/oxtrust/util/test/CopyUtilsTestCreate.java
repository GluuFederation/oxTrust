package org.gluu.oxtrust.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim.PersonMeta;
import org.gluu.oxtrust.model.scim.ScimCustomAttributes;
import org.gluu.oxtrust.model.scim.ScimEntitlements;
import org.gluu.oxtrust.model.scim.ScimName;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonAddresses;
import org.gluu.oxtrust.model.scim.ScimPersonEmails;
import org.gluu.oxtrust.model.scim.ScimPersonGroups;
import org.gluu.oxtrust.model.scim.ScimPersonIms;
import org.gluu.oxtrust.model.scim.ScimPersonPhones;
import org.gluu.oxtrust.model.scim.ScimPersonPhotos;
import org.gluu.oxtrust.model.scim.ScimRoles;
import org.gluu.oxtrust.model.scim.Scimx509Certificates;
import org.gluu.oxtrust.util.CopyUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class CopyUtilsTestCreate extends ConfigurableTest {
	private static final String GLUU_STATUS = "gluuStatus";

	private static final String OX_TRUST_PHOTOS_TYPE = "oxTrustPhotosType";

	private static final String OX_TRUST_PHONE_TYPE = "oxTrustPhoneType";

	private static final String OX_TRUST_ADDRESS_PRIMARY = "oxTrustAddressPrimary";

	private static final String OX_TRUST_ADDRESS_TYPE = "oxTrustAddressType";

	private static final String OX_TRUST_COUNTRY = "oxTrustCountry";

	private static final String OX_TRUST_POSTAL_CODE = "oxTrustPostalCode";

	private static final String OX_TRUST_REGION = "oxTrustRegion";

	private static final String OX_TRUST_LOCALITY = "oxTrustLocality";

	private static final String OX_TRUST_ADDRESS_FORMATTED = "oxTrustAddressFormatted";

	private static final String OX_TRUST_STREET = "oxTrustStreet";

	private static final String OX_TRUST_EMAIL_PRIMARY = "oxTrustEmailPrimary";

	private static final String OX_TRUST_EMAIL_TYPE = "oxTrustEmailType";

	private static final String OX_TRUST_META_LOCATION = "oxTrustMetaLocation";

	private static final String OX_TRUST_META_VERSION = "oxTrustMetaVersion";

	private static final String OX_TRUST_META_LAST_MODIFIED = "oxTrustMetaLastModified";

	private static final String OX_TRUST_META_CREATED = "oxTrustMetaCreated";

	private static final String OX_TRUSTX509_CERTIFICATE = "oxTrustx509Certificate";

	private static final String OX_TRUST_ENTITLEMENTS = "oxTrustEntitlements";

	private static final String OX_TRUST_ROLE = "oxTrustRole";

	private static final String OX_TRUST_ACTIVE = "oxTrustActive";

	private static final String OX_TRUST_LOCALE = "oxTrustLocale";

	private static final String OX_TRUST_TITLE = "oxTrustTitle";

	private static final String OX_TRUST_USER_TYPE = "oxTrustUserType";

	private static final String OX_TRUST_PHOTOS = "oxTrustPhotos";

	private static final String OX_TRUST_IMS_VALUE = "oxTrustImsValue";

	private static final String OX_TRUST_PHONE_VALUE = "oxTrustPhoneValue";

	private static final String OX_TRUST_ADDRESSES = "oxTrustAddresses";

	private static final String OX_TRUST_EMAIL = "oxTrustEmail";

	private static final String OX_TRUST_PROFILE_URL = "oxTrustProfileURL";

	private static final String OX_TRUST_NICK_NAME = "oxTrustNickName";

	private static final String OX_TRUST_EXTERNAL_ID = "oxTrustExternalId";

	private static final String OX_TRUSTHONORIFIC_SUFFIX = "oxTrusthonorificSuffix";

	private static final String OX_TRUSTHONORIFIC_PREFIX = "oxTrusthonorificPrefix";

	private static final String OX_TRUST_MIDDLE_NAME = "oxTrustMiddleName";

	@Test
	public void testCopyScim1EmptyCreate() throws Exception {
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = new ScimPerson();
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNull(copy);
			}
		}.run();
	}

	@Test
	public void testCopyScim1FilledCreate() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = new ScimPerson();
				
				source.setActive("true");
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();
				ScimCustomAttributes customattribute = new ScimCustomAttributes();
				customattribute.setName("custom_name");
				List<String> values = new ArrayList<String>();
				String value = "value";
				values.add(value);
				customattribute.setValues(values);
				customAttributes.add(customattribute);
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group = new ScimPersonGroups();
				group.setDisplay("group_display");
				group.setValue("group_value");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("userName");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNotNull(copy);
				assertEquals("userName",copy.getUid());
				assertEquals("givenName",copy.getGivenName());
				assertEquals("familyName",copy.getSurname());
				assertEquals("displayName",copy.getDisplayName());
				assertEquals("preferredLanguage",copy.getPreferredLanguage());
				assertEquals("timezone",copy.getTimezone());
				assertEquals("password",copy.getUserPassword());
				assertNotNull(copy.getMemberOf());
				assertEquals(1,copy.getMemberOf().size());
				assertEquals("Mocked DN", copy.getMemberOf().get(0));
				
				assertEquals("true",copy.getAttribute(GLUU_STATUS));				
				assertNull(copy.getAttribute(OX_TRUST_PHOTOS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_PHONE_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
				assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
				assertNull(copy.getAttribute(OX_TRUST_REGION));
				assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
				assertNull(copy.getAttribute(OX_TRUST_STREET));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_TYPE));
				assertEquals("location",copy.getAttribute(OX_TRUST_META_LOCATION));
				assertEquals("version",copy.getAttribute(OX_TRUST_META_VERSION));
				assertEquals("lastModified",copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
				assertEquals("created",copy.getAttribute(OX_TRUST_META_CREATED));
				assertEquals("[{\"value\":\"cert_value\"}]",copy.getAttribute(OX_TRUSTX509_CERTIFICATE));
				assertEquals("[{\"value\":\"entitlement_value\"}]",copy.getAttribute(OX_TRUST_ENTITLEMENTS));
				assertEquals("[{\"value\":\"role_value\"}]",copy.getAttribute(OX_TRUST_ROLE));
				assertEquals("true",copy.getAttribute(OX_TRUST_ACTIVE));
				assertEquals("locale",copy.getAttribute(OX_TRUST_LOCALE));
				assertEquals("title",copy.getAttribute(OX_TRUST_TITLE));
				assertEquals("userType",copy.getAttribute(OX_TRUST_USER_TYPE));
				assertEquals("[{\"value\":\"photo_value\",\"type\":\"photo_type\"}]",copy.getAttribute(OX_TRUST_PHOTOS));
				assertEquals("[{\"value\":\"ims_value\",\"type\":\"ims_type\"}]",copy.getAttribute(OX_TRUST_IMS_VALUE));
				assertEquals("[{\"value\":\"phone_value\",\"type\":\"phone_type\"}]",copy.getAttribute(OX_TRUST_PHONE_VALUE));
				assertEquals("[{\"type\":\"address_type\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"formatted\":\"formatted\",\"primary\":\"address_primary\"}]",copy.getAttribute(OX_TRUST_ADDRESSES));
				assertEquals("[{\"value\":\"email_value\",\"type\":\"email_type\",\"primary\":\"email_primary\"}]",copy.getAttribute(OX_TRUST_EMAIL));
				assertEquals("profileUrl",copy.getAttribute(OX_TRUST_PROFILE_URL));
				assertEquals("nickName",copy.getAttribute(OX_TRUST_NICK_NAME));
				assertEquals("externalId",copy.getAttribute(OX_TRUST_EXTERNAL_ID));
				assertEquals("honorificSuffix",copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX));
				assertEquals("honorificPrefix",copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX));
				assertEquals("middleName",copy.getAttribute(OX_TRUST_MIDDLE_NAME));
				assertNull(copy.getAssociatedClient());
				assertNull(copy.getBaseDn());
				assertNull(copy.getCommonName());
				List<GluuCustomAttribute> customAttributes2 = copy.getCustomAttributes();
				assertNotNull(customAttributes2);
				assertEquals(32,customAttributes2.size());
				
				assertEquals("displayName",copy.getDisplayName());
				assertNull(copy.getDn());
				assertEquals("givenName",copy.getGivenName());
				assertNull(copy.getGluuAllowPublication());
				GluuCustomAttribute gluuCustomAttribute = copy.getGluuCustomAttribute("custom_name");
				assertNotNull(gluuCustomAttribute);
				assertEquals("custom_name", gluuCustomAttribute.getName());				
				assertEquals("value", gluuCustomAttribute.getValue());
				assertNull(gluuCustomAttribute.getDate());
				assertEquals("value",gluuCustomAttribute.getDisplayValue());
				assertNull(gluuCustomAttribute.getMetadata());
				assertEquals("value",gluuCustomAttribute.getValues()[0]);
				assertNull(copy.getGluuOptOuts());
				assertNull(copy.getIname());
				assertNull(copy.getMail());
				assertEquals("Mocked DN",copy.getMemberOf().get(0));
				assertNull(copy.getNetworkPoken());
				assertNull(copy.getOxCreationTimestamp());
				assertEquals("preferredLanguage",copy.getPreferredLanguage());
				assertNull(copy.getSLAManager());
				assertNull(copy.getSourceServerName());
				assertNull(copy.getStatus());
				assertEquals("familyName",copy.getSurname());
				assertEquals("timezone",copy.getTimezone());
				assertEquals("userName",copy.getUid());
				assertEquals("password",copy.getUserPassword());
			}
		}.run();
	}
	

	@Test
	public void testCopyScim1FilledCreateExisting() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				
				
				destination.setAllowPublication(true);
				List<String> associatedClientDNs = new ArrayList<String>();
				associatedClientDNs.add("a");
				associatedClientDNs.add("b");
				associatedClientDNs.add("c");
				destination.setAssociatedClient(associatedClientDNs );
				destination.setBaseDn("dn");
				destination.setAttribute(OX_TRUST_NICK_NAME, "original nickname");
				destination.setAttribute(OX_TRUST_PROFILE_URL, "original url");
				destination.setCommonName("CN");
				destination.setGivenName("original givenname");
				destination.setPreferredLanguage("Nederlands");

				
				destination.setAttribute(OX_TRUST_ENTITLEMENTS, "[{\"value\":\"original entitlement_value\"}]");
				
				ScimPerson source = new ScimPerson();
				
				source.setActive("true");
				
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();
				ScimCustomAttributes customattribute = new ScimCustomAttributes();
				customattribute.setName("custom_name");
				List<String> values = new ArrayList<String>();
				String value = "value";
				values.add(value);
				customattribute.setValues(values);
				customAttributes.add(customattribute);
				
				ScimCustomAttributes customattribute2 = new ScimCustomAttributes();
				customattribute2.setName("custom_name2");
				List<String> values2 = new ArrayList<String>();
				String value2 = "value";
				values2.add(value2);
				customattribute2.setValues(values2);
				customAttributes.add(customattribute2);
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group = new ScimPersonGroups();
				group.setDisplay("group_display");
				group.setValue("group_value");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl(null);
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("existing");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNull(copy);
			}
		}.run();
	}

	@Test
	public void testCopyScim1FilledMultipleAttributesCreate() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = new ScimPerson();
				
				source.setActive("true");
				
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();
				ScimCustomAttributes customattribute = new ScimCustomAttributes();
				customattribute.setName("custom_name");
				List<String> values = new ArrayList<String>();
				values.add("value1");
				values.add("value3");
				values.add("value2");
				values.add("value4");
				customattribute.setValues(values);
				customAttributes.add(customattribute);
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group1 = new ScimPersonGroups();
				group1.setDisplay("group_display");
				group1.setValue("group_value");
				ScimPersonGroups group2 = new ScimPersonGroups();
				group2.setDisplay("group_display1");
				group2.setValue("group_value1");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group1);
				groups.add(group2);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("userName");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNotNull(copy);
				assertEquals("userName",copy.getUid());
				assertEquals("givenName",copy.getGivenName());
				assertEquals("familyName",copy.getSurname());
				assertEquals("displayName",copy.getDisplayName());
				assertEquals("preferredLanguage",copy.getPreferredLanguage());
				assertEquals("timezone",copy.getTimezone());
				assertEquals("password",copy.getUserPassword());
				assertNotNull(copy.getMemberOf());
				assertEquals(2,copy.getMemberOf().size());
				assertEquals("Mocked DN", copy.getMemberOf().get(0));
				assertEquals("Mocked DN1", copy.getMemberOf().get(1));
				
				assertEquals("true",copy.getAttribute(GLUU_STATUS));
				
				assertNull(copy.getAttribute(OX_TRUST_PHOTOS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_PHONE_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
				assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
				assertNull(copy.getAttribute(OX_TRUST_REGION));
				assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
				assertNull(copy.getAttribute(OX_TRUST_STREET));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_TYPE));
				assertEquals("location",copy.getAttribute(OX_TRUST_META_LOCATION));
				assertEquals("version",copy.getAttribute(OX_TRUST_META_VERSION));
				assertEquals("lastModified",copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
				assertEquals("created",copy.getAttribute(OX_TRUST_META_CREATED));
				assertEquals("[{\"value\":\"cert_value\"}]",copy.getAttribute(OX_TRUSTX509_CERTIFICATE));
				assertEquals("[{\"value\":\"entitlement_value\"}]",copy.getAttribute(OX_TRUST_ENTITLEMENTS));
				assertEquals("[{\"value\":\"role_value\"}]",copy.getAttribute(OX_TRUST_ROLE));
				assertEquals("true",copy.getAttribute(OX_TRUST_ACTIVE));
				assertEquals("locale",copy.getAttribute(OX_TRUST_LOCALE));
				assertEquals("title",copy.getAttribute(OX_TRUST_TITLE));
				assertEquals("userType",copy.getAttribute(OX_TRUST_USER_TYPE));
				assertEquals("[{\"value\":\"photo_value\",\"type\":\"photo_type\"}]",copy.getAttribute(OX_TRUST_PHOTOS));
				assertEquals("[{\"value\":\"ims_value\",\"type\":\"ims_type\"}]",copy.getAttribute(OX_TRUST_IMS_VALUE));
				assertEquals("[{\"value\":\"phone_value\",\"type\":\"phone_type\"}]",copy.getAttribute(OX_TRUST_PHONE_VALUE));
				assertEquals("[{\"type\":\"address_type\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"formatted\":\"formatted\",\"primary\":\"address_primary\"}]",copy.getAttribute(OX_TRUST_ADDRESSES));
				assertEquals("[{\"value\":\"email_value\",\"type\":\"email_type\",\"primary\":\"email_primary\"}]",copy.getAttribute(OX_TRUST_EMAIL));
				assertEquals("profileUrl",copy.getAttribute(OX_TRUST_PROFILE_URL));
				assertEquals("nickName",copy.getAttribute(OX_TRUST_NICK_NAME));
				assertEquals("externalId",copy.getAttribute(OX_TRUST_EXTERNAL_ID));
				assertEquals("honorificSuffix",copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX));
				assertEquals("honorificPrefix",copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX));
				assertEquals("middleName",copy.getAttribute(OX_TRUST_MIDDLE_NAME));
			}
		}.run();
	}
	
	@Test
	public void testCopyScim1FilledNullCustomAttributesCreate() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = new ScimPerson();
				
				source.setActive("active");
				
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
					
				source.setCustomAttributes(null);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group1 = new ScimPersonGroups();
				group1.setDisplay("group_display");
				group1.setValue("group_value");
				ScimPersonGroups group2 = new ScimPersonGroups();
				group2.setDisplay("group_display1");
				group2.setValue("group_value1");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group1);
				groups.add(group2);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("userName");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNotNull(copy);
				assertEquals("userName",copy.getUid());
				assertEquals("givenName",copy.getGivenName());
				assertEquals("familyName",copy.getSurname());
				assertEquals("displayName",copy.getDisplayName());
				assertEquals("preferredLanguage",copy.getPreferredLanguage());
				assertEquals("timezone",copy.getTimezone());
				assertEquals("password",copy.getUserPassword());
				assertNotNull(copy.getMemberOf());
				assertEquals(2,copy.getMemberOf().size());
				assertEquals("Mocked DN", copy.getMemberOf().get(0));
				assertEquals("Mocked DN1", copy.getMemberOf().get(1));
				
				assertNull(copy.getAttribute(GLUU_STATUS));
				assertNull(copy.getAttribute(OX_TRUST_PHOTOS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_PHONE_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
				assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
				assertNull(copy.getAttribute(OX_TRUST_REGION));
				assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
				assertNull(copy.getAttribute(OX_TRUST_STREET));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_TYPE));
				assertEquals("location",copy.getAttribute(OX_TRUST_META_LOCATION));
				assertEquals("version",copy.getAttribute(OX_TRUST_META_VERSION));
				assertEquals("lastModified",copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
				assertEquals("created",copy.getAttribute(OX_TRUST_META_CREATED));
				assertEquals("[{\"value\":\"cert_value\"}]",copy.getAttribute(OX_TRUSTX509_CERTIFICATE));
				assertEquals("[{\"value\":\"entitlement_value\"}]",copy.getAttribute(OX_TRUST_ENTITLEMENTS));
				assertEquals("[{\"value\":\"role_value\"}]",copy.getAttribute(OX_TRUST_ROLE));
				assertEquals("active",copy.getAttribute(OX_TRUST_ACTIVE));
				assertEquals("locale",copy.getAttribute(OX_TRUST_LOCALE));
				assertEquals("title",copy.getAttribute(OX_TRUST_TITLE));
				assertEquals("userType",copy.getAttribute(OX_TRUST_USER_TYPE));
				assertEquals("[{\"value\":\"photo_value\",\"type\":\"photo_type\"}]",copy.getAttribute(OX_TRUST_PHOTOS));
				assertEquals("[{\"value\":\"ims_value\",\"type\":\"ims_type\"}]",copy.getAttribute(OX_TRUST_IMS_VALUE));
				assertEquals("[{\"value\":\"phone_value\",\"type\":\"phone_type\"}]",copy.getAttribute(OX_TRUST_PHONE_VALUE));
				assertEquals("[{\"type\":\"address_type\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"formatted\":\"formatted\",\"primary\":\"address_primary\"}]",copy.getAttribute(OX_TRUST_ADDRESSES));
				assertEquals("[{\"value\":\"email_value\",\"type\":\"email_type\",\"primary\":\"email_primary\"}]",copy.getAttribute(OX_TRUST_EMAIL));
				assertEquals("profileUrl",copy.getAttribute(OX_TRUST_PROFILE_URL));
				assertEquals("nickName",copy.getAttribute(OX_TRUST_NICK_NAME));
				assertEquals("externalId",copy.getAttribute(OX_TRUST_EXTERNAL_ID));
				assertEquals("honorificSuffix",copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX));
				assertEquals("honorificPrefix",copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX));
				assertEquals("middleName",copy.getAttribute(OX_TRUST_MIDDLE_NAME));
			}
		}.run();
	}
	
	@Test
	public void testCopyScim1MixedCreate() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = new ScimPerson();
				
				source.setActive("false");
				
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setLocality(null);	
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();	
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("");
				meta.setLastModified("");
				meta.setLocation("");
				meta.setVersion("");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("");
				source.setTitle("title");
				source.setUserName("userName");
				source.setUserType("userType");
				
				source.setX509Certificates(null);
				
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNotNull(copy);
				assertEquals("userName",copy.getUid());
				assertEquals("givenName",copy.getGivenName());
				assertEquals("familyName",copy.getSurname());
				assertEquals("displayName",copy.getDisplayName());
				assertNull(copy.getPreferredLanguage());
				assertNull(copy.getTimezone());
				assertEquals("password",copy.getUserPassword());
				assertNotNull(copy.getMemberOf());
				assertEquals(0,copy.getMemberOf().size());
				
				assertEquals("false",copy.getAttribute(GLUU_STATUS));
				
				assertNull(copy.getAttribute(OX_TRUST_PHOTOS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_PHONE_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
				assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
				assertNull(copy.getAttribute(OX_TRUST_REGION));
				assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
				assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
				assertNull(copy.getAttribute(OX_TRUST_STREET));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
				assertNull(copy.getAttribute(OX_TRUST_EMAIL_TYPE));
				assertNull(copy.getAttribute(OX_TRUST_META_LOCATION));
				assertNull(copy.getAttribute(OX_TRUST_META_VERSION));
				assertNull(copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
				assertNull(copy.getAttribute(OX_TRUST_META_CREATED));
				assertNull(copy.getAttribute(OX_TRUSTX509_CERTIFICATE));
				assertEquals("[{\"value\":\"entitlement_value\"}]",copy.getAttribute(OX_TRUST_ENTITLEMENTS));
				assertEquals("[{\"value\":\"role_value\"}]",copy.getAttribute(OX_TRUST_ROLE));
				assertEquals("false",copy.getAttribute(OX_TRUST_ACTIVE));
				assertEquals("locale",copy.getAttribute(OX_TRUST_LOCALE));
				assertEquals("title",copy.getAttribute(OX_TRUST_TITLE));
				assertEquals("userType",copy.getAttribute(OX_TRUST_USER_TYPE));
				assertEquals("[{\"value\":\"photo_value\",\"type\":\"photo_type\"}]",copy.getAttribute(OX_TRUST_PHOTOS));
				assertEquals("[{\"value\":\"ims_value\",\"type\":\"ims_type\"}]",copy.getAttribute(OX_TRUST_IMS_VALUE));
				assertEquals("[{\"value\":\"phone_value\",\"type\":\"phone_type\"}]",copy.getAttribute(OX_TRUST_PHONE_VALUE));
				assertEquals("[{\"type\":\"address_type\",\"streetAddress\":\"streetAddress\",\"locality\":null,\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"formatted\":\"\",\"primary\":\"address_primary\"}]",copy.getAttribute(OX_TRUST_ADDRESSES));
				assertEquals("[{\"value\":\"email_value\",\"type\":\"email_type\",\"primary\":\"email_primary\"}]",copy.getAttribute(OX_TRUST_EMAIL));
				assertEquals("profileUrl",copy.getAttribute(OX_TRUST_PROFILE_URL));
				assertEquals("nickName",copy.getAttribute(OX_TRUST_NICK_NAME));
				assertEquals("externalId",copy.getAttribute(OX_TRUST_EXTERNAL_ID));
				assertEquals("honorificSuffix",copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX));
				assertEquals("honorificPrefix",copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX));
				assertEquals("middleName",copy.getAttribute(OX_TRUST_MIDDLE_NAME));
			}
		}.run();
	}
	
	@Test
	public void testCopyScim1createNullSource() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = new GluuCustomPerson();
				ScimPerson source = null;
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNull(copy);
			}

		}.run();
	}
	
	@Test
	public void testCopyScim1CreateNullDestination() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = null;
				ScimPerson source = new ScimPerson();
				
				source.setActive("true");
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();
				ScimCustomAttributes customattribute = new ScimCustomAttributes();
				customattribute.setName("custom_name");
				List<String> values = new ArrayList<String>();
				String value = "value";
				values.add(value);
				customattribute.setValues(values);
				customAttributes.add(customattribute);
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group = new ScimPersonGroups();
				group.setDisplay("group_display");
				group.setValue("group_value");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("userName");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNotNull(copy);
			}

		}.run();
	}
	@Test
	public void testCopyScim1CreateException() throws Exception {
		
		new ComponentTest() {
			@Override
			protected void testComponents() throws Exception {
				GluuCustomPerson destination = null;
				ScimPerson source = new ScimPerson();
				
				source.setActive("true");
				ScimPersonAddresses address  = new ScimPersonAddresses();
				address.setCountry("country");
				address.setFormatted("formatted");
				address.setLocality("locality");
				address.setPostalCode("postalCode");
				address.setPrimary("address_primary");
				address.setRegion("region");
				address.setStreetAddress("streetAddress");
				address.setType("address_type");
				List<ScimPersonAddresses> addresses = new ArrayList<ScimPersonAddresses>();
				addresses.add(address);
				
				source.setAddresses(addresses );
				
				List<ScimCustomAttributes> customAttributes = new ArrayList<ScimCustomAttributes>();
				ScimCustomAttributes customattribute = new ScimCustomAttributes();
				customattribute.setName("custom_name");
				List<String> values = new ArrayList<String>();
				String value = "value";
				values.add(value);
				customattribute.setValues(values);
				customAttributes.add(customattribute);
				source.setCustomAttributes(customAttributes);
				
				source.setDisplayName("displayName");
				
				ScimPersonEmails email = new ScimPersonEmails();
				email.setPrimary("email_primary");
				email.setType("email_type");
				email.setValue("email_value");
				List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();
				emails.add(email);
				source.setEmails(emails);

				ScimEntitlements entitlement = new ScimEntitlements();
				entitlement.setValue("entitlement_value");
				List<ScimEntitlements> entitlements = new ArrayList<ScimEntitlements>();
				entitlements.add(entitlement);
				source.setEntitlements(entitlements);
				
				source.setExternalId("externalId");

				ScimPersonGroups group = new ScimPersonGroups();
				group.setDisplay("group_display");
				group.setValue("group_value");
				List<ScimPersonGroups> groups = new ArrayList<ScimPersonGroups>();
				groups.add(group);
				source.setGroups(groups);
				
				source.setId("id");
				
				ScimPersonIms personims = new ScimPersonIms();
				personims.setType("ims_type");
				personims.setValue("ims_value");
				List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
				ims.add(personims);
				source.setIms(ims);
				
				source.setLocale("locale");
				
				PersonMeta meta = new PersonMeta();
				meta.setCreated("created");
				meta.setLastModified("lastModified");
				meta.setLocation("location");
				meta.setVersion("version");
				source.setMeta(meta);
				
				ScimName name = new ScimName();
				name.setFamilyName("familyName");
				name.setGivenName("givenName");
				name.setHonorificPrefix("honorificPrefix");
				name.setHonorificSuffix("honorificSuffix");
				name.setMiddleName("middleName");
				source.setName(name);
				
				source.setNickName("nickName");
				source.setPassword("password");
				
				ScimPersonPhones phonenumber = new ScimPersonPhones();
				phonenumber.setType("phone_type");
				phonenumber.setValue("phone_value");
				List<ScimPersonPhones> phoneNumbers = new ArrayList<ScimPersonPhones>();
				phoneNumbers.add(phonenumber);
				source.setPhoneNumbers(phoneNumbers);
				
				ScimPersonPhotos photo= new ScimPersonPhotos();
				photo.setType("photo_type");
				photo.setValue("photo_value");
				List<ScimPersonPhotos> photos = new ArrayList<ScimPersonPhotos>();
				photos.add(photo);
				source.setPhotos(photos);

				source.setPreferredLanguage("preferredLanguage");
				source.setProfileUrl("profileUrl");
				
				ScimRoles role = new ScimRoles();
				role.setValue("role_value");
				List<ScimRoles> roles = new ArrayList<ScimRoles>();
				roles.add(role);
				source.setRoles(roles);
				
				List<String> schemas = new ArrayList<String>();
				schemas.add("shema");
				source.setSchemas(schemas);
				
				source.setTimezone("timezone");
				source.setTitle("title");
				source.setUserName("exception");
				source.setUserType("userType");
				
				Scimx509Certificates cert = new Scimx509Certificates();
				cert.setValue("cert_value");
				List<Scimx509Certificates> x509Certificates = new ArrayList<Scimx509Certificates>();
				x509Certificates.add(cert);
				source.setX509Certificates(x509Certificates);
				GluuCustomPerson copy = CopyUtils.copy(source, destination, false);
				assertNull(copy);
			}

		}.run();
	}

}
