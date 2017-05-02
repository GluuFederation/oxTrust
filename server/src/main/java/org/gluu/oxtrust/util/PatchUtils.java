/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.Serializable;
import java.util.*;

import org.gluu.oxtrust.ldap.service.*;
import org.gluu.oxtrust.model.*;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.User;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.slf4j.Logger;

@Named("patchUtils")
public class PatchUtils implements Serializable {

	private static final long serialVersionUID = -1715995162448707004L;

	@Inject
	private Logger log;	
	
	public static GluuCustomPerson replacePatch(User source, GluuCustomPerson destination) throws Exception {

		if (source == null ) {
			return null;
		}

		IPersonService personService1 = PersonService.instance();

		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new GluuCustomPerson();
		}

		log.trace(" setting schemas ");
		destination.setSchemas(source.getSchemas());

			personService1.addCustomObjectClass(destination);

			log.trace(" setting userName ");
			if (source.getUserName() != null && source.getUserName().length() > 0) {
				destination.setUid(source.getUserName());
			}

			if (source.getName() != null) {

				log.trace(" setting givenname ");
				if (source.getName().getGivenName() != null && source.getName().getGivenName().length() > 0) {
					destination.setGivenName(source.getName().getGivenName());
				}
				log.trace(" setting famillyname ");
				if (source.getName().getFamilyName() != null && source.getName().getFamilyName().length() > 0) {
					destination.setSurname(source.getName().getFamilyName());
				}
				log.trace(" setting middlename ");
				if (source.getName().getMiddleName() != null && source.getName().getMiddleName().length() > 0) {
					// destination.setAttribute("oxTrustMiddleName", source.getName().getMiddleName());
					destination.setAttribute("middleName", source.getName().getMiddleName());
				}
				log.trace(" setting honor");
				if (source.getName().getHonorificPrefix() != null && source.getName().getHonorificPrefix().length() > 0) {
					destination.setAttribute("oxTrusthonorificPrefix", source.getName().getHonorificPrefix());
				}
				if (source.getName().getHonorificSuffix() != null && source.getName().getHonorificSuffix().length() > 0) {
					destination.setAttribute("oxTrusthonorificSuffix", source.getName().getHonorificSuffix());
				}
			}

