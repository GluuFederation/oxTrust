/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import org.gluu.oxtrust.config.OxTrustConfiguration;
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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.web.ServletContexts;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.StringHelper;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.CONVERSATION)
@Name("passwordReminderAction")
public class PasswordReminderAction implements Serializable {

	private static final long serialVersionUID = 1L;

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
	@In
	private LdapEntryManager ldapEntryManager;
	
	@In
	private RecaptchaService recaptchaService;
	
	private static String MESSAGE_NOT_FOUND = "You (or someone else) entered this email when trying to change the password of %1$s identity server account.\n\n" 
											+ "However this email address is not on our database of registered users and therefore the attempted password change has failed.\n\n"
											+ "If you are a %1$s identity server user and were expecting this email, please try again using the email address you gave when registering your account.\n\n"
											+ "If you are not %1$s identity server user, please ignore this email.\n\n"
											+ "Kind regards,\n"
											+ "%1$s Identity Server Support";
	
	private static String MESSAGE_FOUND = "Hello %1$s\n\n" 
			+ "We received a request to reset your password.You may click the button below to choose your new password.\n"
			+ "If you did not make this request, you can safely ignore this message. \n\n"
			+ "<a href='%3$s'> <button>Reset Password</button></a>";
			
	
	@Logger
	private Log log;


	public String requestReminder() throws Exception {
		boolean valid = true;
		if (recaptchaService.isEnabled()) {
			valid = recaptchaService.verifyRecaptchaResponse();
		}

		if (valid && enabled()) {
			GluuCustomPerson person = new GluuCustomPerson();
			person.setMail(email);
			ApplicationConfiguration applicationConfiguration = OxTrustConfiguration.instance().getApplicationConfiguration();
			List<GluuCustomPerson> matchedPersons = PersonService.instance().findPersons(person, 0);
			if(matchedPersons != null && matchedPersons.size()>0){
				GluuAppliance appliance = ApplianceService.instance().getAppliance();
				
				OrganizationalUnit requests = new OrganizationalUnit();
				requests.setOu("resetPasswordRequests");
				requests.setDn("ou=resetPasswordRequests," + appliance.getDn());
				if(! ldapEntryManager.contains(requests)){
					ldapEntryManager.persist(requests);
				}
				
				PasswordResetRequest request = new PasswordResetRequest();
				do{
					request.setCreationDate(Calendar.getInstance().getTime().toString());
					request.setPersonInum(matchedPersons.get(0).getInum());
					request.setOxGuid(StringHelper.getRandomString(16));
					request.setBaseDn("oxGuid=" + request.getOxGuid()+ ", ou=resetPasswordRequests," + appliance.getDn());
				}while(ldapEntryManager.contains(request));

				String subj = String.format("Password reset was requested at %1$s identity server", OrganizationService.instance().getOrganization().getDisplayName());
				MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
						appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), appliance.getSmtpPasswordStr());
				
				mail.sendMail(appliance.getSmtpFromName() + " <" + appliance.getSmtpFromEmailAddress() + ">", email,
						subj, String.format(MESSAGE_FOUND, matchedPersons.get(0).getGivenName(),
								OrganizationService.instance().getOrganization().getDisplayName(), 
								applicationConfiguration.getApplianceUrl() + ServletContexts.instance().getRequest().getContextPath() + "/resetPassword/" + request.getOxGuid()));

				ldapEntryManager.persist(request);
			}else{
				GluuAppliance appliance = ApplianceService.instance().getAppliance();
				String subj = String.format("Password reset was requested at %1$s identity server", OrganizationService.instance().getOrganization().getDisplayName());
				MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
						appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), appliance.getSmtpPasswordStr());
				String fromName = appliance.getSmtpFromName();
				if(fromName == null){
					fromName = String.format("%1$s identity server" , OrganizationService.instance().getOrganization().getDisplayName());
				}
				mail.sendMail(fromName + " <" + appliance.getSmtpFromEmailAddress() + ">", email,
						subj, String.format(MESSAGE_NOT_FOUND, OrganizationService.instance().getOrganization().getDisplayName()));
			}
			return OxTrustConstants.RESULT_SUCCESS;
		}
		return OxTrustConstants.RESULT_FAILURE;
	}
	
	public boolean enabled(){
		GluuAppliance appliance = ApplianceService.instance().getAppliance();
		return 	appliance.getSmtpHost() != null 
					&& appliance.getSmtpPort() != null 
					&&((! appliance.isRequiresAuthentication()) 
							|| (appliance.getSmtpUserName() != null 
								&& appliance.getSmtpPasswordStr() != null))
					&& appliance.getPasswordResetAllowed()!=null 
					&& appliance.getPasswordResetAllowed().isBooleanValue();

		
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

    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }
	
}
