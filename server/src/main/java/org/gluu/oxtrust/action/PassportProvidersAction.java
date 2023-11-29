package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.Provider;
import org.gluu.model.passport.config.Configuration;
import org.gluu.model.passport.idpinitiated.IIConfiguration;
import org.gluu.oxtrust.model.OptionEntry;
import org.gluu.oxtrust.model.PassportProvider;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@Named("passportProvidersAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('passport', 'access')}")
public class PassportProvidersAction implements Serializable {

	private String ISSUER = "issuer";

	private String CLIENT_SECRET = "clientSecret";

	private String CLIENT_ID = "clientID";

	private String DEFAULT_ISSUER = "urn:test:example";

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

	@Inject
	private ConfigurationService configurationService;

	private boolean update = false;

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	private String id;

	@Inject
	private ConversationService conversationService;
	private List<PassportProvider> providerSelections = new ArrayList<>();
	private List<Provider> providers = new ArrayList<>();
	private List<String> optionsKeys = new ArrayList<>();
	private List<OptionEntry> options = new ArrayList<>();
	private Provider provider = new Provider();
	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration idpInitiated;
	private Configuration configuration;
	private String[] providerTypes = { "saml", "openid-client", "openidconnect-oxd", "oauth" };
	private String[] mappings = { "apple", "facebook", "google", "dropbox", "github", "linkedin", "twitter",
			"windowslive", "tumblr", "saml_basic_profile", "saml_ldap_profile", "oxd-default", "openid-client" };
	private String[] strategies = { "@node-saml/passport-saml", "openid-client", "passport-oxd", "@nicokaiser/passport-apple",
			"passport-dropbox-oauth2", "passport-facebook", "passport-github", "passport-google-oauth2",
			"@sokratis/passport-linkedin-oauth2", "passport-tumblr", "passport-twitter", "passport-windowslive" };

