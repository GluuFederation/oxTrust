package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;

/**
 * Action class for displaying attributes
 * 
 * @author Yuriy Movchan Date: 10.17.2010
 */
@Scope(CONVERSATION)
@Name("attributeInventoryAction")
@Restrict("#{identity.loggedIn}")
public class AttributeInventoryAction implements Serializable {

	private static final long serialVersionUID = -3832167044333943686L;

	private boolean showInactive = false;

	@Logger
	private Log log;

	private List<GluuAttribute> attributeList;

	@In
	private AttributeService attributeService;

	private List<GluuAttribute> activeAttributeList;

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String start() {
		if (attributeList == null) {
			try {
				this.attributeList = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
				this.setActiveAttributeList(attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN));
			} catch (LdapMappingException ex) {
				log.error("Failed to load attributes", ex);

				return OxTrustConstants.RESULT_FAILURE;
			}
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuAttribute> getAttributeList() {
		if (showInactive) {
			return attributeList;
		} else {
			return activeAttributeList;
		}

	}

	/**
	 * @return the showInactive
	 */
	public boolean isShowInactive() {
		return showInactive;
	}

	/**
	 * @param showInactive
	 *            the showInactive to set
	 */
	public void setShowInactive(boolean showInactive) {
		this.showInactive = showInactive;
	}

	public void toggleShowInactive() {
		this.showInactive = !showInactive;
		for (GluuAttribute attribute : attributeList) {
			if (attribute.getStatus().equals(GluuStatus.INACTIVE)) {

			}
		}
	}

	/**
	 * @return the activeAttributeList
	 */
	public List<GluuAttribute> getActiveAttributeList() {
		return activeAttributeList;
	}

	/**
	 * @param activeAttributeList
	 *            the activeAttributeList to set
	 */
	public void setActiveAttributeList(List<GluuAttribute> activeAttributeList) {
		this.activeAttributeList = activeAttributeList;
	}

}
