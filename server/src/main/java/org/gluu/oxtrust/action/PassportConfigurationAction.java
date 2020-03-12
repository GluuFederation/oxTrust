package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.service.PassportService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.gluu.config.oxtrust.LdapOxPassportConfiguration;
import org.gluu.model.passport.PassportConfiguration;
import org.gluu.model.passport.config.Configuration;
import org.gluu.service.security.Secure;

@Named("passportConfigurationAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('passport', 'access')}")
public class PassportConfigurationAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2248724241209494078L;

	@Inject
	private Logger log;

	@Inject
	private PassportService passportService;

	@Inject
	private FacesMessages facesMessages;
	@Inject
	private ConversationService conversationService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private Configuration configuration;
	private String[] loggingLevels = { "error", "warn", "info", "verbose", "debug" };

	public String init() {
		try {
			log.debug("Load passport basic configuration");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.configuration = this.passportConfiguration.getConf();
			log.debug("Load passport configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String save() {
		try {
			this.passportConfiguration.setConf(configuration);
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

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String[] getLoggingLevels() {
		return loggingLevels;
	}

	public void setLoggingLevels(String[] loggingLevels) {
		this.loggingLevels = loggingLevels;
	}
}