			log.trace(" setting displayname ");
			if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
				destination.setDisplayName(source.getDisplayName());
			}
			log.trace(" setting externalID ");
			if (source.getExternalId() != null && source.getExternalId().length() > 0) {
				destination.setAttribute("oxTrustExternalId", source.getExternalId());
			}
			log.trace(" setting nickname ");
			if (source.getNickName() != null && source.getNickName().length() > 0) {
				// destination.setAttribute("oxTrustNickName", source.getNickName());
				destination.setAttribute("nickname", source.getNickName());
			}
			log.trace(" setting profileURL ");
			if (source.getProfileUrl() != null && source.getProfileUrl().length() > 0) {
				destination.setAttribute("oxTrustProfileURL", source.getProfileUrl());
			}

			// getting emails
			log.trace(" setting emails ");
			if (source.getEmails() != null && source.getEmails().size() > 0) {
				List<Email> emails = CopyUtils2.getAttributeListValue(destination, Email.class, "oxTrustEmail");
				if(emails != null){
				List<Email> newemails =source.getEmails();
				for(Email email : emails){
					if(email != null && email.getType()!= null){
					for(Email newEmail  : newemails){					
						if((newEmail.getType() != null) &&newEmail.getType().getValue().equals(email.getType().getValue())){
							emails.remove(email);
							emails.add(newEmail);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, emails, "oxTrustEmail");
			}
			}

			// getting addresses
			log.trace(" setting addresses ");
			if (source.getAddresses() != null && source.getAddresses().size() > 0) {
				List<Address> addresses = CopyUtils2.getAttributeListValue(destination, Address.class, "oxTrustAddresses");
				if(addresses!=null){
				List<Address> newaddresses =source.getAddresses();
				for(Address address : addresses){
					if(address!= null && address.getType()!=null){
					for(Address newAddress  : newaddresses){					
						if((newAddress.getType() != null)  && newAddress.getType().getValue().equals(address.getType().getValue())){
							addresses.remove(address);
							addresses.add(newAddress);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, addresses, "oxTrustAddresses");
			}
			}

			// getting phone numbers;
			log.trace(" setting phoneNumbers ");
			if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {				
				List<PhoneNumber> phoneNumbers = CopyUtils2.getAttributeListValue(destination, PhoneNumber.class, "oxTrustPhoneValue");				
				if(phoneNumbers != null ){
				List<PhoneNumber> newPhoneNumbers =source.getPhoneNumbers();
				for(PhoneNumber phoneNumber : phoneNumbers){
					if(phoneNumber != null && phoneNumber.getType() != null){
					for(PhoneNumber newPhoneNumber  : newPhoneNumbers){					
						if((newPhoneNumber.getType() != null) && (phoneNumber.getType().getValue()!=null) && newPhoneNumber.getType().getValue().equals(phoneNumber.getType().getValue())){
							phoneNumbers.remove(phoneNumber);
							phoneNumbers.add(newPhoneNumber);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, phoneNumbers, "oxTrustPhoneValue");
			}
			}

			// getting ims
			log.trace(" setting ims ");
			if (source.getIms() != null && source.getIms().size() > 0) {
				List<Im> ims = CopyUtils2.getAttributeListValue(destination, Im.class, "oxTrustImsValue");
				if (ims != null && ims.size() > 0) {
				List<Im> newims =source.getIms();
				for(Im im : ims){
					if(im != null && im.getType() != null){
					for(Im newIm  : newims){					
						if(newIm.getType() != null  && newIm.getType().getValue().equals(im.getType().getValue())){
							ims.remove(im);
							ims.add(newIm);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, ims, "oxTrustImsValue");
			}
			}

			// getting Photos
			log.trace(" setting photos ");
			if (source.getPhotos() != null && source.getPhotos().size() > 0) {
				List<Photo> photos = CopyUtils2.getAttributeListValue(destination, Photo.class, "oxTrustPhotos");
				if (photos != null && photos.size() > 0) {
				List<Photo> newPhotos =source.getPhotos();
				for(Photo photo : photos){	
					if(photo != null && photo.getType() != null){
					for(Photo newPhoto  : newPhotos){					
						if(newPhoto.getType() !=null  && newPhoto.getType().getValue().equals(photo.getType().getValue())){	
							photos.remove(photo);
							photos.add(newPhoto);
							
						}
					}	
				}
				}
				CopyUtils2.setAttributeListValue(destination, photos, "oxTrustPhotos");
				}					
			}

			if (source.getUserType() != null && source.getUserType().length() > 0) {
				destination.setAttribute("oxTrustUserType", source.getUserType());
			}
			if (source.getTitle() != null && source.getTitle().length() > 0) {
				destination.setAttribute("oxTrustTitle", source.getTitle());
			}
			if (source.getPreferredLanguage() != null && source.getPreferredLanguage().length() > 0) {
				destination.setPreferredLanguage(source.getPreferredLanguage());
			}
			if (source.getLocale() != null && source.getLocale().length() > 0) {
				// destination.setAttribute("oxTrustLocale", source.getLocale());
				destination.setAttribute("locale", source.getLocale());
			}
			if (source.getTimezone() != null && source.getTimezone().length() > 0) {
				destination.setTimezone(source.getTimezone());
			}			
			if (source.isActive() != null) {
				destination.setAttribute("oxTrustActive", source.isActive().toString());
			}			
			if (source.getPassword() != null && source.getPassword().length() > 0) {
				destination.setUserPassword(source.getPassword());
			}

			// getting user groups
			log.trace(" setting groups ");
			if (source.getGroups() != null && source.getGroups().size() > 0) {

				IGroupService groupService = GroupService.instance();
				List<GroupRef> listGroups = source.getGroups();
				List<String> members = new ArrayList<String>();
				for (GroupRef group : listGroups) {
					members.add(groupService.getDnForGroup(group.getValue()));
				}

				destination.setMemberOf(members);
			}

			// getting roles
			log.trace(" setting roles ");
			if (source.getRoles() != null && source.getRoles().size() > 0) {
				List<Role> roles = CopyUtils2.getAttributeListValue(destination, Role.class, "oxTrustRole");
				if(roles!=null && roles.size()>0){
				List<Role> newRoles =source.getRoles();
				for(Role role :roles){
					if(role != null && role.getType() != null){
					for(Role newRole  : newRoles){					
						if((newRole.getType()!=null)  && newRole.getType().getValue().equals(role.getType().getValue())){	
							roles.remove(role);
							roles.add(newRole);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, roles, "oxTrustRole");
			}
			}

			// getting entitlements
			log.trace(" setting entitlements ");
			if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
				List<Entitlement> entitlements = CopyUtils2.getAttributeListValue(destination, Entitlement.class, "oxTrustEntitlements");
				if(entitlements != null && entitlements.size()>0){
				List<Entitlement> newEentitlements =source.getEntitlements();
				for(Entitlement entitlement :entitlements){
					if(entitlement != null && entitlement.getType() != null){
					for(Entitlement newEntitlement  : newEentitlements){					
						if((newEntitlement.getType()!=null)  && newEntitlement.getType().getValue().equals(entitlement.getType().getValue())){
							entitlements.remove(entitlement);
							entitlements.add(newEntitlement);
							
						}
					}
				}
				}
				CopyUtils2.setAttributeListValue(destination, entitlements , "oxTrustEntitlements");
			}
			}

			// getting x509Certificates
			log.trace(" setting certs ");
			if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
				List<X509Certificate> X509Certificates = CopyUtils2.getAttributeListValue(destination, X509Certificate.class, "oxTrustx509Certificate");
				if(X509Certificates != null){
				List<X509Certificate> newX509Certificates =source.getX509Certificates();
				for(X509Certificate X509Certificate :X509Certificates){
					if(X509Certificate != null && X509Certificate.getType() != null){
					for(X509Certificate newX509Certificate  : newX509Certificates){					
						if((newX509Certificate.getType()!=null)  && newX509Certificate.getType().getValue().equals(X509Certificate.getType().getValue())){
							X509Certificates.remove(X509Certificate);
							X509Certificates.add(newX509Certificate);
						}
					}
					}
				}
				CopyUtils2.setAttributeListValue(destination, X509Certificates , "oxTrustx509Certificate");
			}
			}

			log.trace(" setting extensions ");
			if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
				
				destination.setExtensions(source.getExtensions());
			}

            
			if(source.isActive()  != null ){
				CopyUtils2.setGluuStatus(source, destination);
			}

		return destination;
	}
	
	
	
	public static GluuCustomPerson addPatch(User source,GluuCustomPerson destination) throws Exception {

		if (source == null) {
			return null;
		}

		IPersonService personService1 = PersonService.instance();

		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new GluuCustomPerson();
		}

		log.trace(" setting schemas ");
		destination.setSchemas(source.getSchemas());

		personService1.addCustomObjectClass(destination);

		// getting emails
		log.trace(" setting emails ");
		if (source.getEmails() != null && source.getEmails().size() > 0) {
			List<Email> emails = CopyUtils2.getAttributeListValue(destination,	Email.class, "oxTrustEmail");
			if(emails == null){
				emails = new ArrayList<Email>();
			}
			emails.addAll(source.getEmails());
			CopyUtils2.setAttributeListValue(destination, emails, "oxTrustEmail");
			
		}

		// getting addresses
		log.trace(" setting addresses ");
		if (source.getAddresses() != null && source.getAddresses().size() > 0) {
			List<Address> addresses = CopyUtils2.getAttributeListValue(destination,Address.class, "oxTrustAddresses");
			if (addresses==null){
				addresses = new ArrayList<Address>();
			}
			addresses.addAll(source.getAddresses());
			CopyUtils2.setAttributeListValue(destination, addresses,"oxTrustAddresses");
			
		}

		// getting phone numbers;
		log.trace(" setting phoneNumbers ");
		if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
			List<PhoneNumber> phoneNumbers = CopyUtils2.getAttributeListValue(destination,	PhoneNumber.class, "oxTrustPhoneValue");
			if(phoneNumbers == null){
				phoneNumbers = new ArrayList<PhoneNumber>();
			}
			phoneNumbers.addAll(source.getPhoneNumbers());
			CopyUtils2.setAttributeListValue(destination, phoneNumbers,"oxTrustPhoneValue");
			
		}

		// getting ims
		log.trace(" setting ims ");
		if (source.getIms() != null && source.getIms().size() > 0) {
			List<Im> ims = CopyUtils2.getAttributeListValue(destination, Im.class,	"oxTrustImsValue");
			if(ims==null){
				ims = new ArrayList<Im>();
			}
				ims.addAll(source.getIms());
				CopyUtils2.setAttributeListValue(destination, ims, "oxTrustImsValue");
			
		}

		// getting Photos
		log.trace(" setting photos ");
		if (source.getPhotos() != null && source.getPhotos().size() > 0) {
			List<Photo> photos = CopyUtils2.getAttributeListValue(destination,Photo.class, "oxTrustPhotos");
			if(photos==null){
				photos= new ArrayList<Photo>();
			}
				photos.addAll(source.getPhotos());
				CopyUtils2.setAttributeListValue(destination, photos, "oxTrustPhotos");
		}

		// getting user groups
		log.trace(" setting groups ");
		if (source.getGroups() != null && source.getGroups().size() > 0) {
			List<String> groupsList = destination.getMemberOf();

			IGroupService groupService = GroupService.instance();
			List<GroupRef> listGroups = source.getGroups();
			for (GroupRef group : listGroups) {
				String groupToAdd = groupService.getDnForGroup(group.getValue());
				if (groupToAdd != null || !groupToAdd.trim().equalsIgnoreCase("")) {
					groupsList.add(groupToAdd);
				}
			}
			destination.setMemberOf(groupsList);
		}

		// getting roles
		log.trace(" setting roles ");
		if (source.getRoles() != null && source.getRoles().size() > 0) {
			List<Role> roles = CopyUtils2.getAttributeListValue(destination, Role.class,"oxTrustRole");
			if(roles==null){
				roles = new ArrayList<Role>();
			}
				roles.addAll(source.getRoles());
				CopyUtils2.setAttributeListValue(destination, roles, "oxTrustRole");
		}

		// getting entitlements
		log.trace(" setting entitlements ");
		if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
			List<Entitlement> entitlements = CopyUtils2.getAttributeListValue(destination,Entitlement.class, "oxTrustEntitlements");
			if(entitlements==null){
				entitlements = new ArrayList<Entitlement>();
			}
				entitlements.addAll(source.getEntitlements());
				CopyUtils2.setAttributeListValue(destination, entitlements,"oxTrustEntitlements");
			
		}

		// getting x509Certificates
		log.trace(" setting certs ");
		if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
			List<X509Certificate> X509Certificates = CopyUtils2.getAttributeListValue(	destination, X509Certificate.class, "oxTrustx509Certificate");
			if(X509Certificates==null){
				X509Certificates = new ArrayList<X509Certificate>();
			}
			X509Certificates.addAll(source.getX509Certificates());
			CopyUtils2.setAttributeListValue(destination, X509Certificates,"oxTrustx509Certificate");
			

		}

		log.trace(" setting extensions ");
		if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
			Map<String, Extension> destMap = destination.fetchExtensions();
			if(destMap==null){
				destMap= new HashMap<String, Extension>();
			}
			destMap.putAll(source.getExtensions());
			destination.setExtensions(destMap);

		}

		if (source.isActive() != null) {
			CopyUtils2.setGluuStatus(source, destination);
		}

		return destination;
	}
	
	
	public static GluuCustomPerson removePatch(User source, GluuCustomPerson destination) throws Exception {

		if (source == null ) {
			return null;
		}

		IPersonService personService1 = PersonService.instance();

		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new GluuCustomPerson();
		}

		log.trace(" setting schemas ");
		destination.setSchemas(source.getSchemas());

			personService1.addCustomObjectClass(destination);

			log.trace(" setting userName ");
			if (source.getUserName() != null && source.getUserName().length() > 0) {
				destination.setUid(source.getUserName());
			}

			if (source.getName() != null) {

				log.trace(" setting givenname ");
				if (source.getName().getGivenName() != null ) {
					destination.setGivenName("");
				}
				log.trace(" setting famillyname ");
				if (source.getName().getFamilyName() != null) {
					destination.setSurname("");
				}
				log.trace(" setting middlename ");
				if (source.getName().getMiddleName() != null) {
					// destination.setAttribute("oxTrustMiddleName", source.getName().getMiddleName());
					destination.setAttribute("middleName", "");
				}
				log.trace(" setting honor");
				if (source.getName().getHonorificPrefix() != null ) {
					destination.setAttribute("oxTrusthonorificPrefix", "");
				}
				if (source.getName().getHonorificSuffix() != null ) {
					destination.setAttribute("oxTrusthonorificSuffix", "");
				}
			}

			log.trace(" setting displayname ");
			if (source.getDisplayName() != null) {
				destination.setDisplayName(source.getDisplayName());
			}
			log.trace(" setting externalID ");
			if (source.getExternalId() != null) {
				destination.setAttribute("oxTrustExternalId", source.getExternalId());
			}
			log.trace(" setting nickname ");
			if (source.getNickName() != null) {
				// destination.setAttribute("oxTrustNickName", source.getNickName());
				destination.setAttribute("nickname", "");
			}
			log.trace(" setting profileURL ");
			if (source.getProfileUrl() != null ) {
				destination.setAttribute("oxTrustProfileURL", "");
			}

			// getting emails
			log.trace(" setting emails ");
			if (source.getEmails() != null && source.getEmails().size() > 0) {
				List<Email> emails = CopyUtils2.getAttributeListValue(destination,Email.class, "oxTrustEmail");
				if (emails != null && emails.size() > 0) {
					List<Email> newemails = source.getEmails();
					Iterator<Email> emailsIt = emails.iterator();
					Iterator<Email> newemailsIt = newemails.iterator();
					while (emailsIt.hasNext()) {
						Email email = emailsIt.next();
						if (email != null && email.getType() != null) {
							while (newemailsIt.hasNext()) {
								Email newEmail = newemailsIt.next();
								if (newEmail.getType() != null	&& newEmail.getType().getValue().equals(email.getType().getValue())) {
									emailsIt.remove();
								}
							}
						}
					}
					CopyUtils2.setAttributeListValue(destination, emails,"oxTrustEmail");
				}
			}

			// getting addresses
			log.trace(" setting addresses ");
			if (source.getAddresses() != null && source.getAddresses().size() > 0) {
				List<Address> addresses = CopyUtils2.getAttributeListValue(destination, Address.class, "oxTrustAddresses");
				if (addresses != null && addresses.size() > 0) {
					List<Address> newaddresses = source.getAddresses();
					Iterator<Address> addressesIt = addresses.iterator();
					Iterator<Address> newaddressesIt = newaddresses.iterator();
					while (addressesIt.hasNext()) {
						Address address = addressesIt.next();
						if (address != null && address.getType() != null) {
							while (newaddressesIt.hasNext()) {
								Address newaddress = newaddressesIt.next();
								if (newaddress.getType().getValue() != null && newaddress.getType().getValue().equals(address.getType().getValue())) {
									addressesIt.remove();
								}
							}
						}
					}
					CopyUtils2.setAttributeListValue(destination, addresses,"oxTrustAddresses");
				}
			}

			// getting phone numbers;
			log.trace(" setting phoneNumbers ");
			if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
				List<PhoneNumber> phoneNumbers = CopyUtils2.getAttributeListValue(destination, PhoneNumber.class, "oxTrustPhoneValue");
				if (phoneNumbers != null && phoneNumbers.size() > 0) {
					List<PhoneNumber> newPhoneNumbers = source.getPhoneNumbers();
					Iterator<PhoneNumber> phoneNumbersIt = phoneNumbers.iterator();
					Iterator<PhoneNumber> newPhoneNumbersIt = newPhoneNumbers.iterator();
					while (phoneNumbersIt.hasNext()) {
						PhoneNumber phoneNumber = phoneNumbersIt.next();
						while (newPhoneNumbersIt.hasNext()) {
							PhoneNumber newPhoneNumber = newPhoneNumbersIt.next();
							if (newPhoneNumber.getType() != null && newPhoneNumber.getType().getValue().equals(phoneNumber.getType().getValue())) {
								phoneNumbersIt.remove();
							}
						}
					}
					CopyUtils2.setAttributeListValue(destination, phoneNumbers,"oxTrustPhoneValue");
				}
			}

			// getting ims
			log.trace(" setting ims ");
			if (source.getIms() != null && source.getIms().size() > 0) {
				List<Im> ims = CopyUtils2.getAttributeListValue(destination,Im.class, "oxTrustImsValue");
				if (ims != null && ims.size() > 0) {
					List<Im> newims = source.getIms();
					Iterator<Im> imsIt = ims.iterator();
					Iterator<Im> newimssIt = newims.iterator();
					while (imsIt.hasNext()) {
						Im im = imsIt.next();
						if (im != null && im.getType() != null) {
							while (newimssIt.hasNext()) {
								Im newIm = newimssIt.next();
								if (newIm.getType() != null && newIm.getType().getValue().equals(im.getType().getValue())) {
									imsIt.remove();
								}
							}
						}
					}
					CopyUtils2.setAttributeListValue(destination, ims,"oxTrustImsValue");
				}
			}

			// getting Photos
			log.trace(" setting photos ");
			if (source.getPhotos() != null && source.getPhotos().size() > 0) {
				List<Photo> photos = CopyUtils2.getAttributeListValue(destination,Photo.class, "oxTrustPhotos");
				if (photos != null && photos.size() > 0) {
					List<Photo> newPhotos = source.getPhotos();
					Iterator<Photo> photosIt = photos.iterator();
					Iterator<Photo> newphotosIt = newPhotos.iterator();
					while (photosIt.hasNext()) {
						Photo old = photosIt.next();
						if (old != null && old.getType() != null) {
							while (newphotosIt.hasNext()) {
								Photo newelement = newphotosIt.next();
								if (newelement.getType() != null && newelement.getType().getValue().equals(old.getType().getValue())) {
									photosIt.remove();
								}
							}
						}
					}
					CopyUtils2.setAttributeListValue(destination, photos,"oxTrustPhotos");
				}
			}

			if (source.getUserType() != null) {
				destination.setAttribute("oxTrustUserType", "");
			}
			if (source.getTitle() != null ) {
				destination.setAttribute("oxTrustTitle", "");
			}
			if (source.getPreferredLanguage() != null) {
				destination.setPreferredLanguage("");
			}
			if (source.getLocale() != null) {
				// destination.setAttribute("oxTrustLocale", source.getLocale());
				destination.setAttribute("locale", "");
			}
			if (source.getTimezone() != null ) {
				destination.setTimezone("");
			}			
			if (source.isActive() != null) {
				destination.setAttribute("oxTrustActive", source.isActive().toString());
			}			
			if (source.getPassword() != null && source.getPassword().length() > 0) {
				destination.setUserPassword(source.getPassword());
			}

			// getting user groups
			log.trace(" setting groups ");
			if (source.getGroups() != null && source.getGroups().size() > 0) {
				List<String> members = destination.getMemberOf();
				if(members != null || members.size() > 0){					
					members.removeAll(source.getGroups());
				}
				destination.setMemberOf(members);
			}

			// getting roles
				log.trace(" setting roles ");
				if (source.getRoles() != null && source.getRoles().size() > 0) {
					List<Role> roles = CopyUtils2.getAttributeListValue(destination,Role.class, "oxTrustRole");
					if (roles != null && roles.size() > 0) {
						List<Role> newRoles = source.getRoles();
						Iterator<Role> oldsIt = roles.iterator();
						Iterator<Role> newsIt = newRoles.iterator();
						while (oldsIt.hasNext()) {
							Role old = oldsIt.next();
							if (old != null && old.getType() != null) {
								while (newsIt.hasNext()) {
									Role newelement = newsIt.next();
									if (newelement.getType() != null && newelement.getType().getValue().equals(old.getType().getValue())) {
										oldsIt.remove();
									}
								}
							}
						}
						CopyUtils2.setAttributeListValue(destination, roles,
								"oxTrustRole");
					}
				}
		
				// getting entitlements
				log.trace(" setting entitlements ");
				if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
					List<Entitlement> entitlements = CopyUtils2.getAttributeListValue(destination, Entitlement.class, "oxTrustEntitlements");
					if (entitlements != null && entitlements.size() > 0) {
						List<Entitlement> newEentitlements = source.getEntitlements();
						Iterator<Entitlement> oldsIt = entitlements.iterator();
						Iterator<Entitlement> newsIt = newEentitlements.iterator();
						while (oldsIt.hasNext()) {
							Entitlement old = oldsIt.next();
							if (old != null && old.getType() != null) {
								while (newsIt.hasNext()) {
									Entitlement newelement = newsIt.next();
									if (newelement.getType() != null && newelement.getType().getValue().equals(old.getType().getValue())) {
										oldsIt.remove();
									}
								}
							}
						}
						CopyUtils2.setAttributeListValue(destination, entitlements,	"oxTrustEntitlements");
					}
		
				}
		
				// getting x509Certificates
				log.trace(" setting certs ");
				if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
					List<X509Certificate> X509Certificates = CopyUtils2.getAttributeListValue(destination, X509Certificate.class,"oxTrustx509Certificate");
					if (X509Certificates != null && X509Certificates.size() > 0) {
						List<X509Certificate> newX509Certificates = source.getX509Certificates();
						Iterator<X509Certificate> oldsIt = X509Certificates.iterator();
						Iterator<X509Certificate> newsIt = newX509Certificates.iterator();
						while (oldsIt.hasNext()) {
							X509Certificate old = oldsIt.next();
							if (old != null && old.getType() != null) {
								while (newsIt.hasNext()) {
									X509Certificate newelement = newsIt.next();
									if (newelement.getType() != null && newelement.getType().getValue().equals(old.getType().getValue())) {
										oldsIt.remove();
									}
								}
							}
						}
						CopyUtils2.setAttributeListValue(destination, X509Certificates,	"oxTrustx509Certificate");
					}
				}

			log.trace(" setting extensions ");
			if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
				
				destination.setExtensions(source.getExtensions());
			}

            
			if(source.isActive()  != null ){
				CopyUtils2.setGluuStatus(source, destination);
			}

		return destination;
	}
}
