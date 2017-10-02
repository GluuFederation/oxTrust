/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotFoundException;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.MemberService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;
import org.gluu.oxtrust.model.scim2.user.*;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.util.Pair;

import static org.xdi.ldap.model.GluuBoolean.*;

/**
 * This class holds the most important business logic of the SCIM service for the resource type "User". It's devoted to
 * taking objects of class UserResource, feeding instances of GluuCustomPerson, and do persistence to LDAP. The converse
 * is also done: querying LDAP, and transforming GluuCustomPerson into UserResource
 *
 * @author Val Pecaoco
 * Re-engineered by jgomer on 2017-09-15.
 */
@Stateless
@Named
public class Scim2UserService implements Serializable {

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private MemberService memberService;

    @Inject
    private IPersonService personService;

    @Inject
    private IGroupService groupService;

    @Inject
    private ExternalScimService externalScimService;

    @Inject
    private ServiceUtil serviceUtil;

    @Inject
    private ExtensionService extService;

    private void checkUidExistence(String uid) throws Exception{
        if (personService.getPersonByUid(uid) != null)
            throw new DuplicateEntryException("Duplicate UID value: " + uid);
    }

    private void checkUidExistence(String uid, String id) throws Exception{

        // Validate if there is an attempt to supply a userName already in use by a different user than current
        List<GluuCustomPerson> list=personService.findPersonsByUids(Collections.singletonList(uid), new String[]{"inum"});
        if (list!=null && list.size()>0){
            for (GluuCustomPerson p : list)
                if (!p.getInum().equals(id))
                    throw new DuplicateEntryException("Duplicate UID value: " + uid);
        }

    }

