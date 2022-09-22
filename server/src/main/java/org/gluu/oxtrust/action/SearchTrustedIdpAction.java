/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.model.OxTrustedIdp;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.TrustedIDPService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;
import org.slf4j.Logger;

/**
 * Action class for search Trusted Idp
 * 
 * @author Shekhar L. Date: 12.09.2022
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('client', 'access')}")
public class SearchTrustedIdpAction implements Serializable {

	private static final long serialVersionUID = 8361095046179474395L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern = "";

	private String oldSearchPattern;

	private List<OxTrustedIdp> oxTrustedIdpList;

	@Inject
	private TrustedIDPService trustedIdpService;

	public String start() {
		return search();
	}

	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return searchImpl();
	}

	protected String searchImpl() {
		try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				this.oxTrustedIdpList = trustedIdpService.getAllTrustedIDP();
			} else {
				this.oxTrustedIdpList = trustedIdpService.searchOxTrustedIdp(this.searchPattern, 100);
			}
			//this.trustedIdpList.sort(Comparator.comparing(OxAuthClient::getDisplayName));
			this.oldSearchPattern = this.searchPattern;
			this.searchPattern = "";
		} catch (Exception ex) {
			log.error("Failed to find trustedIdps", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find trustedIdps");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public String deleteTrustedIdps() {
		for (OxTrustedIdp trustedIdp : oxTrustedIdpList) {
			if (trustedIdp.isSelected()) {
				trustedIdpService.removeTrustedIDP(trustedIdp);
			}
		}
		return searchImpl();
	}

	public List<OxTrustedIdp> getOxTrustedIdpList() {
		return oxTrustedIdpList;
	}

	public void setOxTrustedIdpList(List<OxTrustedIdp> oxTrustedIdpList) {
		this.oxTrustedIdpList = oxTrustedIdpList;
	}
}
