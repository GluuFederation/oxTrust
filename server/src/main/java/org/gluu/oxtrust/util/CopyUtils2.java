/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.oxtrust.ldap.service.*;
import org.gluu.oxtrust.model.*;
import org.gluu.oxtrust.model.scim.ScimEntitlements;
import org.gluu.oxtrust.model.scim.ScimEntitlementsPatch;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.model.scim.ScimGroupMembers;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonAddressesPatch;
import org.gluu.oxtrust.model.scim.ScimPersonEmailsPatch;
import org.gluu.oxtrust.model.scim.ScimPersonGroups;
import org.gluu.oxtrust.model.scim.ScimPersonGroupsPatch;
import org.gluu.oxtrust.model.scim.ScimPersonIms;
import org.gluu.oxtrust.model.scim.ScimPersonImsPatch;
import org.gluu.oxtrust.model.scim.ScimPersonPatch;
import org.gluu.oxtrust.model.scim.ScimPersonPhones;
import org.gluu.oxtrust.model.scim.ScimPersonPhonesPatch;
import org.gluu.oxtrust.model.scim.ScimPersonPhotos;
import org.gluu.oxtrust.model.scim.ScimPersonPhotosPatch;
import org.gluu.oxtrust.model.scim.ScimRoles;
import org.gluu.oxtrust.model.scim.ScimRolesPatch;
import org.gluu.oxtrust.model.scim.Scimx509Certificates;
import org.gluu.oxtrust.model.scim.Scimx509CertificatesPatch;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.User;
import org.hibernate.internal.util.StringHelper;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.*;

@Name("copyUtils2")
public class CopyUtils2 implements Serializable {

	private static final long serialVersionUID = -1715995162448707004L;

	@Logger
	private static Log log;

	/**
	 * Copy data from Person object to GluuCustomPerson object
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static GluuCustomPerson copy(Person source, GluuCustomPerson destination, List<GluuAttribute> attributes, GluuUserRole role,
			boolean isUpdate) {
		if (source == null || !isValidData(source, isUpdate)) {
			return null;
		}
		if (destination == null) {
			destination = new GluuCustomPerson();
		}

		if (source.getPersonAttrList() != null) {
			for (PersonAttribute personAttr : source.getPersonAttrList()) {
				GluuAttribute attribute = getAttribute(attributes, personAttr.getName());
				if (attribute == null || attribute.getEditType() == null || !containsRole(attribute.getEditType(), role))
					continue;
				destination.setAttribute(personAttr.getName(), personAttr.getValue());
			}
		}

		if (!isUpdate) {
			destination.setUid(source.getUserId());
			destination.setUserPassword(source.getPassword());
			destination.setGivenName(source.getFirstName());
			destination.setDisplayName(source.getDisplayName());
			destination.setSurname(source.getLastName());
			destination.setMail(source.getEmail());
			destination.setCommonName(source.getFirstName() + " " + source.getDisplayName());
		} else {
			if (!isEmpty(source.getFirstName()))
				destination.setGivenName(source.getFirstName());
			if (!isEmpty(source.getDisplayName()))
				destination.setDisplayName(source.getDisplayName());
			if (!isEmpty(source.getLastName()))
				destination.setSurname(source.getLastName());
			if (!isEmpty(source.getEmail()))
				destination.setMail(source.getEmail());
			if (!isEmpty(source.getFirstName()) && !isEmpty(source.getDisplayName()))
				destination.setCommonName(source.getFirstName() + " " + source.getDisplayName());
		}

		return destination;
	}

	/**
	 * Copy data from ScimPerson object to GluuCustomPerson object "Reda"
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */

	public static GluuCustomPerson copy(User source, GluuCustomPerson destination, boolean isUpdate) throws Exception {
		if (source == null || !isValidData(source, isUpdate)) {
			return null;
		}

		IPersonService personService1 = PersonService.instance();


		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new GluuCustomPerson();
		}

		log.trace(" setting schemas ");
		destination.setSchemas(source.getSchemas());

		if (isUpdate) {
			personService1.addCustomObjectClass(destination);

			log.trace(" setting userName ");
			if (source.getUserName() != null && source.getUserName().length() > 0) {
				destination.setUid(source.getUserName());
			}
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
				List<Email> emails = source.getEmails();
				StringWriter listOfEmails = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfEmails, emails);

