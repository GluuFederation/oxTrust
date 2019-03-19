package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.model.PassportProvider;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.passport.PassportConfiguration;
import org.xdi.model.passport.Provider;
import org.xdi.model.passport.config.Configuration;
import org.xdi.model.passport.idpinitiated.IIConfiguration;
import org.xdi.service.security.Secure;

@Named("passportProvidersAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('passport', 'access')}")
public class PassportProvidersAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6747074157779841269L;

	@Inject
	private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private FacesMessages facesMessages;

	private boolean update = false;
	private String id;

	@Inject
	private ConversationService conversationService;
	private List<PassportProvider> providerSelections = new ArrayList<>();
	private List<Provider> providers = new ArrayList<>();
	private List<String> optionsKeys = new ArrayList<>();
	private Provider provider = new Provider();
	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration idpInitiated;
	private Configuration configuration;
	private String[] providerTypes = { "openidconnect-oxd", "saml", "openidconnect", "oauth" };

	public String init() {
		try {
			log.debug("Load passport configuration");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.providers = this.passportConfiguration.getProviders();
			this.providerSelections = this.providers.stream().map(PassportProvider::new).collect(Collectors.toList());
			log.debug("Load passport configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String add() {
		try {
			this.update = false;
			this.provider = new Provider();
			this.provider.setOptions(new HashMap<>());
			this.optionsKeys = new ArrayList<>(this.provider.getOptions().keySet());
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String update() {
		try {
			this.update = true;
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.providers = this.passportConfiguration.getProviders();
			for (Provider pro : providers) {
				if (pro.getId().equalsIgnoreCase(id)) {
					this.provider = pro;
					this.optionsKeys = new ArrayList<>(this.provider.getOptions().keySet());
					break;
				}
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String save() {
		try {
			if (!update) {
				this.provider.setId(UUID.randomUUID().toString());
				this.id = this.provider.getId();
				this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
				this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
				this.providers = this.passportConfiguration.getProviders();
				this.providers.add(provider);
			} else {
				this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
				this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
				this.providers = this.passportConfiguration.getProviders();
				for (Provider pro : this.providers) {
					if (pro.getId().equalsIgnoreCase(this.provider.getId())) {
						this.providers.remove(pro);
						this.providers.add(provider);
						break;
					}
				}
			}
			performSave();
			if (!update) {
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"Provider '#{passportProvidersAction.provider.displayName}' added successfully");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"Provider '#{passportProvidersAction.provider.displayName}' updated successfully");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_SUCCESS;
			}
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	private void performSave() {
		this.passportConfiguration.setProviders(this.providers);
		this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
		this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
	}

	public String cancel() {
		try {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "No provider added");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_SUCCESS;
		}

	}

	public List<Provider> getProviders() {
		return providers;
	}

	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}

	public IIConfiguration getIdpInitiated() {
		return idpInitiated;
	}

	public void setIdpInitiated(IIConfiguration idpInitiated) {
		this.idpInitiated = idpInitiated;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public List<String> getOptionsKeys() {
		return optionsKeys;
	}

	public void setOptionsKeys(List<String> optionsKeys) {
		this.optionsKeys = optionsKeys;
	}

	public String getMapValue(String key) {
		return this.provider.getOptions().get(key);
	}

	public void removeEntry(String key) {
		this.provider.getOptions().remove(key);
		optionsKeys.remove(key);
	}

	public void addEntry() {
		String newKey = "DefaultKey" + UUID.randomUUID().toString().substring(1, 6);
		this.provider.getOptions().put(newKey, "");
		optionsKeys.add(newKey);
	}

	public void deleteProviders() {
		for (PassportProvider passportProvider : providerSelections) {
			if (passportProvider.isChecked()) {
				this.providers.remove(passportProvider.getProvider());
			}
		}
		performSave();
		init();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getProviderTypes() {
		return providerTypes;
	}

	public List<PassportProvider> getProviderSelections() {
		return providerSelections;
	}

	public void setProviderSelections(List<PassportProvider> providerSelections) {
		this.providerSelections = providerSelections;
	}
}
