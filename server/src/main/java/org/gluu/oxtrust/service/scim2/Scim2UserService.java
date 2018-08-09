/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.management.InvalidAttributeValueException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.MemberService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;
import org.gluu.oxtrust.model.scim2.user.Address;
import org.gluu.oxtrust.model.scim2.user.Email;
import org.gluu.oxtrust.model.scim2.user.Entitlement;
import org.gluu.oxtrust.model.scim2.user.Group;
import org.gluu.oxtrust.model.scim2.user.InstantMessagingAddress;
import org.gluu.oxtrust.model.scim2.user.Name;
import org.gluu.oxtrust.model.scim2.user.PhoneNumber;
import org.gluu.oxtrust.model.scim2.user.Photo;
import org.gluu.oxtrust.model.scim2.user.Role;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.model.scim2.user.X509Certificate;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.oxtrust.ws.rs.scim2.GroupWebService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.PagedResult;
import org.gluu.persist.model.SortOrder;
import org.gluu.persist.model.base.GluuBoolean;
import org.gluu.search.filter.Filter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;

/**
 * This class holds the most important business logic of the SCIM service for the resource type "User". It's devoted to
 * taking objects of class UserResource, feeding instances of GluuCustomPerson, and do persistence to LDAP. The converse
 * is also done: querying LDAP, and transforming GluuCustomPerson into UserResource
 *
 * @author Val Pecaoco
 * Re-engineered by jgomer on 2017-09-15.
 */
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
    private GroupWebService groupWS;

    @Inject
    private ExternalScimService externalScimService;

    @Inject
    private ServiceUtil serviceUtil;

    @Inject
    private ExtensionService extService;

    @Inject
    private ScimFilterParserService scimFilterParserService;

    @Inject
    private PersistenceEntryManager ldapEntryManager;

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

        log.debug("transferAttributesToPerson");

        //Set values trying to follow the order found in BaseScimResource class
        person.setAttribute("oxTrustExternalId", res.getExternalId());
        person.setAttribute("oxTrustMetaCreated", res.getMeta().getCreated());
        person.setAttribute("oxTrustMetaLastModified", res.getMeta().getLastModified());
        //When creating user, location will be set again when having an inum
        person.setAttribute("oxTrustMetaLocation", res.getMeta().getLocation());

        //Set values trying to follow the order found in UserResource class
        person.setUid(res.getUserName());

        if (res.getName()!=null){
            person.setGivenName(res.getName().getGivenName());
            person.setSurname(res.getName().getFamilyName());
            person.setAttribute("middleName", res.getName().getMiddleName());
            person.setAttribute("oxTrusthonorificPrefix", res.getName().getHonorificPrefix());
            person.setAttribute("oxTrusthonorificSuffix", res.getName().getHonorificSuffix());
            person.setAttribute("oxTrustNameFormatted", res.getName().computeFormattedName());
        }
        person.setDisplayName(res.getDisplayName());

        person.setAttribute("nickname", res.getNickName());
        person.setAttribute("oxTrustProfileURL", res.getProfileUrl());
        person.setAttribute("oxTrustTitle", res.getTitle());
        person.setAttribute("oxTrustUserType", res.getUserType());

        person.setPreferredLanguage(res.getPreferredLanguage());
        person.setAttribute("locale", res.getLocale());
        person.setTimezone(res.getTimezone());

        //Why are both gluuStatus and oxTrustActive used for active? it's for active being used in filter queries?
        Boolean active=res.getActive()!=null && res.getActive();
        person.setAttribute("oxTrustActive", active.toString());
        person.setAttribute("gluuStatus", active ? GluuStatus.ACTIVE.getValue() : GluuStatus.INACTIVE.getValue());
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

        //Pairwise identifiers must not be supplied here... (they are mutability = readOnly)

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
            Map<String, Object> extendedAttrs= resource.getCustomAttributes();
            
            //Iterates over all extensions this type of resource might have
            for (Extension extension : extService.getResourceExtensions(resource.getClass())){
                Object val=extendedAttrs.get(extension.getUrn());

                if (val!=null) {
                    //Obtains the attribute/value(s) pairs in the current extension
                    Map<String, Object> attrsMap = IntrospectUtil.strObjMap(val);

                    for (String attribute : attrsMap.keySet()) {
                        Object value = attrsMap.get(attribute);

                        //Ignore if the attribute is unassigned in this resource: destination will not be changed in this regard
                        if (value != null) {
                            //Get properly formatted string representations for the value(s) associated to the attribute
                            List<String> values=extService.getStringAttributeValues(extension.getFields().get(attribute), value);
                            log.debug("transferExtendedAttributesToPerson. Setting attribute '{}' with values {}", attribute, values.toString());
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

    public void transferAttributesToUserResource(GluuCustomPerson person, UserResource res, String url) {

        log.debug("transferAttributesToUserResource");

        res.setId(person.getInum());
        res.setExternalId(person.getAttribute("oxTrustExternalId"));

        Meta meta=new Meta();
        meta.setResourceType(ScimResourceUtil.getType(res.getClass()));

        meta.setCreated(person.getAttribute("oxTrustMetaCreated"));
        if (meta.getCreated() == null) {
            Date tmpDate = person.getCreationDate();
            meta.setCreated(tmpDate == null ? null : ISODateTimeFormat.dateTime().withZoneUTC().print(tmpDate.getTime()));
        }

        meta.setLastModified(person.getAttribute("oxTrustMetaLastModified"));
        if (meta.getLastModified() == null) {
            Date tmpDate = person.getUpdatedAt();
            meta.setLastModified(tmpDate == null ? null : ISODateTimeFormat.dateTime().withZoneUTC().print(tmpDate.getTime()));
        }

        meta.setLocation(person.getAttribute("oxTrustMetaLocation"));
        if (meta.getLocation()==null)
            meta.setLocation(url + "/" + person.getInum());

        res.setMeta(meta);

        //Set values in order of appearance in UserResource class
        res.setUserName(person.getUid());

        Name name=new Name();
        name.setGivenName(person.getGivenName());
        name.setFamilyName(person.getSurname());
        name.setMiddleName(person.getAttribute("middleName"));
        name.setHonorificPrefix(person.getAttribute("oxTrusthonorificPrefix"));
        name.setHonorificSuffix(person.getAttribute("oxTrusthonorificSuffix"));

        String formatted=person.getAttribute("oxTrustNameFormatted");
        if (formatted==null)    //recomputes the formatted name if absent in LDAP
            name.computeFormattedName();
        else
            name.setFormatted(formatted);

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
        if (listOfGroups!= null && listOfGroups.size()>0) {
            List<Group> groupList = new ArrayList<Group>();

            for (String groupDN : listOfGroups) {
                try {
                    GluuGroup gluuGroup = groupService.getGroupByDn(groupDN);

                    Group group = new Group();
                    group.setValue(gluuGroup.getInum());
                    String reference = groupWS.getEndpointUrl() + "/" + gluuGroup.getInum();
                    group.setRef(reference);
                    group.setDisplay(gluuGroup.getDisplayName());
                    group.setType(Group.Type.DIRECT);   //Only support direct membership: see section 4.1.2 of RFC 7644

                    groupList.add(group);
                }
                catch (Exception e){
                    log.warn("transferAttributesToUserResource. Group with dn {} could not be added to User Resource. {}", groupDN, person.getUid());
                    log.error(e.getMessage(), e);
                }
            }
            if (groupList.size()>0)
                res.setGroups(groupList);
        }

        res.setEntitlements(getAttributeListValue(person, Entitlement.class, "oxTrustEntitlements"));
        res.setRoles(getAttributeListValue(person, Role.class, "oxTrustRole"));
        res.setX509Certificates(getAttributeListValue(person, X509Certificate.class, "oxTrustx509Certificate"));

        res.setPairwiseIdentitifers(person.getOxPPID());

        transferExtendedAttributesToResource(person, res);
    }

    private void transferExtendedAttributesToResource(GluuCustomPerson person, BaseScimResource resource){

        log.debug("transferExtendedAttributesToResource of type {}", ScimResourceUtil.getType(resource.getClass()));

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
                resource.addCustomAttributes(extension.getUrn(), map);
            }
        }
        for (String urn : resource.getCustomAttributes().keySet())
            resource.getSchemas().add(urn);

    }

    private void writeCommonName(GluuCustomPerson person){

        if (StringUtils.isNotEmpty(person.getGivenName()) && StringUtils.isNotEmpty(person.getSurname()))
            person.setCommonName(person.getGivenName() + " " + person.getSurname());

    }

    private void assignComputedAttributesToPerson(GluuCustomPerson person){

        String inum = personService.generateInumForNewPerson();
        String dn = personService.getDnForPerson(inum);

        person.setInum(inum);
        person.setDn(dn);
        person.setIname(personService.generateInameForNewPerson(person.getUid()));
        writeCommonName(person);

    }

    /**
     * Inserts a new user in LDAP based on the SCIM Resource passed
     * @param user A UserResource object with all info as received by the web service
     * @throws Exception
     */
    public void createUser(UserResource user, String url) throws Exception {

        String userName=user.getUserName();
        log.info("Preparing to create user {}", userName);

        //There is no need to check attributes mutability in this case as there are no original attributes
        //(the resource does not exist yet)
        GluuCustomPerson gluuPerson=new GluuCustomPerson();
        transferAttributesToPerson(user, gluuPerson);
        assignComputedAttributesToPerson(gluuPerson);

        String location=url + "/" + gluuPerson.getInum();
        gluuPerson.setAttribute("oxTrustMetaLocation", location);

        log.info("Persisting user {}", userName);
        personService.addCustomObjectClass(gluuPerson);

        if (externalScimService.isEnabled()){
            boolean result = externalScimService.executeScimCreateUserMethods(gluuPerson);
            if (!result) {
                throw new WebApplicationException("Failed to execute SCIM script successfully", Status.PRECONDITION_FAILED);
            }

            personService.addPerson(gluuPerson);
            //Copy back to user the info from gluuPerson
            transferAttributesToUserResource(gluuPerson, user, url);
            externalScimService.executeScimPostCreateUserMethods(gluuPerson);
        }
        else {
            personService.addPerson(gluuPerson);
            user.getMeta().setLocation(location);
            //We are ignoring the id value received (user.getId())
            user.setId(gluuPerson.getInum());
        }

    }

    public UserResource updateUser(String id, UserResource user, String url) throws InvalidAttributeValueException {

        GluuCustomPerson gluuPerson = personService.getPersonByInum(id);    //This is never null (see decorator involved)
        UserResource tmpUser=new UserResource();

        transferAttributesToUserResource(gluuPerson, tmpUser, url);

        long now=System.currentTimeMillis();
        tmpUser.getMeta().setLastModified(ISODateTimeFormat.dateTime().withZoneUTC().print(now));

        tmpUser=(UserResource) ScimResourceUtil.transferToResourceReplace(user, tmpUser, extService.getResourceExtensions(user.getClass()));
        replacePersonInfo(gluuPerson, tmpUser, url);

        return tmpUser;

    }

    public void replacePersonInfo(GluuCustomPerson gluuPerson, UserResource user, String url){
        transferAttributesToPerson(user, gluuPerson);
        writeCommonName(gluuPerson);

        log.debug("replacePersonInfo. Updating person info in LDAP");
        personService.addCustomObjectClass(gluuPerson);

        if (externalScimService.isEnabled()){
            boolean result = externalScimService.executeScimUpdateUserMethods(gluuPerson);
            if (!result) {
                throw new WebApplicationException("Failed to execute SCIM script successfully", Status.PRECONDITION_FAILED);
            }

            personService.updatePerson(gluuPerson);
            //Copy back to user the info from gluuPerson
            transferAttributesToUserResource(gluuPerson, user, url);
            externalScimService.executeScimPostUpdateUserMethods(gluuPerson);
        }
        else {
            personService.updatePerson(gluuPerson);
        }

    }

    public void deleteUser(GluuCustomPerson gluuPerson) throws Exception {

        String dn = gluuPerson.getDn();
        if (gluuPerson.getMemberOf()!= null && gluuPerson.getMemberOf().size()>0) {
            log.info("Removing user {} from groups", gluuPerson.getUid());
            serviceUtil.deleteUserFromGroup(gluuPerson, dn);
        }
        log.info("Removing user entry {}", dn);

        if (externalScimService.isEnabled()) {
            boolean result = externalScimService.executeScimDeleteUserMethods(gluuPerson);
            if (!result) {
                throw new WebApplicationException("Failed to execute SCIM script successfully", Status.PRECONDITION_FAILED);
            }
        }

        personService.removePerson(gluuPerson);

        if (externalScimService.isEnabled())
            externalScimService.executeScimPostDeleteUserMethods(gluuPerson);

    }

    public PagedResult<BaseScimResource> searchUsers(String filter, String sortBy, SortOrder sortOrder, int startIndex, int count,
                                              String url, int maxCount) throws Exception{

        Filter ldapFilter=scimFilterParserService.createLdapFilter(filter, "inum=*", UserResource.class);
        log.info("Executing search for users using: ldapfilter '{}', sortBy '{}', sortOrder '{}', startIndex '{}', count '{}'",
                ldapFilter.toString(), sortBy, sortOrder.getValue(), startIndex, count);

        PagedResult<GluuCustomPerson> list=ldapEntryManager.findPagedEntries(personService.getDnForPerson(null),
                GluuCustomPerson.class, ldapFilter, null, sortBy, sortOrder, startIndex, count, maxCount);
        List<BaseScimResource> resources=new ArrayList<BaseScimResource>();

        for (GluuCustomPerson person : list.getEntries()){
            UserResource scimUsr=new UserResource();
            transferAttributesToUserResource(person, scimUsr, url);
            resources.add(scimUsr);
        }
        log.info ("Found {} matching entries - returning {}", list.getTotalEntriesCount(), list.getEntries().size());

        PagedResult<BaseScimResource> result = new PagedResult<BaseScimResource>();
        result.setEntries(resources);
        result.setTotalEntriesCount(list.getTotalEntriesCount());

        return result;

    }

    //See: https://github.com/GluuFederation/oxTrust/issues/800
    public void removePPIDsBranch(String dn) {
        try {
            ldapEntryManager.removeRecursively(String.format("ou=pairwiseIdentifiers,%s", dn));
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}
