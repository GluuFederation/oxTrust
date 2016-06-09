/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim.ScimCustomAttributes;
import org.gluu.oxtrust.model.scim.ScimData;
import org.gluu.oxtrust.model.scim.ScimEntitlements;
import org.gluu.oxtrust.model.scim.ScimEntitlementsPatch;
import org.gluu.oxtrust.model.scim.ScimGroup;
import org.gluu.oxtrust.model.scim.ScimGroupMembers;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.model.scim.ScimPersonAddresses;
import org.gluu.oxtrust.model.scim.ScimPersonAddressesPatch;
import org.gluu.oxtrust.model.scim.ScimPersonEmails;
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
import org.hibernate.internal.util.StringHelper;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;

@Name("copyUtils")
public class CopyUtils implements Serializable {

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

	private static final String OX_TRUST_LOCALE = "locale";

	private static final String OX_TRUST_TITLE = "oxTrustTitle";

	private static final String OX_TRUST_USER_TYPE = "oxTrustUserType";

	private static final String OX_TRUST_PHOTOS = "oxTrustPhotos";

	private static final String OX_TRUST_IMS_VALUE = "oxTrustImsValue";

	private static final String OX_TRUST_PHONE_VALUE = "oxTrustPhoneValue";

	private static final String OX_TRUST_ADDRESSES = "oxTrustAddresses";

	private static final String OX_TRUST_EMAIL = "oxTrustEmail";

	private static final String OX_TRUST_PROFILE_URL = "oxTrustProfileURL";

	private static final String OX_TRUST_NICK_NAME = "nickName";

	private static final String OX_TRUST_EXTERNAL_ID = "oxTrustExternalId";

	private static final String OX_TRUSTHONORIFIC_SUFFIX = "oxTrusthonorificSuffix";

	private static final String OX_TRUSTHONORIFIC_PREFIX = "oxTrusthonorificPrefix";

	private static final String OX_TRUST_MIDDLE_NAME = "middleName";

	/**
     *
     */
	private static final long serialVersionUID = -1715995162448707004L;

	@Logger
	private static Log log;

	/**
	 * Copy data from ScimPerson object to GluuCustomPerson object "Reda"
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */

	public static GluuCustomPerson copy(ScimPerson source, GluuCustomPerson destination, boolean isUpdate) throws Exception {

		if (source == null || !isValidData(source, isUpdate)) {
			return null;
		}

		IPersonService personService1 = PersonService.instance();

		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new GluuCustomPerson();
		}

