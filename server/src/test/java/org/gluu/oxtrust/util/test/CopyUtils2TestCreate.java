package org.gluu.oxtrust.util.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.gluu.oxtrust.action.test.ConfigurableTest;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim2.Address;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Email;
import org.gluu.oxtrust.model.scim2.Entitlement;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.GroupRef;
import org.gluu.oxtrust.model.scim2.Im;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.Name;
import org.gluu.oxtrust.model.scim2.PhoneNumber;
import org.gluu.oxtrust.model.scim2.Photo;
import org.gluu.oxtrust.model.scim2.Role;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.X509Certificate;
import org.gluu.oxtrust.util.CopyUtils2;
import org.testng.annotations.Test;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuStatus;

import com.unboundid.util.StaticUtils;

public class CopyUtils2TestCreate extends ConfigurableTest {

	private static final String GLUU_STATUS = "gluuStatus";
	private static final String OX_TRUST_PHOTOS_Type = "oxTrustPhotosType";
	private static final String OX_TRUST_PHONE_Type = "oxTrustPhoneType";
	private static final String OX_TRUST_ADDRESS_PRIMARY = "oxTrustAddressPrimary";
	private static final String OX_TRUST_ADDRESS_Type = "oxTrustAddressType";
	private static final String OX_TRUST_COUNTRY = "oxTrustCountry";
	private static final String OX_TRUST_POSTAL_CODE = "oxTrustPostalCode";
	private static final String OX_TRUST_REGION = "oxTrustRegion";
	private static final String OX_TRUST_LOCALITY = "oxTrustLocality";
	private static final String OX_TRUST_ADDRESS_FORMATTED = "oxTrustAddressFormatted";
	private static final String OX_TRUST_STREET = "oxTrustStreet";
	private static final String OX_TRUST_EMAIL_PRIMARY = "oxTrustEmailPrimary";
	private static final String OX_TRUST_EMAIL_Type = "oxTrustEmailType";
	private static final String OX_TRUST_META_LOCATION = "oxTrustMetaLocation";
	private static final String OX_TRUST_META_VERSION = "oxTrustMetaVersion";
	private static final String OX_TRUST_META_LAST_MODIFIED = "oxTrustMetaLastModified";
	private static final String OX_TRUST_META_CREATED = "oxTrustMetaCreated";
	private static final String OX_TRUSTX509_CERTIFICATE = "oxTrustx509Certificate";
	private static final String OX_TRUST_ENTITLEMENTS = "oxTrustEntitlements";
	private static final String OX_TRUST_ROLE = "oxTrustRole";
	private static final String OX_TRUST_ACTIVE = "oxTrustActive";
	private static final String OX_TRUST_LOCALE = "locale";
	private static final String OX_TRUST_TITLE = "oxTrustTitle";
	private static final String OX_TRUST_USER_Type = "oxTrustUserType";
	private static final String OX_TRUST_PHOTOS = "oxTrustPhotos";
	private static final String OX_TRUST_IMS_VALUE = "oxTrustImsValue";
	private static final String OX_TRUST_PHONE_VALUE = "oxTrustPhoneValue";
	private static final String OX_TRUST_ADDRESSES = "oxTrustAddresses";
	private static final String OX_TRUST_EMAIL = "oxTrustEmail";
	private static final String OX_TRUST_PROFILE_URL = "oxTrustProfileURL";
	private static final String OX_TRUST_NICK_NAME = "nickname";
	private static final String OX_TRUST_EXTERNAL_ID = "oxTrustExternalId";
	private static final String OX_TRUSTHONORIFIC_SUFFIX = "oxTrusthonorificSuffix";
	private static final String OX_TRUSTHONORIFIC_PREFIX = "oxTrusthonorificPrefix";
	private static final String OX_TRUST_MIDDLE_NAME = "middleName";

	@Inject
	private CopyUtils2 copyUtils;

	@Inject
	private AppConfiguration appConfiguration;

