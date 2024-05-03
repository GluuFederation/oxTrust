/*
 * oxTrust is available under the MIT License (2008). 
 * See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.model.GluuUserRole;
import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.attribute.AttributeValidation;
import org.gluu.model.custom.script.conf.CustomScriptConfiguration;
import org.gluu.oxtrust.exception.DuplicateEmailException;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.RecaptchaService;
import org.gluu.oxtrust.service.external.ExternalUserRegistrationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * @author Dejan Maric
 * @author Yuriy Movchan Date: 08.14.2015
 */
@ConversationScoped
@Named("registerPersonAction")
public class RegisterPersonAction implements Serializable {

	private String POST_REGISTRATION_REDIRECT_URI = "post_registration_redirect_uri";

	private String HOST_NAME = "hostName";

	private static final long serialVersionUID = 6002737004324917338L;

	private Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	@Inject
	private Logger log;

	@Inject
	private AttributeService attributeService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ExternalUserRegistrationService externalUserRegistrationService;

	private GluuCustomPerson person;

	@Inject
	private PersonService personService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of password should be between 2 and 30")
	private String password;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of password should be between 2 and 30")
	private String repeatPassword;

	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private RecaptchaService recaptchaService;

	private List<String> hiddenAttributes;

	private String inum;

	private Map<String, String[]> requestParameters = new HashMap<String, String[]>();

	private boolean captchaDisabled = false;

	private boolean confirmationOkay = false;

	private String postRegistrationInformation;

	private String postRegistrationRedirectUri;

