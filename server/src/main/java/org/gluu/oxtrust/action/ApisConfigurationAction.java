package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;

import lombok.Data;

import org.gluu.oxtrust.ldap.service.LinktrackService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Configuration action for APIs integration
 * 
 * @author Oleksiy Tataryn Date: 06.04.2014
 */
@Scope(CONVERSATION)
@Name("apisConfigurationAction")
public @Data class ApisConfigurationAction implements Serializable {

	
	static final long serialVersionUID = 3932865544287448544L;

	@In
	private LinktrackService linktrackService;
	
	@In
	private OrganizationService organizationService;
	
	private Boolean enableLinktrack;
	private boolean linktrackVerified;
	private String linktrackLogin;
	private String linktrackPassword;
	

	public String init(){
		GluuOrganization organization = organizationService.getOrganization();
		enableLinktrack = organization.getLinktrackEnabled();
		linktrackLogin = organization.getLinktrackLogin();
		linktrackPassword = organization.getLinktrackPassword();
		return OxTrustConstants.RESULT_SUCCESS;	
	}
	
	public String verify(){
		String testLink = linktrackService.newLink(linktrackLogin, linktrackPassword, "http://www.google.com");
		if(testLink == null){
			linktrackVerified = false;
			return OxTrustConstants.RESULT_FAILURE;
		}
		linktrackVerified = true;
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String save(){
		GluuOrganization organization = OrganizationService.instance().getOrganization();
		organization.setLinktrackEnabled(enableLinktrack);
		organization.setLinktrackLogin(linktrackLogin);
		organization.setLinktrackPassword(linktrackPassword);
		organizationService.updateOrganization(organization);
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public void invalidate(){
		linktrackVerified = false;	
	}
}
