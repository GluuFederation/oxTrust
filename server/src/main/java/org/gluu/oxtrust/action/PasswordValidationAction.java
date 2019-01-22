/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.AssertTrue;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.site.ldap.persistence.exception.AuthenticationException;
import org.slf4j.Logger;
import org.xdi.model.AttributeValidation;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;

/**
 * Action class for password validation
 * 
 * @author Yuriy Movchan Date: 12/20/2012
 */
@RequestScoped
@Named
@Secure("#{permissionService.hasPermission('profile', 'access')}")
public class PasswordValidationAction implements Cloneable, Serializable {

	private static final long serialVersionUID = 1952428504080910113L;

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	private String oldPassword = "";
	private String password = "";
	private String confirm = "";

	private UIComponent graphValidator;

	@Inject
	private AttributeService attributeService;

	@AssertTrue(message = "Passwords are different or they don't match the requirements define by site administrator.")
	public boolean isPasswordsEquals() {
		AttributeValidation validation = attributeService.getAttributeByName("userPassword").getAttributeValidation();
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
					resultValidateOldPassword = personService.authenticate(person.getUid(), oldPassword);
				}
			} catch (AuthenticationException ex) {
				log.debug("Failed to verify old person password", ex);
			}

			if (!resultValidateOldPassword) {
				if (graphValidator == null) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Old password isn't valid!", "Old password isn't valid!"));

				} else {
					FacesContext.getCurrentInstance().addMessage(graphValidator.getClientId(), new FacesMessage(
							FacesMessage.SEVERITY_ERROR, "Old password isn't valid!", "Old password isn't valid!"));
				}
			}
		}

		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully changed!", "Successfully changed!"));
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}