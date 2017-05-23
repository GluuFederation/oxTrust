/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.ldap.service.RecaptchaService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OrganizationalUnit;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.util.MailUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.security.Secure;
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
	private LdapEntryManager ldapEntryManager;	

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

    /**
     * @return the MESSAGE_NOT_FOUND
     */
    public static String getMESSAGE_NOT_FOUND() {
        return MESSAGE_NOT_FOUND;
    }

    /**
     * @param aMESSAGE_NOT_FOUND the MESSAGE_NOT_FOUND to set
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
     * @param aMESSAGE_FOUND the MESSAGE_FOUND to set
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
											+ "If you are not %1$s identity server user, please ignore this email.\n\n"
											+ "Kind regards,\n"
											+ "Support Team";
	
	private static String MESSAGE_FOUND = "Hello %1$s\n\n" 
			+ "We received a request to reset your password. You may click the button below to choose your new password.\n"
			+ "If you did not make this request, you can safely ignore this message. \n\n"
			+ "<a href='%3$s'> <button>Reset Password</button></a>";
			

	public String requestReminder() throws Exception {
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
			if(matchedPersons != null && matchedPersons.size()>0){
				GluuAppliance appliance = applianceService.getAppliance();
				
				OrganizationalUnit requests = new OrganizationalUnit();
				requests.setOu("resetPasswordRequests");
				requests.setDn("ou=resetPasswordRequests," + appliance.getDn());
				if(! ldapEntryManager.contains(requests)){
					ldapEntryManager.persist(requests);
				}
				
				PasswordResetRequest request = new PasswordResetRequest();
				do{
					request.setCreationDate(Calendar.getInstance().getTime());
					request.setPersonInum(matchedPersons.get(0).getInum());
					request.setOxGuid(StringHelper.getRandomString(16));
					request.setBaseDn("oxGuid=" + request.getOxGuid()+ ", ou=resetPasswordRequests," + appliance.getDn());
				}while(ldapEntryManager.contains(request));

				String subj = String.format("Password reset was requested at %1$s identity server", organizationService.getOrganization().getDisplayName());
				MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
						appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), applianceService.getDecryptedSmtpPassword(appliance));
				
				mail.sendMail(appliance.getSmtpFromName() + " <" + appliance.getSmtpFromEmailAddress() + ">", email,
						subj, String.format(MESSAGE_FOUND, matchedPersons.get(0).getGivenName(),
								organizationService.getOrganization().getDisplayName(), 
								appConfiguration.getApplianceUrl() + httpServletRequest.getContextPath() + "/resetPassword/" + request.getOxGuid()));

				ldapEntryManager.persist(request);
			}else{
				GluuAppliance appliance = applianceService.getAppliance();
				String subj = String.format("Password reset was requested at %1$s identity server", organizationService.getOrganization().getDisplayName());
				MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
						appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), applianceService.getDecryptedSmtpPassword(appliance));
				String fromName = appliance.getSmtpFromName();
				if(fromName == null){
					fromName = String.format("%1$s identity server" , organizationService.getOrganization().getDisplayName());
				}
				mail.sendMail(fromName + " <" + appliance.getSmtpFromEmailAddress() + ">", email,
						subj, String.format(MESSAGE_NOT_FOUND, organizationService.getOrganization().getDisplayName()));
			}
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_FAILURE;
	}
	
	public boolean enabled(){
		GluuAppliance appliance = applianceService.getAppliance();
		boolean valid =	appliance.getSmtpHost() != null 
					&& appliance.getSmtpPort() != null 
					&&((! appliance.isRequiresAuthentication()) 
							|| (appliance.getSmtpUserName() != null 
								&& applianceService.getDecryptedSmtpPassword(appliance) != null))
					&& appliance.getPasswordResetAllowed()!=null 
					&& appliance.getPasswordResetAllowed().isBooleanValue();
		if(valid){
			if (recaptchaService.isEnabled()) {
				valid = recaptchaService.verifyRecaptchaResponse();
				if(!valid)
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Please check your input and CAPTCHA answer.");
			}			
		}else{
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Sorry the Password Reminder functionality is not enabled.Please contact to administrator.");
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
     * @return the ldapEntryManager
     */
    public LdapEntryManager getLdapEntryManager() {
        return ldapEntryManager;
    }

    /**
     * @param ldapEntryManager the ldapEntryManager to set
     */
    public void setLdapEntryManager(LdapEntryManager ldapEntryManager) {
        this.ldapEntryManager = ldapEntryManager;
    }

    /**
     * @return the recaptchaService
     */
    public RecaptchaService getRecaptchaService() {
        return recaptchaService;
    }

    /**
     * @param recaptchaService the recaptchaService to set
     */
    public void setRecaptchaService(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }
	
}
