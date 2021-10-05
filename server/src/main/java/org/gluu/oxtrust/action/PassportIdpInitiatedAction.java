package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SelectableEntity;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.Provider;
import org.gluu.model.passport.idpinitiated.AuthzParams;
import org.gluu.model.passport.idpinitiated.IIConfiguration;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxtrust.model.OptionEntry;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

@ConversationScoped
@Named("passportIdpInitiatedAction")
@Secure("#{permissionService.hasPermission('passport', 'access')}")
public class PassportIdpInitiatedAction implements Serializable {

	private static final long serialVersionUID = 6747074157779841269L;

	@Inject
	private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private FacesMessages facesMessages;
	private boolean showForm = false;
	private boolean isEdition = false;
	@Inject
	private ConversationService conversationService;

	@Inject
	private ClientService clientService;

	@Inject
	private AbstractCustomScriptService customScriptService;

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
	private List<OptionEntry> options = new ArrayList<>();
	private List<SelectableEntity<String>> availableScopes;
	private List<SelectableEntity<String>> availableResponseTypes;
	private List<String> authScripts=new ArrayList<>();
	private AuthzParams authzParam = new AuthzParams();
	private AuthzParams previousParam;
	private String acrValue;

	public String init() {
		try {
			log.debug("Load passport idp initiated configuration");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.iiConfiguration = this.passportConfiguration.getIdpInitiated();
			this.acrValue = this.iiConfiguration.getOpenidclient().getAcrValues();
			this.authzParams = this.iiConfiguration.getAuthorizationParams();
			this.clients = clientService.getAllClients();
			this.scopes.add("openid");
			this.responseTypes.add("code");
			this.providers = this.passportConfiguration.getProviders().stream()
					.filter(e -> e.getType().equalsIgnoreCase("saml")).collect(Collectors.toList());
			loadAuthScripts();
			log.debug("Load passport idp initiated configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}
	private void loadAuthScripts(){
		List<CustomScript> scripts=customScriptService.findCustomScripts(
				Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION), "displayName", "oxLevel", "oxEnabled");
	    this.authScripts=scripts.stream().filter(e ->e.isEnabled()).map(i ->i.getName()).collect(Collectors.toList());
	    this.authScripts.remove(getAcrValue());
		this.authScripts.add(getAcrValue());
	}


	public String save() {
		try {
			this.iiConfiguration.setAuthorizationParams(authzParams);
			this.iiConfiguration.getOpenidclient().setAcrValues(getAcrValue());
			updateClientRedirects();
			this.passportConfiguration.setIdpInitiated(iiConfiguration);
			this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
			passportService.updateLdapOxPassportConfiguration(ldapOxPassportConfiguration);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Changes saved successfully!");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	private void updateClientRedirects() {
		List<String> total = new ArrayList<>();
		List<String> urls = new ArrayList<>();
		for (AuthzParams param : this.iiConfiguration.getAuthorizationParams()) {
			if (param.getRedirectUri() != null) {
				urls.add(param.getRedirectUri().trim());
			}
		}
		OxAuthClient client = clientService.getClientByInum(this.iiConfiguration.getOpenidclient().getClientId());
		List<String> existingUrls = client.getOxAuthRedirectURIs();
		if (existingUrls != null) {
			total.addAll(existingUrls);
			total.addAll(urls);
			Set<String> set = new HashSet<String>(total);
			client.setOxAuthRedirectURIs(new ArrayList<String>(set));
		} else {
			client.setOxAuthRedirectURIs(existingUrls);
		}
		clientService.updateClient(client);
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
		StringBuilder scopesBuilder = new StringBuilder();
		scopes.forEach(e -> {
			scopesBuilder.append(e);
			scopesBuilder.append(" ");
		});
		StringBuilder typesBuilder = new StringBuilder();
		responseTypes.forEach(e -> {
			typesBuilder.append(e);
			typesBuilder.append(" ");
		});
		if (isValid()) {
			this.authzParam.setScopes(scopesBuilder.toString().trim());
			this.authzParam.setResponseType(typesBuilder.toString().trim());
			this.authzParam.setExtraParams(options.stream().filter(e -> e.getKey() != null)
					.collect(Collectors.toMap(OptionEntry::getKey, OptionEntry::getValue)));
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
			this.options.clear();
			save();
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "All fields are required.");
		}
	}

	private boolean isValid() {
		if (this.scopes.isEmpty() || this.responseTypes.isEmpty()) {
			return false;
		}
		if (this.authzParam.getProvider() == null || this.authzParam.getRedirectUri() == null) {
			return false;
		}
		return true;
	}

	public void removeAuthParam(AuthzParams param) {
		this.authzParams.remove(param);
		this.showForm = false;
	}

	public void editAuthParam(AuthzParams param) {
		this.options = param.getExtraParams().entrySet().stream()
				.map(e -> new OptionEntry(e.getKey(), e.getValue())).collect(Collectors.toList());
		this.scopes = Stream.of(param.getScopes()).collect(Collectors.toList());
		this.responseTypes = Stream.of(param.getResponseType()).collect(Collectors.toList());
		this.previousParam = param;
		this.authzParam = param;
		this.isEdition = true;
		this.showForm = true;
	}

	public void cancelParamAdd() {
		this.scopes = new ArrayList<>();
		this.responseTypes = new ArrayList<>();
		this.authzParam = new AuthzParams();
		this.showForm = false;
		this.previousParam = null;
		this.options.clear();
	}

	public boolean isShowForm() {
		return showForm;
	}

	public void setShowForm(boolean showForm) {
		this.showForm = showForm;
	}

	public void activateForm() {
		this.authzParam = new AuthzParams();
		this.authzParam.setRedirectUri(getSamlUrl());
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

	public void removeScope(String scope) {
		this.scopes.remove(scope);
	}

	public void removeResponseType(String type) {
		this.responseTypes.remove(type);
	}

	public void searchAvailableScopes() {
		if (this.availableScopes != null) {
			selectAddedScopes();
			return;
		}
		this.availableScopes = scopeService.getAllScopesList(1000).stream().map(e -> e.getId())
				.map(e -> new SelectableEntity<String>(e)).collect(Collectors.toList());
		selectAddedScopes();
	}

	public void searchAvailableResponseTypes() {
		if (this.availableResponseTypes != null) {
			selectAddedResponseTypes();
			return;
		}
		this.availableResponseTypes = Lists.newArrayList(ResponseType.values()).stream().map(e -> e.getValue())
				.map(e -> new SelectableEntity<String>(e)).collect(Collectors.toList());
		selectAddedResponseTypes();
	}

	public void selectAddedScopes() {
		List<String> addedScopes = getScopes();
		for (SelectableEntity<String> availableScope : this.availableScopes) {
			availableScope.setSelected(addedScopes.contains(availableScope.getEntity()));
		}
	}

	private void selectAddedResponseTypes() {
		List<String> addedResponseTypes = getResponseTypes();
		for (SelectableEntity<String> availableResponseType : this.availableResponseTypes) {
			availableResponseType.setSelected(addedResponseTypes.contains(availableResponseType.getEntity()));
		}
	}

	public void acceptSelectScopes() {
		List<String> addedScopes = getScopes();
		for (SelectableEntity<String> availableScope : this.availableScopes) {
			String scope = availableScope.getEntity();
			if (availableScope.isSelected() && !addedScopes.contains(scope)) {
				this.scopes.add(scope);
			}
			if (!availableScope.isSelected() && addedScopes.contains(scope)) {
				this.scopes.remove(scope);
			}
		}
	}

	public List<String> getAuthScripts() {
		return authScripts;
	}

	public String getAcrValue() {
		return acrValue;
	}

	public void setAcrValue(String acrValue) {
		this.acrValue = acrValue;
	}

	public void acceptSelectResponseTypes() {
		List<String> addedResponseTypes = getResponseTypes();
		for (SelectableEntity<String> availableResponseType : this.availableResponseTypes) {
			String responseType = availableResponseType.getEntity();
			if (availableResponseType.isSelected() && !addedResponseTypes.contains(responseType)) {
				this.responseTypes.add(responseType);
			}

			if (!availableResponseType.isSelected() && addedResponseTypes.contains(responseType)) {
				this.responseTypes.remove(responseType);
			}
		}
	}


	public List<SelectableEntity<String>> getAvailableScopes() {
		return availableScopes;
	}

	public String getProviderName(String providerId) {
		for (Provider provider : this.providers) {
			if (provider.getId().equalsIgnoreCase(providerId)) {
				return provider.getDisplayName();
			}
		}
		return providerId;
	}

	public void setAvailableScopes(List<SelectableEntity<String>> availableScopes) {
		this.availableScopes = availableScopes;
	}

	public List<SelectableEntity<String>> getAvailableResponseTypes() {
		return availableResponseTypes;
	}

	public void setAvailableResponseTypes(List<SelectableEntity<String>> availableResponseTypes) {
		this.availableResponseTypes = availableResponseTypes;
	}

	public void cancelSelectScopes() {
	}

	public void cancelSelectResponseTypes() {
	}

	private String getSamlUrl() {
		return String.format("https://%s/oxauth/auth/passport/sample-redirector.htm",
				configurationService.getConfiguration().getHostname());
	}

	public List<OptionEntry> getOptions() {
		return options;
	}

	public void setOptions(List<OptionEntry> options) {
		this.options = options;
	}
	
	public void addEntry(List<OptionEntry> options) {
		options.add(new OptionEntry("", ""));
	}
	
	public void removeEntry(List<OptionEntry> options, OptionEntry entry) {
		options.remove(entry);
	}
	
	public String getMapValue(String key) {
		return this.authzParam.getExtraParams().get(key);
	}


}