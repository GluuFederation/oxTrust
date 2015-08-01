/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to create a UpdateUser Object to update a existing User
 */
public final class UpdateUser {// NOSONAR - Builder constructs instances of this class

    private User user;
    
    private String userName;
    private String nickName;
    private String externalId;
    private String locale;
    private String password;
    private String preferredLanguage;
    private String profileUrl;
    private String timezone;
    private String title;
    private Name name;
    private String userType;
    private String displayName;
    private Boolean active;
    private Set<String> deleteFields = new HashSet<String>();
    private List<Email> emails = new ArrayList<Email>();
    private List<Im> ims = new ArrayList<Im>();
    private List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
    private List<Address> addresses = new ArrayList<Address>();
    private List<Entitlement> entitlements = new ArrayList<Entitlement>();
    private List<Photo> photos = new ArrayList<Photo>();
    private List<Role> roles = new ArrayList<Role>();
    private List<X509Certificate> certificates = new ArrayList<X509Certificate>();
    private Set<Extension> extensions = new HashSet<Extension>();
    private static final String DELETE = "delete";



    /**
     * the Scim conform User to be used to update a existing User
     * 
     * @return User to update
     */
    public User getScimConformUpdateUser() {
        return user;
    }

    public void setUser(User user) {
		this.user = user;
	}
    
    /**
     * updates the nickName of a existing user
     * 
     * @param userName
     *        the new user name
     * @return The builder itself
     */
    public void updateUserName(String userName) {
        this.userName = userName;
    }

    /**
     * adds a new address to the existing addresses of a existing user
     * 
     * @param address
     *        the new address
     * @return The builder itself
     */
    public void addAddress(Address address) {
        addresses.add(address);
    }

    /**
     * deletes the given address from the list of existing addresses of a existing user
     * 
     * @param address
     *        address to be deleted
     * @return The builder itself
     */
    public void deleteAddress(Address address) {
        Address deleteAddress = new Address();
        deleteAddress.setOperation(DELETE);
        addresses.add(deleteAddress);
    }

    /**
     * deletes all existing addresses of the a existing user
     * 
     * @return The builder itself
     */
    public void deleteAddresses() {
        deleteFields.add("addresses");
    }

    /**
     * updates the old Address with the new one
     * 
     * @param oldAddress
     *        to be replaced
     * @param newAddress
     *        new Address
     * @return The builder itself
     */
    public void updateAddress(Address oldAddress, Address newAddress) {
        deleteAddress(oldAddress);
        addAddress(newAddress);
    }

    /**
     * deletes the nickName of a existing user
     * 
     * @return The builder itself
     */
    public void deleteNickName() {
        deleteFields.add(User_.nickName.getName());
    }

    /**
     * updates the nickName of a existing user
     * 
     * @param nickName
     *        the new nickName
     * @return The builder itself
     */
    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * delete the external Id of a existing user
     * 
     * @return The builder itself
     */
    public void deleteExternalId() {
        deleteFields.add(User_.externalId.getName());
    }

    /**
     * updates the external id of a existing user
     * 
     * @param externalId
     *        new external id
     * @return The builder itself
     */
    public void updateExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * delete the local value of a existing user
     * 
     * @return The builder itself
     */
    public void deleteLocal() {
        deleteFields.add(User_.locale.getName());
    }

    /**
     * updates the local of a existing user
     * 
     * @param locale
     *        new local
     * @return The builder itself
     */
    public void updateLocale(String locale) {
        this.locale = locale;
    }

    /**
     * updates the password of a existing user
     * 
     * @param password
     *        new password
     * @return The builder itself
     */
    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * delete the preferred Language of a existing user
     * 
     * @return The builder itself
     */
    public void deletePreferredLanguage() {
        deleteFields.add(User_.preferredLanguage.getName());
    }

