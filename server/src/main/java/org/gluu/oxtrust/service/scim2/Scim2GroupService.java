/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.group.Member;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.model.ListViewResponse;
import org.gluu.persist.model.SortOrder;
import org.gluu.persist.model.base.GluuStatus;
import org.gluu.search.filter.Filter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.util.Pair;

/**
 * @author Val Pecaoco
 * Re-engineered by jgomer on 2017-10-18.
 */
@Named
public class Scim2GroupService implements Serializable {

    @Inject
    private Logger log;

    @Inject
    private IPersonService personService;

    @Inject
    private IGroupService groupService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private ExtensionService extService;

    @Inject
    private ScimFilterParserService scimFilterParserService;

    @Inject
    private LdapEntryManager ldapEntryManager;

    private void transferAttributesToGroup(GroupResource res, GluuGroup group, String usersUrl) {

        //externalId (so oxTrustExternalId) not part of LDAP schema
        group.setAttribute("oxTrustMetaCreated", res.getMeta().getCreated());
        group.setAttribute("oxTrustMetaLastModified", res.getMeta().getLastModified());
        //When creating group, location will be set again when having an inum
        group.setAttribute("oxTrustMetaLocation", res.getMeta().getLocation());

        group.setDisplayName(res.getDisplayName());
        group.setStatus(GluuStatus.ACTIVE);
        group.setOrganization(organizationService.getDnForOrganization());

        //Add the members, and complement the $refs and users' display names in res
        Set<Member> members=res.getMembers();
        if (members!=null && members.size()>0){
            List<String> listMembers = new ArrayList<String>();

            for (Member member : members){
                String inum=member.getValue();  //it's not null as it is required in GroupResource
                GluuCustomPerson person=personService.getPersonByInum(inum);

                if (person==null)
                    log.info("Member identified by {} does not exist. Ignored", inum);
                else{
                    member.setDisplay(person.getDisplayName());
                    member.setRef(usersUrl + "/" + inum);
                    member.setType(ScimResourceUtil.getType(res.getClass()));

                    listMembers.add(person.getDn());
                }
            }
            group.setMembers(listMembers);
        }
    }

    private void assignComputedAttributesToGroup(GluuGroup gluuGroup) throws Exception{

        String inum = groupService.generateInumForNewGroup();
        String dn = groupService.getDnForGroup(inum);

        gluuGroup.setInum(inum);
        gluuGroup.setDn(dn);
        gluuGroup.setIname(groupService.generateInameForNewGroup(gluuGroup.getDisplayName().replaceAll(" ", "")));

    }

    public void transferAttributesToGroupResource(GluuGroup gluuGroup, GroupResource res, String groupsUrl, String usersUrl) {

        res.setId(gluuGroup.getInum());

        Meta meta=new Meta();
        meta.setResourceType(ScimResourceUtil.getType(res.getClass()));
        meta.setCreated(gluuGroup.getAttribute("oxTrustMetaCreated"));
        meta.setLastModified(gluuGroup.getAttribute("oxTrustMetaLastModified"));
        meta.setLocation(gluuGroup.getAttribute("oxTrustMetaLocation"));
        if (meta.getLocation()==null)
            meta.setLocation(groupsUrl + "/" + gluuGroup.getInum());

        res.setMeta(meta);
        res.setDisplayName(gluuGroup.getDisplayName());

        //Transfer members from GluuGroup to GroupResource
        List<String> memberDNs =gluuGroup.getMembers();
        if (memberDNs !=null){
            Set<Member> members=new HashSet<Member>();

            for (String dn : memberDNs){
                GluuCustomPerson person=null;
                try{
                    person=personService.getPersonByDn(dn);
                }
                catch (Exception e){
                    log.warn("Wrong member entry {} found in group {}", dn, gluuGroup.getDisplayName());
                }
                if (person!=null){
                    Member aMember=new Member();
                    aMember.setValue(person.getInum());
                    aMember.setRef(usersUrl + "/" + person.getInum());
                    aMember.setType(ScimResourceUtil.getType(res.getClass()));
                    aMember.setDisplay(person.getDisplayName());

                    members.add(aMember);
                }
            }
            res.setMembers(members);
        }
    }

    /**
     * Inserts a new group in LDAP based on the SCIM Resource passed
     * There is no need to check attributes mutability in this case as there are no original attributes (the resource does
     * not exist yet)
     * @param group A GroupResource object with all info as received by the web service
     * @return The new created group
     * @throws Exception
     */
    public GluuGroup createGroup(GroupResource group, String groupsUrl, String usersUrl) throws Exception {

        String groupName=group.getDisplayName();
        log.info("Preparing to create group {}", groupName);

        GluuGroup gluuGroup=new GluuGroup();
        transferAttributesToGroup(group, gluuGroup, usersUrl);
        assignComputedAttributesToGroup(gluuGroup);

        String location= groupsUrl + "/" + gluuGroup.getInum();
        gluuGroup.setAttribute("oxTrustMetaLocation", location);

        log.info("Persisting group {}", groupName);
        groupService.addGroup(gluuGroup);

        group.getMeta().setLocation(location);
        //We are ignoring the id value received (group.getId())
        group.setId(gluuGroup.getInum());
        syncMemberAttributeInPerson(gluuGroup.getDn(), null, gluuGroup.getMembers());

        return gluuGroup;

    }

