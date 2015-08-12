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
    public Group() {
    	Meta userMeta = new Meta();
    	userMeta.setResourceType("Group");
    	setMeta(userMeta);
    	Set<String> userSchemas = new HashSet<String>();
    	userSchemas.add("urn:scim:schemas:core:2.0:Group");    	
		setSchemas(userSchemas );
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