		if (isUpdate) {

			personService1.addCustomObjectClass(destination);

			if (StringUtils.isNotEmpty(source.getUserName())) {
				log.trace(" setting userName ");
				destination.setUid(source.getUserName());
			}
			if (StringUtils.isNotEmpty(source.getName().getGivenName())) {
				log.trace(" setting givenname ");
				destination.setGivenName(source.getName().getGivenName());
			}
			if (StringUtils.isNotEmpty(source.getName().getFamilyName())) {
				log.trace(" setting familyname ");
				destination.setSurname(source.getName().getFamilyName());
			}
			if (StringUtils.isNotEmpty(source.getDisplayName())) {
				log.trace(" setting displayname ");
				destination.setDisplayName(source.getDisplayName());
			}

			setOrRemoveOptionalAttribute(destination, source.getName().getMiddleName(), OX_TRUST_MIDDLE_NAME);

			setOrRemoveOptionalAttribute(destination, source.getName().getHonorificPrefix(), OX_TRUSTHONORIFIC_PREFIX);

			setOrRemoveOptionalAttribute(destination, source.getName().getHonorificSuffix(), OX_TRUSTHONORIFIC_SUFFIX);

			setOrRemoveOptionalAttribute(destination, source.getExternalId(), OX_TRUST_EXTERNAL_ID);

			setOrRemoveOptionalAttribute(destination, source.getNickName(), OX_TRUST_NICK_NAME);

			setOrRemoveOptionalAttribute(destination, source.getProfileUrl(), OX_TRUST_PROFILE_URL);

			// setOrRemoveOptionalAttributeList(destination, source.getEmails(), OX_TRUST_EMAIL);
			if (source.getEmails() != null && source.getEmails().size() > 0) {

				List<ScimPersonEmails> emails = source.getEmails();

				/*
				StringWriter listOfEmails = new StringWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(listOfEmails, emails);
				*/

				List<String> emailList = new ArrayList<String>();
				for (ScimPersonEmails email : emails) {
					emailList.add(Utils.getObjectMapper().writeValueAsString(email));
				}

				// destination.setAttribute("oxTrustEmail", listOfEmails.toString());
				destination.setAttribute(OX_TRUST_EMAIL, emailList.toArray(new String[]{}));

			} else {
				log.trace(" removing " + OX_TRUST_EMAIL);
				destination.removeAttribute(OX_TRUST_EMAIL);
			}

			setOrRemoveOptionalAttributeList(destination, source.getAddresses(), OX_TRUST_ADDRESSES);

			setOrRemoveOptionalAttributeList(destination, source.getPhoneNumbers(), OX_TRUST_PHONE_VALUE);

			setOrRemoveOptionalAttributeList(destination, source.getIms(), OX_TRUST_IMS_VALUE);

			setOrRemoveOptionalAttributeList(destination, source.getPhotos(), OX_TRUST_PHOTOS);

			setOrRemoveOptionalAttribute(destination, source.getUserType(), OX_TRUST_USER_TYPE);

			setOrRemoveOptionalAttribute(destination, source.getTitle(), OX_TRUST_TITLE);

			if (source.getPreferredLanguage() != null && source.getPreferredLanguage().length() > 0) {
				destination.setPreferredLanguage(source.getPreferredLanguage());
			}

			setOrRemoveOptionalAttribute(destination, source.getLocale(), OX_TRUST_LOCALE);

			if (source.getTimezone() != null && source.getTimezone().length() > 0) {
				destination.setTimezone(source.getTimezone());
			}

			setOrRemoveOptionalAttribute(destination, source.getActive(), OX_TRUST_ACTIVE);

			if (StringUtils.isNotEmpty(source.getPassword())) {
				destination.setUserPassword(source.getPassword());
			}

			setGroups(source, destination);

			setOrRemoveOptionalAttributeList(destination, source.getRoles(), OX_TRUST_ROLE);

			setOrRemoveOptionalAttributeList(destination, source.getEntitlements(), OX_TRUST_ENTITLEMENTS);

			setOrRemoveOptionalAttributeList(destination, source.getX509Certificates(), OX_TRUSTX509_CERTIFICATE);

			setMetaData(source, destination);

			// getting customAttributes
			log.trace("getting custom attributes");

			if (source.getCustomAttributes() != null) {
				log.trace("source.getCustomAttributes() != null");
				log.trace("getting a list of ScimCustomAttributes");

				List<ScimCustomAttributes> customAttr = source.getCustomAttributes();
				log.trace("checking every attribute in the request");

				for (ScimCustomAttributes oneAttr : customAttr) {
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
				log.trace(" setting displayname ");
				if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
					destination.setDisplayName(source.getDisplayName());
				}
				
				setOrRemoveOptionalAttribute(destination, source.getName().getMiddleName(), OX_TRUST_MIDDLE_NAME);

				setOrRemoveOptionalAttribute(destination, source.getName().getHonorificPrefix(), OX_TRUSTHONORIFIC_PREFIX);

				setOrRemoveOptionalAttribute(destination, source.getName().getHonorificSuffix(), OX_TRUSTHONORIFIC_SUFFIX);

				setOrRemoveOptionalAttribute(destination, source.getExternalId(), OX_TRUST_EXTERNAL_ID);

				setOrRemoveOptionalAttribute(destination, source.getNickName(), OX_TRUST_NICK_NAME);

				setOrRemoveOptionalAttribute(destination, source.getProfileUrl(), OX_TRUST_PROFILE_URL);

				// setOrRemoveOptionalAttributeList(destination, source.getEmails(), OX_TRUST_EMAIL);
				if (source.getEmails() != null && source.getEmails().size() > 0) {

					List<ScimPersonEmails> emails = source.getEmails();

					/*
					StringWriter listOfEmails = new StringWriter();
					ObjectMapper mapper = new ObjectMapper();
					mapper.writeValue(listOfEmails, emails);
					*/

					List<String> emailList = new ArrayList<String>();
					for (ScimPersonEmails email : emails) {
						emailList.add(Utils.getObjectMapper().writeValueAsString(email));
					}

					// destination.setAttribute("oxTrustEmail", listOfEmails.toString());
					destination.setAttribute(OX_TRUST_EMAIL, emailList.toArray(new String[]{}));

				} else {
					log.trace(" removing " + OX_TRUST_EMAIL);
					destination.removeAttribute(OX_TRUST_EMAIL);
				}

				setOrRemoveOptionalAttributeList(destination, source.getAddresses(), OX_TRUST_ADDRESSES);

				setOrRemoveOptionalAttributeList(destination, source.getPhoneNumbers(), OX_TRUST_PHONE_VALUE);

				setOrRemoveOptionalAttributeList(destination, source.getIms(), OX_TRUST_IMS_VALUE);

				setOrRemoveOptionalAttributeList(destination, source.getPhotos(), OX_TRUST_PHOTOS);

				setOrRemoveOptionalAttribute(destination, source.getUserType(), OX_TRUST_USER_TYPE);

				setOrRemoveOptionalAttribute(destination, source.getTitle(), OX_TRUST_TITLE);

				if (source.getPreferredLanguage() != null && source.getPreferredLanguage().length() > 0) {
					destination.setPreferredLanguage(source.getPreferredLanguage());
				}

				setOrRemoveOptionalAttribute(destination, source.getLocale(), OX_TRUST_LOCALE);

				if (source.getTimezone() != null && source.getTimezone().length() > 0) {
					destination.setTimezone(source.getTimezone());
				}
				
				setOrRemoveOptionalAttribute(destination, source.getActive(), OX_TRUST_ACTIVE);
				
				if (source.getPassword() != null && source.getPassword().length() > 0) {
					destination.setUserPassword(source.getPassword());
				}

				setGroups(source, destination);

				setOrRemoveOptionalAttributeList(destination, source.getRoles(), OX_TRUST_ROLE);

				setOrRemoveOptionalAttributeList(destination, source.getEntitlements(), OX_TRUST_ENTITLEMENTS);

				setOrRemoveOptionalAttributeList(destination, source.getX509Certificates(), OX_TRUSTX509_CERTIFICATE);

				
				setMetaData(source, destination);

				// getting customAttributes
				log.trace("getting custom attributes");

				if (source.getCustomAttributes() != null && source.getCustomAttributes().size() > 0) {
					log.trace("source.getCustomAttributes() != null");
					log.trace("getting a list of ScimCustomAttributes");

					List<ScimCustomAttributes> customAttr = source.getCustomAttributes();
					log.trace("checking every attribute in the request");

					for (ScimCustomAttributes oneAttr : customAttr) {
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
			} catch (Exception ex) {
				return null;
			}
		}

		setGluuStatus(source, destination);

		return destination;
	}

	private static void setGroups(ScimPerson source, GluuCustomPerson destination) {
		log.trace(" setting groups ");
		if (source.getGroups() != null && source.getGroups().size() > 0) {
			IGroupService groupService = GroupService.instance();
			List<ScimPersonGroups> listGroups = source.getGroups();
			List<String> members = new ArrayList<String>();
			for (ScimPersonGroups group : listGroups) {

				members.add(groupService.getDnForGroup(group.getValue()));
			}
			destination.setMemberOf(members);
		}
	}

	private static void setMetaData(ScimPerson source, GluuCustomPerson destination) {
		log.trace(" setting meta ");

		if (source.getMeta().getCreated() != null && source.getMeta().getCreated().length() > 0) {
			destination.setAttribute(OX_TRUST_META_CREATED, source.getMeta().getCreated());
		}
		if (source.getMeta().getLastModified() != null && source.getMeta().getLastModified().length() > 0) {
			destination.setAttribute(OX_TRUST_META_LAST_MODIFIED, source.getMeta().getLastModified());
		}
		if (source.getMeta().getVersion() != null && source.getMeta().getVersion().length() > 0) {
			destination.setAttribute(OX_TRUST_META_VERSION, source.getMeta().getVersion());
		}
		if (source.getMeta().getLocation() != null && source.getMeta().getLocation().length() > 0) {
			destination.setAttribute(OX_TRUST_META_LOCATION, source.getMeta().getLocation());
		}
	}

	private static void setOrRemoveOptionalAttributeList(GluuCustomPerson destination, List<?> items, String attributeName)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (items == null) {
			log.trace(" removing " + attributeName);
			destination.removeAttribute(attributeName);
		} else {
			log.trace(" setting " + attributeName);

			StringWriter listOfItems = new StringWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(listOfItems, items);
			destination.setAttribute(attributeName, listOfItems.toString());

		}

	}

