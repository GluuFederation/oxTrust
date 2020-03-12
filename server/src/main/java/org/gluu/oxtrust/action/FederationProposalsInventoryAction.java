/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.FederationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@ConversationScoped
@Named("federationProposalsInventoryAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class FederationProposalsInventoryAction implements Serializable {

	private static final long serialVersionUID = -1477997697645117954L;

	@Inject
	protected AttributeService attributeService;

	@Inject
	private FederationService federationService;

	@Inject
	private Logger log;

	private List<GluuSAMLFederationProposal> proposalsList;

	public String start() {
		try {
			this.proposalsList = federationService.getAllFederationProposals();
		} catch (Exception ex) {
			log.error("Failed to find federation proposals", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuSAMLFederationProposal> getProposalsList() {
		return proposalsList;
	}

	public void setTrustedSpList(List<GluuSAMLFederationProposal> trustedSpList) {
		this.proposalsList = trustedSpList;
	}

}
