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
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RecaptchaService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OrganizationalUnit;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.service.render.RenderService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
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
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

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
	private RenderService renderService;

	private boolean passwordResetIsEnable = false;

	/**
	 * @return the MESSAGE_NOT_FOUND
	 */
	public static String getMESSAGE_NOT_FOUND() {
		return MESSAGE_NOT_FOUND;
	}

	/**
	 * @param aMESSAGE_NOT_FOUND
	 *            the MESSAGE_NOT_FOUND to set
	 */
	public static void setMESSAGE_NOT_FOUND(String aMESSAGE_NOT_FOUND) {
		MESSAGE_NOT_FOUND = aMESSAGE_NOT_FOUND;
	}

	/**
	 * @return the MESSAGE_FOUND
	 */
	public static String getMESSAGE_FOUND() {
		return MESSAGE_FOUND;
	}

	/**
	 * @param aMESSAGE_FOUND
	 *            the MESSAGE_FOUND to set
	 */
	public static void setMESSAGE_FOUND(String aMESSAGE_FOUND) {
		MESSAGE_FOUND = aMESSAGE_FOUND;
	}

	@Email
	@NotEmpty
	@NotBlank
	private String email;

	private static String MESSAGE_NOT_FOUND = "You (or someone else) entered this email when trying to change the password of %1$s identity server account.\n\n"
			+ "However this email address is not on our database of registered users and therefore the attempted password change has failed.\n\n"
			+ "If you are a %1$s identity server user and were expecting this email, please try again using the email address you gave when registering your account.\n\n"
			+ "If you are not %1$s identity server user, please ignore this email.\n\n" + "Kind regards,\n"
			+ "Support Team";

	private static String MESSAGE_FOUND = "Hello %1$s\n\n"
			+ "We received a request to reset your password. You may click the button below to choose your new password.\n"
			+ "If you did not make this request, you can safely ignore this message. \n\n"
			+ "<a href='%3$s'> <button>Reset Password</button></a>";

	public String requestReminder() throws Exception {
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
				GluuAppliance appliance = applianceService.getAppliance();

				OrganizationalUnit requests = new OrganizationalUnit();
				requests.setOu("resetPasswordRequests");
				requests.setDn("ou=resetPasswordRequests," + appliance.getDn());
				if (!ldapEntryManager.contains(requests)) {
					ldapEntryManager.persist(requests);
				}

				PasswordResetRequest request = new PasswordResetRequest();
				do {
					request.setCreationDate(Calendar.getInstance().getTime());
					request.setPersonInum(matchedPersons.get(0).getInum());
					request.setOxGuid(StringHelper.getRandomString(16));
					request.setBaseDn(
							"oxGuid=" + request.getOxGuid() + ", ou=resetPasswordRequests," + appliance.getDn());
				} while (ldapEntryManager.contains(request));

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

				ldapEntryManager.persist(request);
			} else {
				GluuAppliance appliance = applianceService.getAppliance();
				SmtpConfiguration smtpConfiguration = appliance.getSmtpConfiguration();

				rendererParameters.setParameter("organizationName",
						organizationService.getOrganization().getDisplayName());

				String fromName = smtpConfiguration.getFromName();
				if (fromName == null) {
					fromName = String.format("%1$s identity server",
							organizationService.getOrganization().getDisplayName());
				}

				String subj = facesMessages.evalResourceAsString("#{msg['mail.reset.not_found.message.subject']}");
				String messagePlain = facesMessages
						.evalResourceAsString("#{msg['mail.reset.not_found.message.plain.body']}");
				String messageHtml = facesMessages
						.evalResourceAsString("#{msg['mail.reset.not_found.message.html.body']}");

				// rendererParameters.setParameter("mail_body", messageHtml);
				// String mailHtml =
				// renderService.renderView("/WEB-INF/mail/reset_password.xhtml");

				mailService.sendMail(null, fromName, email, null, subj, messagePlain, messageHtml);
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
