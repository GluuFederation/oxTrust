package org.gluu.oxtrust.api.group;

import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

class Group {

    private final GluuGroup group;

    Group(GluuGroup group) {
        this.group = group;
    }

    void save(IGroupService groupService) throws Exception {
        String inum = groupService.generateInumForNewGroup();
        String dn = groupService.getDnForGroup(inum);

        group.setDn(dn);
        group.setInum(inum);

        groupService.addGroup(group);
    }

    String id() {
        return group.getInum();
    }

    void update(String inum, IGroupService groupService) throws Exception {
        GluuGroup group = groupService.getGroupByInum(inum);
        if (group == null) {
            throw new NotFoundException();
        }
        group.setInum(inum);
        groupService.updateGroup(group);
    }
}