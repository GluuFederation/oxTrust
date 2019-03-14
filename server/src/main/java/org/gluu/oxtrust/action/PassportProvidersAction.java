package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.PassportService;
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

	private List<Provider> providers;
	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration idpInitiated;
	private Configuration configuration;

	public void init() {
		log.debug("Load passport configuration");
		this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
		this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
		this.providers = this.passportConfiguration.getProviders();
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
}
