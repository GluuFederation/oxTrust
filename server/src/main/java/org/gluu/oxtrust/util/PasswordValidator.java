package org.gluu.oxtrust.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.xdi.model.AttributeValidation;

@ApplicationScoped
@FacesValidator("gluuPasswordValidator")
public class PasswordValidator implements javax.faces.validator.Validator {

	private static final String USER_PASSWORD = "userPassword";
	private Pattern pattern;
	private Matcher matcher;
	private boolean hasValidation = false;
	@Inject
	private AttributeService attributeService;
	@Inject
	private FacesMessages facesMessages;

	public PasswordValidator() {

	}

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value) throws ValidatorException {
		AttributeValidation validation = attributeService.getAttributeByName(USER_PASSWORD).getAttributeValidation();
		if (validation != null) {
			String regexp = validation.getRegexp();
			if (regexp != null && !regexp.isEmpty()) {
				pattern = Pattern.compile(regexp);
				matcher = pattern.matcher(value.toString());
				hasValidation = true;
			}
		}

		if (hasValidation && !matcher.matches()) {
			FacesMessage msg = new FacesMessage(
					facesMessages.evalResourceAsString("#{msg['password.validation.invalid']}"),
					facesMessages.evalResourceAsString("#{msg['password.validation.invalid']}"));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);

		}

	}

}
