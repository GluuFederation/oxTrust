/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.scim2.schema.strategy;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.schema.SchemaType;
import org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeUserSerializer;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Loading strategy for the User Core schema.
 *
 * @author Val Pecaoco
 */
@Name("userCoreLoadingStrategy")
public class UserCoreLoadingStrategy implements LoadingStrategy {

    @Logger
    private static Log log;

    @Override
    public SchemaType load(SchemaType schemaType) throws Exception {

        log.info(" load() ");

        // Use serializer to walk the class structure
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        SimpleModule userCoreLoadingStrategyFilterModule = new SimpleModule("UserCoreLoadingStrategyFilterModule", new Version(1, 0, 0, ""));
        SchemaTypeUserSerializer serializer = new SchemaTypeUserSerializer();
        serializer.setSchemaType(schemaType);
        userCoreLoadingStrategyFilterModule.addSerializer(User.class, serializer);
        mapper.registerModule(userCoreLoadingStrategyFilterModule);

        mapper.writeValueAsString(createDummyUser());

        return serializer.getSchemaType();
    }

    private User createDummyUser() {

        User user = new User();

        org.gluu.oxtrust.model.scim2.Name name = new org.gluu.oxtrust.model.scim2.Name();
        name.setGivenName("");
        name.setMiddleName("");
        name.setFamilyName("");
        name.setHonorificPrefix("");
        name.setHonorificSuffix("");
        user.setName(name);

        user.setActive(false);

        user.setId("");
        user.setExternalId("");

        user.setUserName("");
        user.setPassword("");
        user.setDisplayName("");
        user.setNickName("");
        user.setProfileUrl("");
        user.setLocale("");
        user.setPreferredLanguage("");
        user.setTimezone("");
        user.setTitle("");

        List<GroupRef> groups = new ArrayList<GroupRef>();
        GroupRef groupRef = new GroupRef();
        groupRef.setOperation("");
        groupRef.setPrimary(false);
        groupRef.setValue("test");
        groupRef.setDisplay("");
        groupRef.setType(GroupRef.Type.DIRECT);
        groupRef.setReference("");
        groups.add(groupRef);
        user.setGroups(groups);

        List<Email> emails = new ArrayList<Email>();
        Email email = new Email();
        email.setOperation("");
        email.setPrimary(false);
        email.setValue("a@b.com");
        email.setDisplay("");
        email.setType(Email.Type.WORK);
        email.setReference("");
        emails.add(email);
        user.setEmails(emails);

        List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setOperation("");
        phoneNumber.setPrimary(false);
        phoneNumber.setValue("123-456-7890");
        phoneNumber.setDisplay("");
        phoneNumber.setType(PhoneNumber.Type.WORK);
        phoneNumber.setReference("");
        phoneNumbers.add(phoneNumber);
        user.setPhoneNumbers(phoneNumbers);

        List<Im> ims = new ArrayList<Im>();
        Im im = new Im();
        im.setOperation("");
        im.setPrimary(false);
        im.setValue("test");
        im.setDisplay("");
        im.setType(Im.Type.SKYPE);
        im.setReference("");
        ims.add(im);
        user.setIms(ims);

        List<Photo> photos = new ArrayList<Photo>();
        Photo photo = new Photo();
        photo.setOperation("");
        photo.setPrimary(false);
        photo.setValue("data:image/jpg;charset=utf-8;base64,dGVzdA==");
        photo.setDisplay("");
        photo.setType(Photo.Type.PHOTO);
        photo.setReference("");
        photos.add(photo);
        user.setPhotos(photos);

        List<Address> addresses = new ArrayList<Address>();
        Address address = new Address();
        address.setOperation("");
        address.setPrimary(false);
        address.setValue("test");
        address.setDisplay("");
        address.setType(Address.Type.WORK);
        address.setReference("");
        address.setStreetAddress("");
        address.setLocality("");
        address.setPostalCode("");
        address.setRegion("");
        address.setCountry("");
        address.setFormatted("");
        addresses.add(address);
        user.setAddresses(addresses);

        List<Entitlement> entitlements = new ArrayList<Entitlement>();
        Entitlement entitlement = new Entitlement();
        entitlement.setOperation("");
        entitlement.setPrimary(false);
        entitlement.setValue("test");
        entitlement.setDisplay("");
        entitlement.setType(new Entitlement.Type("test"));
        entitlement.setReference("");
        entitlements.add(entitlement);
        user.setEntitlements(entitlements);

        List<Role> roles = new ArrayList<Role>();
        Role role = new Role();
        role.setOperation("");
        role.setPrimary(false);
        role.setValue("test");
        role.setDisplay("");
        role.setType(new Role.Type("test"));
        role.setReference("");
        roles.add(role);
        user.setRoles(roles);

        List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
        X509Certificate x509Certificate = new X509Certificate();
        x509Certificate.setOperation("");
        x509Certificate.setPrimary(false);
        x509Certificate.setValue("test");
        x509Certificate.setDisplay("");
        x509Certificate.setType(new X509Certificate.Type("test"));
        x509Certificate.setReference("");
        x509Certificates.add(x509Certificate);
        user.setX509Certificates(x509Certificates);

        return user;
    }
}
