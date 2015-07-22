/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.HashSet;
import java.util.Set;

import org.gluu.oxtrust.model.exception.SCIMDataValidationException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Strings;

/**
 * This class represent a Group resource.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
 * </p>
 * 
 * <p>
 * client info: The scim2 schema is mainly meant as a connection link between the
 * OSIAM server and by a client like the connector4Java. 
 * Some values will be not accepted by the OSIAM server.
 * These specific values have an own client info documentation section.
 * </p>
 */
@JsonInclude(Include.NON_EMPTY)
public class Group extends Resource {

    private String displayName;
    private Set<MemberRef> members = new HashSet<MemberRef>();

    /**
     * Default constructor for Jackson
     */
    private Group() {
    }

    private Group(Builder builder) {
        super(builder);
        this.displayName = builder.displayName;
        this.members = builder.members;
    }

    /**
     * Gets the human readable name of this {@link Group}.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the list of members of this Group.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
     * </p>
     * 
     * @return the list of Members as a Set
     */
    public Set<MemberRef> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "Group [displayName=" + displayName + ", members=" + members + ", getId()=" + getId()
                + ", getExternalId()=" + getExternalId() + ", getMeta()=" + getMeta() + ", getSchemas()="
                + getSchemas() + "]";
    }

    /**
     * Builder class that is used to build {@link Group} instances
     */
    public static class Builder extends Resource.Builder {

        private String displayName;
        private Set<MemberRef> members = new HashSet<MemberRef>();

        /**
         * creates a new Group.Builder based on the given displayName and group. All values of the given group will be
         * copied expect the displayName will be be overridden by the given one
         * 
         * @param displayName
         *        the new displayName of the group
         * @param group
         *        a existing group
         */
        public Builder(String displayName, Group group) {
            super(group);
            addSchema(Constants.GROUP_CORE_SCHEMA);
            if (group != null) {
                this.displayName = group.displayName;
                members = group.members;
            }
            if (!Strings.isNullOrEmpty(displayName)) {
                this.displayName = displayName;
            }
        }

        /**
         * creates a new Group without a displayName
         */
        public Builder() {
            this(null, null);
        }

        /**
         * Constructs a new builder by copying all values from the given {@link Group}
         * 
         * @param group
         *        {@link Group} to be copied from
         * 
         * @throws SCIMDataValidationException
         *         if the given group is null
         */
        public Builder(Group group) {
            this(null, group);
            if (group == null) {
                throw new SCIMDataValidationException("The given group can't be null.");
            }
        }

        /**
         * Constructs a new builder and sets the display name (See {@link Group#getDisplayName()}).
         * 
         * @param displayName
         *        the display name
         * 
         * @throws SCIMDataValidationException
         *         if the displayName is null or empty
         */
        public Builder(String displayName) {
            this(displayName, null);
            if (displayName == null) {
                throw new SCIMDataValidationException("The given resource can't be null");
            }
        }

        @Override
        public Builder setId(String id) {
            super.setId(id);
            return this;
        }

        @Override
        public Builder setMeta(Meta meta) {
            super.setMeta(meta);
            return this;
        }

        @Override
        public Builder setExternalId(String externalId) {
            super.setExternalId(externalId);
            return this;
        }

        @Override
        public Builder setSchemas(Set<String> schemas) {
            super.setSchemas(schemas);
            return this;
        }

        /**
         * Sets the list of members as {@link Set} (See {@link Group#getMembers()}).
         * 
         * @param members
         *        the set of members
         * @return the builder itself
         */
        public Builder setMembers(Set<MemberRef> members) {
            this.members = members;
            return this;
        }

        @Override
        public Group build() {
            return new Group(this);
        }
    }
}
