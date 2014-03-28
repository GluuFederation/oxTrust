package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for displaying trust relationships
 * 
 * @author Pankaj
 * @author Yuriy Movchan Date: 11.05.2010
 * 
 */
@Scope(CONVERSATION)
@Name("trustRelationshipInventoryAction")
@Restrict("#{identity.loggedIn}")
public class TrustRelationshipInventoryAction implements Serializable {

	private static final long serialVersionUID = 8388485274418394665L;

	@In
	protected AttributeService attributeService;

	@In
	private TrustService trustService;

	@Logger
	private Log log;

	private List<GluuSAMLTrustRelationship> trustedSpList;

	public List<GluuSAMLTrustRelationship> getTrustedSpList() {
		return trustedSpList;
	}

	public void setTrustedSpList(List<GluuSAMLTrustRelationship> trustedSpList) {
		this.trustedSpList = trustedSpList;
	}

	@Restrict("#{s:hasPermission('trust', 'access')}")
	public String start() {
		if (trustedSpList != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		return search();
	}

	@Restrict("#{s:hasPermission('trust', 'access')}")
	public String search() {
		try {
			this.trustedSpList = trustService.getAllTrustRelationships();

			setCustomAttributes(this.trustedSpList);
		} catch (Exception ex) {
			log.error("Failed to find trust relationships", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void setCustomAttributes(List<GluuSAMLTrustRelationship> trustRelationships) {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		HashMap<String, GluuAttribute> attributesByDNs = attributeService.getAttributeMapByDNs(attributes);

		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			trustRelationship.setReleasedCustomAttributes(attributeService.getCustomAttributesByAttributeDNs(
					trustRelationship.getReleasedAttributes(), attributesByDNs));
		}
	}
}
