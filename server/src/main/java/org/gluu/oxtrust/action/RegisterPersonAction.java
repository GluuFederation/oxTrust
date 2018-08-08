/*
 * oxTrust is available under the MIT License (2008). 
 * See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

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
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.RecaptchaService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.RegistrationConfiguration;
import org.gluu.oxtrust.service.external.ExternalUserRegistrationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;
import org.xdi.model.user.UserRole;
import org.xdi.util.StringHelper;

/**
 * @author Dejan Maric
 * @author Yuriy Movchan Date: 08.14.2015
 */
@ConversationScoped
@Named("registerPersonAction")
public class RegisterPersonAction implements Serializable {

	private static final long serialVersionUID = 6002737004324917338L;

	@Inject
	private Logger log;

	@Inject
	private AttributeService attributeService;

	@Inject
	private OrganizationService organizationService;

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
	private IPersonService personService;

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

    private String postRegistrationInformation;
    

	/**
	 * Initializes attributes for registering new person
	 *
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String initPerson() {
		String outcome = initPersonImpl();
		
		if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "You cannot enter this page. Please contact site administration.");
			conversationService.endConversation();
		} else if (OxTrustConstants.RESULT_NO_PERMISSIONS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to execute registration script. Please contact site administration.");
			conversationService.endConversation();
		}
		
		return outcome;
	}

	public String initPersonImpl() {
		initRecaptcha();

		String result = sanityCheck();
		if (result.equals(OxTrustConstants.RESULT_SUCCESS)) {

			if(!externalUserRegistrationService.isEnabled()){
				return OxTrustConstants.RESULT_NO_PERMISSIONS;
			}      
				
			this.person = (inum == null || inum.isEmpty()) ? new GluuCustomPerson() : personService.getPersonByInum(inum);

			boolean isPersonActiveOrDisabled = GluuStatus.ACTIVE.equals(person.getStatus()) || GluuStatus.INACTIVE.equals(person.getStatus());

			if (isPersonActiveOrDisabled) {
				result = OxTrustConstants.RESULT_NO_PERMISSIONS;
			} else {
				initAttributes();
				boolean initScriptResult = externalUserRegistrationService.executeExternalInitRegistrationMethods(this.person, requestParameters);
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
		String outcome = registerImpl();
		
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "You successfully registered.");
			conversationService.endConversation();
		} else if (OxTrustConstants.RESULT_DISABLED.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "You successfully registered. But your account is disabled.");
			conversationService.endConversation();
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			log.error("Failed to register new user. Please make sure you are not registering a duplicate account or try another username.");
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to register new user. Please make sure you are not registering a duplicate account or try another username.");
		} else if (OxTrustConstants.RESULT_CAPTCHA_VALIDATION_FAILED.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Captcha validation failed. Please try again.");
		}
		
		return outcome;
	}

	public String registerImpl() throws CloneNotSupportedException {
		boolean registrationFormValid = StringHelper.equals(password, repeatPassword);

		if (!captchaDisabled) {
			boolean reCaptchaResponse = recaptchaService.verifyRecaptchaResponse();
			registrationFormValid &= reCaptchaResponse;
		}

		if (registrationFormValid) {
			GluuCustomPerson archivedPerson = (GluuCustomPerson) person.clone();

			String customObjectClass = attributeService.getCustomOrigin();

			this.person.setCustomObjectClasses(new String[] { customObjectClass });

			// Save person
			if (person.getInum() == null) {
				String inum = personService.generateInumForNewPerson();
				this.person.setInum(inum);
			}

			if (person.getIname() == null) {
				String iname = personService.generateInameForNewPerson(this.person.getUid());
				this.person.setIname(iname);
			}

			if (person.getDn() == null) {
				String dn = personService.getDnForPerson(this.person.getInum());
				this.person.setDn(dn);
			}

			List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
			if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
				List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
				changedAttributes.addAll(personAttributes);
				changedAttributes.add(new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getSurname()));
				this.person.setCustomAttributes(changedAttributes);
			} else {
				this.person.setCommonName(this.person.getCommonName());
			}
			// save password
			this.person.setUserPassword(password);
			this.person.setCreationDate(new Date());
			this.person.setMail(email);
			
			try {
				// Set default message
				this.postRegistrationInformation = "You have successfully registered with oxTrust. Login to begin your session.";

				boolean result = false;
				result = externalUserRegistrationService.executeExternalPreRegistrationMethods(this.person, requestParameters);
				if (!result) {
					this.person = archivedPerson;
					return OxTrustConstants.RESULT_FAILURE;
				}
				if ((this.inum != null) && !this.inum.isEmpty()) {
					personService.updatePerson(this.person);
				} else {
					personService.addPerson(this.person);
				}
				
				result = externalUserRegistrationService.executeExternalPostRegistrationMethods(this.person, requestParameters);
				
				if (!result) {
					this.person = archivedPerson;
					return OxTrustConstants.RESULT_FAILURE;
				}
				
				if (GluuStatus.INACTIVE.equals(person.getStatus())) {
					return OxTrustConstants.RESULT_DISABLED;
				}
			} catch (Exception ex) {
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
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String code = request.getParameter("code");
		requestParameters.put("code", new String[]{code});
		try {
			boolean result = externalUserRegistrationService.executeExternalConfirmRegistrationMethods(this.person, requestParameters);
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
		List<GluuAttribute> allPersonAttributes = attributeService.getAllActivePersonAttributes(UserRole.ADMIN);

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
		customAttributeAction.initCustomAttributes(allPersonAttributes, customAttributes, allAttributOrigins, personOCs, personOCDisplayNames);

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
		hiddenAttributes.add("iname");
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

	/**
	 * Returns person's attributes
	 *
	 * @return list of person's attributes
	 */
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
    
    public void validateEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException {
    	  String email = (String) value;
    	  
    	  if ( (email == null) || (email.trim().equals(""))) {
    		  FacesMessage message = new FacesMessage(
    				  "Please Enter Your Email Address.");
    		  message.setSeverity(FacesMessage.SEVERITY_ERROR);
              throw new ValidatorException(message);
          }
    	  
    	  Pattern pattern=Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
  										+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    	  Matcher matcher = pattern.matcher(email);
    	  
    	  if(!(matcher.matches())){
    		  FacesMessage message = new FacesMessage(
    				  "Please Enter Valid Email Address.");
    		  message.setSeverity(FacesMessage.SEVERITY_ERROR);
              throw new ValidatorException(message);
    		  
    	  }
    	  
    	  GluuCustomPerson  gluuCustomPerson = personService.getPersonByEmail(email);
    	  if(gluuCustomPerson != null){
    		  FacesMessage message = new FacesMessage(
    				  "Email Address Already Registered.");
    		  message.setSeverity(FacesMessage.SEVERITY_ERROR);
              throw new ValidatorException(message);
    	  }
    	}

}
