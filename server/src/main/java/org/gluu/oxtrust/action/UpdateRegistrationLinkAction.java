/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.Data;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.LinktrackService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RegistrationLinkService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.OxLink;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.web.ServletContexts;
import org.xdi.util.Util;

/**
 * Action class for listing of registration links
 * @author Oleksiy Tataryn Date: 23.03.2014
 * 
 */
@Scope(CONVERSATION)
@Name("updateRegistrationLinkAction")
@Restrict("#{identity.loggedIn}")
public @Data class UpdateRegistrationLinkAction implements Serializable {

	private static final long serialVersionUID = 3337947202256952024L;

	@In
	private RegistrationLinkService registrationLinkService;
	
	@In
	private PersonService personService;
	
	@In
	private OrganizationService organizationService;
	
	@In
	private LinktrackService linktrackService;
	
	@In
	private GroupService groupService;
	
	@In 
	private Credentials credentials;
	
	private String searchPattern;
	
	private String oldSearchPattern;
	
	private String personData;
	
	private String personUid;
	
	@Logger
	private Log log;

	private OxLink link;
	
	private List<GluuCustomPerson> people;
	private List<GluuCustomPerson> moderators = new ArrayList<GluuCustomPerson>();

	@Restrict("#{s:hasPermission('person', 'access')}")
	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		
		if (this.searchPattern == null || this.searchPattern.length() < 2) {
			return OxTrustConstants.RESULT_VALIDATION_ERROR;
		}

		try {
			this.people = personService.searchPersons(this.searchPattern, OxTrustConstants.searchPersonsSizeLimit);
			for(GluuCustomPerson moderator : moderators){
				if(! people.contains(moderator)){
					people.add(moderator);
				}
			}
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	
	@Restrict("#{s:hasPermission('registrationLinks', 'access')}")
	public String add() {
		if (this.link == null) {
			this.link = new OxLink();
			this.link.setGuid(java.util.UUID.randomUUID().toString());
			Calendar expiryCalendar = Calendar.getInstance();
			expiryCalendar.add(Calendar.MONTH, 1);
			this.link.setLinkExpirationDate(expiryCalendar.getTime());
			this.link.setLinkCreator(credentials.getUsername());
			this.link.setLinkModerated(false);
			this.link.setLinkModerators(new ArrayList<String>());
		}
		
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String save() {
		if(link.getLinkModerated() && moderators.isEmpty()){
			return OxTrustConstants.RESULT_VALIDATION_ERROR;
		}
		for(GluuCustomPerson moderator: moderators){
			this.link.getLinkModerators().add(moderator.getUid());
		}
		GluuOrganization organization = organizationService.getOrganization();
		if(organization.getLinktrackEnabled() != null && organization.getLinktrackEnabled()){
			String contextPath = ServletContexts.instance().getRequest().getContextPath();
			String linktrackLink = linktrackService.newLink(organization.getLinktrackLogin(), 
					organization.getLinktrackPassword(), 
					OxTrustConfiguration.instance()
						.getApplicationConfiguration().getApplianceUrl()
						+ contextPath + "/register/" + link.getGuid());
			link.setLinktrackLink(linktrackLink);
		}
		registrationLinkService.addLink(link);
		return OxTrustConstants.RESULT_SUCCESS;

	}
	
	public String cancel() {
		return OxTrustConstants.RESULT_SUCCESS;

	}
	
	public String lookupPersonData(){
		GluuCustomPerson person = personService.getPersonByUid(personUid);
		personData = "Uid:\t" +  personUid;
		personData += "<br/>CN:\t" +  person.getCommonName();
		List<String> groups = person.getMemberOf();
		if(groups != null && ! groups.isEmpty()){
			for(String groupDn: groups){
				GluuGroup group = groupService.getGroupByDn(groupDn);
				if(group != null){
					personData += "<br/>memberOf:\t" + group.getDisplayName();
				}
			}
		}
		personData += "<br/>email:\t" +  person.getMail();

		return OxTrustConstants.RESULT_SUCCESS;
	}

}
