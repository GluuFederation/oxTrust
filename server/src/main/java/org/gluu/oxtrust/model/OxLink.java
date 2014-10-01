/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;
import java.util.Date;
import java.util.List;


/**
 * @author "Oleksiy Tataryn"
 *
 */
@LdapEntry
@LdapObjectClass(values = { "top", "oxLink" })
@EqualsAndHashCode(callSuper=false)
public @Data class OxLink extends Entry implements Serializable {

	private static final long serialVersionUID = -2129922260303558907L;

	@LdapAttribute(name="oxGuid")
	private String guid;
	
	@LdapAttribute(name="oxLinkExpirationDate")
	private Date linkExpirationDate;
	
	@LdapAttribute(name="oxLinkModerated")
	private Boolean linkModerated;
	
	@LdapAttribute(name="oxLinkModerators")
	private List<String> linkModerators;
	
	@LdapAttribute(name="oxLinkCreator")
	private String linkCreator;
	
	@LdapAttribute(name="oxLinkPending")
	private List<String> linkPending;
	
	@LdapAttribute(name="oxLinkLinktrack")
	private String linktrackLink;
	
	@LdapAttribute(name="description")
	private String description;
	
}
