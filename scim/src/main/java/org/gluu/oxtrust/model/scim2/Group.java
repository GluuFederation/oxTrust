/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

/**
 * This class represent a Group resource.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-8">SCIM core schema 2.0, sections 8</a>
 * </p>
 *
 * ===== IMPORTANT! =====
 * There might be JSON serializers/deserializers dependent on this class via reflection, most notably
 * org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseGroupSerializer. You should consult this file first
 * before changing anything here.
 */
@JsonInclude(Include.NON_EMPTY)
public class Group extends Resource {

    @LdapAttribute(name = "displayName")
    private String displayName;

    @LdapAttribute(name = "member")
    private Set<MemberRef> members = new HashSet<MemberRef>();

    /**
     * Default constructor for Jackson
     */
    public Group() {
    	Meta groupMeta = new Meta();
    	groupMeta.setResourceType("Group");
    	setMeta(groupMeta);
    	Set<String> groupSchemas = new HashSet<String>();
    	groupSchemas.add(Constants.GROUP_CORE_SCHEMA_ID);
		setSchemas(groupSchemas);
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

    public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setMembers(Set<MemberRef> members) {
		this.members = members;
	}

	@Override
    public String toString() {
        return "Group [displayName=" + displayName + ", members=" + members + ", getId()=" + getId()
                + ", getExternalId()=" + getExternalId() + ", getMeta()=" + getMeta() + ", getSchemas()="
                + getSchemas() + "]";
    }
}
