/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;
import org.xdi.model.user.UserRole;
import org.xdi.service.security.Secure;

/**
 * Action class for displaying trust relationships
 * 
 * @author Pankaj
 * @author Yuriy Movchan Date: 11.05.2010
 * 
 */
@ConversationScoped
@Named("trustRelationshipInventoryAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class TrustRelationshipInventoryAction implements Serializable {

	private static final long serialVersionUID = 8388485274418394665L;
	
	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	protected AttributeService attributeService;

	@Inject
	private TrustService trustService;

	@Inject
	private Logger log;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<GluuSAMLTrustRelationship> trustedSpList;

	public List<GluuSAMLTrustRelationship> getTrustedSpList() {
		return trustedSpList;
	}

	public void setTrustedSpList(List<GluuSAMLTrustRelationship> trustedSpList) {
		this.trustedSpList = trustedSpList;
	}

	public String start() {
		if (trustedSpList != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		return search();
	}

	public String search() {
		try {
			if(searchPattern == null || searchPattern.isEmpty()){
				this.trustedSpList = trustService.getAllSAMLTrustRelationships(100);
			}else{
				this.trustedSpList = trustService.searchSAMLTrustRelationships(searchPattern,100);
			}
			this.oldSearchPattern = this.searchPattern;

			setCustomAttributes(this.trustedSpList);
		} catch (Exception ex) {
			log.error("Failed to find trust relationships", ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find trust relationships");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void setCustomAttributes(List<GluuSAMLTrustRelationship> trustRelationships) {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(UserRole.ADMIN);
		HashMap<String, GluuAttribute> attributesByDNs = attributeService.getAttributeMapByDNs(attributes);

		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			trustRelationship.setReleasedCustomAttributes(attributeService.getCustomAttributesByAttributeDNs(
					trustRelationship.getReleasedAttributes(), attributesByDNs));
		}
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public String getOldSearchPattern() {
		return oldSearchPattern;
	}

	public void setOldSearchPattern(String oldSearchPattern) {
		this.oldSearchPattern = oldSearchPattern;
	}
}