	/**
	 * Initializes attributes for registering new person
	 *
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String initPerson() {
		String outcome = initPersonImpl();
		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"You cannot enter this page. Please contact site administration.");
			conversationService.endConversation();
		} else if (OxTrustConstants.RESULT_NO_PERMISSIONS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to execute registration script.Please contact site administration.");
			conversationService.endConversation();
		}
		return outcome;
	}

	public String initPersonImpl() {
		initRecaptcha();
		String result = sanityCheck();
		if (result.equals(OxTrustConstants.RESULT_SUCCESS)) {
			if (!externalUserRegistrationService.isEnabled()) {
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}

			this.person = (inum == null || inum.isEmpty()) ? new GluuCustomPerson()
					: personService.getPersonByInum(inum);

			boolean isPersonActiveOrDisabled = GluuStatus.ACTIVE.equals(person.getStatus())
					|| GluuStatus.INACTIVE.equals(person.getStatus());

			if (isPersonActiveOrDisabled) {
				result = OxTrustConstants.RESULT_NO_PERMISSIONS;
			} else {
				initAttributes();
				boolean initScriptResult = externalUserRegistrationService
						.executeExternalInitRegistrationMethods(this.person, requestParameters);
				result = initScriptResult ? OxTrustConstants.RESULT_SUCCESS : OxTrustConstants.RESULT_FAILURE;
			}
		}

		return result;
	}

	/**
	 * Checks if session is correct for person registration.
	 * 
	 * @return OxTrustConstants constant to be returned by action
	 */
	private String sanityCheck() {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		requestParameters.putAll(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap());
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initRecaptcha() {
		GluuOrganization organization = organizationService.getOrganization();
		RegistrationConfiguration config = organization.getOxRegistrationConfiguration();
		boolean registrationCustomized = config != null;

		this.captchaDisabled = !recaptchaService.isEnabled();
		if (!this.captchaDisabled) {
			this.captchaDisabled = registrationCustomized && config.isCaptchaDisabled();
		}
	}

	public String register() throws CloneNotSupportedException {
		try {
			GluuCustomPerson gluuCustomPerson = personService.getPersonByEmail(email);
			if (gluuCustomPerson != null && appConfiguration.getEnforceEmailUniqueness()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"Registration failed. Please try again, or contact the system administrator.");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} catch (Exception e) {
			log.error("===========", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
		String outcome = registerImpl();
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			setPostRegistrationInformation("You've successfully created your account, please go to you email to successfully register your account.");
		} else if (OxTrustConstants.RESULT_DISABLED.equals(outcome)) {
			setPostRegistrationInformation(
					"You successfully registered. Please contact site administration to enable your account.");
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Registration failed. Please try again, or contact the system administrator.");
		} else if (OxTrustConstants.RESULT_CAPTCHA_VALIDATION_FAILED.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Captcha validation failed. Please try again.");
		}
		redirectIfNeeded();
		return outcome;
	}

	private void redirectIfNeeded() {
		if (postRegistrationRedirectUri != null) {
			try {
				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect(postRegistrationRedirectUri);
			} catch (IOException e) {
			}
		}
	}

	private String getRegistrationRedirectUri() {
		try {
			CustomScriptConfiguration value = externalUserRegistrationService.getDefaultExternalCustomScript();
			SimpleCustomProperty uriEntry = value.getConfigurationAttributes().get(POST_REGISTRATION_REDIRECT_URI);
			if (uriEntry != null) {
				log.info("Redirect uri is :" + uriEntry.getValue2());
				return uriEntry.getValue2();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public String registerImpl() throws CloneNotSupportedException {
		boolean registrationFormValid = StringHelper.equals(password, repeatPassword);
		if (!captchaDisabled) {
			String gRecaptchaRresponse = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("g-recaptcha-response");
			boolean reCaptchaResponse = recaptchaService.verifyRecaptchaResponse(gRecaptchaRresponse);
			registrationFormValid &= reCaptchaResponse;
		}
		if (registrationFormValid) {
			GluuCustomPerson archivedPerson = (GluuCustomPerson) person.clone();
			try {
				String customObjectClass = attributeService.getCustomOrigin();
				this.person.setCustomObjectClasses(new String[] { customObjectClass });
				if (person.getInum() == null) {
					String inum = personService.generateInumForNewPerson();
					this.person.setInum(inum);
				}
				if (person.getDn() == null) {
					String dn = personService.getDnForPerson(this.person.getInum());
					this.person.setDn(dn);
				}
				List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
				if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
					List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
					changedAttributes.addAll(personAttributes);
					changedAttributes.add(
							new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getSurname()));
					this.person.setCustomAttributes(changedAttributes);
				} else {
					this.person.setCommonName(this.person.getCommonName());
				}
				this.person.setUserPassword(password);
				this.person.setCreationDate(new Date());
				this.person.setMail(email);
				this.postRegistrationInformation = "You have successfully registered with oxTrust. Login to begin your session.";
				boolean result = false;
				result = externalUserRegistrationService.executeExternalPreRegistrationMethods(this.person,
						requestParameters);
				postRegistrationRedirectUri = getRegistrationRedirectUri();
				if (!result) {
					this.person = archivedPerson;
					return OxTrustConstants.RESULT_FAILURE;
				}
				if ((this.inum != null) && !this.inum.isEmpty()) {
					personService.updatePerson(this.person);
					try {
						oxTrustAuditService.audit(
								this.person.getInum() + " **" + this.person.getDisplayName()
										+ "** REGISTRATION UPDATED",
								identity.getUser(), (HttpServletRequest) FacesContext.getCurrentInstance()
										.getExternalContext().getRequest());
					} catch (Exception e) {
					}
				} else {
					personService.addPerson(this.person);
					try {
						oxTrustAuditService.audit(
								this.person.getInum() + " **" + this.person.getDisplayName() + "** REGISTERED",
								identity.getUser(), (HttpServletRequest) FacesContext.getCurrentInstance()
										.getExternalContext().getRequest());
					} catch (Exception e) {
					}
				}
				requestParameters.put(HOST_NAME,
						new String[] { configurationService.getConfiguration().getHostname() });
				result = externalUserRegistrationService.executeExternalPostRegistrationMethods(this.person,
						requestParameters);
				if (!result) {
					this.person = archivedPerson;
					return OxTrustConstants.RESULT_FAILURE;
				}

				if (GluuStatus.INACTIVE.equals(person.getStatus())) {
					return OxTrustConstants.RESULT_DISABLED;
				}
			} catch (DuplicateEmailException ex) {
				log.error("Failed to add new person {}", this.person.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, ex.getMessage());
				this.person = archivedPerson;
				return OxTrustConstants.RESULT_FAILURE;
			}

			catch (Exception ex) {
				log.error("Failed to add new person {}", this.person.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new person");
				this.person = archivedPerson;
				return OxTrustConstants.RESULT_FAILURE;
			}
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_CAPTCHA_VALIDATION_FAILED;
	}

	public void confirm() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String code = request.getParameter("code");
		requestParameters.put("code", new String[] { code });
		try {
			confirmationOkay = externalUserRegistrationService.executeExternalConfirmRegistrationMethods(this.person,
					requestParameters);
		} catch (Exception ex) {
			log.error("Failed to confirm registration.", ex);
		}
	}

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "You didn't register.");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initAttributes() {
		List<GluuAttribute> allPersonAttributes = attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN);
		List<String> allAttributOrigins = attributeService.getAllAttributeOrigins(allPersonAttributes);
		GluuOrganization organization = organizationService.getOrganization();
		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		boolean isNewPerson = (customAttributes == null) || customAttributes.isEmpty();
		if (isNewPerson) {
			customAttributes = new ArrayList<GluuCustomAttribute>();
			this.person.setCustomAttributes(customAttributes);
		}

		String[] personOCs = appConfiguration.getPersonObjectClassTypes();
		String[] personOCDisplayNames = appConfiguration.getPersonObjectClassDisplayNames();
		customAttributeAction.initCustomAttributes(allPersonAttributes, customAttributes, allAttributOrigins, personOCs,
				personOCDisplayNames);
		List<GluuCustomAttribute> mandatoryAttributes = new ArrayList<GluuCustomAttribute>();
		RegistrationConfiguration config = organization.getOxRegistrationConfiguration();
		boolean registrationCustomized = config != null;
		boolean registrationAttributesCustomized = registrationCustomized && config.getAdditionalAttributes() != null
				&& !config.getAdditionalAttributes().isEmpty();
		if (registrationAttributesCustomized) {
			for (String attributeInum : config.getAdditionalAttributes()) {
				GluuAttribute attribute = attributeService.getAttributeByInum(attributeInum);
				GluuCustomAttribute customAttribute = new GluuCustomAttribute(attribute.getName(), "", false, false);
				mandatoryAttributes.add(customAttribute);
			}
		}
		for (GluuCustomAttribute attribute : personService.getMandatoryAtributes()) {
			if (!mandatoryAttributes.contains(attribute)) {
				mandatoryAttributes.add(attribute);
			}
		}
		mandatoryAttributes.addAll(personService.getMandatoryAtributes());
		if (isNewPerson) {
			customAttributeAction.addCustomAttributes(mandatoryAttributes);
		}
		hiddenAttributes = new ArrayList<String>();
		hiddenAttributes.add("inum");
		hiddenAttributes.add("userPassword");
		hiddenAttributes.add("gluuStatus");
		hiddenAttributes.add("oxExternalUid");
		hiddenAttributes.add("oxLastLogonTime");
	}

	/**
	 * Returns list of mandatory attributes
	 *
	 * @return list of person's mandatory attributes
	 * @throws Exception
	 */
	public List<GluuCustomAttribute> getMandatoryAttributes() {
		return personService.getMandatoryAtributes();
	}

	protected String getActionName() {
		return "registerPersonAction";
	}

	public List<GluuCustomAttribute> getCustomAttributes() {
		return this.person.getCustomAttributes();
	}

	protected String getEventQueue() {
		return "personQueue";
	}

	public GluuCustomPerson getPerson() {
		return person;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public List<String> getHiddenAttributes() {
		return hiddenAttributes;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

	public boolean isCaptchaDisabled() {
		return captchaDisabled;
	}

	public String getPostRegistrationInformation() {
		return postRegistrationInformation;
	}

	public void setPostRegistrationInformation(String postRegistrationInformation) {
		this.postRegistrationInformation = postRegistrationInformation;
	}

	public void validateEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String email = (String) value;
		if ((email == null) || (email.trim().equals(""))) {
			FacesMessage message = new FacesMessage("Please Enter Your Email Address.");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
		if (!(matcher.matches())) {
			FacesMessage message = new FacesMessage("Please Enter Valid Email Address.");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

	public boolean isConfirmationOkay() {
		return confirmationOkay;
	}

	public void setConfirmationOkay(boolean confirmationOkay) {
		this.confirmationOkay = confirmationOkay;
	}
	public void validateConfirmPassword(FacesContext context, UIComponent comp, Object value) {
		Pattern pattern = null;
		String attributeValue = (String) value;
		if (StringHelper.isEmpty(attributeValue)) {
			FacesMessage message = new FacesMessage("Value is required");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
		boolean validateOnlyPwd = false;
		AttributeValidation validation = attributeService.getAttributeByName("userPassword").getAttributeValidation();
		boolean canValidate = validation != null && validation.getRegexp() != null && !validation.getRegexp().isEmpty();
		if (comp.getClientId().endsWith("password")) {
			this.password = (String) value;
			validateOnlyPwd = true;
		} else if (comp.getClientId().endsWith("passwordValidation")) {
			this.repeatPassword = (String) value;
		}
		this.repeatPassword = this.repeatPassword == null ? "" : this.repeatPassword;
		if (canValidate) {
			pattern = Pattern.compile(validation.getRegexp());
		}

		if (canValidate && validateOnlyPwd
				&& (!pattern.matcher(this.password).matches())) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"),
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"));
			context.addMessage(comp.getClientId(context), message);
		}
		if (!validateOnlyPwd && !StringHelper.equals(password, repeatPassword) && this.repeatPassword != null) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage("Both passwords should be the same!");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
		if (canValidate && !validateOnlyPwd
				&& (!pattern.matcher(this.password).matches() || !pattern.matcher(this.repeatPassword).matches())) {
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"),
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"));
			context.addMessage(comp.getClientId(context), message);
		}
	}

}