    /**
     * updates the preferred language of a existing user
     * 
     * @param preferredLanguage
     *        new preferred language
     * @return The builder itself
     */
    public void updatePreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /**
     * deletes the profil Url of a existing user
     * 
     * @return The builder itself
     */
    public void deleteProfileUrl() {
        deleteFields.add(User_.profileUrl.getName());
    }

    /**
     * updates the profil URL of a existing user
     * 
     * @param profileUrl
     *        new profilUrl
     * @return The builder itself
     */
    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    /**
     * deletes the timezone of a existing user
     * 
     * @return The builder itself
     */
    public void deleteTimezone() {
        deleteFields.add(User_.timezone.getName());
    }

    /**
     * updates the timezone of a existing user
     * 
     * @param timezone
     *        new timeZone
     * @return The builder itself
     */
    public void updateTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * deletes the title of a existing user
     * 
     * @return The builder itself
     */
    public void deleteTitle() {
        deleteFields.add(User_.title.getName());
    }

    /**
     * updates the title of a existing user
     * 
     * @param title
     *        new tile
     * @return The builder itself
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * deletes the name of a existing user
     * 
     * @return The builder itself
     */
    public void deleteName() {
        deleteFields.add("name");
    }

    /**
     * updates the name of a existing user
     * 
     * @param name
     *        new Name
     * @return The builder itself
     */
    public void updateName(Name name) {
        this.name = name;
    }

    /**
     * deletes the user type of a existing user
     * 
     * @return The builder itself
     */
    public void deleteUserType() {
        deleteFields.add(User_.userType.getName());
    }

    /**
     * updates the user type of a existing user
     * 
     * @param userType
     *        new user type
     * @return The builder itself
     */
    public void updateUserType(String userType) {
        this.userType = userType;
    }

    /**
     * deletes the display name of a existing user
     * 
     * @return The builder itself
     */
    public void deleteDisplayName() {
        deleteFields.add(User_.displayName.getName());
    }

    /**
     * updates the display name of a existing user
     * 
     * @param displayName
     *        new display name
     * @return The builder itself
     */
    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * deletes all emails of a existing user
     * 
     * @return The builder itself
     */
    public void deleteEmails() {
        deleteFields.add("emails");
    }

    /**
     * deletes the given email of a existing user
     * 
     * @param email
     *        to be deleted
     * @return The builder itself
     */
    public void deleteEmail(Email email) {
        Email deleteEmail = new Email();
        deleteEmail.setValue(email.getValue());
        deleteEmail.setType(email.getType());
        deleteEmail.setOperation(DELETE);
        emails.add(deleteEmail);
    }

    /**
     * adds or updates a emil of an existing user if the .getValue() already exists a update will be done. If not a
     * new one will be added
     * 
     * @param email
     *        new email
     * @return The builder itself
     */
    public void addEmail(Email email) {
        emails.add(email);
    }

    /**
     * updates the old Email with the new one
     * 
     * @param oldEmail
     *        to be replaced
     * @param newEmail
     *        new Email
     * @return The builder itself
     */
    public void updateEmail(Email oldEmail, Email newEmail) {
        deleteEmail(oldEmail);
        addEmail(newEmail);
    }

    /**
     * deletes all X509Certificates of a existing user
     * 
     * @return The builder itself
     */
    public void deleteX509Certificates() {
        deleteFields.add("x509Certificates");
    }

    /**
     * deletes the given certificate of a existing user
     * 
     * @param certificate
     *        to be deleted
     * @return The builder itself
     */
    public void deleteX509Certificate(X509Certificate certificate) {
        X509Certificate deleteCertificates = new X509Certificate();
        deleteCertificates.setValue(certificate.getValue());
        deleteCertificates.setOperation(DELETE);
        certificates.add(deleteCertificates);
    }

    /**
     * adds or updates certificate to an existing user if the .getValue() already exists a update will be done. If
     * not a new one will be added
     * 
     * @param certificate
     *        new certificate
     * @return The builder itself
     */
    public void addX509Certificate(X509Certificate certificate) {
        certificates.add(certificate);
    }

