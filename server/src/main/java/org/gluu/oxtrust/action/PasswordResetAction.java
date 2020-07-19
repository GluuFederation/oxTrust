/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.exception.DuplicateEmailException;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.PasswordResetService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.RecaptchaService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * User: Dejan Maric
 */
@ConversationScoped
@Named("passwordResetAction")
public class PasswordResetAction implements Serializable {

	private static final long serialVersionUID = 6457422770824016614L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private RecaptchaService recaptchaService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private PersonService personService;

	@Inject
	private PasswordResetService passwordResetService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private PasswordResetRequest request;

	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String password;
	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String confirm;
	private String code;
	private String guid;
	private String securityQuestion;
	private String securityAnswer;

	public String start() throws ParseException {
		if (StringHelper.isEmpty(guid)) {
			sendExpirationError();
			return OxTrustConstants.RESULT_FAILURE;
		}
		setCode(guid);
		PasswordResetRequest passwordResetRequest;
		try {
			passwordResetRequest = passwordResetService.findPasswordResetRequest(getGuid());
		} catch (EntryPersistenceException ex) {
			log.error("Failed to find password reset request by '{}'", guid, ex);
			sendExpirationError();
			return OxTrustConstants.RESULT_FAILURE;
		}
		if (passwordResetRequest == null) {
			sendExpirationError();
			return OxTrustConstants.RESULT_FAILURE;
		}
		PasswordResetRequest personPasswordResetRequest = passwordResetService
				.findActualPasswordResetRequest(passwordResetRequest.getPersonInum());
		if (personPasswordResetRequest == null) {
			sendExpirationError();
			return OxTrustConstants.RESULT_FAILURE;
		}
		if (!StringHelper.equalsIgnoreCase(guid, personPasswordResetRequest.getOxGuid())) {
			sendExpirationError();
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.request = personPasswordResetRequest;
		Calendar requestCalendarExpiry = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		if (request != null) {
			requestCalendarExpiry.setTime(request.getCreationDate());
		}
		currentCalendar.add(Calendar.SECOND, -appConfiguration.getPasswordResetRequestExpirationTime());
		GluuCustomPerson person = personService.getPersonByInum(request.getPersonInum());
		GluuCustomAttribute question = null;
		if (person != null) {
			question = person.getGluuCustomAttribute("secretQuestion");
		}

		if ((request != null) && requestCalendarExpiry.after(currentCalendar)) {
			if (question != null) {
				securityQuestion = question.getValue();
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Your link is not valid or your user is not allowed to perform a password reset. If you want to initiate a reset password procedure please fill this form.");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	protected void sendExpirationError() {
		facesMessages.add(FacesMessage.SEVERITY_ERROR,
				"The reset link is no longer valid.\n\n " + "Re-enter your e-mail to generate a new link.");
		conversationService.endConversation();
	}

	public String update() {
		String outcome = updateImpl();
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Password reset successful.");
			conversationService.endConversation();
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Your secret answer or Captcha code may have been wrong. Please try to correct it or contact your administrator to change your password.");
			conversationService.endConversation();
		}
		return outcome;
	}

	public String updateImpl() {
		boolean valid = true;
		if (recaptchaService.isEnabled() && getAuthenticationRecaptchaEnabled()) {
			valid = recaptchaService.verifyRecaptchaResponse();
		}

		if (valid) {
			GluuOrganization organization = organizationService.getOrganization();
			try {
				this.request = ldapEntryManager.find(PasswordResetRequest.class,
						"oxGuid=" + getCode() + ",ou=resetPasswordRequests," + organization.getDn());
			} catch (Exception e) {
				log.error("=================", e);
				return OxTrustConstants.RESULT_FAILURE;
			}
			Calendar requestCalendarExpiry = Calendar.getInstance();
			Calendar currentCalendar = Calendar.getInstance();
			if (request != null) {
				requestCalendarExpiry.setTime((request.getCreationDate()));
				requestCalendarExpiry.add(Calendar.HOUR, 2);
			}
			GluuCustomPerson person = personService.getPersonByInum(request.getPersonInum());
			GluuCustomAttribute question = null;
			GluuCustomAttribute answer = null;
			if (person != null) {
				question = person.getGluuCustomAttribute("secretQuestion");
				answer = person.getGluuCustomAttribute("secretAnswer");
			}
			if (request != null && requestCalendarExpiry.after(currentCalendar)) {
				PasswordResetRequest removeRequest = new PasswordResetRequest();
				removeRequest.setBaseDn(request.getBaseDn());
				ldapEntryManager.remove(removeRequest);
				try {
					oxTrustAuditService.audit("PASSWORD RESET REQUEST" + removeRequest.getBaseDn() + " REMOVED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				} catch (Exception e) {
				}
				if (question != null && answer != null) {
					String correctAnswer = answer.getValue();
					Boolean securityQuestionAnswered = (this.securityAnswer != null)
							&& this.securityAnswer.equalsIgnoreCase(correctAnswer);
					if (securityQuestionAnswered) {
						person.setUserPassword(password);
						try {
							personService.updatePerson(person);
							return OxTrustConstants.RESULT_SUCCESS;
						} catch (DuplicateEmailException e) {
							facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
							log.error("", e);
						} catch (Exception e) {
							facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error while processing the request");
							log.error("", e);
						}
						return OxTrustConstants.RESULT_FAILURE;
					}
				} else {
					person.setUserPassword(password);
					try {
						personService.updatePerson(person);
						return OxTrustConstants.RESULT_SUCCESS;
					} catch (DuplicateEmailException e) {
						facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
					} catch (Exception e) {
						facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error while processing the request");
					}
					return OxTrustConstants.RESULT_FAILURE;
				}
			}
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msg['person.passwordreset.catch.checkInputAndCaptcha']}"));
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String cancel() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String checkAnswer() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@AssertTrue(message = "Different passwords entered!")
	public boolean isPasswordsEquals() {
		return password.equals(confirm);
	}

	public PasswordResetRequest getRequest() {
		return request;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}

	public String getSecurityAnswer() {
		return securityAnswer;
	}

	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public boolean getAuthenticationRecaptchaEnabled() {
		return jsonConfigurationService.getOxTrustappConfiguration().isAuthenticationRecaptchaEnabled();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
