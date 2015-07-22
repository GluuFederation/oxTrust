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

    private UpdateGroup(Builder builder) {
        group = builder.updateGroup.build();
    }

    /**
     * the Scim conform Group to be used to update a existing Group
     * 
     * @return Group to update
     */
    public Group getScimConformUpdateGroup() {
        return group;
    }

    /**
     * The Builder is used to construct instances of the {@link UpdateGroup}
     */
    public static class Builder {

        private Group.Builder updateGroup = null;
        private String displayName = null;
        private String externalId = null;
        private Set<String> deleteFields = new HashSet<String>();
        private static final String DELETE = "delete";
        private Set<MemberRef> members = new HashSet<MemberRef>();

        /**
         * delete the external Id of a existing group
         * 
         * @return The builder itself
         */
        public Builder deleteExternalId() {
            deleteFields.add(Group_.externalId.getName());
            return this;
        }

        /**
         * updates the external id of a existing group
         * 
         * @param externalID
         *        new external id
         * @return The builder itself
         */
        public Builder updateExternalId(String externalID) {
            this.externalId = externalID;
            return this;
        }

        /**
         * updates the display name of a existing group
         * 
         * @param displayName
         *        new display name
         * @return The builder itself
         */
        public Builder updateDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * deletes all group members of a existing group
         * 
         * @return The builder itself
         */
        public Builder deleteMembers() {
            deleteFields.add("members");
            return this;
        }

        /**
         * removes the membership of the given group or user in a existing group
         * 
         * @param memberId
         *        group or user id to be removed
         * @return The builder itself
         */
        public Builder deleteMember(String memberId) {
            MemberRef deleteGroup = new MemberRef.Builder()
                    .setValue(memberId)
                    .setOperation(DELETE).build();
            members.add(deleteGroup);
            return this;
        }

        /**
         * adds a new membership of a group or a user to a existing group
         * 
         * @param memberId
         *        user or group id to be added
         * @return The builder itself
         */
        public Builder addMember(String memberId) {
            MemberRef newGroup = new MemberRef.Builder()
                    .setValue(memberId).build();
            members.add(newGroup);
            return this;
        }

        /**
         * constructs a {@link UpdateGroup} with the given values
         * 
         * @return a valid {@link UpdateGroup}
         */
        public UpdateGroup build() {
            if (displayName != null) {
                updateGroup = new Group.Builder(displayName);
            } else {
                updateGroup = new Group.Builder();
            }
            if (externalId != null) {
                updateGroup.setExternalId(externalId);
            }
            if (deleteFields.size() > 0) {
                Meta meta = new Meta.Builder()
                        .setAttributes(deleteFields).build();
                updateGroup.setMeta(meta);
            }
            if (members.size() > 0) {
                updateGroup.setMembers(members);
            }

            return new UpdateGroup(this);
        }
    }
}