    /**
     * updates the old X509Certificate with the new one
     * 
     * @param oldCertificate
     *        to be replaced
     * @param newCertificate
     *        new X509Certificate
     * @return The builder itself
     */
    public void updateX509Certificate(X509Certificate oldCertificate, X509Certificate newCertificate) {
        deleteX509Certificate(oldCertificate);
        addX509Certificate(newCertificate);
    }

    /**
     * deletes all roles of a existing user
     * 
     * @return The builder itself
     */
    public void deleteRoles() {
        deleteFields.add("roles");
    }

    /**
     * deletes the given role of a existing user
     * 
     * @param role
     *        to be deleted
     * @return The builder itself
     */
    public void deleteRole(Role role) {
        Role deleteRole = new Role();
        deleteRole.setValue(role.getValue());
        deleteRole.setOperation(DELETE);
        roles.add(deleteRole);
    }

    /**
     * deletes the given role of a existing user
     * 
     * @param role
     *        to be deleted
     * @return The builder itself
     */
    public void deleteRole(String role) {
    	Role deleteRole = new Role();
    	deleteRole.setValue(role);
        deleteRole.setOperation(DELETE);
        roles.add(deleteRole);
    }

    /**
     * adds or updates a role of an existing user if the .getValue() already exists a update will be done. If not a
     * new one will be added
     * 
     * @param role
     *        new role
     * @return The builder itself
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     * updates the old Role with the new one
     * 
     * @param oldRole
     *        to be replaced
     * @param newRole
     *        new Role
     * @return The builder itself
     */
    public void updateRole(Role oldRole, Role newRole) {
        deleteRole(oldRole);
        addRole(newRole);
    }

    /**
     * deletes all ims of a existing user
     * 
     * @return The builder itself
     */
    public void deleteIms() {
        deleteFields.add("ims");
    }

    /**
     * deletes the ims of a existing user
     * 
     * @param im
     *        to be deleted
     * @return The builder itself
     */
    public void deleteIm(Im im) {
        Im deleteIms = new Im();
        deleteIms.setValue(im.getValue());
        deleteIms.setType(im.getType());
        deleteIms.setOperation(DELETE);
        this.ims.add(deleteIms);
    }

    /**
     * adds or updates a ims to an existing user if the .getValue() already exists a update will be done. If not a
     * new one will be added
     * 
     * @param im
     *        new ims
     * @return The builder itself
     */
    public void addIm(Im im) {
        this.ims.add(im);
    }

    /**
     * updates the old Ims with the new one
     * 
     * @param oldIm
     *        to be replaced
     * @param newIm
     *        new Ims
     * @return The builder itself
     */
    public void updateIm(Im oldIm, Im newIm) {
        deleteIm(oldIm);
        addIm(newIm);
    }

    /**
     * adds or updates a phoneNumber to an existing user if the .getValue() already exists a update will be done. If
     * not a new one will be added
     * 
     * @param phoneNumber
     *        new phoneNumber
     * @return The builder itself
     */
    public void addPhoneNumber(PhoneNumber phoneNumber) {
        phoneNumbers.add(phoneNumber);
    }

    /**
     * deletes the phonenumber of a existing user
     * 
     * @param phoneNumber
     *        to be deleted
     * @return The builder itself
     */
    public void deletePhoneNumber(PhoneNumber phoneNumber) {
        PhoneNumber deletePhoneNumber = new PhoneNumber();
        deletePhoneNumber.setValue(phoneNumber.getValue());
        deletePhoneNumber.setType(phoneNumber.getType());
        deletePhoneNumber.setOperation(DELETE);
        phoneNumbers.add(deletePhoneNumber);
    }

    /**
     * deletes all phonenumbers of a existing user
     * 
     * @return The builder itself
     */
    public void deletePhoneNumbers() {
        deleteFields.add("phoneNumbers");
    }