    public Pair<GluuGroup, GroupResource> updateGroup(String id, GroupResource group, String groupsUrl, String usersUrl) throws Exception {

        GluuGroup gluuGroup = groupService.getGroupByInum(id);    //This is never null (see decorator involved)
        GroupResource tmpGroup=new GroupResource();
        transferAttributesToGroupResource(gluuGroup, tmpGroup, groupsUrl, usersUrl);

        long now=System.currentTimeMillis();
        tmpGroup.getMeta().setLastModified(ISODateTimeFormat.dateTime().withZoneUTC().print(now));

        tmpGroup=(GroupResource) ScimResourceUtil.transferToResourceReplace(group, tmpGroup, extService.getResourceExtensions(group.getClass()));
        replaceGroupInfo(gluuGroup, tmpGroup, usersUrl);

        return new Pair<GluuGroup, GroupResource>(gluuGroup, tmpGroup);

    }

    public void replaceGroupInfo(GluuGroup gluuGroup, GroupResource group, String usersUrl) throws Exception{

        List<String> olderMembers=new ArrayList<String>();
        if (gluuGroup.getMembers()!=null)
            olderMembers.addAll(gluuGroup.getMembers());

        transferAttributesToGroup(group, gluuGroup, usersUrl);
        log.debug("replaceGroupInfo. Updating group info in LDAP");
        groupService.updateGroup(gluuGroup);
        syncMemberAttributeInPerson(gluuGroup.getDn(), olderMembers, gluuGroup.getMembers());

    }

    public ListViewResponse<BaseScimResource> searchGroups(String filter, String sortBy, SortOrder sortOrder, int startIndex, int count,
                                               String groupsUrl, String usersUrl, int maxCount) throws Exception{

        Filter ldapFilter=scimFilterParserService.createLdapFilter(filter, "inum=*", GroupResource.class);
        //Transform scim attribute to LDAP attribute
        sortBy = FilterUtil.getLdapAttributeOfResourceAttribute(sortBy, GroupResource.class).getFirst();

        log.info("Executing search for groups using: ldapfilter '{}', sortBy '{}', sortOrder '{}', startIndex '{}', count '{}'",
                ldapFilter.toString(), sortBy, sortOrder.getValue(), startIndex, count);

        ListViewResponse<GluuGroup> list=ldapEntryManager.findListViewResponse(groupService.getDnForGroup(null),
                GluuGroup.class, ldapFilter, startIndex, count, maxCount, sortBy, sortOrder, null);
        List<BaseScimResource> resources=new ArrayList<BaseScimResource>();

        for (GluuGroup group: list.getResult()){
            GroupResource scimGroup=new GroupResource();
            transferAttributesToGroupResource(group, scimGroup, groupsUrl, usersUrl);
            //TODO: Delete this IF in the future - added for backwards compatibility with SCIM-Client <= 3.1.2.
            if (scimGroup.getMembers()==null)
                scimGroup.setMembers(new HashSet<Member>());

            resources.add(scimGroup);
        }
        log.info ("Found {} matching entries - returning {}", list.getTotalResults(), list.getResult().size());
        
        ListViewResponse<BaseScimResource> result = new ListViewResponse<BaseScimResource>();
        result.setResult(resources);
        result.setTotalResults(list.getTotalResults());

        return result;

    }

    private void syncMemberAttributeInPerson(String groupDn, List<String> beforeMemberDns, List<String> afterMemberDns){

        log.debug("syncMemberAttributeInPerson. Updating memberOf attribute in user LDAP entries");
        log.trace("Before member dns {}; After member dns {}", beforeMemberDns, afterMemberDns);

        //Build 2 sets of DNs
        Set<String> before=new HashSet<String>();
        if (beforeMemberDns!=null)
            before.addAll(beforeMemberDns);

        Set<String> after=new HashSet<String>();
        if (afterMemberDns!=null)
            after.addAll(afterMemberDns);

        //Do removals
        for (String dn : before){
            if (!after.contains(dn)){
                try{
                    GluuCustomPerson gluuPerson = personService.getPersonByDn(dn);

                    List<String> memberOf=new ArrayList<String>();
                    memberOf.addAll(gluuPerson.getMemberOf());
                    memberOf.remove(groupDn);

                    gluuPerson.setMemberOf(memberOf);
                    personService.updatePerson(gluuPerson);
                }
                catch (Exception e){
                    log.error("An error occurred while removing user {} from group {}", dn, groupDn);
                    log.error(e.getMessage(), e);
                }
            }
        }

        //Do insertions
        for (String dn : after){
            if (!before.contains(dn)){
                try{
                    GluuCustomPerson gluuPerson = personService.getPersonByDn(dn);

                    List<String> memberOf=new ArrayList<String>();
                    memberOf.add(groupDn);

                    if (gluuPerson.getMemberOf()!=null)
                        memberOf.addAll(gluuPerson.getMemberOf());

                    gluuPerson.setMemberOf(memberOf);
                    personService.updatePerson(gluuPerson);
                }
                catch (Exception e){
                    log.error("An error occurred while adding user {} to group {}", dn, groupDn);
                    log.error(e.getMessage(), e);
                }
            }
        }

    }

}
