package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.util.StringHelper;

/**
 * SCIM Access Token retriever Action
 * 
 * @author Yuriy Movchan Date: 08/05/2013
 */
@Scope(ScopeType.CONVERSATION)
@Name("scimConfigurationAction")
@Restrict("#{identity.loggedIn}")
public class ScimConfigurationAction implements Serializable {

	private static final long serialVersionUID = 6356638577562487737L;

	@Logger
	private Log log;

	@In
	private OrganizationService organizationService;
	
	@In
	private GroupService groupService;

	private boolean initialized;

	private GluuOrganization organization;

	private GluuGroup scimGroup;

	@Restrict("#{s:hasPermission('scim', 'access')}")
	public String init() {
		if (this.organization != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.organization = organizationService.getOrganization();
		this.scimGroup = getScimGroup(this.organization);
		if (this.scimGroup == null) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scim', 'access')}")
	public void cancel() {
	}

	private GluuGroup getScimGroup(GluuOrganization organization) {
		String scimGroupDn = organization.getScimGroup();
		if (StringHelper.isEmpty(scimGroupDn)) {
			return null;
		}

		try {
			GluuGroup scimGroup = groupService.getGroupByDn(scimGroupDn);
			if (scimGroup == null) {
				return null;
			}

			return scimGroup;
		} catch (LdapMappingException ex) {
			log.error("Failed to load SCIM group by DN: '{0}'", ex, scimGroupDn);

			return null;
		}
	}


	@Restrict("#{s:hasPermission('scim', 'access')}")
	public String changeScimStatus() {
		try {
			if (GluuStatus.ACTIVE.equals(this.organization.getScimStatus())) {
				this.organization.setScimStatus(GluuStatus.INACTIVE);
			} else {
				this.organization.setScimStatus(GluuStatus.ACTIVE);
			}

			this.organizationService.updateOrganization(this.organization);
		} catch (Exception ex) {
			log.error("Could not change ScimStatus", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scim', 'access')}")
	public String changeAuthMode() {
		try {
			if (StringHelper.equalsIgnoreCase(this.organization.getScimAuthMode(), "bearer")) {
				this.organization.setScimAuthMode("basic");
			} else {
				this.organization.setScimAuthMode("bearer");
			}

			this.organizationService.updateOrganization(this.organization);
		} catch (Exception ex) {
			log.error("Could not change ScimStatus", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public GluuOrganization getOrganization() {
		return organization;
	}

	public GluuGroup getScimGroup() {
		return scimGroup;
	}

}
