/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.exception.PersonRequiredFieldsException;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.Person;
import org.gluu.oxtrust.model.PersonAttribute;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.Address;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Email;
import org.gluu.oxtrust.model.scim2.Entitlement;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.GroupRef;
import org.gluu.oxtrust.model.scim2.Im;
import org.gluu.oxtrust.model.scim2.MemberRef;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.MultiValuedAttribute;
import org.gluu.oxtrust.model.scim2.PhoneNumber;
import org.gluu.oxtrust.model.scim2.Photo;
import org.gluu.oxtrust.model.scim2.Role;
import org.gluu.oxtrust.model.scim2.ScimData;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.X509Certificate;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.GluuUserRole;
import org.xdi.model.OxMultivalued;
import org.xdi.util.StringHelper;

@Stateless
@Named
public class CopyUtils2 implements Serializable {

    private static final long serialVersionUID = -1715995162448707004L;

    private Logger log= LoggerFactory.getLogger(getClass());

    @Inject
    private OrganizationService organizationService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private IPersonService personService;

    @Inject
    private IGroupService groupService;

    @Inject
    private AttributeService attributeService;

    /**
     * Copy data from Person object to GluuCustomPerson object
     *
     * @param source
     * @param destination
     * @return
     * @throws Exception
     */
    public GluuCustomPerson copy(Person source, GluuCustomPerson destination, List<GluuAttribute> attributes, GluuUserRole role,
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

    public GluuCustomPerson copy(User source, GluuCustomPerson destination, boolean isUpdate) throws Exception {
        if (source == null || !isValidData(source, isUpdate)) {
            return null;
        }

        if (destination == null) {
            log.trace(" creating a new GluuCustomPerson instant ");
            destination = new GluuCustomPerson();
        }

        log.trace(" setting schemas ");
        destination.setSchemas(source.getSchemas());

        if (isUpdate) {

            personService.addCustomObjectClass(destination);

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
                destination.setAttribute("middleName", StringUtils.isEmpty(source.getName().getMiddleName()) ? null : source.getName().getMiddleName());

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

				/*
				List<Email> emails = source.getEmails();

				// StringWriter listOfEmails = new StringWriter();
				// mapper.writeValue(listOfEmails, emails);

				List<String> emailList = new ArrayList<String>();
				for (Email email : emails) {
					emailList.add(mapper.writeValueAsString(email));
				}

				// destination.setAttribute("oxTrustEmail", listOfEmails.toString());
				destination.setAttribute("oxTrustEmail", emailList.toArray(new String[]{}));
				*/

                setAttributeListValue(destination, source.getEmails(), "oxTrustEmail");
            }

            // getting addresses
            log.trace(" setting addresses ");
            if (source.getAddresses() != null && source.getAddresses().size() > 0) {
                setAttributeListValue(destination, source.getAddresses(), "oxTrustAddresses");
            }

            // getting phone numbers;
            log.trace(" setting phoneNumbers ");
            if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
                setAttributeListValue(destination, source.getPhoneNumbers(), "oxTrustPhoneValue");
            }

            // getting ims
            log.trace(" setting ims ");
            if (source.getIms() != null && source.getIms().size() > 0) {
                setAttributeListValue(destination, source.getIms(), "oxTrustImsValue");
            }

            // getting Photos
            log.trace(" setting photos ");
            if (source.getPhotos() != null && source.getPhotos().size() > 0) {
                setAttributeListValue(destination, source.getPhotos(), "oxTrustPhotos");
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
                setAttributeListValue(destination, source.getRoles(), "oxTrustRole");
            }

            // getting entitlements
            log.trace(" setting entitlements ");
            if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
                setAttributeListValue(destination, source.getEntitlements(), "oxTrustEntitlements");
            }

