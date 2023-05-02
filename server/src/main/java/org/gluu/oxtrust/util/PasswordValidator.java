package org.gluu.oxtrust.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.model.GluuAttribute;
import org.gluu.model.attribute.AttributeValidation;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.service.cdi.util.CdiUtil;

@ApplicationScoped
@FacesValidator("gluuPasswordValidator")
public class PasswordValidator implements Validator<Object> {

	private String USER_PASSWORD = "userPassword";
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
		if (attributeService == null) {
			attributeService = CdiUtil.bean(AttributeService.class);
		}
		GluuAttribute attributeByName = attributeService.getAttributeByName(USER_PASSWORD);
		AttributeValidation validation = attributeByName.getAttributeValidation();
		if (validation != null && validation.getRegexp() != null && !validation.getRegexp().isEmpty()) {
			pattern = Pattern.compile(validation.getRegexp());
			hasValidation = true;
		}
		if (hasValidation) {
			matcher = pattern.matcher(value.toString());
		}
		if (facesMessages == null) {
			facesMessages = CdiUtil.bean(FacesMessages.class);
		}
		if (hasValidation && !matcher.matches()) {
			FacesMessage msg = new FacesMessage(
					facesMessages.evalResourceAsString("#{msgs['password.validation.invalid']}"));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);

		}
	}

}