    /**
     * updates the old PhoneNumber with the new one
     * 
     * @param oldPhoneNumber
     *        to be replaced
     * @param newPhoneNumber
     *        new PhoneNumber
     * @return The builder itself
     */
    public void updatePhoneNumber(PhoneNumber oldPhoneNumber, PhoneNumber newPhoneNumber) {
        deletePhoneNumber(oldPhoneNumber);
        addPhoneNumber(newPhoneNumber);
    }

    /**
     * adds or updates a photo to an existing user if the .getValue() already exists a update will be done. If not a
     * new one will be added
     * 
     * @param photo
     *        new photo
     * @return The builder itself
     */
    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    /**
     * deletes the photo of a existing user
     * 
     * @param photo
     *        to be deleted
     * @return The builder itself
     */
    public void deletePhoto(Photo photo) {
        Photo deletePhoto = new Photo();
        deletePhoto.setValue(photo.getValueAsURI());
        deletePhoto.setType(photo.getType());
        deletePhoto.setOperation(DELETE);
        photos.add(deletePhoto);
    }

    /**
     * deletes all photos of a existing user
     * 
     * @return The builder itself
     */
    public void deletePhotos() {
        deleteFields.add("photos");
    }

    /**
     * updates the old Photo with the new one
     * 
     * @param oldPhoto
     *        to be replaced
     * @param newPhoto
     *        new Photo
     * @return The builder itself
     */
    public void updatePhoto(Photo oldPhoto, Photo newPhoto) {
        deletePhoto(oldPhoto);
        addPhoto(newPhoto);
    }

    /**
     * deletes all entitlements of a existing user
     * 
     * @return The builder itself
     */
    public void deleteEntitlements() {
        deleteFields.add("entitlements");
    }

    /**
     * deletes the entitlement of a existing user
     * 
     * @param entitlement
     *        to be deleted
     * @return The builder itself
     */
    public void deleteEntitlement(Entitlement entitlement) {
        Entitlement deleteEntitlement = new Entitlement();
        deleteEntitlement.setValue(entitlement.getValue());
        deleteEntitlement.setType(entitlement.getType());
        deleteEntitlement.setOperation(DELETE);
        entitlements.add(deleteEntitlement);
    }

    /**
     * adds or updates a entitlement to an existing user if the .getValue() already exists a update will be done. If
     * not a new one will be added
     * 
     * @param entitlement
     *        new entitlement
     * @return The builder itself
     */
    public void addEntitlement(Entitlement entitlement) {
        entitlements.add(entitlement);
    }

    /**
     * updates the old Entitlement with the new one
     * 
     * @param oldEntitlement
     *        to be replaced
     * @param newEntitlement
     *        new Entitlement
     * @return The builder itself
     */
    public void updateEntitlement(Entitlement oldEntitlement, Entitlement newEntitlement) {
        deleteEntitlement(oldEntitlement);
        addEntitlement(newEntitlement);
    }

    /**
     * updates the active status of a existing User to the given value
     * 
     * @param active
     *        new active status
     * @return The builder itself
     */
    public void updateActive(boolean active) {
        this.active = active;
    }

    /**
     * deletes the given extension of a existing user
     * 
     * @param urn
     *        the id of the extension to be deleted
     * @return The builder itself
     */
    public void deleteExtension(String urn) {
        deleteFields.add(urn);
    }

    /**
     * deletes the given extension field of a existing user
     * 
     * @param urn
     *        the id of the extension to be deleted
     * @param fieldName
     *        the fieldName of a the extension to be deleted
     * @return The builder itself
     */
    public void deleteExtensionField(String urn, String fieldName) {
        deleteFields.add(urn + "." + fieldName);
    }

    /**
     * updates the given fields in the extension. If the User doesn't have the given extension fields, they will be
     * added.
     * 
     * @param extension
     *        extension with all fields that need to be updated or added
     * @return The builder itself
     */
    public void updateExtension(Extension extension) {
        extensions.add(extension);
    }

	
}