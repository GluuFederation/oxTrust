package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.PassportProviderType;
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

	private List<Provider> providers;
	private List<String> optionsKeys;
	private Provider provider;
	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration idpInitiated;
	private Configuration configuration;

	public void init() {
		log.info("+++++++++++++++++++Initiation+++++++++++++++++++");
		log.debug("Load passport configuration");
		this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
		this.providers = this.passportConfiguration.getProviders();
		setProvider(providers.get(0));
		optionsKeys = new ArrayList<>(provider.getOptions().keySet());
		this.configuration = this.passportConfiguration.getConf();
		this.idpInitiated = this.passportConfiguration.getIdpInitiated();
		log.debug("Load passport configuration done");
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

	public PassportProviderType[] getProvidersTypes() {
		return PassportProviderType.values();
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

	public String add() {
		log.info("+++++++++++++++++++Add+++++++++++++++++++");
		if (this.provider != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = false;
		this.provider = new Provider();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() {
		log.info("+++++++++++++++++++Update+++++++++++++++++++");
		if (this.provider != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = true;
		this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
		this.providers = this.passportConfiguration.getProviders();
		for (Provider pro : providers) {
			if (pro.getId().equalsIgnoreCase(id)) {
				this.provider = pro;
				break;
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void save() {
		log.info("+++++++++++++++++++Saving+++++++++++++++++++");
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
		this.passportConfiguration.setProviders(this.providers);
		this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
		this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
		facesMessages.add(FacesMessage.SEVERITY_INFO,
				"New client '#{updateClientAction.provider.displayName}' added successfully");
		conversationService.endConversation();
	}

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "New client not added");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

}
