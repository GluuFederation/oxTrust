/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.model.RenderParameters;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SmtpConfiguration;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.PasswordResetService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.RecaptchaService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.MailService;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * User: Dejan Maric
 */
@ConversationScoped
@Named("passwordReminderAction")
public class PasswordReminderAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RecaptchaService recaptchaService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private PersonService personService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private RenderParameters rendererParameters;

	@Inject
	private MailService mailService;

	@Inject
	private PasswordResetService passwordResetService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	private boolean passwordResetIsEnable = false;

	private AppConfiguration oxTrustappConfiguration;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	@Email
	@NotEmpty
	@NotBlank
	private String email;

	public String requestReminder() throws Exception {
		this.oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
		String outcome = requestReminderImpl();
		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			if (passwordResetIsEnable) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						facesMessages.evalResourceAsString("#{msgs['person.passwordreset.letterNotSent']}"));
			}
		}
		this.email = null;
		conversationService.endConversation();
		return outcome;
	}

	public String requestReminderImpl() throws Exception {
		if (enabled()) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			if (facesContext == null || externalContext == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}
			HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
			GluuCustomPerson person = new GluuCustomPerson();
			person.setMail(email);
			List<GluuCustomPerson> matchedPersons = personService.findPersons(person, 0);
			if (matchedPersons != null && matchedPersons.size() > 0) {
				passwordResetService.prepareBranch();
				PasswordResetRequest request = new PasswordResetRequest();
				String guid = passwordResetService.generateGuidForNewPasswordResetRequest();
				Date creationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
				request.setCreationDate(creationTime);
				request.setPersonInum(matchedPersons.get(0).getInum());
				request.setOxGuid(guid);
				request.setDn(passwordResetService.getDnForPasswordResetRequest(guid));
				int value = this.oxTrustappConfiguration.getPasswordResetRequestExpirationTime() / 60;

				// Set expiration time
				request.setExpirationDate(DateUtils.addSeconds(creationTime, appConfiguration.getPasswordResetRequestExpirationTime()));
		        int ttl = (int) ((request.getExpirationDate().getTime() - creationTime.getTime()) / 1000L);
		        request.setTtl(ttl);

				String expirationTime = Integer.toString(value) + " minute(s)";
				rendererParameters.setParameter("expirationTime", expirationTime);
				rendererParameters.setParameter("givenName", matchedPersons.get(0).getGivenName());
				rendererParameters.setParameter("organizationName",
						organizationService.getOrganization().getDisplayName());
				rendererParameters.setParameter("resetLink", appConfiguration.getApplicationUrl()
						+ httpServletRequest.getContextPath() + "/resetPassword.htm?guid=" + request.getOxGuid());

				String subj = facesMessages.evalResourceAsString("#{msgs['mail.reset.found.message.subject']}");
				String messagePlain = facesMessages
						.evalResourceAsString("#{msgs['mail.reset.found.message.plain.body']}");
				String messageHtml = facesMessages.evalResourceAsString("#{msgs['mail.reset.found.message.html.body']}");
				mailService.sendMail(email, null, subj, messagePlain, messageHtml);
				passwordResetService.addPasswordResetRequest(request);
				try {
					oxTrustAuditService.audit("PASSWORD REMINDER REQUEST" + request.getBaseDn() + " ADDED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				} catch (Exception e) {
				}
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					facesMessages.evalResourceAsString("#{msgs['resetPasswordSuccess.pleaseCheckYourEmail']}"));
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public boolean enabled() {
		GluuConfiguration configuration = configurationService.getConfiguration();
		SmtpConfiguration smtpConfiguration = configuration.getSmtpConfiguration();
		boolean valid = smtpConfiguration != null && smtpConfiguration.isValid()
				&& configurationService.getConfiguration().isPasswordResetAllowed();
		if (valid) {
			passwordResetIsEnable = true;
			if (recaptchaService.isEnabled() && getAuthenticationRecaptchaEnabled()) {
				valid = recaptchaService.verifyRecaptchaResponse();
				if (!valid) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, facesMessages
							.evalResourceAsString("#{msgs['person.passwordreset.catch.checkInputAndCaptcha']}"));
				}

			}
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['person.passwordreset.notActivate']}"));
		}
		return valid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RecaptchaService getRecaptchaService() {
		return recaptchaService;
	}

	public void setRecaptchaService(RecaptchaService recaptchaService) {
		this.recaptchaService = recaptchaService;
	}

	public boolean getAuthenticationRecaptchaEnabled() {
		return jsonConfigurationService.getOxTrustappConfiguration().isAuthenticationRecaptchaEnabled();
	}
}
