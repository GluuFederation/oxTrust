package org.gluu.oxtrust.action.uma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.LookupService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.ldap.service.uma.UmaPolicyService;
import org.gluu.oxtrust.model.DisplayNameEntry;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.oxauth.model.uma.persistence.UmaPolicy;
import org.xdi.util.SelectableEntityHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update UMA policies
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@Name("updateUmaPolicyAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UpdateUmaPolicyAction implements Serializable {

	private static final long serialVersionUID = 9180729281938167478L;

	@Logger
	private Log log;

	@In
	protected GluuCustomPerson currentPerson;

	@In
	private UmaPolicyService umaPolicyService;

	@In
	private ScopeDescriptionService scopeDescriptionService;

	@In
	private ClientService clientService;

	@In
	private LookupService lookupService;

	private String policyInum;

	private UmaPolicy umaPolicy;
	private List<DisplayNameEntry> scopes;

	private List<SelectableEntity<ScopeDescription>> availableScopes;
	private String searchAvailableScopePattern, oldSearchAvailableScopePattern;

	private boolean update;

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String modify() {
		if (this.umaPolicy != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.policyInum);

		try {
			umaPolicyService.prepareUmaPolicyBranch();
		} catch (Exception ex) {
			log.error("Failed to initialize form", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (update) {
			return update();
		} else {
			return add();
		}
	}

	private String add() {
		this.umaPolicy = new UmaPolicy();

		this.scopes = new ArrayList<DisplayNameEntry>();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		log.debug("Loading UMA UMA policy '{0}' for host '{1}'", this.policyInum);
		try {
			String policyDn = umaPolicyService.getDnForUmaPolicy(this.policyInum);
			this.umaPolicy = umaPolicyService.getUmaPolicyByDn(policyDn);
		} catch (LdapMappingException ex) {
			log.error("Failed to find UMA policy '{0}'", ex, this.policyInum);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.umaPolicy == null) {
			log.error("UMA policy is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.scopes = getScopesDisplayNameEntries();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String save() {
		updateScopes();

		if (this.update) {
			// Update UMA policy
			try {
				umaPolicyService.updateUmaPolicy(this.umaPolicy);
			} catch (LdapMappingException ex) {
				log.error("Failed to update UMA policy '{0}'", ex, this.umaPolicy.getInum());
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			String inum = umaPolicyService.generateInumForNewUmaPolicy();
			String policyDn = umaPolicyService.getDnForUmaPolicy(inum);

			this.umaPolicy.setInum(inum);
			this.umaPolicy.setDn(policyDn);

			// Save UMA policy
			try {
				umaPolicyService.addUmaPolicy(this.umaPolicy);
			} catch (LdapMappingException ex) {
				log.error("Failed to add new UMA policy '{0}'", ex, this.umaPolicy.getInum());
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;
		}

		log.debug("UMA policy were {0} successfully", (this.update ? "added" : "updated"));
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String delete() {
		if (update) {
			// Remove UMA policy
			try {
				umaPolicyService.removeUmaPolicy(this.umaPolicy);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove UMA policy {0}", ex, this.umaPolicy.getInum());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	@Destroy
	public void destroy() throws Exception {
		cancel();
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void searchAvailableScopes() {
		if (Util.equals(this.oldSearchAvailableScopePattern, this.searchAvailableScopePattern)) {
			return;
		}

		try {
			this.availableScopes = SelectableEntityHelper.convertToSelectableEntityModel(scopeDescriptionService.findScopeDescriptions(
					this.searchAvailableScopePattern, 100));
			this.oldSearchAvailableScopePattern = this.searchAvailableScopePattern;

			selectAddedScopes();
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void selectAddedScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<ScopeDescription> availableScope : this.availableScopes) {
			availableScope.setSelected(addedScopeInums.contains(availableScope.getEntity().getInum()));
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void acceptSelectScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<ScopeDescription> availableScope : this.availableScopes) {
			ScopeDescription scopeDescription = availableScope.getEntity();
			String scopeDescriptionInum = scopeDescription.getInum();

			if (availableScope.isSelected() && !addedScopeInums.contains(scopeDescriptionInum)) {
				addScope(scopeDescription);
			}

			if (!availableScope.isSelected() && addedScopeInums.contains(scopeDescriptionInum)) {
				removeScope(scopeDescriptionInum);
			}
		}

	}

	private Set<String> getAddedScopesInums() {
		Set<String> addedScopeInums = new HashSet<String>();

		if (this.availableScopes == null) {
			return addedScopeInums;
		}

		for (DisplayNameEntry scope : this.scopes) {
			addedScopeInums.add(scope.getInum());
		}
		return addedScopeInums;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelSelectScopes() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void addScope(ScopeDescription scope) {
		DisplayNameEntry oneScope = new DisplayNameEntry(scope.getDn(), scope.getInum(), scope.getDisplayName());
		this.scopes.add(oneScope);
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void removeScope(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeScopeDn = scopeDescriptionService.getDnForScopeDescription(inum);

		for (Iterator<DisplayNameEntry> iterator = this.scopes.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneScope = iterator.next();
			if (removeScopeDn.equals(oneScope.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	private void updateScopes() {
		if ((this.scopes == null) || (this.scopes.size() == 0)) {
			this.umaPolicy.setScopeDns(null);
			return;
		}

		List<String> tmpScopes = new ArrayList<String>();
		for (DisplayNameEntry scope : this.scopes) {
			tmpScopes.add(scope.getDn());
		}

		this.umaPolicy.setScopeDns(tmpScopes);
	}

	private List<DisplayNameEntry> getScopesDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeDescriptionService.getDnForScopeDescription(null),
				this.umaPolicy.getScopeDns());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public boolean isUpdate() {
		return update;
	}

	public String getPolicyInum() {
		return policyInum;
	}

	public void setPolicyInum(String policyInum) {
		this.policyInum = policyInum;
	}

	public UmaPolicy getUmaPolicy() {
		return umaPolicy;
	}

	public List<DisplayNameEntry> getScopes() {
		return scopes;
	}

	public List<SelectableEntity<ScopeDescription>> getAvailableScopes() {
		return availableScopes;
	}

	public String getSearchAvailableScopePattern() {
		return searchAvailableScopePattern;
	}

	public void setSearchAvailableScopePattern(String searchAvailableScopePattern) {
		this.searchAvailableScopePattern = searchAvailableScopePattern;
	}

}