	@Test
	public void testCopyScim2EmptyCreate() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = new User();
		GluuCustomPerson copy = null;
		try {
			copy = copyUtils.copy(source, destination, false);
		} catch (PersonRequiredFieldsException ex) {}
		assertNull(copy);
	}

	@Test
	public void testCopyScim2FilledCreate() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = new User();

		source.setActive(true);
		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setField("custom_name", "20170731150544.790Z");
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group = new GroupRef();
		group.setDisplay("group_display");
		group.setValue("group_value");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("userName");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);

		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
		assertEquals(copy.getUid(), "userName");
		assertEquals(copy.getGivenName(), "givenName");
		assertEquals(copy.getSurname(), "familyName");
		assertEquals(copy.getDisplayName(), "displayName");
		assertEquals(copy.getPreferredLanguage(), "preferredLanguage");
		assertEquals(copy.getTimezone(), "timezone");
		assertEquals(copy.getUserPassword(), "password");
		assertNotNull(copy.getMemberOf());
		assertEquals(copy.getMemberOf().size(), 1);
		assertEquals(copy.getMemberOf().get(0), String.format("inum=group_value,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));

		assertEquals(copy.getAttribute(GLUU_STATUS), "active");
		assertNull(copy.getAttribute(OX_TRUST_PHOTOS_Type));
		assertNull(copy.getAttribute(OX_TRUST_PHONE_Type));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_Type));
		assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
		assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
		assertNull(copy.getAttribute(OX_TRUST_REGION));
		assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
		assertNull(copy.getAttribute(OX_TRUST_STREET));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_Type));
		assertNull(copy.getAttribute(OX_TRUST_META_LOCATION));
		assertNull(copy.getAttribute(OX_TRUST_META_VERSION));
		assertNull(copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
		assertNull(copy.getAttribute(OX_TRUST_META_CREATED));
		assertEquals(copy.getAttribute(OX_TRUSTX509_CERTIFICATE), "{\"operation\":null,\"value\":\"cert_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ENTITLEMENTS), "{\"operation\":null,\"value\":\"entitlement_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ROLE), "{\"operation\":null,\"value\":\"role_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ACTIVE), "true");
		assertEquals(copy.getAttribute(OX_TRUST_LOCALE), "locale");
		assertEquals(copy.getAttribute(OX_TRUST_TITLE), "title");
		assertEquals(copy.getAttribute(OX_TRUST_USER_Type), "userType");
		assertEquals(copy.getAttribute(OX_TRUST_PHOTOS), "{\"operation\":null,\"value\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"display\":null,\"primary\":false,\"type\":\"photo\",\"valueAsImageDataURI\":{\"asURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"mimeType\":\"image/png\",\"asInputStream\":{}},\"valueType\":\"IMAGE_DATA_URI\",\"valueAsURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\"}");
		assertEquals(copy.getAttribute(OX_TRUST_IMS_VALUE), "{\"operation\":null,\"value\":\"ims_value\",\"display\":null,\"primary\":false,\"type\":\"skype\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PHONE_VALUE), "{\"operation\":null,\"value\":\"phone_value\",\"display\":null,\"primary\":false,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_ADDRESSES),
				"{\"operation\":null,\"primary\":true,\"formatted\":\"formatted\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_EMAIL),
				"{\"operation\":null,\"value\":\"email_value@gluu.org\",\"display\":null,\"primary\":true,\"reference\":null,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PROFILE_URL), "profileUrl");
		assertEquals(copy.getAttribute(OX_TRUST_NICK_NAME), "nickName");
		assertEquals(copy.getAttribute(OX_TRUST_EXTERNAL_ID), "externalId");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX), "honorificSuffix");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX), "honorificPrefix");
		assertEquals(copy.getAttribute(OX_TRUST_MIDDLE_NAME), "middleName");
		assertNull(copy.getAssociatedClient());
		assertNull(copy.getBaseDn());
		assertNull(copy.getCommonName());
		List<GluuCustomAttribute> customAttributes2 = copy.getCustomAttributes();
		assertNotNull(customAttributes2);
		assertEquals(customAttributes2.size(), 28);

		assertEquals(copy.getDisplayName(), "displayName");
		assertNull(copy.getDn());
		assertEquals(copy.getGivenName(), "givenName");
		assertNull(copy.getGluuAllowPublication());
		GluuCustomAttribute gluuCustomAttribute = copy.getGluuCustomAttribute("custom_name");
		assertNotNull(gluuCustomAttribute);
		assertEquals(gluuCustomAttribute.getName(), "custom_name");
		assertEquals(gluuCustomAttribute.getValue(), "20170731150544.790Z");
		assertEquals(gluuCustomAttribute.getDate(), StaticUtils.decodeGeneralizedTime("20170731150544.790Z"));
		assertEquals(gluuCustomAttribute.getDisplayValue(), "20170731150544.790Z");
		assertNull(gluuCustomAttribute.getMetadata());
		assertEquals(gluuCustomAttribute.getValues()[0], "20170731150544.790Z");
		assertNull(copy.getGluuOptOuts());
		assertNull(copy.getIname());
		assertNull(copy.getMail());
		assertEquals(copy.getMemberOf().get(0), String.format("inum=group_value,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));
		assertNull(copy.getNetworkPoken());
		assertNull(copy.getCreationDate());
		assertEquals(copy.getPreferredLanguage(), "preferredLanguage");
		assertNull(copy.getSLAManager());
		assertNull(copy.getSourceServerName());
		assertEquals(copy.getStatus(), GluuStatus.ACTIVE);
		assertEquals(copy.getSurname(), "familyName");
		assertEquals(copy.getTimezone(), "timezone");
		assertEquals(copy.getUid(), "userName");
		assertEquals(copy.getUserPassword(), "password");
	}

	@Test
	public void testCopyScim2FilledCreateExisting() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();

		destination.setAllowPublication(true);
		List<String> associatedClientDNs = new ArrayList<String>();
		associatedClientDNs.add("a");
		associatedClientDNs.add("b");
		associatedClientDNs.add("c");
		destination.setAssociatedClient(associatedClientDNs);
		destination.setBaseDn("dn");
		destination.setAttribute(OX_TRUST_NICK_NAME, "original nickname");
		destination.setAttribute(OX_TRUST_PROFILE_URL, "original url");
		destination.setCommonName("CN");
		destination.setGivenName("original givenname");
		destination.setPreferredLanguage("Nederlands");

		destination.setAttribute(OX_TRUST_ENTITLEMENTS, "[{\"value\":\"original entitlement_value\"}]");

		User source = new User();

		source.setActive(true);

		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setField("custom_name", "20170731150544.790Z");
        extensionBuilder.setField("custom_name2", "20170731150544.790Z");
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group = new GroupRef();
		group.setDisplay("group_display");
		group.setValue("group_value");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl(null);

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("existing");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);

		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
	}

	@Test
	public void testCopyScim2FilledMultipleAttributesCreate() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = new User();

		source.setActive(true);

		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setFieldAsList("custom_name",  Arrays.asList(new String[]{ "value1", "value2", "value3", "value4" }));
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group1 = new GroupRef();
		group1.setDisplay("group_display");
		group1.setValue("group_value");
		GroupRef group2 = new GroupRef();
		group2.setDisplay("group_display1");
		group2.setValue("group_value1");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group1);
		groups.add(group2);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("userName");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);

		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
		assertEquals(copy.getUid(), "userName");
		assertEquals(copy.getGivenName(), "givenName");
		assertEquals(copy.getSurname(), "familyName");
		assertEquals(copy.getDisplayName(), "displayName");
		assertEquals(copy.getPreferredLanguage(), "preferredLanguage");
		assertEquals(copy.getTimezone(), "timezone");
		assertEquals(copy.getUserPassword(), "password");
		assertNotNull(copy.getMemberOf());
		assertEquals(copy.getMemberOf().size(), 2);
		assertEquals(copy.getMemberOf().get(0), String.format("inum=group_value,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));
		assertEquals(copy.getMemberOf().get(1), String.format("inum=group_value1,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));

		assertEquals(copy.getAttribute(GLUU_STATUS), "active");

		assertNull(copy.getAttribute(OX_TRUST_PHOTOS_Type));
		assertNull(copy.getAttribute(OX_TRUST_PHONE_Type));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_Type));
		assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
		assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
		assertNull(copy.getAttribute(OX_TRUST_REGION));
		assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
		assertNull(copy.getAttribute(OX_TRUST_STREET));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_Type));
		assertNull(copy.getAttribute(OX_TRUST_META_LOCATION));
		assertNull(copy.getAttribute(OX_TRUST_META_VERSION));
		assertNull(copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
		assertNull(copy.getAttribute(OX_TRUST_META_CREATED));
		assertEquals(copy.getAttribute(OX_TRUSTX509_CERTIFICATE), "{\"operation\":null,\"value\":\"cert_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ENTITLEMENTS), "{\"operation\":null,\"value\":\"entitlement_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ROLE), "{\"operation\":null,\"value\":\"role_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ACTIVE), "true");
		assertEquals(copy.getAttribute(OX_TRUST_LOCALE), "locale");
		assertEquals(copy.getAttribute(OX_TRUST_TITLE), "title");
		assertEquals(copy.getAttribute(OX_TRUST_USER_Type), "userType");
		assertEquals(copy.getAttribute(OX_TRUST_PHOTOS), "{\"operation\":null,\"value\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"display\":null,\"primary\":false,\"type\":\"photo\",\"valueAsImageDataURI\":{\"asURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"mimeType\":\"image/png\",\"asInputStream\":{}},\"valueType\":\"IMAGE_DATA_URI\",\"valueAsURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\"}");
		assertEquals(copy.getAttribute(OX_TRUST_IMS_VALUE), "{\"operation\":null,\"value\":\"ims_value\",\"display\":null,\"primary\":false,\"type\":\"skype\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PHONE_VALUE), "{\"operation\":null,\"value\":\"phone_value\",\"display\":null,\"primary\":false,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_ADDRESSES),
				"{\"operation\":null,\"primary\":true,\"formatted\":\"formatted\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_EMAIL),
				"{\"operation\":null,\"value\":\"email_value@gluu.org\",\"display\":null,\"primary\":true,\"reference\":null,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PROFILE_URL), "profileUrl");
		assertEquals(copy.getAttribute(OX_TRUST_NICK_NAME), "nickName");
		assertEquals(copy.getAttribute(OX_TRUST_EXTERNAL_ID), "externalId");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX), "honorificSuffix");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX), "honorificPrefix");
		assertEquals(copy.getAttribute(OX_TRUST_MIDDLE_NAME), "middleName");
	}

	@Test
	public void testCopyScim2FilledNullCustomAttributesCreate() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = new User();

		source.setActive(true);

		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group1 = new GroupRef();
		group1.setDisplay("group_display");
		group1.setValue("group_value");
		GroupRef group2 = new GroupRef();
		group2.setDisplay("group_display1");
		group2.setValue("group_value1");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group1);
		groups.add(group2);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("userName");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);

		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
		assertEquals(copy.getUid(), "userName");
		assertEquals(copy.getGivenName(), "givenName");
		assertEquals(copy.getSurname(), "familyName");
		assertEquals(copy.getDisplayName(), "displayName");
		assertEquals(copy.getPreferredLanguage(), "preferredLanguage");
		assertEquals(copy.getTimezone(), "timezone");
		assertEquals(copy.getUserPassword(), "password");
		assertNotNull(copy.getMemberOf());
		assertEquals(copy.getMemberOf().size(), 2);
		assertEquals(copy.getMemberOf().get(0), String.format("inum=group_value,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));
		assertEquals(copy.getMemberOf().get(1), String.format("inum=group_value1,ou=groups,o=%s,o=gluu", appConfiguration.getOrgInum()));

		assertEquals(copy.getAttribute(GLUU_STATUS), "active");
		assertNull(copy.getAttribute(OX_TRUST_PHOTOS_Type));
		assertNull(copy.getAttribute(OX_TRUST_PHONE_Type));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_Type));
		assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
		assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
		assertNull(copy.getAttribute(OX_TRUST_REGION));
		assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
		assertNull(copy.getAttribute(OX_TRUST_STREET));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_Type));
		assertNull(copy.getAttribute(OX_TRUST_META_LOCATION));
		assertNull(copy.getAttribute(OX_TRUST_META_VERSION));
		assertNull(copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
		assertNull(copy.getAttribute(OX_TRUST_META_CREATED));
		assertEquals(copy.getAttribute(OX_TRUSTX509_CERTIFICATE), "{\"operation\":null,\"value\":\"cert_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ENTITLEMENTS), "{\"operation\":null,\"value\":\"entitlement_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ROLE), "{\"operation\":null,\"value\":\"role_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ACTIVE), "true");
		assertEquals(copy.getAttribute(OX_TRUST_LOCALE), "locale");
		assertEquals(copy.getAttribute(OX_TRUST_TITLE), "title");
		assertEquals(copy.getAttribute(OX_TRUST_USER_Type), "userType");
		assertEquals(copy.getAttribute(OX_TRUST_PHOTOS), "{\"operation\":null,\"value\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"display\":null,\"primary\":false,\"type\":\"photo\",\"valueAsImageDataURI\":{\"asURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"mimeType\":\"image/png\",\"asInputStream\":{}},\"valueType\":\"IMAGE_DATA_URI\",\"valueAsURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\"}");
		assertEquals(copy.getAttribute(OX_TRUST_IMS_VALUE), "{\"operation\":null,\"value\":\"ims_value\",\"display\":null,\"primary\":false,\"type\":\"skype\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PHONE_VALUE), "{\"operation\":null,\"value\":\"phone_value\",\"display\":null,\"primary\":false,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_ADDRESSES),
				"{\"operation\":null,\"primary\":true,\"formatted\":\"formatted\",\"streetAddress\":\"streetAddress\",\"locality\":\"locality\",\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_EMAIL),
				"{\"operation\":null,\"value\":\"email_value@gluu.org\",\"display\":null,\"primary\":true,\"reference\":null,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PROFILE_URL), "profileUrl");
		assertEquals(copy.getAttribute(OX_TRUST_NICK_NAME), "nickName");
		assertEquals(copy.getAttribute(OX_TRUST_EXTERNAL_ID), "externalId");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX), "honorificSuffix");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX), "honorificPrefix");
		assertEquals(copy.getAttribute(OX_TRUST_MIDDLE_NAME), "middleName");
	}

	@Test
	public void testCopyScim2MixedCreate() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = new User();

		source.setActive(false);

		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setLocality(null);
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		List<GroupRef> groups = new ArrayList<GroupRef>();
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(null);
		meta.setLastModified(null);
		meta.setLocation("");
		meta.setVersion("");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("");
		source.setTitle("title");
		source.setUserName("userName");
		source.setUserType("userType");

		source.setX509Certificates(null);

		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
		assertEquals(copy.getUid(), "userName");
		assertEquals(copy.getGivenName(), "givenName");
		assertEquals(copy.getSurname(), "familyName");
		assertEquals(copy.getDisplayName(), "displayName");
		assertNull(copy.getPreferredLanguage());
		assertNull(copy.getTimezone());
		assertEquals(copy.getUserPassword(), "password");
		assertNotNull(copy.getMemberOf());
		assertEquals(copy.getMemberOf().size(), 0);

		assertEquals(copy.getAttribute(GLUU_STATUS), "inactive");

		assertNull(copy.getAttribute(OX_TRUST_PHOTOS_Type));
		assertNull(copy.getAttribute(OX_TRUST_PHONE_Type));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_Type));
		assertNull(copy.getAttribute(OX_TRUST_COUNTRY));
		assertNull(copy.getAttribute(OX_TRUST_POSTAL_CODE));
		assertNull(copy.getAttribute(OX_TRUST_REGION));
		assertNull(copy.getAttribute(OX_TRUST_LOCALITY));
		assertNull(copy.getAttribute(OX_TRUST_ADDRESS_FORMATTED));
		assertNull(copy.getAttribute(OX_TRUST_STREET));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_PRIMARY));
		assertNull(copy.getAttribute(OX_TRUST_EMAIL_Type));
		assertNull(copy.getAttribute(OX_TRUST_META_LOCATION));
		assertNull(copy.getAttribute(OX_TRUST_META_VERSION));
		assertNull(copy.getAttribute(OX_TRUST_META_LAST_MODIFIED));
		assertNull(copy.getAttribute(OX_TRUST_META_CREATED));
		assertNull(copy.getAttribute(OX_TRUSTX509_CERTIFICATE));
		assertEquals(copy.getAttribute(OX_TRUST_ENTITLEMENTS), "{\"operation\":null,\"value\":\"entitlement_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ROLE), "{\"operation\":null,\"value\":\"role_value\",\"display\":null,\"primary\":false,\"type\":null}");
		assertEquals(copy.getAttribute(OX_TRUST_ACTIVE), "false");
		assertEquals(copy.getAttribute(OX_TRUST_LOCALE), "locale");
		assertEquals(copy.getAttribute(OX_TRUST_TITLE), "title");
		assertEquals(copy.getAttribute(OX_TRUST_USER_Type), "userType");
		assertEquals(copy.getAttribute(OX_TRUST_PHOTOS), "{\"operation\":null,\"value\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"display\":null,\"primary\":false,\"type\":\"photo\",\"valueAsImageDataURI\":{\"asURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\",\"mimeType\":\"image/png\",\"asInputStream\":{}},\"valueType\":\"IMAGE_DATA_URI\",\"valueAsURI\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC\"}");
		assertEquals(copy.getAttribute(OX_TRUST_IMS_VALUE), "{\"operation\":null,\"value\":\"ims_value\",\"display\":null,\"primary\":false,\"type\":\"skype\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PHONE_VALUE), "{\"operation\":null,\"value\":\"phone_value\",\"display\":null,\"primary\":false,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_ADDRESSES),
				"{\"operation\":null,\"primary\":true,\"formatted\":\"\",\"streetAddress\":\"streetAddress\",\"locality\":null,\"region\":\"region\",\"postalCode\":\"postalCode\",\"country\":\"country\",\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_EMAIL),
				"{\"operation\":null,\"value\":\"email_value@gluu.org\",\"display\":null,\"primary\":true,\"reference\":null,\"type\":\"work\"}");
		assertEquals(copy.getAttribute(OX_TRUST_PROFILE_URL), "profileUrl");
		assertEquals(copy.getAttribute(OX_TRUST_NICK_NAME), "nickName");
		assertEquals(copy.getAttribute(OX_TRUST_EXTERNAL_ID), "externalId");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_SUFFIX), "honorificSuffix");
		assertEquals(copy.getAttribute(OX_TRUSTHONORIFIC_PREFIX), "honorificPrefix");
		assertEquals(copy.getAttribute(OX_TRUST_MIDDLE_NAME), "middleName");
	}

	@Test
	public void testCopyScim2createNullSource() throws Exception {
		GluuCustomPerson destination = new GluuCustomPerson();
		User source = null;
		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNull(copy);
	}

	@Test
	public void testCopyScim2CreateNullDestination() throws Exception {
		GluuCustomPerson destination = null;
		User source = new User();

		source.setActive(true);
		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setField("custom_name", "20170731150544.790Z");
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group = new GroupRef();
		group.setDisplay("group_display");
		group.setValue("group_value");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("userName");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);
		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
	}

	@Test
	public void testCopyScim2CreateException() throws Exception {
		GluuCustomPerson destination = null;
		User source = new User();

		source.setActive(true);
		Address address = new Address();
		address.setCountry("country");
		address.setFormatted("formatted");
		address.setLocality("locality");
		address.setPostalCode("postalCode");
		address.setPrimary(true);
		address.setRegion("region");
		address.setStreetAddress("streetAddress");
		address.setType(Address.Type.WORK);
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);

		source.setAddresses(addresses);

		Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);
        extensionBuilder.setField("custom_name", "20170731150544.790Z");
        source.addExtension(extensionBuilder.build());

		source.setDisplayName("displayName");

		Email email = new Email();
		email.setPrimary(true);
		email.setType(Email.Type.WORK);
		email.setValue("email_value@gluu.org");
		List<Email> emails = new ArrayList<Email>();
		emails.add(email);
		source.setEmails(emails);

		Entitlement entitlement = new Entitlement();
		entitlement.setValue("entitlement_value");
		List<Entitlement> entitlements = new ArrayList<Entitlement>();
		entitlements.add(entitlement);
		source.setEntitlements(entitlements);

		source.setExternalId("externalId");

		GroupRef group = new GroupRef();
		group.setDisplay("group_display");
		group.setValue("group_value");
		List<GroupRef> groups = new ArrayList<GroupRef>();
		groups.add(group);
		source.setGroups(groups);

		source.setId("id");

		Im personims = new Im();
		personims.setType(Im.Type.SKYPE);
		personims.setValue("ims_value");
		List<Im> ims = new ArrayList<Im>();
		ims.add(personims);
		source.setIms(ims);

		source.setLocale("locale");

		Meta meta = new Meta();
		meta.setCreated(new Date());
		meta.setLastModified(new Date());
		meta.setLocation("location");
		meta.setVersion("version");
		source.setMeta(meta);

		Name name = new Name();
		name.setFamilyName("familyName");
		name.setGivenName("givenName");
		name.setHonorificPrefix("honorificPrefix");
		name.setHonorificSuffix("honorificSuffix");
		name.setMiddleName("middleName");
		source.setName(name);

		source.setNickName("nickName");
		source.setPassword("password");

		PhoneNumber phonenumber = new PhoneNumber();
		phonenumber.setType(PhoneNumber.Type.WORK);;
		phonenumber.setValue("phone_value");
		List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(phonenumber);
		source.setPhoneNumbers(phoneNumbers);

		Photo photo = new Photo();
		photo.setType(Photo.Type.PHOTO);
		photo.setValue("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0lEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6P9/AFGGFyjOXZtQAAAAAElFTkSuQmCC");
		List<Photo> photos = new ArrayList<Photo>();
		photos.add(photo);
		source.setPhotos(photos);

		source.setPreferredLanguage("preferredLanguage");
		source.setProfileUrl("profileUrl");

		Role role = new Role();
		role.setValue("role_value");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		source.setRoles(roles);

		Set<String> schemas = new HashSet<String>();
		schemas.add("shema");
		source.setSchemas(schemas);

		source.setTimezone("timezone");
		source.setTitle("title");
		source.setUserName("exception");
		source.setUserType("userType");

		X509Certificate cert = new X509Certificate();
		cert.setValue("cert_value");
		List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
		x509Certificates.add(cert);
		source.setX509Certificates(x509Certificates);
		GluuCustomPerson copy = copyUtils.copy(source, destination, false);
		assertNotNull(copy);
	}

}
