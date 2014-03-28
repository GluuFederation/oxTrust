package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

import net.tanesha.recaptcha.ReCaptchaResponse;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuUserRole;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.oxtrust.util.MailUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.RecaptchaUtils;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
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
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter;

/**
 * User: Dejan Maric
 */
@Scope(ScopeType.CONVERSATION)
@Name("passwordResetAction")
public @Data class PasswordResetAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@In
	private LdapEntryManager ldapEntryManager;

	
	@Logger
	private Log log;
	
	private PasswordResetRequest request;
	private String guid;
	private String securityQuestion;
	private String securityAnswer;
	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String password;
	@Size(min = 3, max = 60, message = "Password length must be between {min} and {max} characters.")
	private String confirm;


	public String start() throws ParseException{
		GluuAppliance appliance = ApplianceService.instance().getAppliance();
		this.request = ldapEntryManager.find(PasswordResetRequest.class, "oxGuid=" + this.guid + ", ou=resetPasswordRequests," + appliance.getDn());
		Calendar requestCalendarExpiry = Calendar.getInstance();
		Calendar currentCalendar = Calendar.getInstance();
		if (request!= null ){
		    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		    requestCalendarExpiry.setTime(sdf.parse(request.getCreationDate()));
		    requestCalendarExpiry.add(Calendar.HOUR, 2);
		}
		GluuCustomPerson person = PersonService.instance().getPersonByInum(request.getPersonInum());
		GluuCustomAttribute question = null;
		GluuCustomAttribute answer = null;
		if(person != null ){
			question = person.getGluuCustomAttribute("secretQuestion");
			answer = person.getGluuCustomAttribute("secretAnswer");
		}
		if(request!= null && requestCalendarExpiry.after(currentCalendar) && question != null && answer != null){			   
		    securityQuestion = question.getValue();
		    return OxTrustConstants.RESULT_SUCCESS;
		    
		}else{
			return OxTrustConstants.RESULT_FAILURE;
		}
		
	}
	
	public String update() throws ParseException{
		ReCaptchaResponse reCaptchaResponse = RecaptchaUtils.getRecaptchaResponseFromServletContext();
		if (reCaptchaResponse.isValid()) {
			GluuAppliance appliance = ApplianceService.instance().getAppliance();
			this.request = ldapEntryManager.find(PasswordResetRequest.class, "oxGuid=" + this.guid + ", ou=resetPasswordRequests," + appliance.getDn());
			Calendar requestCalendarExpiry = Calendar.getInstance();
			Calendar currentCalendar = Calendar.getInstance();
			if (request!= null ){
			    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			    requestCalendarExpiry.setTime(sdf.parse(request.getCreationDate()));
			    requestCalendarExpiry.add(Calendar.HOUR, 2);
			}
			GluuCustomPerson person = PersonService.instance().getPersonByInum(request.getPersonInum());
			GluuCustomAttribute question = null;
			GluuCustomAttribute answer = null;
			if(person != null ){
				question = person.getGluuCustomAttribute("secretQuestion");
				answer = person.getGluuCustomAttribute("secretAnswer");
			}
			if(request!= null && requestCalendarExpiry.after(currentCalendar) && question != null && answer != null){	
			 
			    String correctAnswer = answer.getValue();
			    Boolean securityQuestionAnswered = (securityAnswer != null) && securityAnswer.equals(correctAnswer);
			    if(securityQuestionAnswered){
			    	person.setUserPassword(password);
			    	PersonService.instance().updatePerson(person);
			    	return OxTrustConstants.RESULT_SUCCESS;
			    }
			}
		}
		return OxTrustConstants.RESULT_FAILURE;
		
	}
	
	public String checkAnswer(){
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	@AssertTrue(message = "Different passwords entered!")
	public boolean isPasswordsEquals() {
		return password.equals(confirm);
	}
	
}
