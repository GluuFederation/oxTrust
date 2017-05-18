/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to create a UpdateGroup Object to update a existing Group
 */
public final class UpdateGroup {// NOSONAR - Builder constructs instances of this class

    private Group group;
    private String displayName = null;
    private String externalId = null;
    private Set<String> deleteFields = new HashSet<String>();
    private static final String DELETE = "delete";
    private Set<MemberRef> members = new HashSet<MemberRef>();

    private UpdateGroup(Group group) {
        group = this.group;
    }

    /**
     * the Scim conform Group to be used to update a existing Group
     * 
     * @return Group to update
     */
    public Group getScimConformUpdateGroup() {
        return group;
    }

    public void setGroup(Group group) {
		this.group = group;
	}
    
    /**
     * delete the external Id of a existing group
     */
    public void deleteExternalId() {
        deleteFields.add(Group_.externalId.getName());
    }

    /**
     * updates the external id of a existing group
     * 
     * @param externalID
     *        new external id
     */
    public void updateExternalId(String externalID) {
        this.externalId = externalID;
    }

    /**
     * updates the display name of a existing group
     * 
     * @param displayName
     *        new display name
     */
    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * deletes all group members of a existing group
     */
    public void deleteMembers() {
        deleteFields.add("members");
    }

    /**
     * removes the membership of the given group or user in a existing group
     * 
     * @param memberId
     *        group or user id to be removed
     */
    public void deleteMember(String memberId) {
        MemberRef deleteGroup = new MemberRef();
        deleteGroup.setValue(memberId);
        deleteGroup.setOperation(DELETE);
        members.add(deleteGroup);
    }

    /**
     * adds a new membership of a group or a user to a existing group
     * 
     * @param memberId
     *        user or group id to be added
     */
    public void addMember(String memberId) {
    	MemberRef newGroup = new MemberRef();
    	newGroup.setValue(memberId);
    	members.add(newGroup);
    }

	
}