    private String[] getComplexMultivaluedAsArray(List items){

        String array[]=null;

        try {
            if (items!=null && items.size()>0) {
                ObjectMapper mapper = ServiceUtil.getObjectMapper();
                List<String> itemList = new ArrayList<String>();

                for (Object item : items)
                    itemList.add(mapper.writeValueAsString(item));

                array = itemList.toArray(new String[]{});
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return array;

    }

    private <T> List<T> getAttributeListValue(GluuCustomPerson source, Class<T> clazz, String attrName) {

        List<T> items = null;
        try {
            ObjectMapper mapper = ServiceUtil.getObjectMapper();
            //This is already disabled in ServiceUtil
            //mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

            String[] attributeArray = source.getAttributeArray(attrName);
            if (attributeArray != null) {
                items = new ArrayList<T>();
                for (String attribute : attributeArray) {
                    T item = mapper.readValue(attribute, clazz);
                    items.add(item);
                }
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return items;

    }

    private void transferAttributesToPerson(UserResource res, GluuCustomPerson person) {

        //Set values in order of appearance in BaseScimResource class
        person.setAttribute("oxTrustExternalId", res.getExternalId());
        //setSchemas does not have effect
        //person.setSchemas(new HashSet<String>(res.getSchemas()));
        person.setAttribute("oxTrustMetaCreated", res.getMeta().getCreated());
        person.setAttribute("oxTrustMetaLastModified", res.getMeta().getLastModified());
        //location will be set when we have an inum

        //Set values in order of appearance in UserResource class
        person.setUid(res.getUserName());

        if (res.getName()!=null){
            person.setGivenName(res.getName().getGivenName());
            person.setSurname(res.getName().getFamilyName());
            person.setAttribute("middleName", res.getName().getMiddleName());
            person.setAttribute("oxTrusthonorificPrefix", res.getName().getHonorificPrefix());
            person.setAttribute("oxTrusthonorificSuffix", res.getName().getHonorificSuffix());
        }
        person.setDisplayName(res.getDisplayName());

        person.setAttribute("nickname", res.getNickName());
        person.setAttribute("oxTrustProfileURL", res.getProfileUrl());
        person.setAttribute("oxTrustTitle", res.getTitle());
        person.setAttribute("oxTrustUserType", res.getUserType());

        person.setPreferredLanguage(res.getPreferredLanguage());
        person.setAttribute("locale", res.getLocale());
        person.setTimezone(res.getTimezone());

        //TODO: are both attrs used for active?
        person.setAttribute("oxTrustActive", new Boolean(res.isActive()).toString());
        person.setAttribute("gluuStatus", res.isActive() ? ACTIVE.getValue() : INACTIVE.getValue());
        person.setUserPassword(res.getPassword());

        person.setAttribute("oxTrustEmail", getComplexMultivaluedAsArray(res.getEmails()));
        try {
            person = serviceUtil.syncEmailForward(person, true);
        }
        catch (Exception e){
            log.error("Problem syncing emails forward", e);
        }

        person.setAttribute("oxTrustPhoneValue", getComplexMultivaluedAsArray(res.getPhoneNumbers()));
        person.setAttribute("oxTrustImsValue", getComplexMultivaluedAsArray(res.getIms()));
        person.setAttribute("oxTrustPhotos", getComplexMultivaluedAsArray(res.getPhotos()));
        person.setAttribute("oxTrustAddresses", getComplexMultivaluedAsArray(res.getAddresses()));

        //group membership changes MUST be applied via the "Group" Resource (Section 4.1.2 & 8.7.1 RFC 7643) only

        person.setAttribute("oxTrustEntitlements", getComplexMultivaluedAsArray(res.getEntitlements()));
        person.setAttribute("oxTrustRole", getComplexMultivaluedAsArray(res.getRoles()));
        person.setAttribute("oxTrustx509Certificate", getComplexMultivaluedAsArray(res.getX509Certificates()));

        //Pairwise identifiers are not supplied here...

        transferExtendedAttributesToPerson(res, person);
    }

    /**
     * Takes all extended attributes found in the SCIM resource and copies them to a GluuCustomPerson
     * This method is called after validations take place (see associated decorator for User Service), so all inputs are
     * OK and can go straight to LDAP with no runtime surprises
     * @param resource A SCIM resource used as origin of data
     * @param person a GluuCustomPerson used as destination
     */
    private void transferExtendedAttributesToPerson(BaseScimResource resource, GluuCustomPerson person){

        try {
            //Gets all the extended attributes for this resource
            Map<String, Object> extendedAttrs= resource.getExtendedAttributes();
            
            //Iterates over all extensions this type of resource might have
            for (Extension extension : extService.getResourceExtensions(resource.getClass())){
                Object val=extendedAttrs.get(extension.getUrn());

                if (val!=null) {
                    //Obtains the attribute/value(s) pairs in the current extension
                    Map<String, Object> attrsMap = (Map<String, Object>) val;

                    for (String attribute : attrsMap.keySet()) {
                        Object value = attrsMap.get(attribute);

                        //Ignore if the attribute is unassigned in this resource: destination will not be changed in this regard
                        if (value != null) {
                            //Get properly formatted string representations for the value(s) associated to the attribute
                            List<String> values=extService.getStringAttributeValues(extension.getFields().get(attribute), value);
                            log.debug("transferExtendedAttributesToPerson. Setting attribute '{}' with values ", attribute, values.toString());
                            person.setAttribute(attribute, values.toArray(new String[]{}));
                        }
                    }
                }
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }

    public void transferAttributesToUserResource(GluuCustomPerson person, UserResource res) {

        //Set values in order of appearance in BaseScimResource class
        List<String> schemas=new ArrayList<String>();
        schemas.add(res.getClass().getAnnotation(Schema.class).id());
        res.setSchemas(schemas);    //Further this list is fed if custom attributes are found

        res.setId(person.getInum());
        res.setExternalId(person.getAttribute("oxTrustExternalId"));

        Meta meta=new Meta();
        meta.setResourceType(BaseScimResource.getType(res.getClass()));
        meta.setCreated(person.getAttribute("oxTrustMetaCreated"));
        meta.setLastModified(person.getAttribute("oxTrustMetaLastModified"));
        meta.setLocation(person.getAttribute("oxTrustMetaLocation"));

        res.setMeta(meta);

        //Set values in order of appearance in UserResource class
        res.setUserName(person.getUid());

        Name name=new Name();
        name.setGivenName(person.getGivenName());
        name.setFamilyName(person.getSurname());
        name.setMiddleName(person.getAttribute("middleName"));
        name.setHonorificPrefix(person.getAttribute("oxTrusthonorificPrefix"));
        name.setHonorificSuffix(person.getAttribute("oxTrusthonorificSuffix"));

        res.setName(name);
        res.setDisplayName(person.getDisplayName());

        res.setNickName(person.getAttribute("nickname"));
        res.setProfileUrl(person.getAttribute("oxTrustProfileURL"));
        res.setTitle(person.getAttribute("oxTrustTitle"));
        res.setUserType(person.getAttribute("oxTrustUserType"));

        res.setPreferredLanguage(person.getPreferredLanguage());
        res.setLocale(person.getAttribute("locale"));
        res.setTimezone(person.getTimezone());

        res.setActive(Boolean.valueOf(person.getAttribute("oxTrustActive"))
                || GluuBoolean.getByValue(person.getAttribute("gluuStatus")).isBooleanValue());
        res.setPassword(person.getUserPassword());

        res.setEmails(getAttributeListValue(person, Email.class, "oxTrustEmail"));
        res.setPhoneNumbers(getAttributeListValue(person, PhoneNumber.class, "oxTrustPhoneValue"));
        res.setIms(getAttributeListValue(person, InstantMessagingAddress.class, "oxTrustImsValue"));
        res.setPhotos(getAttributeListValue(person, Photo.class, "oxTrustPhotos"));
        res.setAddresses(getAttributeListValue(person, Address.class, "oxTrustAddresses"));

        List<String> listOfGroups = person.getMemberOf();
        if (listOfGroups!= null) {
            List<Group> groupList = new ArrayList<Group>();

            for (String groupDN : listOfGroups) {
                GluuGroup gluuGroup = groupService.getGroupByDn(groupDN);

                Group group = new Group();
                group.setValue(gluuGroup.getInum());
                String reference = String.format("%s/scim/v2/Groups/%s", appConfiguration.getBaseEndpoint(),gluuGroup.getInum());
                group.setRef(reference);
                group.setDisplay(gluuGroup.getDisplayName());

                groupList.add(group);
            }
            res.setGroups(groupList);
        }

        res.setEntitlements(getAttributeListValue(person, Entitlement.class, "oxTrustEntitlements"));
        res.setRoles(getAttributeListValue(person, Role.class, "oxTrustRole"));
        res.setX509Certificates(getAttributeListValue(person, X509Certificate.class, "oxTrustx509Certificate"));

        res.setPairwiseIdentitifers(person.getOxPPID());

        transferExtendedAttributesToResource(person, res);
    }

    private void transferExtendedAttributesToResource(GluuCustomPerson person, BaseScimResource resource){

        //Gets the list of extensions associated to the resource passed. In practice, this will be at most a singleton list
        List<Extension> extensions=extService.getResourceExtensions(resource.getClass());

        //Iterate over every extension to copy extended attributes from person to resource
        for (Extension extension : extensions){
            Map<String, ExtensionField> fields=extension.getFields();
            //Create empty map to store the values of the extended attributes found for current extension in object person
            Map<String, Object> map=new HashMap<String, Object>();

            log.debug("transferExtendedAttributesToResource. Revising attributes of extension '{}'", extension.getUrn());

            //Iterate over every attribute part of this extension
            for (String attr : fields.keySet()){
                //Gets the values associated to this attribute that were found in LDAP
                String values[]=person.getAttributes(attr);

                if (values!=null){
                    log.debug("transferExtendedAttributesToResource. Copying to resource the value(s) for attribute '{}'", attr);

                    ExtensionField field=fields.get(attr);
                    if (field.isMultiValued())
                        map.put(attr, extService.convertValues(field, values));
                    else
                        map.put(attr, extService.convertValues(field, values).get(0));
                }
            }
            //Stores all extended attributes (with their values) in the resource object
            if (map.size()>0) {
                resource.addExtendedAttributes(extension.getUrn(), map);
            }
        }
        for (String urn : resource.getExtendedAttributes().keySet())
            resource.getSchemas().add(urn);

    }

    private void assignComputedAttributesToPerson(GluuCustomPerson person){

        String inum = personService.generateInumForNewPerson();
        String dn = personService.getDnForPerson(inum);

        person.setInum(inum);
        person.setDn(dn);
        person.setIname(personService.generateInameForNewPerson(person.getUid()));
        person.setCommonName(person.getGivenName() + " " + person.getSurname());

    }

    /**
     * Inserts a new user in LDAP based on the SCIM Resource passed
     * There is no need to check attributes mutability in this case as there are no original attributes (the resource does
     * not exist yet)
     * @param user A UserResource object with all info as received by the web service
     * @return New created user
     * @throws Exception
     */
    public GluuCustomPerson createUser(UserResource user, String url) throws Exception {

        String userName=user.getUserName();
        log.info("Preparing to create user {}", userName);
        checkUidExistence(userName);

        GluuCustomPerson gluuPerson=new GluuCustomPerson();
        transferAttributesToPerson(user, gluuPerson);
        assignComputedAttributesToPerson(gluuPerson);

        String location=url + "/" + gluuPerson.getInum();
        gluuPerson.setAttribute("oxTrustMetaLocation", location);

        log.info("Persisting user {}", userName);
        //TODO: uncomment addperson
        personService.addCustomObjectClass(gluuPerson);
        personService.addPerson(gluuPerson);

        user.getMeta().setLocation(location);
        user.setId(gluuPerson.getInum());

        return gluuPerson;
    }

    public Pair<GluuCustomPerson, UserResource> updateUser(String id, UserResource user) throws Exception {

        GluuCustomPerson gluuPerson = personService.getPersonByInum(id);
        UserResource tmpUser=new UserResource();

        if (gluuPerson!=null){
            checkUidExistence(user.getUserName(), id);

            transferAttributesToUserResource(gluuPerson, tmpUser);

            long now=new Date().getTime();
            tmpUser.getMeta().setLastModified(ISODateTimeFormat.dateTime().withZoneUTC().print(now));

            tmpUser=(UserResource) ScimResourceUtil.transferToResource(user, tmpUser, extService.getResourceExtensions(user.getClass()));

            transferAttributesToPerson(tmpUser, gluuPerson);
            gluuPerson.setCommonName(gluuPerson.getGivenName() + " " + gluuPerson.getSurname());

            personService.addCustomObjectClass(gluuPerson);
            //TODO: uncomment update person
            personService.updatePerson(gluuPerson);
        }
        else{
            throw new NotFoundException("User resource with " + id + " not found");
        }
        return new Pair<GluuCustomPerson, UserResource>(gluuPerson, tmpUser);

    }

    public void deleteUser(GluuCustomPerson gluuPerson) throws Exception {

        String dn = gluuPerson.getDn();
        if (gluuPerson.getMemberOf()!= null && gluuPerson.getMemberOf().size()>0) {
            log.info("Removing user {} from groups", gluuPerson.getUid());
            serviceUtil.deleteUserFromGroup(gluuPerson, dn);
        }
        log.info("Removing user entry {}", dn);
        //TODO: uncomment remove person
        //memberService.removePerson(gluuPerson);

    }
/*

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

            memberService.removePerson(gluuPerson);
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
*/
}