	public String init() {
		try {
			log.debug("Load passport configuration");
			loadProviders();
			this.providerSelections = this.providers.stream().map(PassportProvider::new).collect(Collectors.toList());
			log.debug("Load passport configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	private void loadProviders() {
		this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
		this.providers = this.passportConfiguration.getProviders();
	}

	public String add() {
		try {
			loadProviders();
			this.update = false;
			this.provider = new Provider();
			this.provider.setOptions(new HashMap<>());
			this.options = this.provider.getOptions().entrySet().stream()
					.map(e -> new OptionEntry(e.getKey(), e.getValue())).collect(Collectors.toList());
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public void handleRequiredOptions(ValueChangeEvent e) {
		String type = e.getNewValue().toString();
		if (!this.update && type != null) {
			if (type.equalsIgnoreCase(providerTypes[0])) {
				this.options = new ArrayList<>();
				this.options.add(new OptionEntry("entryPoint", "https://idp.example.com/idp/profile/SAML2/POST/SSO"));
				this.options.add(
						new OptionEntry("identifierFormat", "urn:oasis:names:tc:SAML:2.0:nameid-format:transient"));
				this.options.add(new OptionEntry("authnRequestBinding", "HTTP-POST"));
				this.options.add(new OptionEntry(ISSUER, DEFAULT_ISSUER));
				this.options.add(new OptionEntry("cert", ""));
			}
			if (type.equalsIgnoreCase(providerTypes[1])) {
				String scopes = "[\"openid\",\"email\",\"profile\"]";
				this.options = new ArrayList<>();
				this.options.add(new OptionEntry("client_id", ""));
				this.options.add(new OptionEntry("client_secret", ""));
				this.options.add(new OptionEntry(ISSUER, "https://server.example.com"));
				this.options.add(new OptionEntry("scope", scopes));
				this.options.add(new OptionEntry("token_endpoint_auth_method", "client_secret_post"));
			}
			if (type.equalsIgnoreCase(providerTypes[2])) {
				this.options = new ArrayList<>();
				this.options.add(new OptionEntry(CLIENT_ID, ""));
				this.options.add(new OptionEntry(CLIENT_SECRET, ""));
				this.options.add(new OptionEntry("oxdID", ""));
				this.options.add(new OptionEntry(ISSUER, "https://server.example.com"));
				this.options.add(new OptionEntry("oxdServer", "https://oxd-server.acme.com:8443"));
			}
			if (type.equalsIgnoreCase(providerTypes[3])) {
				this.options = new ArrayList<>();
				this.options.add(new OptionEntry(CLIENT_ID, ""));
				this.options.add(new OptionEntry(CLIENT_SECRET, ""));
			}
		}

	}

	public String update() {
		try {
			this.update = true;
			loadProviders();
			for (Provider pro : providers) {
				if (pro.getId().equalsIgnoreCase(id)) {
					this.provider = pro;
					this.optionsKeys = new ArrayList<>(this.provider.getOptions().keySet());
					break;
				}
			}
			this.options = this.provider.getOptions().entrySet().stream()
					.map(e -> new OptionEntry(e.getKey(), e.getValue())).collect(Collectors.toList());
			this.options.sort(Comparator.comparing(OptionEntry::getKey));
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String save() {
		try {
			if (this.provider.getLogoImg().isEmpty()) {
				this.provider.setLogoImg(null);
			}
			if (this.provider.getPassportAuthnParams().isEmpty()) {
				this.provider.setPassportAuthnParams(null);
			}
			if (providerIdContainsBadCharacters()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "This provider id contains unauthorized characters.");
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (!update) {
				if (providerIdIsInUse()) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"This provider id is already in use. Please provide a new one.");
					return OxTrustConstants.RESULT_FAILURE;
				}
				setCallbackUrl();
				this.id = this.provider.getId();
				this.provider.setOptions(options.stream().filter(e -> e.getKey() != null)
						.collect(Collectors.toMap(OptionEntry::getKey, OptionEntry::getValue)));
				loadProviders();
				this.providers.add(provider);
			} else {
				this.provider.setOptions(options.stream().filter(e -> e.getKey() != null)
						.collect(Collectors.toMap(OptionEntry::getKey, OptionEntry::getValue)));
				loadProviders();
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
				return OxTrustConstants.RESULT_CONFIRM;
			}
		} catch (Exception e) {
			log.debug("", e);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	private void setCallbackUrl() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		String hostname = context.getRequestServerName();
		if (hostname == null || hostname.isEmpty()) {
			hostname = configurationService.getConfiguration().getHostname();
		}
		if (this.provider.getType().equalsIgnoreCase("saml")) {
			this.provider.setCallbackUrl(
					String.format("https://%s/passport/auth/saml/%s/callback", hostname, this.provider.getId()));
		} else {
			this.provider.setCallbackUrl(
					String.format("https://%s/passport/auth/%s/callback", hostname, this.provider.getId()));
		}
	}

	private boolean providerIdIsInUse() {
		loadProviders();
		boolean result = false;
		for (Provider provider : this.providers) {
			if (provider.getId().equalsIgnoreCase(this.provider.getId())) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean providerIdContainsBadCharacters() {
		return !Pattern.compile("^[-a-zA-Z0-9_\\\\-\\\\:\\\\/\\\\.]+$").matcher(this.provider.getId()).matches();
	}

	private void performSave() {
		this.passportConfiguration.setProviders(this.providers);
		this.ldapOxPassportConfiguration.setPassportConfiguration(this.passportConfiguration);
		this.passportService.updateLdapOxPassportConfiguration(this.ldapOxPassportConfiguration);
	}

	public String cancel() {
		try {
			if (update) {
				facesMessages.add(FacesMessage.SEVERITY_INFO, "No change performed");
			} else {
				facesMessages.add(FacesMessage.SEVERITY_INFO, "No provider added");
			}
			conversationService.endConversation();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
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

	public void removeEntry(List<OptionEntry> options, OptionEntry entry) {
		options.remove(entry);
	}

	public void addEntry(List<OptionEntry> options) {
		options.add(new OptionEntry("", ""));
	}

	public String deleteProvider(Provider provider) {
		this.providers.remove(provider);
		performSave();
		init();
		facesMessages.add(FacesMessage.SEVERITY_INFO,
				"Provider " + provider.getDisplayName() + " successfully deleted");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<OptionEntry> getOptions() {
		return options;
	}

	public void setOptions(List<OptionEntry> options) {
		this.options = options;
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

	public String[] getMappings() {
		return mappings;
	}

	public void setMappings(String[] mappings) {
		this.mappings = mappings;
	}

	public String[] getStrategies() {
		return strategies;
	}

	public void setStrategies(String[] strategies) {
		this.strategies = strategies;
	}
}
