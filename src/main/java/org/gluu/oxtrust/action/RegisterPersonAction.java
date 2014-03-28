package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

import net.tanesha.recaptcha.ReCaptchaResponse;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.RecaptchaUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.CONVERSATION)
@Name("registerPersonAction")
@Data
public class RegisterPersonAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	@In
	private AttributeService attributeService;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private CustomAttributeAction customAttributeAction;

	private GluuCustomPerson person;

	@In
	private PersonService personService;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of password should be between 2 and 30")
	private String password;

	@NotNull
	@Size(min = 2, max = 30, message = "Length of password should be between 2 and 30")
	private String repeatPassword;

	@In
	private FacesMessages facesMessages;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private String redirectUri;
	/**
	 * Initializes attributes for registering new person
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	public String initPerson() throws Exception {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.person = new GluuCustomPerson();
		initAttributes();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String register() throws Exception {
		ReCaptchaResponse reCaptchaResponse = RecaptchaUtils.getRecaptchaResponseFromServletContext();
		if (reCaptchaResponse.isValid() && password.equals(repeatPassword)) {
			String customObjectClass = attributeService.getCustomOrigin();
			this.person.setStatus(GluuStatus.ACTIVE);
			this.person.setCustomObjectClasses(new String[] { customObjectClass });

			String inum = personService.generateInumForNewPerson();
			String iname = personService.generateInameForNewPerson(this.person.getUid());
			String dn = personService.getDnForPerson(inum);

			// Save person
			this.person.setDn(dn);
			this.person.setInum(inum);
			this.person.setIname(iname);

			List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
			if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
				List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
				changedAttributes.addAll(personAttributes);
				changedAttributes.add(new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getDisplayName()));
				this.person.setCustomAttributes(changedAttributes);
			} else {
				this.person.setCommonName(this.person.getCommonName() + " " + this.person.getGivenName());
			}

			// save password
			this.person.setUserPassword(password);

			try {
				personService.addPerson(this.person);
				Events.instance().raiseEvent(OxTrustConstants.EVENT_PERSON_SAVED, this.person, null, null, null, null, true);
			} catch (Exception ex) {
				log.error("Failed to add new person {0}", ex, this.person.getInum());
				facesMessages.add(StatusMessage.Severity.ERROR, "Failed to add new person");
				return OxTrustConstants.RESULT_FAILURE;
			}

			if(redirectUri == null){
				redirectUri = applicationConfiguration.getApplianceUrl();
			}
			
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_CAPTCHA_VALIDATION_FAILED;
	}

	public void cancel() {
	}

	private void initAttributes() throws Exception {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		List<String> origins = attributeService.getAllAttributeOrigins(attributes);

		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		boolean newPerson = (customAttributes == null) || customAttributes.isEmpty();
		if (newPerson) {
			customAttributes = new ArrayList<GluuCustomAttribute>();
			this.person.setCustomAttributes(customAttributes);
		}

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins, applicationConfiguration
				.getPersonObjectClassTypes(), applicationConfiguration.getPersonObjectClassDisplayNames());

		if (newPerson) {
			customAttributeAction.addCustomAttributes(personService.getMandatoryAtributes());
		}
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
}