	private static void setOrRemoveOptionalAttribute(GluuCustomPerson destination, String attributevalue, String attributeName) {
		if (attributevalue == null) {
			log.trace(" removing " + attributeName);
			destination.removeAttribute(attributeName);
		} else {
			log.trace(" setting " + attributeName);
			destination.setAttribute(attributeName, attributevalue);
		}
	}

	/**
	 * Copy data from GluuCustomPerson object to ScimPerson object "Reda"
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws Exception
	 */
	public static ScimPerson copy(GluuCustomPerson source, ScimPerson destination) throws Exception {

		if (source == null) {
			return null;
		}

		ObjectMapper mapper = Utils.getObjectMapper();

		if (destination == null) {
			log.trace(" creating a new GluuCustomPerson instant ");
			destination = new ScimPerson();
		}
		destination.getSchemas().add("urn:scim:schemas:core:1.0");
		log.trace(" setting ID ");
		if (source.getInum() != null) {
			destination.setId(source.getInum());
		}
		log.trace(" setting userName ");
		if (source.getUid() != null) {
			destination.setUserName(source.getUid());
		}
		log.trace(" setting ExternalID ");
		if (source.getAttribute(OX_TRUST_EXTERNAL_ID) != null) {
			destination.setExternalId(source.getAttribute(OX_TRUST_EXTERNAL_ID));
		}
		log.trace(" setting givenname ");
		if (source.getGivenName() != null) {
			destination.getName().setGivenName(source.getGivenName());
		}
		log.trace(" getting family name ");
		if (source.getSurname() != null) {
			destination.getName().setFamilyName(source.getSurname());
		}
		log.trace(" getting middlename ");
		if (source.getAttribute(OX_TRUST_MIDDLE_NAME) != null) {
			destination.getName().setMiddleName(source.getAttribute(OX_TRUST_MIDDLE_NAME));
		}
		;
		log.trace(" getting honorificPrefix ");
		if (source.getAttribute(OX_TRUSTHONORIFIC_PREFIX) != null) {
			destination.getName().setHonorificPrefix(source.getAttribute(OX_TRUSTHONORIFIC_PREFIX));
		}
		;
		log.trace(" getting honorificSuffix ");
		if (source.getAttribute(OX_TRUSTHONORIFIC_SUFFIX) != null) {
			destination.getName().setHonorificSuffix(source.getAttribute(OX_TRUSTHONORIFIC_SUFFIX));
		}
		;
		log.trace(" getting displayname ");
		if (source.getDisplayName() != null) {
			destination.setDisplayName(source.getDisplayName());
		}
		log.trace(" getting nickname ");
		if (source.getAttribute(OX_TRUST_NICK_NAME) != null) {
			destination.setNickName(source.getAttribute(OX_TRUST_NICK_NAME));
		}
		log.trace(" getting profileURL ");
		if (source.getAttribute(OX_TRUST_PROFILE_URL) != null) {
			destination.setProfileUrl(source.getAttribute(OX_TRUST_PROFILE_URL));
		}

		log.trace(" getting emails ");
		// source = Utils.syncEmailReverse(source, false);
		// if (source.getAttribute(OX_TRUST_EMAIL) != null) {
		if (source.getAttributeArray(OX_TRUST_EMAIL) != null) {

			// List<ScimPersonEmails> listOfEmails = Utils.getObjectMapper().readValue(source.getAttribute(OX_TRUST_EMAIL), new TypeReference<List<ScimPersonEmails>>(){});

			/*
			 * List<ScimPersonEmails> emails = new
			 * ArrayList<ScimPersonEmails>(); String[] listEmails =
			 * source.getAttributes("oxTrustEmail"); String[] listEmailTyps =
			 * source.getAttributes("oxTrustEmailType"); String[]
			 * listEmailPrimary = source.getAttributes("oxTrustEmailPrimary");
			 * for(int i = 0 ; i<listEmails.length ; i++ ){ ScimPersonEmails
			 * oneEmail = new ScimPersonEmails();
			 * oneEmail.setValue(listEmails[i]);
			 * oneEmail.setType(listEmailTyps[i]);
			 * oneEmail.setPrimary(listEmailPrimary[i]); emails.add(oneEmail); }
			 */

			// destination.setEmails(listOfEmails);

			String[] emailArray = source.getAttributeArray(OX_TRUST_EMAIL);
			List<ScimPersonEmails> emails = new ArrayList<ScimPersonEmails>();

			for (String emailStr : emailArray) {
				ScimPersonEmails email = mapper.readValue(emailStr, ScimPersonEmails.class);
				emails.add(email);
			}

			destination.setEmails(emails);
		}

		log.trace(" getting addresses ");
		// getting addresses

		if (source.getAttribute(OX_TRUST_ADDRESSES) != null) {
			List<ScimPersonAddresses> listOfAddresses = mapper.readValue(source.getAttribute(OX_TRUST_ADDRESSES),
					new TypeReference<List<ScimPersonAddresses>>() {
					});

			/*
			 * List<ScimPersonAddresses> addresses = new
			 * ArrayList<ScimPersonAddresses>(); String[] listStreets =
			 * source.getAttributes("oxTrustStreet"); String[] listAddressTypes
			 * = source.getAttributes("oxTrustAddressType"); String[]
			 * listLocalities = source.getAttributes("oxTrustLocality");
			 * String[] listRegions = source.getAttributes("oxTrustRegion");
			 * String[] listPostalCodes =
			 * source.getAttributes("oxTrustPostalCode"); String[] listCountries
			 * = source.getAttributes("oxTrustCountry"); String[]
			 * listAddressFormatted =
			 * source.getAttributes("oxTrustAddressFormatted"); String[]
			 * listAddressPrimary =
			 * source.getAttributes("oxTrustAddressPrimary");
			 * if(listStreets.length > 0){ for(int i = 0 ; i <
			 * listStreets.length ; i++ ){ ScimPersonAddresses address = new
			 * ScimPersonAddresses();
			 * 
			 * if(!listAddressFormatted[i].equalsIgnoreCase("empty")){address.
			 * setFormatted
			 * (listAddressFormatted[i]);}else{address.setFormatted("");}
			 * if(!listStreets
			 * [i].equalsIgnoreCase("empty")){address.setStreetAddress
			 * (listStreets[i]);}else{address.setStreetAddress("");}
			 * if(!listAddressTypes
			 * [i].equalsIgnoreCase("empty")){address.setType
			 * (listAddressTypes[i]);}else{address.setType("");}
			 * if(!listLocalities
			 * [i].equalsIgnoreCase("empty")){address.setLocality
			 * (listLocalities[i]);}else{address.setLocality("");}
			 * if(!listRegions
			 * [i].equalsIgnoreCase("empty")){address.setRegion(listRegions
			 * [i]);}else{address.setRegion("");}
			 * if(!listPostalCodes[i].equalsIgnoreCase
			 * ("empty")){address.setPostalCode
			 * (listPostalCodes[i]);}else{address.setPostalCode("");}
			 * if(!listCountries
			 * [i].equalsIgnoreCase("empty")){address.setCountry
			 * (listCountries[i]);}else{address.setCountry("");}
			 * if(!listAddressPrimary
			 * [i].equalsIgnoreCase("empty")){address.setPrimary
			 * (listAddressPrimary[i]);}else{address.setPrimary("");}
			 * addresses.add(address);
			 * 
			 * } }
			 */
			destination.setAddresses(listOfAddresses);
		}
		log.trace(" setting phoneNumber ");
		// getting user's PhoneNumber
		if (source.getAttribute(OX_TRUST_PHONE_VALUE) != null) {
			List<ScimPersonPhones> listOfPhones = mapper.readValue(source.getAttribute(OX_TRUST_PHONE_VALUE),
					new TypeReference<List<ScimPersonPhones>>() {
					});

			/*
			 * List<ScimPersonPhones> phones = new
			 * ArrayList<ScimPersonPhones>(); String[] listNumbers =
			 * source.getAttributes("oxTrustPhoneValue"); String[]
			 * listPhoneTypes = source.getAttributes("oxTrustPhoneType");
			 * if(listNumbers.length > 0){ for(int i = 0 ; i <
			 * listNumbers.length ; i++){ ScimPersonPhones phone = new
			 * ScimPersonPhones();
			 * if(!listNumbers[i].equalsIgnoreCase("empty")){
			 * phone.setValue(listNumbers[i]);}else{phone.setValue("");}
			 * if(!listPhoneTypes
			 * [i].equalsIgnoreCase("empty")){phone.setType(listPhoneTypes
			 * [i]);}else{phone.setType("");} phones.add(phone);
			 * 
			 * } }
			 */

			destination.setPhoneNumbers(listOfPhones);
		}

		log.trace(" getting ims ");
		// getting ims
		if (source.getAttribute(OX_TRUST_IMS_VALUE) != null) {
			List<ScimPersonIms> listOfIms = mapper.readValue(source.getAttribute(OX_TRUST_IMS_VALUE),
					new TypeReference<List<ScimPersonIms>>() {
					});

			/*
			 * List<ScimPersonIms> ims = new ArrayList<ScimPersonIms>();
			 * String[] imValues = source.getAttributes("oxTrustImsValue");
			 * String[] imTypes = source.getAttributes("oxTrustImsType");
			 * if(imValues.length > 0){ for(int i = 0 ; i < imValues.length ;
			 * i++){ ScimPersonIms im = new ScimPersonIms(); if(imValues[i] !=
			 * null){im.setValue(imValues[i]);im.setType(imTypes[i]);}
			 * ims.add(im); } }
			 */
			destination.setIms(listOfIms);
		}
		log.trace(" setting photos ");
		// getting photos

		if (source.getAttribute(OX_TRUST_PHOTOS) != null) {
			List<ScimPersonPhotos> listOfPhotos = mapper.readValue(source.getAttribute(OX_TRUST_PHOTOS),
					new TypeReference<List<ScimPersonPhotos>>() {
					});

			/*
			 * List<ScimPersonPhotos> photos = new
			 * ArrayList<ScimPersonPhotos>(); String[] photoList =
			 * source.getAttributes("oxTrustPhotos"); String[] photoTypes =
			 * source.getAttributes("oxTrustPhotosType");
			 * 
			 * if(photoList.length > 0){ for(int i = 0 ; i < photoList.length ;
			 * i++){
			 * 
			 * ScimPersonPhotos photo = new ScimPersonPhotos(); if(photoList[i]
			 * !=
			 * null){photo.setValue(photoList[i]);photo.setType(photoTypes[i]);}
			 * photos.add(photo); } }
			 */
			destination.setPhotos(listOfPhotos);
		}
		log.trace(" setting userType ");
		if (source.getAttribute(OX_TRUST_USER_TYPE) != null) {
			destination.setUserType(source.getAttribute(OX_TRUST_USER_TYPE));
		}
		log.trace(" setting title ");
		if (source.getAttribute(OX_TRUST_TITLE) != null) {
			destination.setTitle(source.getAttribute(OX_TRUST_TITLE));
		}
		log.trace(" setting Locale ");
		if (source.getAttribute(OX_TRUST_LOCALE) != null) {
			destination.setLocale(source.getAttribute(OX_TRUST_LOCALE));
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
		if (source.getAttribute(OX_TRUST_ACTIVE) != null) {
			destination.setActive(source.getAttribute(OX_TRUST_ACTIVE));
		}
		log.trace(" setting password ");
		destination.setPassword("Hidden for Privacy Reasons");

		// getting user groups
		log.trace(" setting  groups ");
		if (source.getMemberOf() != null) {
			IGroupService groupService = GroupService.instance();

			List<String> listOfGroups = source.getMemberOf();

			List<ScimPersonGroups> groupsList = new ArrayList<ScimPersonGroups>();

			for (String groupDN : listOfGroups) {
				ScimPersonGroups group = new ScimPersonGroups();
				GluuGroup gluuGroup = groupService.getGroupByDn(groupDN);
				group.setValue(gluuGroup.getInum());
				group.setDisplay(gluuGroup.getDisplayName());
				groupsList.add(group);
			}
			destination.setGroups(groupsList);
		}

		// getting roles
		if (source.getAttribute(OX_TRUST_ROLE) != null) {
			List<ScimRoles> listOfRoles = mapper.readValue(source.getAttribute(OX_TRUST_ROLE),
					new TypeReference<List<ScimRoles>>() {
					});

			/*
			 * List<ScimRoles> roles = new ArrayList<ScimRoles>(); String[]
			 * listRoles = source.getAttributes("oxTrustRole");
			 * if(listRoles.length > 0){ for(int i = 0 ; i < listRoles.length
			 * ;i++){ ScimRoles role = new ScimRoles(); if(listRoles[i] !=
			 * null){role.setValue(listRoles[i]);} roles.add(role); } }
			 */

			destination.setRoles(listOfRoles);
		}
		log.trace(" getting entitlements ");
		// getting entitlements
		if (source.getAttribute(OX_TRUST_ENTITLEMENTS) != null) {
			List<ScimEntitlements> listOfEnts = mapper.readValue(source.getAttribute(OX_TRUST_ENTITLEMENTS),
					new TypeReference<List<ScimEntitlements>>() {
					});

			/*
			 * List<ScimEntitlements> entitlements = new
			 * ArrayList<ScimEntitlements>(); String[] listEntitlements =
			 * source.getAttributes("oxTrustEntitlements");
			 * if(listEntitlements.length > 0){ for(int i = 0 ; i <
			 * listEntitlements.length ; i++ ){ ScimEntitlements ent = new
			 * ScimEntitlements(); if(listEntitlements[i] !=
			 * null){ent.setValue(listEntitlements[i]);} entitlements.add(ent);
			 * } }
			 */

			destination.setEntitlements(listOfEnts);
		}

		// getting x509Certificates
		log.trace(" setting certs ");
		if (source.getAttribute(OX_TRUSTX509_CERTIFICATE) != null) {
			List<Scimx509Certificates> listOfCerts = mapper.readValue(source.getAttribute(OX_TRUSTX509_CERTIFICATE),
					new TypeReference<List<Scimx509Certificates>>() {
					});

			/*
			 * List<Scimx509Certificates> certificates = new
			 * ArrayList<Scimx509Certificates>(); String[] listCertif =
			 * source.getAttributes("oxTrustx509Certificate");
			 * if(listCertif.length > 0){ for(int i = 0 ; i < listCertif.length
			 * ; i++){ Scimx509Certificates cert = new Scimx509Certificates();
			 * if(listCertif[i] != null){cert.setValue(listCertif[i]);}
			 * certificates.add(cert);
			 * 
			 * } }
			 */

			destination.setX509Certificates(listOfCerts);
		}
		log.trace(" setting meta ");
		// getting meta data
		if (source.getAttribute(OX_TRUST_META_CREATED) != null) {
			destination.getMeta().setCreated(source.getAttribute(OX_TRUST_META_CREATED));
		}
		if (source.getAttribute(OX_TRUST_META_LAST_MODIFIED) != null) {
			destination.getMeta().setLastModified(source.getAttribute(OX_TRUST_META_LAST_MODIFIED));
		}
		if (source.getAttribute(OX_TRUST_META_VERSION) != null) {
			destination.getMeta().setVersion(source.getAttribute(OX_TRUST_META_VERSION));
		}
		if (source.getAttribute(OX_TRUST_META_LOCATION) != null) {
			destination.getMeta().setLocation(source.getAttribute(OX_TRUST_META_LOCATION));
		}
		log.trace(" getting custom Attributes ");
		// getting custom Attributes

		AttributeService attributeService = AttributeService.instance();

		List<GluuAttribute> listOfAttr = attributeService.getSCIMRelatedAttributes();

		if (listOfAttr != null && listOfAttr.size() > 0) {
			List<ScimCustomAttributes> listOfCustomAttr = new ArrayList<ScimCustomAttributes>();
			for (GluuAttribute attr : listOfAttr) {
				boolean isEmpty = attr.getOxMultivaluedAttribute() == null;
				if (!isEmpty && attr.getOxMultivaluedAttribute().getValue().equalsIgnoreCase("true")) {
					boolean isAttrEmpty = source.getAttributes(attr.getName()) == null;
					if (!isAttrEmpty) {

						String[] arrayValues = source.getAttributes(attr.getName());
						List<String> values = new ArrayList<String>(Arrays.asList(arrayValues));
						ScimCustomAttributes scimAttr = new ScimCustomAttributes();
						scimAttr.setName(attr.getName());
						scimAttr.setValues(values);
						listOfCustomAttr.add(scimAttr);
					}

				} else {
					boolean isAttrEmpty = source.getAttributes(attr.getName()) == null;
					if (!isAttrEmpty) {
						List<String> values = new ArrayList<String>();
						values.add(source.getAttribute(attr.getName()));
						ScimCustomAttributes scimAttr = new ScimCustomAttributes();
						scimAttr.setName(attr.getName());
						scimAttr.setValues(values);
						listOfCustomAttr.add(scimAttr);
					}
				}
			}
			if (listOfCustomAttr.size() > 0) {
				destination.setCustomAttributes(listOfCustomAttr);
			}
		}
		log.trace(" returning destination ");
		return destination;
	}

	public static boolean isValidData(ScimPerson person, boolean isUpdate) throws Exception {

		if (isUpdate) {
			// if (isEmpty(person.getFirstName()) ||
			// isEmpty(person.getDisplayName())
			// || isEmpty(person.getLastName())
			// || isEmpty(person.getEmail())) {
			// return false;
			// }
		} else if (isEmpty(person.getUserName()) || isEmpty(person.getName().getGivenName()) || isEmpty(person.getDisplayName())
			// || (person.getEmails() == null || person.getEmails().size() < 1)	|| isEmpty(person.getPassword())) {
			|| isEmpty(person.getName().getFamilyName())) {

			String message = "There are missing required parameters: userName, givenName, displayName, or familyName";
			throw new PersonRequiredFieldsException(message);
			// return false;
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

	public static ScimGroup copy(GluuGroup source, ScimGroup destination) throws Exception {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new ScimGroup();
		}
		IPersonService personService = PersonService.instance();

		List<String> schemas = new ArrayList<String>();
		schemas.add("urn:scim:schemas:core:1.0");
		destination.setSchemas(schemas);
		destination.setDisplayName(source.getDisplayName());
		destination.setId(source.getInum());
		if (source.getMembers() != null) {
			if (source.getMembers().size() != 0) {
				List<ScimGroupMembers> members = new ArrayList<ScimGroupMembers>();
				List<String> membersList = source.getMembers();
				for (String oneMember : membersList) {
					ScimGroupMembers member = new ScimGroupMembers();
					GluuCustomPerson person = personService.getPersonByDn(oneMember);
					member.setValue(person.getInum());
					member.setDisplay(person.getDisplayName());
					members.add(member);
				}

				destination.setMembers(members);
			}
		}

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
			destination.setAttribute(OX_TRUST_MIDDLE_NAME, source.getName().getMiddleName());
		}
		log.trace(" setting honor");
		if (source.getName().getHonorificPrefix() != null && source.getName().getHonorificPrefix().length() > 0) {
			destination.setAttribute(OX_TRUSTHONORIFIC_PREFIX, source.getName().getHonorificPrefix());
		}
		if (source.getName().getHonorificSuffix() != null && source.getName().getHonorificSuffix().length() > 0) {
			destination.setAttribute(OX_TRUSTHONORIFIC_SUFFIX, source.getName().getHonorificSuffix());
		}
		log.trace(" setting displayname ");
		if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
			destination.setDisplayName(source.getDisplayName());
		}
		log.trace(" setting externalID ");
		if (source.getExternalId() != null && source.getExternalId().length() > 0) {
			destination.setAttribute(OX_TRUST_EXTERNAL_ID, source.getExternalId());
		}
		log.trace(" setting nickname ");
		if (source.getNickName() != null && source.getNickName().length() > 0) {
			destination.setAttribute(OX_TRUST_NICK_NAME, source.getNickName());
		}
		log.trace(" setting profileURL ");
		if (source.getProfileUrl() != null && source.getProfileUrl().length() > 0) {
			destination.setAttribute(OX_TRUST_PROFILE_URL, source.getProfileUrl());
		}

		// getting emails
		log.trace(" setting emails ");
		if (source.getEmails() != null && source.getEmails().size() > 0) {
			List<ScimPersonEmailsPatch> emails = source.getEmails();
			String[] emailsList = new String[source.getEmails().size()];
			String[] emailsTypes = new String[source.getEmails().size()];
			String[] emailsPrimary = new String[source.getEmails().size()];

			int emailsSize = 0;
			if (destination.getAttributes(OX_TRUST_EMAIL) != null && destination.getAttributes(OX_TRUST_EMAIL).length > 0) {
				emailsList = destination.getAttributes(OX_TRUST_EMAIL);
				emailsTypes = destination.getAttributes(OX_TRUST_EMAIL_TYPE);
				emailsPrimary = destination.getAttributes(OX_TRUST_EMAIL_PRIMARY);
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

			destination.setAttribute(OX_TRUST_EMAIL, emailsList);
			destination.setAttribute(OX_TRUST_EMAIL_TYPE, emailsTypes);
			destination.setAttribute(OX_TRUST_EMAIL_PRIMARY, emailsPrimary);
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

			if (destination.getAttributes(OX_TRUST_STREET) != null && destination.getAttributes(OX_TRUST_STREET).length > 0) {
				street = destination.getAttributes(OX_TRUST_STREET);
				formatted = destination.getAttributes(OX_TRUST_ADDRESS_FORMATTED);
				locality = destination.getAttributes(OX_TRUST_LOCALITY);
				region = destination.getAttributes(OX_TRUST_REGION);
				postalCode = destination.getAttributes(OX_TRUST_POSTAL_CODE);
				country = destination.getAttributes(OX_TRUST_COUNTRY);
				addressType = destination.getAttributes(OX_TRUST_ADDRESS_TYPE);
				addressPrimary = destination.getAttributes(OX_TRUST_ADDRESS_PRIMARY);
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

			destination.setAttribute(OX_TRUST_STREET, street);
			destination.setAttribute(OX_TRUST_LOCALITY, locality);
			destination.setAttribute(OX_TRUST_REGION, region);
			destination.setAttribute(OX_TRUST_POSTAL_CODE, postalCode);
			destination.setAttribute(OX_TRUST_COUNTRY, country);
			destination.setAttribute(OX_TRUST_ADDRESS_FORMATTED, formatted);
			destination.setAttribute(OX_TRUST_ADDRESS_PRIMARY, addressPrimary);
			destination.setAttribute(OX_TRUST_ADDRESS_TYPE, addressType);
		}

		// getting phone numbers;
		log.trace(" setting phoneNumbers ");
		if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
			List<ScimPersonPhonesPatch> phones = source.getPhoneNumbers();
			String[] phoneNumber = new String[source.getPhoneNumbers().size()];
			String[] phoneType = new String[source.getPhoneNumbers().size()];

			int phoneSize = 0;

			if (destination.getAttributes(OX_TRUST_PHONE_VALUE) != null && destination.getAttributes(OX_TRUST_PHONE_VALUE).length > 0) {
				phoneNumber = destination.getAttributes(OX_TRUST_PHONE_VALUE);
				phoneType = destination.getAttributes(OX_TRUST_PHONE_TYPE);
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
			destination.setAttribute(OX_TRUST_PHONE_VALUE, phoneNumber);
			destination.setAttribute(OX_TRUST_PHONE_TYPE, phoneType);
		}

		// getting ims
		log.trace(" setting ims ");
		if (source.getIms() != null && source.getIms().size() > 0) {
			List<ScimPersonImsPatch> ims = source.getIms();
			String[] imValue = new String[source.getIms().size()];
			String[] imType = new String[source.getIms().size()];

			int imSize = 0;
			if (destination.getAttributes(OX_TRUST_IMS_VALUE) != null && destination.getAttributes(OX_TRUST_IMS_VALUE).length > 0) {
				imValue = destination.getAttributes(OX_TRUST_IMS_VALUE);
				imType = destination.getAttributes("oxTrustImsType");
				imSize = destination.getAttributes(OX_TRUST_IMS_VALUE).length;
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
			destination.setAttribute(OX_TRUST_IMS_VALUE, imValue);
			destination.setAttribute("oxTrustImsType", imType);
		}

		// getting Photos
		log.trace(" setting photos ");
		if (source.getPhotos() != null && source.getPhotos().size() > 0) {
			List<ScimPersonPhotosPatch> photos = source.getPhotos();
			String[] photoType = new String[source.getPhotos().size()];
			String[] photoValue = new String[source.getPhotos().size()];

			int photoSize = 0;
			if (destination.getAttributes(OX_TRUST_PHOTOS) != null && destination.getAttributes(OX_TRUST_PHOTOS).length > 0) {
				photoType = destination.getAttributes(OX_TRUST_PHOTOS_TYPE);
				photoValue = destination.getAttributes(OX_TRUST_PHOTOS);
				photoSize = destination.getAttributes(OX_TRUST_PHOTOS_TYPE).length;
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
			destination.setAttribute(OX_TRUST_PHOTOS_TYPE, photoType);
			destination.setAttribute(OX_TRUST_PHOTOS, photoValue);
		}

		if (source.getUserType() != null && source.getUserType().length() > 0) {
			destination.setAttribute(OX_TRUST_USER_TYPE, source.getUserType());
		}
		if (source.getTitle() != null && source.getTitle().length() > 0) {
			destination.setAttribute(OX_TRUST_TITLE, source.getTitle());
		}
		if (source.getPreferredLanguage() != null && source.getPreferredLanguage().length() > 0) {
			destination.setPreferredLanguage(source.getPreferredLanguage());
		}
		if (source.getLocale() != null && source.getLocale().length() > 0) {
			destination.setAttribute(OX_TRUST_LOCALE, source.getLocale());
		}
		if (source.getTimezone() != null && source.getTimezone().length() > 0) {
			destination.setTimezone(source.getTimezone());
		}
		if (source.getActive() != null && source.getActive().length() > 0) {
			destination.setAttribute(OX_TRUST_ACTIVE, source.getActive());
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

			if (destination.getAttributes(OX_TRUST_ROLE) != null && destination.getAttributes(OX_TRUST_ROLE).length > 0) {
				scimRole = destination.getAttributes(OX_TRUST_ROLE);
				rolesSize = destination.getAttributes(OX_TRUST_ROLE).length;
			}

			for (ScimRoles role : roles) {

				if (role.getValue() != null && role.getValue().length() > 0) {
					scimRole[rolesSize] = role.getValue();
				}
				rolesSize++;
			}
			destination.setAttribute(OX_TRUST_ROLE, scimRole);
		}

		// getting entitlements
		log.trace(" setting entitlements ");
		if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
			List<ScimEntitlementsPatch> ents = source.getEntitlements();
			String[] listEnts = new String[source.getEntitlements().size()];

			int entsSize = 0;

			if (destination.getAttributes(OX_TRUST_ENTITLEMENTS) != null
					&& destination.getAttributes(OX_TRUST_ENTITLEMENTS).length > 0) {
				listEnts = destination.getAttributes(OX_TRUST_ENTITLEMENTS);
				entsSize = destination.getAttributes(OX_TRUST_ENTITLEMENTS).length;
			}

			for (ScimEntitlements ent : ents) {
				if (ent.getValue() != null && ent.getValue().length() > 0) {
					listEnts[entsSize] = ent.getValue();
				}
				entsSize++;
			}
			destination.setAttribute(OX_TRUST_ENTITLEMENTS, listEnts);
		}

		// getting x509Certificates
		log.trace(" setting certs ");
		if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
			List<Scimx509CertificatesPatch> certs = source.getX509Certificates();
			String[] listCerts = new String[source.getX509Certificates().size()];
			int certsSize = 0;
			if (destination.getAttributes(OX_TRUSTX509_CERTIFICATE) != null
					&& destination.getAttributes(OX_TRUSTX509_CERTIFICATE).length > 0) {
				listCerts = destination.getAttributes(OX_TRUSTX509_CERTIFICATE);
				certsSize = destination.getAttributes(OX_TRUSTX509_CERTIFICATE).length;
			}

			for (Scimx509Certificates cert : certs) {
				if (cert.getValue() != null && cert.getValue().length() > 0) {
					listCerts[certsSize] = cert.getValue();
				}
				certsSize++;
			}

			destination.setAttribute(OX_TRUSTX509_CERTIFICATE, listCerts);
		}

		// getting meta
		log.trace(" setting meta ");

		if (source.getMeta().getCreated() != null && source.getMeta().getCreated().length() > 0) {
			destination.setAttribute(OX_TRUST_META_CREATED, source.getMeta().getCreated());
		}
		if (source.getMeta().getLastModified() != null && source.getMeta().getLastModified().length() > 0) {
			destination.setAttribute(OX_TRUST_META_LAST_MODIFIED, source.getMeta().getLastModified());
		}
		if (source.getMeta().getVersion() != null && source.getMeta().getVersion().length() > 0) {
			destination.setAttribute(OX_TRUST_META_VERSION, source.getMeta().getVersion());
		}
		if (source.getMeta().getLocation() != null && source.getMeta().getLocation().length() > 0) {
			destination.setAttribute(OX_TRUST_META_LOCATION, source.getMeta().getLocation());
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

	public static GluuGroup copy(ScimGroup source, GluuGroup destination, boolean isUpdate) throws Exception {
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
				List<ScimGroupMembers> members = source.getMembers();
				List<String> listMembers = new ArrayList<String>();
				for (ScimGroupMembers member : members) {
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
				List<ScimGroupMembers> members = source.getMembers();
				List<String> listMembers = new ArrayList<String>();
				for (ScimGroupMembers member : members) {
					listMembers.add(personService.getDnForPerson(member.getValue()));
				}

				destination.setMembers(listMembers);

			}

			destination.setStatus(GluuStatus.ACTIVE);
			OrganizationService orgService = OrganizationService.instance();
			destination.setOrganization(orgService.getDnForOrganization());

		}

		return destination;

	}

	public static boolean isValidData(ScimGroup group, boolean isUpdate) {
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
	public static ScimGroup copy(ScimData source, ScimGroup destination) {
		if (source == null) {
			return null;
		}
		if (destination == null) {
			destination = new ScimGroup();
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

	private static void setGluuStatus(ScimPerson source, GluuCustomPerson destination) {
		String active = source.getActive();
		setGluuStatus(destination, active);
	}

	private static void setGluuStatus(ScimPersonPatch source, GluuCustomPerson destination) {
		String active = source.getActive();
		setGluuStatus(destination, active);
	}

	private static void setGluuStatus(GluuCustomPerson destination, String active) {
		if (StringHelper.isNotEmpty(active) && (destination.getAttribute(GLUU_STATUS) == null)) {
			GluuBoolean gluuStatus = GluuBoolean.getByValue(org.xdi.util.StringHelper.toLowerCase(active));
			if (gluuStatus != null) {
				destination.setAttribute(GLUU_STATUS, gluuStatus.getValue());
			}
		}
	}

}