				destination.setAttribute("oxTrustEmail", listOfEmails.toString());

			}

			// getting addresses
			log.trace(" setting addresses ");
			if (source.getAddresses() != null && source.getAddresses().size() > 0) {
				List<Address> addresses = source.getAddresses();

				StringWriter listOfAddresses = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfAddresses, addresses);

				destination.setAttribute("oxTrustAddresses", listOfAddresses.toString());
			}

			// getting phone numbers;
			log.trace(" setting phoneNumbers ");
			if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
				List<PhoneNumber> phones = source.getPhoneNumbers();

				StringWriter listOfPhones = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfPhones, phones);

				destination.setAttribute("oxTrustPhoneValue", listOfPhones.toString());
			}

			// getting ims
			log.trace(" setting ims ");
			if (source.getIms() != null && source.getIms().size() > 0) {

				List<Im> ims = source.getIms();

				StringWriter listOfIms = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfIms, ims);

				destination.setAttribute("oxTrustImsValue", listOfIms.toString());
			}

			// getting Photos
			log.trace(" setting photos ");
			if (source.getPhotos() != null && source.getPhotos().size() > 0) {

				List<Photo> photos = source.getPhotos();

				StringWriter listOfPhotos = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfPhotos, photos);

				destination.setAttribute("oxTrustPhotos", listOfPhotos.toString());
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
				List<Role> roles = source.getRoles();

				StringWriter listOfRoles = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfRoles, roles);

				destination.setAttribute("oxTrustRole", listOfRoles.toString());
			}

			// getting entitlements
			log.trace(" setting entilements ");
			if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
				List<Entitlement> ents = source.getEntitlements();

				StringWriter listOfEnts = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfEnts, ents);

				destination.setAttribute("oxTrustEntitlements", listOfEnts.toString());
			}

			// getting x509Certificates
			log.trace(" setting certs ");
			if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
				List<X509Certificate> certs = source.getX509Certificates();

				StringWriter listOfCerts = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfCerts, certs);

				destination.setAttribute("oxTrustx509Certificate", listOfCerts.toString());
			}

			log.trace(" setting extensions ");
			if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
				destination.setExtensions(source.getExtensions());
			}

            /*
			// getting customAttributes
			log.trace("getting custom attributes");

			if (source.getCustomAttributes() != null) {
				log.trace("source.getCustomAttributes() != null");
				log.trace("getting a list of ScimCustomAttributes");

				List<CustomAttributes> customAttr = source.getCustomAttributes();
				log.trace("checling every attribute in the request");

				for (CustomAttributes oneAttr : customAttr) {
					if (oneAttr == null) {
						continue;
					}

					int countValues = oneAttr.getValues().size();
					if (countValues == 0) {
						log.trace("setting a empty attribute");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), oneAttr.getValues().toArray(new String[0]));
					} else if (countValues == 1) {
						log.trace("setting a single attribute");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), oneAttr.getValues().get(0));
					} else if (countValues > 1) {
						log.trace("setting a multivalued attribute");

						List<String> listOfAttr = oneAttr.getValues();
						String[] AttrArray = new String[listOfAttr.size()];
						int i = 0;
						for (String oneValue : listOfAttr) {
							if (oneValue != null && oneValue.length() > 0) {
								log.trace("setting a value");
								AttrArray[i] = oneValue;
								i++;
							}

						}
						log.trace("setting the list of multivalued attributes");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), AttrArray);
					}
				}
			}
            */

			// log.trace("getting meta attributes");

			/*
			if (source.getMeta()!=null && source.getMeta().getAttributes() != null) {
				log.trace("source.getCustomAttributes() != null");
				log.trace("getting a list of ScimCustomAttributes");

				Set<String> customAttr = source.getMeta().getAttributes();
				log.trace("checling every attribute in the request");

				for (String oneAttr : customAttr) {
					if (oneAttr == null) {
						continue;
					}
					destination.setAttribute(oneAttr.replaceAll(" ", ""), "");
					*/
					/* NOTE : WRITE CODE FOR THIS
					int countValues = oneAttr.getValues().size();
					if (countValues == 0) {
						log.trace("setting a empty attribute");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), oneAttr.getValues().toArray(new String[0]));
					} else if (countValues == 1) {
						log.trace("setting a single attribute");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), oneAttr.getValues().get(0));
					} else if (countValues > 1) {
						log.trace("setting a multivalued attribute");

						List<String> listOfAttr = oneAttr.getValues();
						String[] AttrArray = new String[listOfAttr.size()];
						int i = 0;
						for (String oneValue : listOfAttr) {
							if (oneValue != null && oneValue.length() > 0) {
								log.trace("setting a value");
								AttrArray[i] = oneValue;
								i++;
							}

						}
						log.trace("setting the list of multivalued attributes");
						destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), AttrArray);
					}
					*//*
				}
			}
			*/

		} else {

			try {

				if (personService1.getPersonByUid(source.getUserName()) != null) {
					return null;
				}

				personService1.addCustomObjectClass(destination);

				log.trace(" setting userName ");
				if (source.getUserName() != null && source.getUserName().length() > 0) {
					destination.setUid(source.getUserName());
				}
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
					destination.setAttribute("oxTrustMiddleName", source.getName().getMiddleName());
				}
				log.trace(" setting honor");
				if (source.getName().getHonorificPrefix() != null && source.getName().getHonorificPrefix().length() > 0) {
					destination.setAttribute("oxTrusthonorificPrefix", source.getName().getHonorificPrefix());
				}
				if (source.getName().getHonorificSuffix() != null && source.getName().getHonorificSuffix().length() > 0) {
					destination.setAttribute("oxTrusthonorificSuffix", source.getName().getHonorificSuffix());
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
					destination.setAttribute("oxTrustNickName", source.getNickName());
				}
				log.trace(" setting profileURL ");
				if (source.getProfileUrl() != null && source.getProfileUrl().length() > 0) {
					destination.setAttribute("oxTrustProfileURL", source.getProfileUrl());
				}

				// getting emails
				log.trace(" setting emails ");
				if (source.getEmails() != null && source.getEmails().size() > 0) {
					List<Email> emails = source.getEmails();
					StringWriter listOfEmails = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfEmails, emails);

					destination.setAttribute("oxTrustEmail", listOfEmails.toString());

				}

				// getting addresses
				log.trace(" setting addresses ");
				if (source.getAddresses() != null && source.getAddresses().size() > 0) {
					List<Address> addresses = source.getAddresses();

					StringWriter listOfAddresses = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfAddresses, addresses);

					destination.setAttribute("oxTrustAddresses", listOfAddresses.toString());
				}

				// getting phone numbers;
				log.trace(" setting phoneNumbers ");
				if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
					List<PhoneNumber> phones = source.getPhoneNumbers();

					StringWriter listOfPhones = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfPhones, phones);

					destination.setAttribute("oxTrustPhoneValue", listOfPhones.toString());
				}

				// getting ims
				log.trace(" setting ims ");
				if (source.getIms() != null && source.getIms().size() > 0) {

					List<Im> ims = source.getIms();

					StringWriter listOfIms = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfIms, ims);

					destination.setAttribute("oxTrustImsValue", listOfIms.toString());
				}

				// getting Photos
				log.trace(" setting photos ");
				if (source.getPhotos() != null && source.getPhotos().size() > 0) {

					List<Photo> photos = source.getPhotos();

					StringWriter listOfPhotos = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfPhotos, photos);

					destination.setAttribute("oxTrustPhotos", listOfPhotos.toString());
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
					List<Role> roles = source.getRoles();

					StringWriter listOfRoles = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfRoles, roles);

					destination.setAttribute("oxTrustRole", listOfRoles.toString());
				}

				// getting entitlements
				log.trace(" setting entilements ");
				if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
					List<Entitlement> ents = source.getEntitlements();

					StringWriter listOfEnts = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfEnts, ents);

					destination.setAttribute("oxTrustEntitlements", listOfEnts.toString());
				}

				// getting x509Certificates
				log.trace(" setting certs ");
				if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
					List<X509Certificate> certs = source.getX509Certificates();

					StringWriter listOfCerts = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfCerts, certs);

					destination.setAttribute("oxTrustx509Certificate", listOfCerts.toString());
				}

				log.trace(" setting extensions ");
				if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
					destination.setExtensions(source.getExtensions());
				}

				/*
				// getting customAttributes
				log.trace("getting custom attributes");

				if (source.getCustomAttributes() != null && source.getCustomAttributes().size() > 0) {
					log.trace("source.getCustomAttributes() != null");
					log.trace("getting a list of CustomAttributes");

					List<CustomAttributes> customAttr = source.getCustomAttributes();
					log.trace("checling every attribute in the request");

					for (CustomAttributes oneAttr : customAttr) {
						if (oneAttr != null && oneAttr.getValues().size() == 1) {
							log.trace("setting a single attribute");
							destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), oneAttr.getValues().get(0));
						} else if (oneAttr != null && oneAttr.getValues().size() > 1) {
							log.trace("setting a multivalued attribute");

							List<String> listOfAttr = oneAttr.getValues();
							String[] AttrArray = new String[listOfAttr.size()];
							int i = 0;
							for (String oneValue : listOfAttr) {
								if (oneValue != null && oneValue.length() > 0) {
									log.trace("setting a value");
									AttrArray[i] = oneValue;
									i++;
								}

							}
							log.trace("setting the list of multivalued attributes");
							destination.setAttribute(oneAttr.getName().replaceAll(" ", ""), AttrArray);
						}
					}
				}
				*/

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		setGluuStatus(source, destination);

		return destination;
	}

	/**
	 * Copy data from GluuCustomPerson object to Person object
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static Person copy(GluuCustomPerson source, Person destination, List<GluuAttribute> attributes) {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new Person();
		}
		destination.setInum(source.getInum());
		destination.setIname(source.getIname());
		destination.setUserId(source.getUid());
		destination.setFirstName(source.getGivenName());
		destination.setDisplayName(source.getDisplayName());
		destination.setLastName(source.getSurname());
		destination.setEmail(source.getMail());
		destination.setPassword(source.getUserPassword());
		destination.setCommonName(source.getGivenName() + " " + source.getDisplayName());

		List<PersonAttribute> personAttrList = new ArrayList<PersonAttribute>();
		for (GluuAttribute attribute : attributes) {
			PersonAttribute personAttr = new PersonAttribute(attribute.getName(), source.getAttribute(attribute.getName()),
					attribute.getDisplayName());
			personAttrList.add(personAttr);
		}

		destination.setPersonAttrList(personAttrList);

		return destination;
	}

	/**
	 * Copy data from GluuCustomPerson object to ScimPerson object "Reda"
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static User copy(GluuCustomPerson source, User destination) throws Exception {

		if (source == null) {
			return null;
		}
		if (destination == null) {
            log.trace(" creating a new GluuCustomPerson instant ");
			destination = new User();
		}

		log.trace(" setting ID ");
		if (source.getInum() != null) {
			destination.setId(source.getInum());
		}
		log.trace(" setting userName ");
		if (source.getUid() != null) {
			destination.setUserName(source.getUid());			
		}
		
		log.trace(" setting ExternalID ");
		if (source.getAttribute("oxTrustExternalId") != null) {
			destination.setExternalId(source.getAttribute("oxTrustExternalId"));
		}
		log.trace(" setting givenname ");
		if (source.getGivenName() != null) {
			org.gluu.oxtrust.model.scim2.Name name=new org.gluu.oxtrust.model.scim2.Name();
			name.setGivenName(source.getGivenName());
			if (source.getSurname() != null)  
				name.setFamilyName(source.getSurname());
			if (source.getAttribute("middleName") != null)
				name.setMiddleName(source.getAttribute("middleName"));
			/*
			if (source.getAttribute("oxTrustMiddleName") != null)
				name.setMiddleName(source.getAttribute("oxTrustMiddleName"));
			*/
			if (source.getAttribute("oxTrusthonorificPrefix") != null)
				name.setHonorificPrefix(source.getAttribute("oxTrusthonorificPrefix"));
			if (source.getAttribute("oxTrusthonorificSuffix") != null) 
				name.setHonorificSuffix(source.getAttribute("oxTrusthonorificSuffix"));
			name.setFormatted(name.getFormatted());
			destination.setName(name);
		}

		log.trace(" getting displayname ");
		if (source.getDisplayName() != null) {
			destination.setDisplayName(source.getDisplayName());
		}
		log.trace(" getting nickname ");
		/*
		if (source.getAttribute("oxTrustNickName") != null) {
			destination.setNickName(source.getAttribute("oxTrustNickName"));
		}
		*/
		if (source.getAttribute("nickname") != null) {
			destination.setNickName(source.getAttribute("nickname"));
		}
		log.trace(" getting profileURL ");
		if (source.getAttribute("oxTrustProfileURL") != null) {
			destination.setProfileUrl(source.getAttribute("oxTrustProfileURL"));
		}
		log.trace(" getting emails ");
		// getting emails
		if (source.getAttributeArray("oxTrustEmail") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Email> listOfEmails = mapper.readValue(source.getAttribute("oxTrustEmail"), new TypeReference<List<Email>>(){});
			destination.setEmails(listOfEmails);
		}
		log.trace(" getting addresses ");
		// getting addresses
		if (source.getAttribute("oxTrustAddresses") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Address> listOfAddresses = mapper.readValue(source.getAttribute("oxTrustAddresses"), new TypeReference<List<Address>>(){});
			destination.setAddresses(listOfAddresses);
		}
		log.trace(" setting phoneNumber ");
		// getting user's PhoneNumber
		if (source.getAttribute("oxTrustPhoneValue") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<PhoneNumber> listOfPhones = mapper.readValue(source.getAttribute("oxTrustPhoneValue"),	new TypeReference<List<PhoneNumber>>(){});
			destination.setPhoneNumbers(listOfPhones);
		}
		log.trace(" getting ims ");
		// getting ims
		if (source.getAttribute("oxTrustImsValue") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Im> listOfIms = mapper.readValue(source.getAttribute("oxTrustImsValue"), new TypeReference<List<Im>>(){});
			destination.setIms(listOfIms);
		}
		log.trace(" setting photos ");
		// getting photos
		if (source.getAttribute("oxTrustPhotos") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Photo> listOfPhotos = mapper.readValue(source.getAttribute("oxTrustPhotos"), new TypeReference<List<Photo>>(){});
			destination.setPhotos(listOfPhotos);
		}
		log.trace(" setting userType ");
		if (source.getAttribute("oxTrustUserType") != null) {
			destination.setUserType(source.getAttribute("oxTrustUserType"));
		}
		log.trace(" setting title ");
		if (source.getAttribute("oxTrustTitle") != null) {
			destination.setTitle(source.getAttribute("oxTrustTitle"));
		}
		log.trace(" setting Locale ");
		/*
		if (source.getAttribute("oxTrustLocale") != null) {
			destination.setLocale(source.getAttribute("oxTrustLocale"));
		}
		*/
		if (source.getAttribute("locale") != null) {
			destination.setLocale(source.getAttribute("locale"));
		}
		log.trace(" setting preferredLanguage ");
		if (source.getPreferredLanguage() != null) {
			destination.setPreferredLanguage(source.getPreferredLanguage());
		}
		log.trace(" setting timeZone ");
		if (source.getTimezone() != null) {
			destination.setTimezone(source.getTimezone());
		}
		log.trace(" setting active ");
		if (source.getAttribute("oxTrustActive") != null) {
			destination.setActive(Boolean.parseBoolean(source.getAttribute("oxTrustActive")));
		}
		log.trace(" setting password ");
		destination.setPassword("Hidden for Privacy Reasons");

		// getting user groups
		log.trace(" setting  groups ");
		if (source.getMemberOf() != null) {
			IGroupService groupService = GroupService.instance();

			List<String> listOfGroups = source.getMemberOf();
			List<GroupRef> groupRefList = new ArrayList<GroupRef>();
			
			for (String groupDN : listOfGroups) {

				GluuGroup gluuGroup = groupService.getGroupByDn(groupDN);
				
				GroupRef groupRef = new GroupRef();
				groupRef.setDisplay(gluuGroup.getDisplayName());
				groupRef.setValue(gluuGroup.getInum());
				String reference = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + "/scim/v2/Groups/" + gluuGroup.getInum();
				groupRef.setReference(reference);

				groupRefList.add(groupRef);
			}
			
			destination.setGroups(groupRefList);
		}

		// getting roles
		if (source.getAttribute("oxTrustRole") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Role> listOfRoles = mapper.readValue(source.getAttribute("oxTrustRole"), new TypeReference<List<Role>>(){});
			destination.setRoles(listOfRoles);
		}
		log.trace(" getting entilements ");
		// getting entitlements
		if (source.getAttribute("oxTrustEntitlements") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<Entitlement> listOfEnts = mapper.readValue(source.getAttribute("oxTrustEntitlements"),	new TypeReference<List<Entitlement>>(){});
			destination.setEntitlements(listOfEnts);
		}

		// getting x509Certificates
		log.trace(" setting certs ");
		if (source.getAttribute("oxTrustx509Certificate") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<X509Certificate> listOfCerts = mapper.readValue(source.getAttribute("oxTrustx509Certificate"),	new TypeReference<List<X509Certificate>>(){});
			destination.setX509Certificates(listOfCerts);
		}

		log.trace(" setting extensions ");

		AttributeService attributeService = AttributeService.instance();
		List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributesImpl(attributeService.getCustomAttributes());

		if (scimCustomAttributes != null && !scimCustomAttributes.isEmpty()) {

			Map<String, Extension> extensionMap = new HashMap<String, Extension>();
			Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);

			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

			boolean hasExtension = false;

			outer:
			for (GluuCustomAttribute customAttribute : source.getCustomAttributes()) {

				for (GluuAttribute scimCustomAttribute : scimCustomAttributes) {

					if (customAttribute.getName().equals(scimCustomAttribute.getName())) {

						hasExtension = true;
						GluuAttributeDataType scimCustomAttributeDataType = scimCustomAttribute.getDataType();

						if ((scimCustomAttribute.getOxMultivaluedAttribute() != null) && scimCustomAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {

							extensionBuilder.setFieldAsList(customAttribute.getName(), Arrays.asList(customAttribute.getValues()));

						} else {

							if (scimCustomAttributeDataType.equals(GluuAttributeDataType.STRING) || scimCustomAttributeDataType.equals(GluuAttributeDataType.PHOTO)) {
								String value = ExtensionFieldType.STRING.fromString(customAttribute.getValue());
								extensionBuilder.setField(customAttribute.getName(), value);
							} else if (scimCustomAttributeDataType.equals(GluuAttributeDataType.DATE)) {
								Date value = ExtensionFieldType.DATE_TIME.fromString(customAttribute.getValue());
								extensionBuilder.setField(customAttribute.getName(), value);
							} else if (scimCustomAttributeDataType.equals(GluuAttributeDataType.NUMERIC)) {
								BigDecimal value = ExtensionFieldType.DECIMAL.fromString(customAttribute.getValue());
								extensionBuilder.setField(customAttribute.getName(), value);
							}
						}

						continue outer;
					}
				}
			}
			if (hasExtension) {
				extensionMap.put(Constants.USER_EXT_SCHEMA_ID, extensionBuilder.build());
				destination.getSchemas().add(Constants.USER_EXT_SCHEMA_ID);
				destination.setExtensions(extensionMap);
			}
		}

		log.trace(" getting meta ");

		Meta meta = (destination.getMeta() != null) ? destination.getMeta() : new Meta();

		if (source.getAttribute("oxTrustMetaVersion") != null) {
			meta.setVersion(source.getAttribute("oxTrustMetaVersion"));
		}

		String location = source.getAttribute("oxTrustMetaLocation");
		if (location != null && !location.isEmpty()) {
			if (!location.startsWith("https://") && !location.startsWith("http://")) {
				location = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + location;
			}
		} else {
			location = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + "/scim/v2/Users/" + source.getInum();
		}
		meta.setLocation(location);

		if (source.getAttribute("oxTrustMetaCreated") != null && !source.getAttribute("oxTrustMetaCreated").isEmpty()) {

			try {
				DateTime dateTimeUtc = new DateTime(source.getAttribute("oxTrustMetaCreated"), DateTimeZone.UTC);
				meta.setCreated(dateTimeUtc.toDate());
			} catch (Exception e) {
				log.error(" Date parse exception (NEW format), continuing...", e);
				// For backward compatibility
				try {
					meta.setCreated(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(source.getAttribute("oxTrustMetaCreated")));
				} catch (Exception ex) {
					log.error(" Date parse exception (OLD format)", ex);
				}
			}
		}

		if (source.getAttribute("oxTrustMetaLastModified") != null && !source.getAttribute("oxTrustMetaLastModified").isEmpty()) {

			try {
				DateTime dateTimeUtc = new DateTime(source.getAttribute("oxTrustMetaLastModified"), DateTimeZone.UTC);
				meta.setLastModified(dateTimeUtc.toDate());
			} catch (Exception e) {
				log.error(" Date parse exception (NEW format), continuing...", e);
				// For backward compatibility
				try {
					meta.setLastModified(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(source.getAttribute("oxTrustMetaLastModified")));
				} catch (Exception ex) {
					log.error(" Date parse exception (OLD format)", ex);
				}
			}
		}

		destination.setMeta(meta);
			
			// log.trace(" getting custom Attributes ");
			// getting custom Attributes
			/*AttributeService attributeService = AttributeService.instance();
			List<GluuAttribute> listOfAttr = attributeService.getSCIMRelatedAttributes();
			if (listOfAttr != null && listOfAttr.size() > 0) {
				//List<ScimCustomAttributes> listOfCustomAttr = new ArrayList<ScimCustomAttributes>();
				Set<String> listOfCustomAttr=new HashSet<String>();
				for (GluuAttribute attr : listOfAttr) {
					boolean isEmpty = attr.getOxMultivaluedAttribute() == null;
					if (!isEmpty && attr.getOxMultivaluedAttribute().getValue().equalsIgnoreCase("true")) {
						boolean isAttrEmpty = source.getAttributes(attr.getName()) == null;
						if (!isAttrEmpty) {
							listOfCustomAttr.add(attr.getName());
						}

					} else {
						boolean isAttrEmpty = source.getAttributes(attr.getName()) == null;
						if (!isAttrEmpty) {
							listOfCustomAttr.add(attr.getName());
						}
					}
				}
				if (listOfCustomAttr.size() > 0) {
					meta.setAttributes(listOfCustomAttr);
					destination.setMeta(meta);
				}
			}*/

		return destination;
	}

	/**
	 * Copy data from ScimPerson object to GluuCustomPerson object
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static GluuCustomPerson copyChangePassword(ScimPerson source, GluuCustomPerson destination) {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			return null;
		}
		// only update password
		destination.setUserPassword(source.getPassword());
		return destination;
	}

	/**
	 * Copy the User Password
	 * 
	 * @param person
	 * @param password
	 * @return
	 */
	public static GluuCustomPerson updatePassword(GluuCustomPerson person, String password) {
		try {
			person.setUserPassword(password);
		} catch (Exception ex) {
			return null;
		}
		return person;
	}

	// -

	public static GluuAttribute getAttribute(List<GluuAttribute> attributes, String attributeName) {
		GluuAttribute gluuAttribute = null;
		for (GluuAttribute gluuAttr : attributes) {
			if (attributeName.equalsIgnoreCase(gluuAttr.getName())) {
				gluuAttribute = gluuAttr;
				break;
			}
		}
		return gluuAttribute;
	}

	public static boolean containsRole(GluuUserRole[] roles, GluuUserRole role) {
		for (int i = 0; i < roles.length; i++) {
			if (roles[i] == role)
				return true;
		}
		return false;
	}

	public static boolean isValidData(Person person, boolean isUpdate) {
		if (isUpdate) {
			// if (isEmpty(person.getFirstName()) ||
			// isEmpty(person.getDisplayName())
			// || isEmpty(person.getLastName())
			// || isEmpty(person.getEmail())) {
			// return false;
			// }
		} else if (isEmpty(person.getUserId()) || isEmpty(person.getFirstName()) || isEmpty(person.getDisplayName())
				|| isEmpty(person.getLastName()) || isEmpty(person.getEmail()) || isEmpty(person.getPassword())) {
			return false;
		}
		return true;
	}

	public static boolean isValidData(User person, boolean isUpdate) {
		if (isUpdate) {
			// if (isEmpty(person.getFirstName()) ||
			// isEmpty(person.getDisplayName())
			// || isEmpty(person.getLastName())
			// || isEmpty(person.getEmail())) {
			// return false;
			// }
		} else if (isEmpty(person.getUserName()) || isEmpty(person.getName().getGivenName()) || isEmpty(person.getDisplayName())
				|| isEmpty(person.getName().getFamilyName())
				// || (person.getEmails() == null || person.getEmails().size() <
				// 1)
				|| isEmpty(person.getPassword())) {
			return false;
		}
		return true;
	}

	public static boolean isEmpty(String value) {
		if (value == null || value.trim().equals(""))
			return true;
		return false;
	}

	/**
	 * Copy data from GluuGroup object to ScimGroup object
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */

	public static Group copy(GluuGroup source, Group destination) throws Exception {

		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new Group();
		}

		IPersonService personService = PersonService.instance();

		destination.setDisplayName(source.getDisplayName());
		destination.setId(source.getInum());

		if (source.getMembers() != null) {

			if (source.getMembers().size() != 0) {

				Set<MemberRef> memberRefSet = new HashSet<MemberRef>();
				List<String> membersList = source.getMembers();

				for (String oneMember : membersList) {

					GluuCustomPerson gluuCustomPerson = personService.getPersonByDn(oneMember);

					MemberRef memberRef = new MemberRef();
					memberRef.setValue(gluuCustomPerson.getInum());
					memberRef.setDisplay(gluuCustomPerson.getDisplayName());
					String reference = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + "/scim/v2/Users/" + gluuCustomPerson.getInum();
					memberRef.setReference(reference);

					memberRefSet.add(memberRef);
				}

				destination.setMembers(memberRefSet);
			}
		}

		log.trace(" getting meta ");

		Meta meta = (destination.getMeta() != null) ? destination.getMeta() : new Meta();

		if (source.getAttribute("oxTrustMetaVersion") != null) {
			meta.setVersion(source.getAttribute("oxTrustMetaVersion"));
		}

		String location = source.getAttribute("oxTrustMetaLocation");
		if (location != null && !location.isEmpty()) {
			if (!location.startsWith("https://") && !location.startsWith("http://")) {
				location = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + location;
			}
		} else {
			location = JsonConfigurationService.instance().getOxTrustApplicationConfiguration().getBaseEndpoint() + "/scim/v2/Groups/" + source.getInum();
		}
		meta.setLocation(location);

		if (source.getAttribute("oxTrustMetaCreated") != null && !source.getAttribute("oxTrustMetaCreated").isEmpty()) {

			try {
				DateTime dateTimeUtc = new DateTime(source.getAttribute("oxTrustMetaCreated"), DateTimeZone.UTC);
				meta.setCreated(dateTimeUtc.toDate());
			} catch (Exception e) {
				log.error(" Date parse exception (NEW format), continuing...", e);
				// For backward compatibility
				try {
					meta.setCreated(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(source.getAttribute("oxTrustMetaCreated")));
				} catch (Exception ex) {
					log.error(" Date parse exception (OLD format)", ex);
				}
			}
		}

		if (source.getAttribute("oxTrustMetaLastModified") != null && !source.getAttribute("oxTrustMetaLastModified").isEmpty()) {

			try {
				DateTime dateTimeUtc = new DateTime(source.getAttribute("oxTrustMetaLastModified"), DateTimeZone.UTC);
				meta.setLastModified(dateTimeUtc.toDate());
			} catch (Exception e) {
				log.error(" Date parse exception (NEW format), continuing...", e);
				// For backward compatibility
				try {
					meta.setLastModified(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(source.getAttribute("oxTrustMetaLastModified")));
				} catch (Exception ex) {
					log.error(" Date parse exception (OLD format)", ex);
				}
			}
		}

		destination.setMeta(meta);

		return destination;
	}

	public static GluuGroup copy(ScimGroup source, GluuGroup destination, List<GluuGroup> attributes) throws Exception {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new GluuGroup();
		}
		destination.setInum(source.getId());
		destination.setDisplayName(source.getDisplayName());
		List<ScimGroupMembers> mapMembers = source.getMembers();
		List<String> listMembers = new ArrayList<String>();
		// mapMembers.

		IPersonService personservice = PersonService.instance();
		for (String dn : listMembers) {
			GluuCustomPerson gluuPerson = personservice.getPersonByDn(dn);
			ScimGroupMembers member = new ScimGroupMembers();
			member.setDisplay(gluuPerson.getDisplayName());
			member.setValue(gluuPerson.getInum());
			mapMembers.add(member);
		}
		destination.setMembers(listMembers);
		return destination;
	}

	public static GluuCustomPerson patch(ScimPersonPatch source, GluuCustomPerson destination, boolean isUpdate) throws Exception {
		if (source == null || !isValidData(source, isUpdate)) {
			return null;
		}
		if (destination == null) {
			log.trace(" creating a new Scimperson instant ");
			destination = new GluuCustomPerson();

		}

		log.trace(" setting userName ");
		log.trace(" source.getUserName() :" + source.getUserName() + "h");
		log.trace("  userName length : " + source.getUserName().length());
		if (source.getUserName() != null && source.getUserName().length() > 0) {
			destination.setUid(source.getUserName());
		}
		log.trace(" setting givenname ");
		if (source.getName().getGivenName() != null && source.getName().getGivenName().length() > 0) {
			destination.setGivenName(source.getName().getGivenName());
		}
		log.trace(" setting famillyname ");
		if (source.getName().getFamilyName() != null && source.getName().getGivenName().length() > 0) {
			destination.setSurname(source.getName().getFamilyName());
		}
		log.trace(" setting middlename ");
		if (source.getName().getMiddleName() != null && source.getName().getMiddleName().length() > 0) {
			destination.setAttribute("oxTrustMiddleName", source.getName().getMiddleName());
		}
		log.trace(" setting honor");
		if (source.getName().getHonorificPrefix() != null && source.getName().getHonorificPrefix().length() > 0) {
			destination.setAttribute("oxTrusthonorificPrefix", source.getName().getHonorificPrefix());
		}
		if (source.getName().getHonorificSuffix() != null && source.getName().getHonorificSuffix().length() > 0) {
			destination.setAttribute("oxTrusthonorificSuffix", source.getName().getHonorificSuffix());
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
			destination.setAttribute("oxTrustNickName", source.getNickName());
		}
		log.trace(" setting profileURL ");
		if (source.getProfileUrl() != null && source.getProfileUrl().length() > 0) {
			destination.setAttribute("oxTrustProfileURL", source.getProfileUrl());
		}

		// getting emails
		log.trace(" setting emails ");
		if (source.getEmails() != null && source.getEmails().size() > 0) {
			List<ScimPersonEmailsPatch> emails = source.getEmails();
			String[] emailsList = new String[source.getEmails().size()];
			String[] emailsTypes = new String[source.getEmails().size()];
			String[] emailsPrimary = new String[source.getEmails().size()];

			int emailsSize = 0;
			if (destination.getAttributes("oxTrustEmail") != null && destination.getAttributes("oxTrustEmail").length > 0) {
				emailsList = destination.getAttributes("oxTrustEmail");
				emailsTypes = destination.getAttributes("oxTrustEmailType");
				emailsPrimary = destination.getAttributes("oxTrustEmailPrimary");
				// emailsSize =
				// destination.getAttributes("oxTrustEmail").length;
			}

			boolean emailIsFound = false;
			while (emailIsFound != true) {
				if (emails.get(0).getPrimary() == "true") {
					for (String oneEmail : emailsList) {
						if (oneEmail == emails.get(0).getValue()) {
							if (emails.get(0).getPrimary() != null && emails.get(0).getPrimary().length() > 0) {
								emailsPrimary[emailsSize] = emails.get(0).getPrimary();
							}
							emailIsFound = true;
						}
						emailsSize++;
					}
					emailsSize = 0;
					for (String onePrimary : emailsPrimary) {
						if (onePrimary == emails.get(0).getPrimary()) {
							if (emails.get(0).getPrimary() != null && emails.get(0).getPrimary().length() > 0) {
								emailsPrimary[emailsSize] = "false";
							}
						}
						emailsSize++;
					}
					if (emails.get(0).getValue() != null && emails.get(0).getValue().length() > 0) {
						emailsList[emailsSize] = emails.get(0).getValue();
					}
					if (emails.get(0).getType() != null && emails.get(0).getType().length() > 0) {
						emailsTypes[emailsSize] = emails.get(0).getType();
					}
					if (emails.get(0).getPrimary() != null && emails.get(0).getPrimary().length() > 0) {
						emailsPrimary[emailsSize] = emails.get(0).getPrimary();
					}
					emailIsFound = true;
				}
				if (emails.get(0).getPrimary() == "false") {
					emailsSize = emailsList.length;
					if (emails.get(0).getValue() != null && emails.get(0).getValue().length() > 0) {
						emailsList[emailsSize] = emails.get(0).getValue();
					}
					if (emails.get(0).getType() != null && emails.get(0).getType().length() > 0) {
						emailsTypes[emailsSize] = emails.get(0).getType();
					}
					if (emails.get(0).getPrimary() != null && emails.get(0).getPrimary().length() > 0) {
						emailsPrimary[emailsSize] = emails.get(0).getPrimary();
					}
					emailIsFound = true;
				}
			}

			destination.setAttribute("oxTrustEmail", emailsList);
			destination.setAttribute("oxTrustEmailType", emailsTypes);
			destination.setAttribute("oxTrustEmailPrimary", emailsPrimary);
		}

		// getting addresses
		log.trace(" settting addresses ");
		if (source.getAddresses() != null && source.getAddresses().size() == 2) {
			List<ScimPersonAddressesPatch> addresses = source.getAddresses();
			String[] street = new String[source.getAddresses().size()];
			String[] formatted = new String[source.getAddresses().size()];
			String[] locality = new String[source.getAddresses().size()];
			String[] region = new String[source.getAddresses().size()];
			String[] postalCode = new String[source.getAddresses().size()];
			String[] country = new String[source.getAddresses().size()];
			String[] addressType = new String[source.getAddresses().size()];
			String[] addressPrimary = new String[source.getAddresses().size()];

			int addressSize = 0;

			if (destination.getAttributes("oxTrustStreet") != null && destination.getAttributes("oxTrustStreet").length > 0) {
				street = destination.getAttributes("oxTrustStreet");
				formatted = destination.getAttributes("oxTrustAddressFormatted");
				locality = destination.getAttributes("oxTrustLocality");
				region = destination.getAttributes("oxTrustRegion");
				postalCode = destination.getAttributes("oxTrustPostalCode");
				country = destination.getAttributes("oxTrustCountry");
				addressType = destination.getAttributes("oxTrustAddressType");
				addressPrimary = destination.getAttributes("oxTrustAddressPrimary");
				// addressSize =
				// destination.getAttributes("oxTrustStreet").length;
			}

			for (String oneStreet : street) {
				if (oneStreet == addresses.get(0).getStreetAddress()) {
					if (addresses.get(1).getStreetAddress() != null && addresses.get(1).getStreetAddress().length() > 0) {
						street[addressSize] = addresses.get(1).getStreetAddress();
					}
					if (addresses.get(1).getFormatted() != null && addresses.get(1).getFormatted().length() > 0) {
						formatted[addressSize] = addresses.get(1).getFormatted();
					}
					if (addresses.get(1).getLocality() != null && addresses.get(1).getLocality().length() > 0) {
						locality[addressSize] = addresses.get(1).getLocality();
					}
					if (addresses.get(1).getRegion() != null && addresses.get(1).getRegion().length() > 0) {
						region[addressSize] = addresses.get(1).getRegion();
					}
					if (addresses.get(1).getPostalCode() != null && addresses.get(1).getPostalCode().length() > 0) {
						postalCode[addressSize] = addresses.get(1).getPostalCode();
					}
					if (addresses.get(1).getCountry() != null && addresses.get(1).getCountry().length() > 0) {
						country[addressSize] = addresses.get(1).getCountry();
					}
					if (addresses.get(1).getType() != null && addresses.get(1).getType().length() > 0) {
						addressType[addressSize] = addresses.get(1).getType();
					}
					if (addresses.get(1).getPrimary() != null && addresses.get(1).getPrimary().length() > 0) {
						addressPrimary[addressSize] = addresses.get(1).getPrimary();
					}
				}
				addressSize++;
			}

			destination.setAttribute("oxTrustStreet", street);
			destination.setAttribute("oxTrustLocality", locality);
			destination.setAttribute("oxTrustRegion", region);
			destination.setAttribute("oxTrustPostalCode", postalCode);
			destination.setAttribute("oxTrustCountry", country);
			destination.setAttribute("oxTrustAddressFormatted", formatted);
			destination.setAttribute("oxTrustAddressPrimary", addressPrimary);
			destination.setAttribute("oxTrustAddressType", addressType);
		}

		// getting phone numbers;
		log.trace(" setting phoneNumbers ");
		if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
			List<ScimPersonPhonesPatch> phones = source.getPhoneNumbers();
			String[] phoneNumber = new String[source.getPhoneNumbers().size()];
			String[] phoneType = new String[source.getPhoneNumbers().size()];

			int phoneSize = 0;

			if (destination.getAttributes("oxTrustPhoneValue") != null && destination.getAttributes("oxTrustPhoneValue").length > 0) {
				phoneNumber = destination.getAttributes("oxTrustPhoneValue");
				phoneType = destination.getAttributes("oxTrustPhoneType");
				// phoneSize =
				// destination.getAttributes("oxTrustPhoneValue").length;
			}

			for (ScimPersonPhones phone : phones) {
				if (phone.getValue() != null && phone.getValue().length() > 0) {
					phoneNumber[phoneSize] = phone.getValue();
				}
				if (phone.getType() != null && phone.getType().length() > 0) {
					phoneType[phoneSize] = phone.getType();
				}
				phoneSize++;
			}
			destination.setAttribute("oxTrustPhoneValue", phoneNumber);
			destination.setAttribute("oxTrustPhoneType", phoneType);
		}

		// getting ims
		log.trace(" setting ims ");
		if (source.getIms() != null && source.getIms().size() > 0) {
			List<ScimPersonImsPatch> ims = source.getIms();
			String[] imValue = new String[source.getIms().size()];
			String[] imType = new String[source.getIms().size()];

			int imSize = 0;
			if (destination.getAttributes("oxTrustImsValue") != null && destination.getAttributes("oxTrustImsValue").length > 0) {
				imValue = destination.getAttributes("oxTrustImsValue");
				imType = destination.getAttributes("oxTrustImsType");
				imSize = destination.getAttributes("oxTrustImsValue").length;
			}

			for (ScimPersonIms im : ims) {
				if (im.getValue() != null && im.getValue().length() > 0) {
					imValue[imSize] = im.getValue();
				}
				if (im.getType() != null && im.getType().length() > 0) {
					imType[imSize] = im.getType();
				}
				imSize++;
			}
			destination.setAttribute("oxTrustImsValue", imValue);
			destination.setAttribute("oxTrustImsType", imType);
		}

		// getting Photos
		log.trace(" setting photos ");
		if (source.getPhotos() != null && source.getPhotos().size() > 0) {
			List<ScimPersonPhotosPatch> photos = source.getPhotos();
			String[] photoType = new String[source.getPhotos().size()];
			String[] photoValue = new String[source.getPhotos().size()];

			int photoSize = 0;
			if (destination.getAttributes("oxTrustPhotos") != null && destination.getAttributes("oxTrustPhotos").length > 0) {
				photoType = destination.getAttributes("oxTrustPhotosType");
				photoValue = destination.getAttributes("oxTrustPhotos");
				photoSize = destination.getAttributes("oxTrustPhotosType").length;
			}

			for (ScimPersonPhotos photo : photos) {
				if (photo.getType() != null && photo.getType().length() > 0) {
					photoType[photoSize] = photo.getType();
				}
				if (photo.getValue() != null && photo.getValue().length() > 0) {
					photoValue[photoSize] = photo.getValue();
				}
				photoSize++;
			}
			destination.setAttribute("oxTrustPhotosType", photoType);
			destination.setAttribute("oxTrustPhotos", photoValue);
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
		if (source.getActive() != null && source.getActive().length() > 0) {
			destination.setAttribute("oxTrustActive", source.getActive());
		}
		if (source.getPassword() != null && source.getPassword().length() > 0) {
			destination.setUserPassword(source.getPassword());
		}

		// getting user groups
		log.trace(" setting groups ");
		if (source.getGroups() != null && source.getGroups().size() > 0) {
			IGroupService groupService = GroupService.instance();
			List<ScimPersonGroupsPatch> listGroups = source.getGroups();
			List<String> members = new ArrayList<String>();
			for (ScimPersonGroups group : listGroups) {

				members.add(groupService.getDnForGroup(group.getValue()));
			}
			destination.setMemberOf(members);
		}

		// getting roles

		log.trace(" setting roles ");
		if (source.getRoles() != null && source.getRoles().size() > 0) {
			List<ScimRolesPatch> roles = source.getRoles();
			String[] scimRole = new String[source.getRoles().size()];

			int rolesSize = 0;

			if (destination.getAttributes("oxTrustRole") != null && destination.getAttributes("oxTrustRole").length > 0) {
				scimRole = destination.getAttributes("oxTrustRole");
				rolesSize = destination.getAttributes("oxTrustRole").length;
			}

			for (ScimRoles role : roles) {

				if (role.getValue() != null && role.getValue().length() > 0) {
					scimRole[rolesSize] = role.getValue();
				}
				rolesSize++;
			}
			destination.setAttribute("oxTrustRole", scimRole);
		}

		// getting entitlements
		log.trace(" setting entilements ");
		if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
			List<ScimEntitlementsPatch> ents = source.getEntitlements();
			String[] listEnts = new String[source.getEntitlements().size()];

			int entsSize = 0;

			if (destination.getAttributes("oxTrustEntitlements") != null && destination.getAttributes("oxTrustEntitlements").length > 0) {
				listEnts = destination.getAttributes("oxTrustEntitlements");
				entsSize = destination.getAttributes("oxTrustEntitlements").length;
			}

			for (ScimEntitlements ent : ents) {
				if (ent.getValue() != null && ent.getValue().length() > 0) {
					listEnts[entsSize] = ent.getValue();
				}
				entsSize++;
			}
			destination.setAttribute("oxTrustEntitlements", listEnts);
		}

		// getting x509Certificates
		log.trace(" setting certs ");
		if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
			List<Scimx509CertificatesPatch> certs = source.getX509Certificates();
			String[] listCerts = new String[source.getX509Certificates().size()];
			int certsSize = 0;
			if (destination.getAttributes("oxTrustx509Certificate") != null
					&& destination.getAttributes("oxTrustx509Certificate").length > 0) {
				listCerts = destination.getAttributes("oxTrustx509Certificate");
				certsSize = destination.getAttributes("oxTrustx509Certificate").length;
			}

			for (Scimx509Certificates cert : certs) {
				if (cert.getValue() != null && cert.getValue().length() > 0) {
					listCerts[certsSize] = cert.getValue();
				}
				certsSize++;
			}

			destination.setAttribute("oxTrustx509Certificate", listCerts);
		}

		// getting meta
		log.trace(" setting meta ");

		if (source.getMeta().getCreated() != null && source.getMeta().getCreated().length() > 0) {
			destination.setAttribute("oxTrustMetaCreated", source.getMeta().getCreated());
		}
		if (source.getMeta().getLastModified() != null && source.getMeta().getLastModified().length() > 0) {
			destination.setAttribute("oxTrustMetaLastModified", source.getMeta().getLastModified());
		}
		if (source.getMeta().getVersion() != null && source.getMeta().getVersion().length() > 0) {
			destination.setAttribute("oxTrustMetaVersion", source.getMeta().getVersion());
		}
		if (source.getMeta().getLocation() != null && source.getMeta().getLocation().length() > 0) {
			destination.setAttribute("oxTrustMetaLocation", source.getMeta().getLocation());
		}

		setGluuStatus(source, destination);

		return destination;
	}

	private static boolean isValidData(ScimPersonPatch person, boolean isUpdate) {
		if (isUpdate) {
			// if (isEmpty(person.getFirstName()) ||
			// isEmpty(person.getDisplayName())
			// || isEmpty(person.getLastName())
			// || isEmpty(person.getEmail())) {
			// return false;
			// }
		} else if (isEmpty(person.getUserName()) || isEmpty(person.getName().getGivenName()) || isEmpty(person.getDisplayName())
				|| isEmpty(person.getName().getFamilyName())
				// || (person.getEmails() == null || person.getEmails().size() <
				// 1)
				|| isEmpty(person.getPassword())) {
			return false;
		}
		return true;
	}

	/**
	 * Copy data from ScimGroup object to GluuGroupn object
	 * 
	 * @param source
	 * @param destination
	 * @param isUpdate
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws Exception
	 */

	public static GluuGroup copy(Group source, GluuGroup destination, boolean isUpdate) throws Exception {
		if (source == null || !isValidData(source, isUpdate)) {
			return null;
		}
		if (destination == null) {
			log.trace(" creating a new GluuGroup instant ");
			destination = new GluuGroup();

		}
		if (isUpdate) {

			if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
				destination.setDisplayName(source.getDisplayName());
			}
			if (source.getMembers() != null && source.getMembers().size() > 0) {
				IPersonService personService = PersonService.instance();
				Set<MemberRef> members = source.getMembers();
				List<String> listMembers = new ArrayList<String>();
				for (MemberRef member : members) {
					listMembers.add(personService.getDnForPerson(member.getValue()));
				}

				destination.setMembers(listMembers);
			}

		} else {

			log.trace(" creating a new GroupService instant ");

			IGroupService groupService1 = GroupService.instance();
			log.trace(" source.getDisplayName() : ", source.getDisplayName());

			if (groupService1.getGroupByDisplayName(source.getDisplayName()) != null) {
				log.trace(" groupService1.getGroupByDisplayName(source.getDisplayName() != null : ");

				return null;
			}
			if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
				destination.setDisplayName(source.getDisplayName());
			}

			log.trace(" source.getMembers() : ", source.getMembers());
			log.trace(" source.getMembers().size() : ", source.getMembers().size());

			if (source.getMembers() != null && source.getMembers().size() > 0) {

				IPersonService personService = PersonService.instance();

				Set<MemberRef> members = source.getMembers();
				List<String> listMembers = new ArrayList<String>();
				for (MemberRef member : members) {
					listMembers.add(personService.getDnForPerson(member.getValue()));
				}

				destination.setMembers(listMembers);
			}

			/*GluuCustomPerson authUser = (GluuCustomPerson) Contexts.getSessionContext().get(OxTrustConstants.CURRENT_PERSON);
			destination.setOwner(authUser.getDn());
			log.trace(" authUser.getDn() : ", authUser.getDn());*/
			destination.setStatus(GluuStatus.ACTIVE);
			OrganizationService orgService = OrganizationService.instance();
			destination.setOrganization(orgService.getDnForOrganization());
		}

		return destination;
	}

	public static boolean isValidData(Group group, boolean isUpdate) {
		if (isUpdate) {

		} else if (isEmpty(group.getDisplayName())) {
			return false;
		}
		return true;
	}

	/**
	 * Copy data from ScimData object to ScimGroup object
	 * 
	 * @param source
	 * @param destination
	 * @return ScimGroup
	 * @throws Exception
	 */
	public static Group copy(ScimData source, Group destination) {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new Group();
		}

		if (source.getId() != null && source.getId().length() > 0) {
			destination.setId(source.getId());
		}
		if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
			destination.setDisplayName(source.getDisplayName());
		}
		if (source.getSchemas() != null && source.getSchemas().size() > 0) {
			destination.setSchemas(source.getSchemas());
		}
		if (source.getMembers() != null && source.getMembers().size() > 0) {
			destination.setMembers(source.getMembers());
		}

		return destination;

	}

	private static void setGluuStatus(User source, GluuCustomPerson destination) {
		Boolean active = source.isActive();
		if (active != null) {
			if (active.equals(Boolean.TRUE)) {
				setGluuStatus(destination, GluuBoolean.ACTIVE.getValue());
			} else {
				setGluuStatus(destination, GluuBoolean.INACTIVE.getValue());
			}
		}
	}

	private static void setGluuStatus(ScimPersonPatch source, GluuCustomPerson destination) {
		String active = source.getActive();
		setGluuStatus(destination, active);
	}

	private static void setGluuStatus(GluuCustomPerson destination, String active) {
		if (StringHelper.isNotEmpty(active) && (destination.getAttribute("gluuStatus") == null)) {
			GluuBoolean gluuStatus = GluuBoolean.getByValue(org.xdi.util.StringHelper.toLowerCase(active));
			if (gluuStatus != null) {
				destination.setAttribute("gluuStatus", gluuStatus.getValue());
			}
		}
	}

}
