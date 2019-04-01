package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.Provider;
import org.gluu.model.passport.idpinitiated.AuthzParams;
import org.gluu.model.passport.idpinitiated.IIConfiguration;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.service.security.Secure;

import com.google.common.collect.Lists;

@Named("passportIdpInitiatedAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('passport', 'access')}")
public class PassportIdpInitiatedAction implements Serializable {

	private static final long serialVersionUID = 6747074157779841269L;

	@Inject
	private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private FacesMessages facesMessages;
	private boolean showForm = false;
	private boolean isEdition = false;
	@Inject
	private ConversationService conversationService;

	@Inject
	private ClientService clientService;

	@Inject
	private ScopeService scopeService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration iiConfiguration;
	private List<AuthzParams> authzParams = new ArrayList<>();
	private List<OxAuthClient> clients = new ArrayList<>();
	private List<Provider> providers = new ArrayList<>();
	private List<String> scopes = new ArrayList<>();
	private List<String> responseTypes = new ArrayList<>();

	private AuthzParams authzParam = new AuthzParams();
	private AuthzParams previousParam;

	private String selectedTypes = null;

	public String init() {
		try {
			log.debug("Load passport idp initiated configuration");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.iiConfiguration = this.passportConfiguration.getIdpInitiated();
			this.authzParams = this.iiConfiguration.getAuthorizationParams();
			this.clients = clientService.getAllClients();
			this.providers = this.passportConfiguration.getProviders();
			this.scopes = scopeService.getAllScopesList(1000).stream().map(e -> e.getDisplayName())
					.collect(Collectors.toList());
			this.responseTypes = Lists.newArrayList(ResponseType.values()).stream().map(e -> e.getValue())
					.collect(Collectors.toList());
			log.debug("Load passport idp initiated configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String save() {
		try {
			this.iiConfiguration.setAuthorizationParams(authzParams);
			this.passportConfiguration.setIdpInitiated(iiConfiguration);
			this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
			passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "changes saved successfully!");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String cancel() {
		try {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "No change applied");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public List<AuthzParams> getAuthzParams() {
		return authzParams;
	}

	public void setAuthzParams(List<AuthzParams> authzParams) {
		this.authzParams = authzParams;
	}

	public IIConfiguration getIiConfiguration() {
		return iiConfiguration;
	}

	public void setIiConfiguration(IIConfiguration iiConfiguration) {
		this.iiConfiguration = iiConfiguration;
	}

	public void addAuthParam() {
		log.info("********************************");
		log.info("**********" + this.authzParam.getScopes());
		log.info("**********" + this.selectedTypes);
		if (this.isEdition) {
			this.authzParams.remove(this.previousParam);
			this.authzParams.add(this.authzParam);
		} else {
			this.authzParams.add(this.authzParam);
		}
		this.showForm = false;
		this.isEdition = false;
		this.authzParam = new AuthzParams();
		this.previousParam = null;
	}

	public void removeAuthParam(AuthzParams param) {
		this.authzParams.remove(param);
		this.showForm = false;
	}

	public void editAuthParam(AuthzParams param) {
		this.previousParam = param;
		this.authzParam = param;
		this.isEdition = true;
		this.showForm = true;
	}

	public void cancelParamAdd() {
		this.authzParam = new AuthzParams();
		this.showForm = false;
		this.previousParam = null;
	}

	public boolean isShowForm() {
		return showForm;
	}

	public void setShowForm(boolean showForm) {
		this.showForm = showForm;
	}

	public void activateForm() {
		this.authzParam = new AuthzParams();
		this.showForm = true;
	}

	public AuthzParams getAuthzParam() {
		return authzParam;
	}

	public void setAuthzParam(AuthzParams authzParam) {
		this.authzParam = authzParam;
	}

	public boolean isEdition() {
		return isEdition;
	}

	public void setEdition(boolean isEdition) {
		this.isEdition = isEdition;
	}

	public AuthzParams getPreviousParam() {
		return previousParam;
	}

	public void setPreviousParam(AuthzParams previousParam) {
		this.previousParam = previousParam;
	}

	public List<OxAuthClient> getClients() {
		return clients;
	}

	public void setClients(List<OxAuthClient> clients) {
		this.clients = clients;
	}

	public List<Provider> getProviders() {
		return providers;
	}

	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public List<String> getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(List<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	public String getSelectedTypes() {
		return selectedTypes;
	}

	public void setSelectedTypes(String selectedTypes) {
		this.selectedTypes = selectedTypes;
	}
}
