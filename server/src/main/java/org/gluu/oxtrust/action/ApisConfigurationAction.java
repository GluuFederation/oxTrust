/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;

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
public class ApisConfigurationAction implements Serializable {

	
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

    /**
     * @return the linktrackService
     */
    public LinktrackService getLinktrackService() {
        return linktrackService;
    }

    /**
     * @param linktrackService the linktrackService to set
     */
    public void setLinktrackService(LinktrackService linktrackService) {
        this.linktrackService = linktrackService;
    }

    /**
     * @return the organizationService
     */
    public OrganizationService getOrganizationService() {
        return organizationService;
    }

    /**
     * @param organizationService the organizationService to set
     */
    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * @return the enableLinktrack
     */
    public Boolean getEnableLinktrack() {
        return enableLinktrack;
    }

    /**
     * @param enableLinktrack the enableLinktrack to set
     */
    public void setEnableLinktrack(Boolean enableLinktrack) {
        this.enableLinktrack = enableLinktrack;
    }

    /**
     * @return the linktrackVerified
     */
    public boolean isLinktrackVerified() {
        return linktrackVerified;
    }

    /**
     * @param linktrackVerified the linktrackVerified to set
     */
    public void setLinktrackVerified(boolean linktrackVerified) {
        this.linktrackVerified = linktrackVerified;
    }

    /**
     * @return the linktrackLogin
     */
    public String getLinktrackLogin() {
        return linktrackLogin;
    }

    /**
     * @param linktrackLogin the linktrackLogin to set
     */
    public void setLinktrackLogin(String linktrackLogin) {
        this.linktrackLogin = linktrackLogin;
    }

    /**
     * @return the linktrackPassword
     */
    public String getLinktrackPassword() {
        return linktrackPassword;
    }

    /**
     * @param linktrackPassword the linktrackPassword to set
     */
    public void setLinktrackPassword(String linktrackPassword) {
        this.linktrackPassword = linktrackPassword;
    }
}