            // getting x509Certificates
            log.trace(" setting certs ");
            if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
                setAttributeListValue(destination, source.getX509Certificates(), "oxTrustx509Certificate");
            }

            log.trace(" setting extensions ");
            if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
                destination.setExtensions(source.getExtensions());
            }

            log.trace(" setting PPIDs");
            destination.setOxPPID(source.getPairwiseIdentitifers());
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

                if (personService.getPersonByUid(source.getUserName()) != null) {
                    throw new DuplicateEntryException("Duplicate UID value: " + source.getUserName());
                }

                personService.addCustomObjectClass(destination);

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
                    destination.setAttribute("nickname", source.getNickName());
                }
                log.trace(" setting profileURL ");
                if (source.getProfileUrl() != null && source.getProfileUrl().length() > 0) {
                    destination.setAttribute("oxTrustProfileURL", source.getProfileUrl());
                }

                // getting emails
                log.trace(" setting emails ");
                if (source.getEmails() != null && source.getEmails().size() > 0) {

					/*
					List<Email> emails = source.getEmails();

					// StringWriter listOfEmails = new StringWriter();
					// mapper.writeValue(listOfEmails, emails);

					List<String> emailList = new ArrayList<String>();
					for (Email email : emails) {
						emailList.add(mapper.writeValueAsString(email));
					}

					// destination.setAttribute("oxTrustEmail", listOfEmails.toString());
					destination.setAttribute("oxTrustEmail", emailList.toArray(new String[]{}));
					*/

                    setAttributeListValue(destination, source.getEmails(), "oxTrustEmail");
                }

                // getting addresses
                log.trace(" setting addresses ");
                if (source.getAddresses() != null && source.getAddresses().size() > 0) {
                    setAttributeListValue(destination, source.getAddresses(), "oxTrustAddresses");
                }

                // getting phone numbers;
                log.trace(" setting phoneNumbers ");
                if (source.getPhoneNumbers() != null && source.getPhoneNumbers().size() > 0) {
                    setAttributeListValue(destination, source.getPhoneNumbers(), "oxTrustPhoneValue");
                }

                // getting ims
                log.trace(" setting ims ");
                if (source.getIms() != null && source.getIms().size() > 0) {
                    setAttributeListValue(destination, source.getIms(), "oxTrustImsValue");
                }

                // getting Photos
                log.trace(" setting photos ");
                if (source.getPhotos() != null && source.getPhotos().size() > 0) {
                    setAttributeListValue(destination, source.getPhotos(), "oxTrustPhotos");
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
                    setAttributeListValue(destination, source.getRoles(), "oxTrustRole");
                }

                // getting entitlements
                log.trace(" setting entitlements ");
                if (source.getEntitlements() != null && source.getEntitlements().size() > 0) {
                    setAttributeListValue(destination, source.getEntitlements(), "oxTrustEntitlements");
                }

                // getting x509Certificates
                log.trace(" setting certs ");
                if (source.getX509Certificates() != null && source.getX509Certificates().size() > 0) {
                    setAttributeListValue(destination, source.getX509Certificates(), "oxTrustx509Certificate");
                }

                log.trace(" setting extensions ");
                if (source.getExtensions() != null && (source.getExtensions().size() > 0)) {
                    destination.setExtensions(source.getExtensions());
                }

                log.trace(" setting PPIDs");
                destination.setOxPPID(source.getPairwiseIdentitifers());

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

            } catch (DuplicateEntryException e) {
                throw e;
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
    public Person copy(GluuCustomPerson source, Person destination, List<GluuAttribute> attributes) {
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
    public User copy(GluuCustomPerson source, User destination) throws Exception {

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
        // source = Utils.syncEmailReverse(source, true);
        if (source.getAttributeArray("oxTrustEmail") != null) {

			/*
			String[] emailArray = source.getAttributeArray("oxTrustEmail");
			List<Email> emails = new ArrayList<Email>();

			for (String emailStr : emailArray) {
				Email email = mapper.readValue(emailStr, Email.class);
				emails.add(email);
			}

			// List<Email> listOfEmails = mapper.readValue(source.getAttribute("oxTrustEmail"), new TypeReference<List<Email>>(){});
			// destination.setEmails(listOfEmails);
			*/

            List<Email> emails = getAttributeListValue(source, Email.class, "oxTrustEmail");
            destination.setEmails(emails);
        }

        log.trace(" getting addresses ");
        // getting addresses
        if (source.getAttribute("oxTrustAddresses") != null) {
            List<Address> addresses = getAttributeListValue(source, Address.class, "oxTrustAddresses");
            destination.setAddresses(addresses);
        }

        log.trace(" setting phoneNumber ");
        // getting user's PhoneNumber
        if (source.getAttribute("oxTrustPhoneValue") != null) {
            List<PhoneNumber> phoneNumbers = getAttributeListValue(source, PhoneNumber.class, "oxTrustPhoneValue");
            destination.setPhoneNumbers(phoneNumbers);
        }

        if ((source.getOxPPID()) != null) {
            destination.setPairwiseIdentitifers(source.getOxPPID());
        }

        log.trace(" getting ims ");
        // getting ims
        if (source.getAttribute("oxTrustImsValue") != null) {
            List<Im> ims = getAttributeListValue(source, Im.class, "oxTrustImsValue");
            destination.setIms(ims);
        }

        log.trace(" setting photos ");
        // getting photos
        if (source.getAttribute("oxTrustPhotos") != null) {
            List<Photo> photos = getAttributeListValue(source, Photo.class, "oxTrustPhotos");
            destination.setPhotos(photos);
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
            List<String> listOfGroups = source.getMemberOf();
            List<GroupRef> groupRefList = new ArrayList<GroupRef>();

            for (String groupDN : listOfGroups) {

                GluuGroup gluuGroup = groupService.getGroupByDn(groupDN);

                GroupRef groupRef = new GroupRef();
                groupRef.setDisplay(gluuGroup.getDisplayName());
                groupRef.setValue(gluuGroup.getInum());
                String reference = appConfiguration.getBaseEndpoint() + "/scim/v2/Groups/" + gluuGroup.getInum();
                groupRef.setReference(reference);

                groupRefList.add(groupRef);
            }

            destination.setGroups(groupRefList);
        }

        // getting roles
        if (source.getAttribute("oxTrustRole") != null) {
            List<Role> roles = getAttributeListValue(source, Role.class, "oxTrustRole");
            destination.setRoles(roles);
        }

        log.trace(" getting entitlements ");
        // getting entitlements
        if (source.getAttribute("oxTrustEntitlements") != null) {
            List<Entitlement> entitlements = getAttributeListValue(source, Entitlement.class, "oxTrustEntitlements");
            destination.setEntitlements(entitlements);
        }

        // getting x509Certificates
        log.trace(" setting certs ");
        if (source.getAttribute("oxTrustx509Certificate") != null) {
            List<X509Certificate> x509Certificates = getAttributeListValue(source, X509Certificate.class, "oxTrustx509Certificate");
            destination.setX509Certificates(x509Certificates);
        }

        log.trace(" setting extensions ");

        // List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributesImpl(attributeService.getCustomAttributes());
        List<GluuAttribute> scimCustomAttributes = attributeService.getSCIMRelatedAttributes();

        if (scimCustomAttributes != null && !scimCustomAttributes.isEmpty()) {

            Map<String, Extension> extensionMap = new HashMap<String, Extension>();
            Extension.Builder extensionBuilder = new Extension.Builder(Constants.USER_EXT_SCHEMA_ID);

            boolean hasExtension = false;

            outer:
            for (GluuCustomAttribute customAttribute : source.getCustomAttributes()) {

                for (GluuAttribute scimCustomAttribute : scimCustomAttributes) {

                    if (customAttribute.getName().equals(scimCustomAttribute.getName())) {
                        log.debug("Found custom attribute {}", customAttribute.getName());
                        hasExtension = true;
                        GluuAttributeDataType scimCustomAttributeDataType = scimCustomAttribute.getDataType();

                        if ((scimCustomAttribute.getOxMultivaluedAttribute() != null) && scimCustomAttribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE)) {
                            log.debug("Multivalued cust. attribute contents: {}", customAttribute.getValues());
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
                location = appConfiguration.getBaseEndpoint() + location;
            }
        } else {
            location = appConfiguration.getBaseEndpoint() + "/scim/v2/Users/" + source.getInum();
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
     * Copy the User Password
     *
     * @param person
     * @param password
     * @return
     */
    public GluuCustomPerson updatePassword(GluuCustomPerson person, String password) {
        try {
            person.setUserPassword(password);
        } catch (Exception ex) {
            return null;
        }
        return person;
    }

    // -

    public GluuAttribute getAttribute(List<GluuAttribute> attributes, String attributeName) {
        GluuAttribute gluuAttribute = null;
        for (GluuAttribute gluuAttr : attributes) {
            if (attributeName.equalsIgnoreCase(gluuAttr.getName())) {
                gluuAttribute = gluuAttr;
                break;
            }
        }
        return gluuAttribute;
    }

    public boolean containsRole(GluuUserRole[] roles, GluuUserRole role) {
        for (int i = 0; i < roles.length; i++) {
            if (roles[i] == role)
                return true;
        }
        return false;
    }

    public boolean isValidData(Person person, boolean isUpdate) {
        if (isUpdate) {
            // if (isEmpty(person.getFirstName()) ||
            // isEmpty(person.getDisplayName())
            // || isEmpty(person.getLastName())
            // || isEmpty(person.getEmail())) {
            // return false;
            // }
        } else if (isEmpty(person.getUserId()) || isEmpty(person.getFirstName()) || isEmpty(person.getDisplayName())
                // || isEmpty(person.getLastName()) || isEmpty(person.getEmail()) || isEmpty(person.getPassword())) {
                || isEmpty(person.getLastName()) || isEmpty(person.getEmail())) {
            return false;
        }
        return true;
    }

    public boolean isValidData(User person, boolean isUpdate) throws Exception {

        if (isUpdate) {
            // if (isEmpty(person.getFirstName()) ||
            // isEmpty(person.getDisplayName())
            // || isEmpty(person.getLastName())
            // || isEmpty(person.getEmail())) {
            // return false;
            // }
        } else if (isEmpty(person.getUserName()) || isEmpty(person.getName().getGivenName()) || isEmpty(person.getDisplayName())
                // || (person.getEmails() == null || person.getEmails().size() < 1) || isEmpty(person.getPassword())
                || isEmpty(person.getName().getFamilyName())) {

            String message = "There are missing required parameters: userName, givenName, displayName, or familyName";
            throw new PersonRequiredFieldsException(message);
            // return false;
        }

        return true;
    }

    public boolean isEmpty(String value) {
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

    public Group copy(GluuGroup source, Group destination) throws Exception {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            destination = new Group();
        }

        destination.setDisplayName(source.getDisplayName());
        destination.setId(source.getInum());

        if (source.getMembers() != null) {

            if (source.getMembers().size() > 0) {

                Set<MemberRef> memberRefSet = new HashSet<MemberRef>();
                List<String> membersList = source.getMembers();

                for (String oneMember : membersList) {

                    if (oneMember != null && !oneMember.isEmpty()) {

                        GluuCustomPerson gluuCustomPerson = personService.getPersonByDn(oneMember);

                        MemberRef memberRef = new MemberRef();
                        memberRef.setValue(gluuCustomPerson.getInum());
                        memberRef.setDisplay(gluuCustomPerson.getDisplayName());
                        String reference = appConfiguration.getBaseEndpoint() + "/scim/v2/Users/" + gluuCustomPerson.getInum();
                        memberRef.setReference(reference);

                        memberRefSet.add(memberRef);
                    }
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
                location = appConfiguration.getBaseEndpoint() + location;
            }
        } else {
            location = appConfiguration.getBaseEndpoint() + "/scim/v2/Groups/" + source.getInum();
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

    public GluuGroup copy(Group source, GluuGroup destination, boolean isUpdate) throws Exception {
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
                Set<MemberRef> members = source.getMembers();
                List<String> listMembers = new ArrayList<String>();
                for (MemberRef member : members) {
                    listMembers.add(personService.getDnForPerson(member.getValue()));
                }

                destination.setMembers(listMembers);
            }

        } else {

            log.trace(" creating a new GroupService instant ");

            log.trace(" source.getDisplayName() : ", source.getDisplayName());

            if (groupService.getGroupByDisplayName(source.getDisplayName()) != null) {
                log.trace(" groupService1.getGroupByDisplayName(source.getDisplayName() != null : ");

                return null;
            }
            if (source.getDisplayName() != null && source.getDisplayName().length() > 0) {
                destination.setDisplayName(source.getDisplayName());
            }

            log.trace(" source.getMembers() : ", source.getMembers());
            log.trace(" source.getMembers().size() : ", source.getMembers().size());

            if (source.getMembers() != null && source.getMembers().size() > 0) {
                Set<MemberRef> members = source.getMembers();
                List<String> listMembers = new ArrayList<String>();
                for (MemberRef member : members) {
                    listMembers.add(personService.getDnForPerson(member.getValue()));
                }

                destination.setMembers(listMembers);
            }

			/*GluuCustomPerson authUser = (GluuCustomPerson) identity.getSessionMap().get(OxTrustConstants.CURRENT_PERSON);
			destination.setOwner(authUser.getDn());
			log.trace(" authUser.getDn() : ", authUser.getDn());*/
            destination.setStatus(GluuStatus.ACTIVE);
            destination.setOrganization(organizationService.getDnForOrganization());
        }

        return destination;
    }

    public boolean isValidData(Group group, boolean isUpdate) {
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
    public Group copy(ScimData source, Group destination) {
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

    public FidoDevice copy(GluuCustomFidoDevice source, FidoDevice destination) {

        if (source == null) {
            return null;
        }

        if (destination == null) {
            destination = new FidoDevice();
        }

        destination.setId(source.getId());
        destination.setCreationDate(source.getCreationDate());
        destination.setApplication(source.getApplication());
        destination.setCounter(source.getCounter());
        destination.setDeviceData(source.getDeviceData());
        destination.setDeviceHashCode(source.getDeviceHashCode());
        destination.setDeviceKeyHandle(source.getDeviceKeyHandle());
        destination.setDeviceRegistrationConf(source.getDeviceRegistrationConf());
        destination.setLastAccessTime(source.getLastAccessTime());
        destination.setStatus(source.getStatus());
        destination.setDisplayName(source.getDisplayName());
        destination.setDescription(source.getDescription());
        destination.setNickname(source.getNickname());
      
        if (source.getDn() != null) {
            String[] dnArray = source.getDn().split("\\,");
            for (String e : dnArray) {
                if (e.startsWith("inum=")) {
                    String[] inumArray = e.split("\\=");
                    if (inumArray.length > 1) {
                        destination.setUserId(inumArray[1]);
                    }
                }
            }
        }

        Meta meta = (destination.getMeta() != null) ? destination.getMeta() : new Meta();

        if (source.getMetaVersion() != null) {
            meta.setVersion(source.getMetaVersion());
        }

        String location = source.getMetaLocation();
        if (location != null && !location.isEmpty()) {
            if (!location.startsWith("https://") && !location.startsWith("http://")) {
                location = appConfiguration.getBaseEndpoint() + location;
            }
        } else {
            location = appConfiguration.getBaseEndpoint() + "/scim/v2/FidoDevices/" + source.getId();
        }
        meta.setLocation(location);

        if (source.getCreationDate() != null && !source.getCreationDate().isEmpty()) {

            try {
                meta.setCreated(new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'").parse(source.getCreationDate()));
            } catch (Exception e) {
                log.error(" Date parse exception (OLD format)", e);
            }
        }

        if (source.getMetaLastModified() != null && !source.getMetaLastModified().isEmpty()) {

            try {
                DateTime dateTimeUtc = new DateTime(source.getMetaLastModified(), DateTimeZone.UTC);
                meta.setLastModified(dateTimeUtc.toDate());
            } catch (Exception e) {
                log.error(" Date parse exception (NEW format), continuing...", e);
                try {
                    meta.setLastModified(new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'").parse(source.getMetaLastModified()));
                } catch (Exception ex) {
                    log.error(" Date parse exception (OLD format)", ex);
                }
            }
        }

        destination.setMeta(meta);

        return destination;
    }

    public GluuCustomFidoDevice updateGluuCustomFidoDevice(FidoDevice source, GluuCustomFidoDevice destination) {

        if (source == null) {
            return null;
        }

        if (destination == null) {
            destination = new GluuCustomFidoDevice();
        }

        // Only update displayName, description and nickname
        // All the other fields are not editable
        destination.setDisplayName(source.getDisplayName());
        destination.setDescription(source.getDescription());
        destination.setNickname(source.getNickname());
      
        return destination;
    }

    protected void setGluuStatus(User source, GluuCustomPerson destination) {
        Boolean active = source.isActive();
        if (active != null) {
            if (active.equals(Boolean.TRUE)) {
                setGluuStatus(destination, GluuBoolean.ACTIVE.getValue());
            } else {
                setGluuStatus(destination, GluuBoolean.INACTIVE.getValue());
            }
        }
    }

    private void setGluuStatus(GluuCustomPerson destination, String active) {
        // if (StringHelper.isNotEmpty(active) && (destination.getAttribute("gluuStatus") == null)) {
        if (StringHelper.isNotEmpty(active)) {
            GluuBoolean gluuStatus = GluuBoolean.getByValue(org.xdi.util.StringHelper.toLowerCase(active));
            if (gluuStatus != null) {
                destination.setAttribute("gluuStatus", gluuStatus.getValue());
            }
        }
    }

    protected <T extends MultiValuedAttribute> void setAttributeListValue(GluuCustomPerson destination, List<T> items, String attributeName) throws Exception {

        ObjectMapper mapper = ServiceUtil.getObjectMapper();

        List<String> itemList = new ArrayList<String>();
        for (T item : items) {
            itemList.add(mapper.writeValueAsString(item));
        }

        destination.setAttribute(attributeName, itemList.toArray(new String[]{}));
    }

    protected <T extends MultiValuedAttribute> List<T> getAttributeListValue(GluuCustomPerson source, Class<T> clazz, String attributeName) throws Exception {

        ObjectMapper mapper = ServiceUtil.getObjectMapper();

        String[] attributeArray = source.getAttributeArray(attributeName);
        List<T> items = new ArrayList<T>();
        if(attributeArray == null) {
            return null;
        }

        for (String attribute : attributeArray) {
            T item = mapper.readValue(attribute, clazz);
            items.add(item);
        }

        // List<Email> listOfEmails = mapper.readValue(source.getAttribute("oxTrustEmail"), new TypeReference<List<Email>>(){});
        // destination.setEmails(listOfEmails);

        return items;
    }

}