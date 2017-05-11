/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.scim2.Operation;
import org.gluu.oxtrust.model.scim2.ScimPatchUser;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.PatchUtil;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

/**
 * Centralizes calls by the UserWebService and BulkWebService service classes
 *
 * @author Val Pecaoco
 */
@Stateless
@Named
public class Scim2UserService implements Serializable {

    @Inject
    private Logger log;

    @Inject
    private IPersonService personService;

    @Inject
    private ExternalScimService externalScimService;

    @Inject
    private CopyUtils2 copyUtils2;
    
    @Inject
    private ServiceUtil serviceUtil;
    
    @Inject
    private PatchUtil patchUtil;

    public User createUser(User user) throws Exception {
        log.debug(" copying gluuperson ");
        GluuCustomPerson gluuPerson = copyUtils2.copy(user, null, false);
        if (gluuPerson == null) {
            throw new Exception("Scim2UserService.createUser(): Failed to create user; GluuCustomPerson is null");
        }

        log.debug(" generating inum ");
        String inum = personService.generateInumForNewPerson(); // inumService.generateInums(Configuration.INUM_TYPE_PEOPLE_SLUG);
        // //personService.generateInumForNewPerson();
        log.debug(" getting DN ");
        String dn = personService.getDnForPerson(inum);

        log.debug(" getting iname ");
        String iname = personService.generateInameForNewPerson(user.getUserName());

        log.debug(" setting dn ");
        gluuPerson.setDn(dn);

        log.debug(" setting inum ");
        gluuPerson.setInum(inum);

        log.debug(" setting iname ");
        gluuPerson.setIname(iname);

        log.debug(" setting commonName ");
        gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

        log.info("gluuPerson.getMemberOf().size() : " + gluuPerson.getMemberOf().size());
        if (user.getGroups().size() > 0) {
            log.info(" jumping to groupMembersAdder ");
            log.info("gluuPerson.getDn() : " + gluuPerson.getDn());
            serviceUtil.groupMembersAdder(gluuPerson, gluuPerson.getDn());
        }

        // As per spec, the SP must be the one to assign the meta attributes
        log.info(" Setting meta: create user ");
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
        Date dateCreated = DateTime.now().toDate();
        String relativeLocation = "/scim/v2/Users/" + inum;
        gluuPerson.setAttribute("oxTrustMetaCreated", dateTimeFormatter.print(dateCreated.getTime()));
        gluuPerson.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateCreated.getTime()));
        gluuPerson.setAttribute("oxTrustMetaLocation", relativeLocation);

        // Sync email, forward ("oxTrustEmail" -> "mail")
        gluuPerson = serviceUtil.syncEmailForward(gluuPerson, true);

        // For custom script: create user
        if (externalScimService.isEnabled()) {
            externalScimService.executeScimCreateUserMethods(gluuPerson);
        }

        log.debug("adding new GluuPerson");
        personService.addPerson(gluuPerson);

        User createdUser = copyUtils2.copy(gluuPerson, null);

        return createdUser;
    }

    public User updateUser(String id, User user) throws Exception {
        GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
        if (gluuPerson == null) {

            throw new EntryPersistenceException("Scim2UserService.updateUser(): " + "Resource " + id + " not found");

        } else {

            // Validate if attempting to update userName of a different id
            if (user.getUserName() != null) {

                GluuCustomPerson personToFind = new GluuCustomPerson();
                personToFind.setUid(user.getUserName());

                List<GluuCustomPerson> foundPersons = personService.findPersons(personToFind, 2);
                if (foundPersons != null && foundPersons.size() > 0) {
                    for (GluuCustomPerson foundPerson : foundPersons) {
                        if (foundPerson != null && !foundPerson.getInum().equalsIgnoreCase(gluuPerson.getInum())) {
                            throw new DuplicateEntryException("Cannot update userName of a different id: " + user.getUserName());
                        }
                    }
                }
            }
        }

        GluuCustomPerson updatedGluuPerson = copyUtils2.copy(user, gluuPerson, true);

        if (user.getGroups().size() > 0) {
            serviceUtil.groupMembersAdder(updatedGluuPerson, personService.getDnForPerson(id));
        }

        log.info(" Setting meta: update user ");
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
        Date dateLastModified = DateTime.now().toDate();
        updatedGluuPerson.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
        if (updatedGluuPerson.getAttribute("oxTrustMetaLocation") == null || (updatedGluuPerson.getAttribute("oxTrustMetaLocation") != null && updatedGluuPerson.getAttribute("oxTrustMetaLocation").isEmpty())) {
            String relativeLocation = "/scim/v2/Users/" + id;
            updatedGluuPerson.setAttribute("oxTrustMetaLocation", relativeLocation);
        }

        // Sync email, forward ("oxTrustEmail" -> "mail")
        updatedGluuPerson = serviceUtil.syncEmailForward(updatedGluuPerson, true);

        // For custom script: update user
        if (externalScimService.isEnabled()) {
            externalScimService.executeScimUpdateUserMethods(updatedGluuPerson);
        }

        personService.updatePerson(updatedGluuPerson);

        log.debug(" person updated ");

        User updatedUser = copyUtils2.copy(updatedGluuPerson, null);

        return updatedUser;
    }

    public void deleteUser(String id) throws Exception {
        GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
        if (gluuPerson == null) {

            throw new EntryPersistenceException("Scim2UserService.deleteUser(): " + "Resource " + id + " not found");

        } else {

            // For custom script: delete user
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimDeleteUserMethods(gluuPerson);
            }

            log.info("person.getMemberOf().size() : " + gluuPerson.getMemberOf().size());
            if (gluuPerson.getMemberOf() != null) {

                if (gluuPerson.getMemberOf().size() > 0) {

                    String dn = personService.getDnForPerson(id);
                    log.info("DN : " + dn);

                    serviceUtil.deleteUserFromGroup(gluuPerson, dn);
                }
            }

            personService.removePerson(gluuPerson);
        }
    }
    

    public User patchUser(String id, ScimPatchUser patchUser) throws Exception {
    	
    	for(Operation operation : patchUser.getOperatons()){
    		String val = operation.getOperationName();
    		
    		if(val.equalsIgnoreCase("replace")){
    			replaceUserPatch(operation,id);
    		}
    		
    		if(val.equalsIgnoreCase("remove")){
    			removeUserPatch(operation,id);
    		}
    		
    		if(val.equalsIgnoreCase("add")){
    			addUserPatch(operation,id);
    		}       
    		
    	}

    	GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
    	User updatedUser = copyUtils2.copy(gluuPerson, null);   	
    	
		return updatedUser;  	
    }
    
   private void removeUserPatch(Operation operation,String id) throws Exception{	   
	   User user = operation.getValue();	
		
		GluuCustomPerson updatedGluuPerson = patchUtil.removePatch(user, validUsernameByInum(user, id));
		log.info(" Setting meta: removeUserPatch update user ");
		setMeta(updatedGluuPerson);    	
    }
   
	private void replaceUserPatch(Operation operation, String id) throws Exception {
		User user = operation.getValue();		
		
		GluuCustomPerson updatedGluuPerson = patchUtil.replacePatch(user, validUsernameByInum(user, id));
		log.info(" Setting meta: replaceUserPatch update user ");
		setMeta(updatedGluuPerson);
	}
   
	private void addUserPatch(Operation operation, String id) throws Exception {
		User user = operation.getValue();		
		
		GluuCustomPerson updatedGluuPerson = patchUtil.addPatch(user, validUsernameByInum(user, id));
		log.info(" Setting meta: addUserPatch update user ");
		setMeta(updatedGluuPerson);
	}
	
	private GluuCustomPerson validUsernameByInum(User user,String id) throws DuplicateEntryException{
		GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
		if (gluuPerson == null) {

			throw new EntryPersistenceException("Scim2UserService.updateUser(): " + "Resource " + id + " not found");

		} else {

			// Validate if attempting to update userName of a different id
			if (user.getUserName() != null) {

				GluuCustomPerson personToFind = new GluuCustomPerson();
				personToFind.setUid(user.getUserName());

				List<GluuCustomPerson> foundPersons = personService	.findPersons(personToFind, 2);
				if (foundPersons != null && foundPersons.size() > 0) {
					for (GluuCustomPerson foundPerson : foundPersons) {
						if (foundPerson != null && !foundPerson.getInum().equalsIgnoreCase(gluuPerson.getInum())) {
							throw new DuplicateEntryException("Cannot update userName of a different id: "+ user.getUserName());
						}
					}
				}
			}
		}
		return gluuPerson;
		
	}
	
	private void setMeta(GluuCustomPerson updatedGluuPerson) throws Exception{
		
		DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC(); // Date should be in UTC format
		Date dateLastModified = DateTime.now().toDate();
		updatedGluuPerson.setAttribute("oxTrustMetaLastModified",dateTimeFormatter.print(dateLastModified.getTime()));
		if (updatedGluuPerson.getAttribute("oxTrustMetaLocation") == null 
				|| (updatedGluuPerson.getAttribute("oxTrustMetaLocation") != null 
				&& updatedGluuPerson.getAttribute("oxTrustMetaLocation").isEmpty())) {

			String relativeLocation = "/scim/v2/Users/" + updatedGluuPerson.getInum();
			updatedGluuPerson.setAttribute("oxTrustMetaLocation",relativeLocation);
		}
		updatedGluuPerson = serviceUtil.syncEmailForward(updatedGluuPerson, true);

		// For custom script: update user
		if (externalScimService.isEnabled()) {
			externalScimService.executeScimUpdateUserMethods(updatedGluuPerson);
		}
		personService.updatePerson(updatedGluuPerson);

		log.debug(" person updated ");
		
	}
    
}
