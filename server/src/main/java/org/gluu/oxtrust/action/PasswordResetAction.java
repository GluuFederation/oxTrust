/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.OrganizationService;
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

	/**
	 * 
	 */
	private String SECRET_QUESTION = "secretQuestion";
	private String SECRET_ANSWER = "secretAnswer";

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
	private AppConfiguration appConfiguration;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private PasswordResetRequest request;

	private boolean hasSecurityQuestion = false;

	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String password;
	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String confirm;
	private String code;
	private String guid;
	private String securityQuestion;
	private GluuCustomAttribute answer;
	private String securityAnswer;
	private String response;

	public String start() throws ParseException {
		if (StringHelper.isEmpty(guid)) {
			sendExpirationError();
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		setCode(guid);
		PasswordResetRequest passwordResetRequest;
		try {
			passwordResetRequest = passwordResetService.findPasswordResetRequest(getGuid());
		} catch (EntryPersistenceException ex) {
			log.error("Failed to find password reset request by '{}'", guid, ex);
			passwordResetRequest = null;
		}
		if (passwordResetRequest == null) {
			sendExpirationError();
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		PasswordResetRequest personPasswordResetRequest = passwordResetService
				.findActualPasswordResetRequest(passwordResetRequest.getPersonInum());
		if (personPasswordResetRequest == null) {
			sendExpirationError();
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		if (!StringHelper.equalsIgnoreCase(guid, personPasswordResetRequest.getOxGuid())) {
			sendExpirationError();
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.request = personPasswordResetRequest;
		Calendar requestCalendarExpiry = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		requestCalendarExpiry.setTime(request.getCreationDate());
		currentCalendar.add(Calendar.SECOND, -appConfiguration.getPasswordResetRequestExpirationTime());
		if (requestCalendarExpiry.after(currentCalendar)) {
			return checkSecurityQuetion();
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Your link is not valid or your user is not allowed to perform a password reset. If you want to initiate a reset password procedure please fill this form.");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	/**
	 * 
	 */
	private String checkSecurityQuetion() {
		GluuCustomPerson person = personService.getPersonByInum(request.getPersonInum());
		GluuCustomAttribute question = null;
		if (person != null) {
			question = person.getGluuCustomAttribute(SECRET_QUESTION);
			this.setAnswer(person.getGluuCustomAttribute(SECRET_ANSWER));
			if (question != null && question.getValue() != null && !question.getStringValue().isEmpty()) {
				this.securityQuestion = (String) question.getValue();
				this.hasSecurityQuestion = true;
				hasSecurityQuestion(true);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	protected void sendExpirationError() {
		facesMessages.add(FacesMessage.SEVERITY_ERROR,
				"The reset link is no longer valid.\n\n " + "Re-enter your e-mail to generate a new link.");
		conversationService.endConversation();
	}

	public void update() {
		String outcome = updateImpl();
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Password reset successful.");
			redirect();
			conversationService.endConversation();
		}
		
	}

	public String updateImpl() {
		boolean valid = true;
		if (captchaEnable()) {
			valid = recaptchaService.verifyRecaptchaResponse();
		}
		if (this.password != null && this.confirm != null) {
			if (!this.password.equalsIgnoreCase(this.confirm)) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Password mismatch.");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Incorrect data send.");
			return OxTrustConstants.RESULT_FAILURE;
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
			checkSecurityQuetion();
			Calendar requestCalendarExpiry = Calendar.getInstance();
			Calendar currentCalendar = Calendar.getInstance();
			if (request != null) {
				requestCalendarExpiry.setTime((request.getCreationDate()));
				requestCalendarExpiry.add(Calendar.HOUR, 2);
			}
			GluuCustomPerson person = personService.getPersonByInum(request.getPersonInum());
			if (securityAnswer == null) {
				securityAnswer = getResponse();
			}
			if (requestCalendarExpiry.after(currentCalendar)) {
				PasswordResetRequest removeRequest = new PasswordResetRequest();
				removeRequest.setBaseDn(request.getBaseDn());
				if (this.securityQuestion != null && this.answer != null) {
					Boolean securityQuestionAnswered = (this.securityAnswer != null)
							&& this.securityAnswer.equalsIgnoreCase(answer.getStringValue());
					if (securityQuestionAnswered) {
						person.setUserPassword(password);
						try {
							personService.updatePerson(person);
							ldapEntryManager.remove(removeRequest);
							return OxTrustConstants.RESULT_SUCCESS;
						} catch (DuplicateEmailException e) {
							facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
							log.error("", e);
						} catch (Exception e) {
							facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error while processing the request");
							log.error("", e);
						}
						return OxTrustConstants.RESULT_FAILURE;
					} else {
						facesMessages.add(FacesMessage.SEVERITY_ERROR,
								"The provided security answer is not correct. Please try again from the link!");
						return OxTrustConstants.RESULT_FAILURE;
					}
				} else {
					person.setUserPassword(password);
					try {
						personService.updatePerson(person);
						ldapEntryManager.remove(removeRequest);
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
					facesMessages.evalResourceAsString("#{msgs['person.passwordreset.catch.checkInputAndCaptcha']}"));
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public boolean captchaEnable() {
		return recaptchaService.isEnabled() && getAuthenticationRecaptchaEnabled();
	}

	public String cancel() {
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void redirect() {
		try {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect("/identity/passwordResetResult.htm");
		}catch (Exception e){
			log.warn("Error redirecting to password reset result page");
		}
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

	public GluuCustomAttribute getAnswer() {
		return answer;
	}

	public void setAnswer(GluuCustomAttribute answer) {
		this.answer = answer;
	}

	public boolean hasSecurityQuestion() {
		return hasSecurityQuestion;
	}

	public void hasSecurityQuestion(boolean hasSecurityQuestion) {
		this.hasSecurityQuestion = hasSecurityQuestion;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
