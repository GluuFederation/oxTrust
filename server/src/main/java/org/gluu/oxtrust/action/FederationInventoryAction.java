/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

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
@Name("federationInventoryAction")
public class FederationInventoryAction implements Serializable {

	private static final long serialVersionUID = -1477997697645117954L;

	@In
	protected AttributeService attributeService;

	@In
	private FederationService federationService;

	@Logger
	private Log log;

	private List<GluuSAMLFederationProposal> federations;

	public String start() {
		try {
			this.federations = federationService.getAllFederations();
		} catch (Exception ex) {
			log.error("Failed to find federations", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuSAMLFederationProposal> getFederations() {
		return federations;
	}

}
