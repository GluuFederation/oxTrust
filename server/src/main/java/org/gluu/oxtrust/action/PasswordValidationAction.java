/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.AssertTrue;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.FacesService;
import org.gluu.model.attribute.AttributeValidation;
import org.gluu.oxtrust.exception.DuplicateEmailException;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.IPersonService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Action class for password validation
 * 
 * @author Yuriy Movchan Date: 12/20/2012
 */
@RequestScoped
@Named("passwordValidationAction")
@Secure("#{permissionService.hasPermission('profile', 'access')}")
public class PasswordValidationAction implements Cloneable, Serializable {

	private String USER_PASSWORD = "userPassword";

	private static final long serialVersionUID = 1952428504080910113L;

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private FacesService facesService;

	private String oldPassword = "";

	private String password = "";

	private String confirm = "";

	private UIComponent graphValidator;

	private boolean checkOldPassword = false;

	private GluuCustomPerson person;
	@Inject
	private LogoutAction logoutAction;

	@AssertTrue(message = "Passwords are different or they don't match the requirements define by site administrator.")
	public boolean isPasswordsEquals() {
		AttributeValidation validation = attributeService.getAttributeByName(USER_PASSWORD).getAttributeValidation();
		if (validation != null && validation.getRegexp() != null && !validation.getRegexp().isEmpty()) {
			Pattern pattern = Pattern.compile(validation.getRegexp());
			return password.equals(confirm) && pattern.matcher(password).matches()
					&& pattern.matcher(confirm).matches();
		} else {
			return password.equals(confirm);
		}
	}

	public void reset() {
		this.password = this.confirm = null;
	}

	public void storeNewPassword(GluuCustomPerson person, boolean validateOldPassword) {
		if (validateOldPassword) {
			boolean resultValidateOldPassword = false;
			try {
				if ((person != null) && StringHelper.isNotEmpty(person.getUid())) {
					resultValidateOldPassword = personService.authenticate(person.getDn(), oldPassword);
				}
			} catch (Exception ex) {
				log.debug("Failed to verify old person password", ex);
			}

			if (!resultValidateOldPassword) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Old password isn't valid!");
			} else {
				person.setUserPassword(this.password);
				try {
					personService.updatePerson(person);
					oxTrustAuditService.audit(
							"USER " + person.getInum() + " **" + person.getDisplayName() + "** PASSWORD UPDATED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
					facesMessages.add(FacesMessage.SEVERITY_INFO, "Successfully changed!");
					facesService.redirectWithExternal("/logout.htm", new HashMap<String, Object>());
				} catch (DuplicateEmailException e) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
				} catch (Exception e) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, " Error changing password");
					log.error("",e);
				}

			}
		} else {
			if (this.password.equals(this.confirm)) {
				try {
					person.setUserPassword(this.password);
					personService.updatePerson(person);
					
					if(identity.getUser().getUid().equals(person.getUid())) {
							logoutAction.processLogout();
							facesMessages.add(FacesMessage.SEVERITY_INFO,
									"Profile '#{userProfileAction.person.displayName}' updated successfully");
					}
					
					oxTrustAuditService.audit(
							"USER " + person.getInum() + " **" + person.getDisplayName() + "** PASSWORD UPDATED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
					facesMessages.add(FacesMessage.SEVERITY_INFO, "Successfully changed!");
				} catch (DuplicateEmailException e) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
				} catch (Exception e) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, " Error changing password");
				}
			} else {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Password and confirm password value don't match");
			}
		}
	}

	public void notifyBindPasswordChange() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Bind password successfully changed!");
	}

	public void notifyClientPasswordChange() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Client secret successfully changed!");
	}

	public UIComponent getGraphValidator() {
		return graphValidator;
	}

	public void setGraphValidator(UIComponent graphValidator) {
		this.graphValidator = graphValidator;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getPassword() {
		return password;
	}

	public String getConfirm() {
		return confirm;
	}

	public boolean isCheckOldPassword() {
		return checkOldPassword;
	}

	public void setCheckOldPassword(boolean checkOldPassword) {
		this.checkOldPassword = checkOldPassword;
	}

	public void setPerson(GluuCustomPerson person) {
		this.person = person;
	}

	/**
	 * @return the person
	 */
	public GluuCustomPerson getPerson() {
		return person;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}