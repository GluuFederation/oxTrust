/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.model.RenderParameters;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.OxTrustAuditService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RecaptchaService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.PasswordResetService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.SmtpConfiguration;
import org.xdi.service.MailService;
import org.xdi.util.StringHelper;

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
	private ApplianceService applianceService;

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

		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					facesMessages.evalResourceAsString("#{msg['person.passwordreset.emailLetterSent']}"));
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			if (passwordResetIsEnable) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						facesMessages.evalResourceAsString("#{msg['person.passwordreset.letterNotSent']}"));
			}

		}

		this.email = null;
		conversationService.endConversation();

		return outcome;
	}

	public String requestReminderImpl() throws Exception {
		if (enabled()) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}

			ExternalContext externalContext = facesContext.getExternalContext();
			if (externalContext == null) {
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

				request.setCreationDate(Calendar.getInstance().getTime());
				request.setPersonInum(matchedPersons.get(0).getInum());
				request.setOxGuid(guid);
				request.setDn(passwordResetService.getDnForPasswordResetRequest(guid));

				double value = new Double(this.oxTrustappConfiguration.getPasswordResetRequestExpirationTime()) / 60;
				String expirationTime = value + " minute(s)";
				rendererParameters.setParameter("expirationTime", expirationTime);
				rendererParameters.setParameter("givenName", matchedPersons.get(0).getGivenName());
				rendererParameters.setParameter("organizationName",
						organizationService.getOrganization().getDisplayName());
				rendererParameters.setParameter("resetLink", appConfiguration.getApplianceUrl()
						+ httpServletRequest.getContextPath() + "/resetPassword/" + request.getOxGuid());

				String subj = facesMessages.evalResourceAsString("#{msg['mail.reset.found.message.subject']}");
				String messagePlain = facesMessages
						.evalResourceAsString("#{msg['mail.reset.found.message.plain.body']}");
				String messageHtml = facesMessages.evalResourceAsString("#{msg['mail.reset.found.message.html.body']}");

				// rendererParameters.setParameter("mail_body", messageHtml);
				// String mailHtml =
				// renderService.renderView("/WEB-INF/mail/reset_password.xhtml");

				mailService.sendMail(email, null, subj, messagePlain, messageHtml);

				passwordResetService.addPasswordResetRequest(request);
				try {
					oxTrustAuditService.audit("PASSWORD REMINDER REQUEST" + request.getBaseDn() + " ADDED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				} catch (Exception e) {
				}

			}
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public boolean enabled() {
		GluuAppliance appliance = applianceService.getAppliance();
		SmtpConfiguration smtpConfiguration = appliance.getSmtpConfiguration();

		boolean valid = smtpConfiguration != null && smtpConfiguration.getHost() != null
				&& smtpConfiguration.getPort() != 0
				&& ((!smtpConfiguration.isRequiresAuthentication())
						|| (smtpConfiguration.getUserName() != null && smtpConfiguration.getPassword() != null))
				&& appliance.getPasswordResetAllowed() != null && appliance.getPasswordResetAllowed().isBooleanValue();
		if (valid) {
			passwordResetIsEnable = true;
			if (recaptchaService.isEnabled()) {
				valid = recaptchaService.verifyRecaptchaResponse();
				if (!valid) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, facesMessages
							.evalResourceAsString("#{msg['person.passwordreset.catch.checkInputAndCaptcha']}"));
				}

			}
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msg['person.passwordreset.notActivate']}"));
		}
		return valid;

	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the recaptchaService
	 */
	public RecaptchaService getRecaptchaService() {
		return recaptchaService;
	}

	/**
	 * @param recaptchaService
	 *            the recaptchaService to set
	 */
	public void setRecaptchaService(RecaptchaService recaptchaService) {
		this.recaptchaService = recaptchaService;
	}

}
