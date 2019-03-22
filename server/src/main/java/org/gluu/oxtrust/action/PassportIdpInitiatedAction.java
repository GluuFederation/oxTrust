package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.PassportService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.model.passport.PassportConfiguration;
import org.xdi.model.passport.idpinitiated.AuthzParams;
import org.xdi.model.passport.idpinitiated.IIConfiguration;
import org.xdi.service.security.Secure;

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
	@Inject
	private ConversationService conversationService;

	private LdapOxPassportConfiguration ldapOxPassportConfiguration;
	private PassportConfiguration passportConfiguration;
	private IIConfiguration iiConfiguration;
	private List<AuthzParams> authzParams = new ArrayList<>();

	private AuthzParams authzParam = new AuthzParams();

	public String init() {
		try {
			log.debug("Load passport idp initiated configuration");
			this.ldapOxPassportConfiguration = passportService.loadConfigurationFromLdap();
			this.passportConfiguration = this.ldapOxPassportConfiguration.getPassportConfiguration();
			this.iiConfiguration = this.passportConfiguration.getIdpInitiated();
			this.authzParams = this.iiConfiguration.getAuthorizationParams();
			log.debug("Load passport idp initiated configuration done");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.debug("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String save() {
		try {
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
		this.authzParams.add(this.authzParam);
	}

	public void removeAuthParam() {
		this.authzParams.remove(this.authzParam);
		this.showForm=false;
	}

	public void cancelParamAdd() {
		this.authzParam = new AuthzParams();
		this.showForm=false;
	}

	public boolean isShowForm() {
		log.info("+++++++++++++isShowForm");
		return showForm;
	}

	public void setShowForm(boolean showForm) {
		this.showForm = showForm;
	}

	public void activateForm() {
		log.info("+++++++++++++Activation");
		this.showForm = true;
		log.info("+++++++++++++Done: "+this.showForm);
	}

	public AuthzParams getAuthzParam() {
		return authzParam;
	}

	public void setAuthzParam(AuthzParams authzParam) {
		this.authzParam = authzParam;
	}

}
