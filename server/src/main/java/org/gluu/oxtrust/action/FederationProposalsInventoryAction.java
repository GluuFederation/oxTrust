package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.FederationService;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Scope(CONVERSATION)
@Name("federationProposalsInventoryAction")
public class FederationProposalsInventoryAction implements Serializable {

	private static final long serialVersionUID = -1477997697645117954L;

	@In
	protected AttributeService attributeService;

	@In
	private FederationService federationService;

	@Logger
	private Log log;

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
