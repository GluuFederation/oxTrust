package org.gluu.oxtrust.service.scim2;

import com.unboundid.ldap.sdk.Filter;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.group.Member;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.GroupFilterVisitor;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;
import org.xdi.util.Pair;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * @author Val Pecaoco
 * Re-engineered by jgomer on 2017-10-18.
 */
@Stateless
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
    private ServiceUtil serviceUtil;

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
                String inum=member.getValue(); //it's not null as it's required in GroupResource
                GluuCustomPerson person=personService.getPersonByInum(inum);

                if (person==null)
                    log.info("Member identified by {} does not exist. Ignored", inum);
                else{
                    member.setDisplay(person.getDisplayName());
                    member.setRef(usersUrl + "/" + inum);
                    member.setType(BaseScimResource.getType(res.getClass()));

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

    private void checkDisplayNameExistence(String displayName, String id) throws Exception{
        //Validate if there is an attempt to supply a displayName already in use by a group other than current

        GluuGroup groupToFind = new GluuGroup();
        groupToFind.setDisplayName(displayName);

        List<GluuGroup> list=groupService.findGroups(groupToFind,2 );
        if (list!=null && list.size()>0){
            for (GluuGroup g : list)
                if (!g.getInum().equals(id))
                    throw new DuplicateEntryException("Duplicate displayName value: " + displayName);
        }

    }

    public void transferAttributesToGroupResource(GluuGroup gluuGroup, GroupResource res, String groupsUrl, String usersUrl) {

        res.setId(gluuGroup.getInum());

        Meta meta=new Meta();
        meta.setResourceType(BaseScimResource.getType(res.getClass()));
        meta.setCreated(gluuGroup.getAttribute("oxTrustMetaCreated"));
        meta.setLastModified(gluuGroup.getAttribute("oxTrustMetaLastModified"));
        meta.setLocation(gluuGroup.getAttribute("oxTrustMetaLocation"));
        if (meta.getLocation()==null)
            meta.setLocation(groupsUrl + "/" + gluuGroup.getInum());

        res.setMeta(meta);

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
                    aMember.setType(BaseScimResource.getType(res.getClass()));
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

        try{
            if (gluuGroup.getMembers()!=null && gluuGroup.getMembers().size()>0)
                serviceUtil.personMembersAdder(gluuGroup, gluuGroup.getDn());
        }
        catch (Exception e){
            log.error("Group created but couldn't update the memberOf attribute of some user entries");
        }

        return gluuGroup;

    }

    public Pair<GluuGroup, GroupResource> updateGroup(String id, GroupResource group, String groupsUrl, String usersUrl) throws Exception {

        GluuGroup gluuGroup = groupService.getGroupByInum(id);
        GroupResource tmpGroup=new GroupResource();

        if (gluuGroup!=null){
            checkDisplayNameExistence(group.getDisplayName(), id);
            transferAttributesToGroupResource(gluuGroup, tmpGroup, groupsUrl, usersUrl);

            long now=new Date().getTime();
            tmpGroup.getMeta().setLastModified(ISODateTimeFormat.dateTime().withZoneUTC().print(now));

            tmpGroup=(GroupResource) ScimResourceUtil.transferToResource(group, tmpGroup, extService.getResourceExtensions(group.getClass()));

            transferAttributesToGroup(tmpGroup, gluuGroup, usersUrl);

            groupService.updateGroup(gluuGroup);
            try{
                if (gluuGroup.getMembers()!=null && gluuGroup.getMembers().size()>0)
                    serviceUtil.personMembersAdder(gluuGroup, gluuGroup.getDn());
            }
            catch (Exception e){
                log.error("Group created but couldn't update the memberOf attribute of some user entries");
            }
        }
        else
            throw new NotFoundException("Group resource with " + id + " not found");

        return new Pair<GluuGroup, GroupResource>(gluuGroup, tmpGroup);

    }


    private Filter getFilter(String filterString) throws SCIMException {

        Filter filter;
        try {
            if (StringUtils.isEmpty(filterString))
                filter = Filter.create("inum=*");
            else
                filter = scimFilterParserService.createFilter(filterString, GroupResource.class);
        }
        catch (Exception e){
            throw new SCIMException("An error occurred parsing the filter expression (" + e.getMessage() + ")", e);
        }
        return filter;

    }

    public List<BaseScimResource> searchGroups(String filter, String sortBy, SortOrder sortOrder, int startIndex, int count,
                                               VirtualListViewResponse vlvResponse, String groupsUrl, String usersUrl) throws Exception{

        Filter ldapFilter=getFilter(filter);
        //Transform scim attribute to LDAP attribute
        sortBy = GroupFilterVisitor.getLdapAttributeName(sortBy, GroupResource.class);

        log.info("Executing search for groups using: ldapfilter '{}', sortBy '{}', sortOrder '{}', startIndex '{}', count '{}'",
                ldapFilter.toString(), sortBy, sortOrder.getValue(), startIndex, count);

        List<GluuGroup> list=ldapEntryManager.findEntriesSearchSearchResult(groupService.getDnForGroup(null),
                GluuGroup.class, ldapFilter, startIndex, count, MAX_COUNT, sortBy, sortOrder, vlvResponse, null);
        List<BaseScimResource> resources=new ArrayList<BaseScimResource>();

        for (GluuGroup group: list){
            GroupResource scimGroup=new GroupResource();
            transferAttributesToGroupResource(group, scimGroup, groupsUrl, usersUrl);
            resources.add(scimGroup);
        }
        log.info ("Found {} matching entries - returning {}", vlvResponse.getTotalResults(), list.size());
        return resources;

    }

}
