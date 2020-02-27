package org.gluu.oxtrust.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.gluu.model.attribute.AttributeDataType;
import org.gluu.util.StringHelper;

@FacesValidator("gluuAttributeValidator")
public class GluuAttributeValidator implements Validator {
	@Override
	public void validate(FacesContext context, UIComponent comp, Object value) {
		String attributeValue;
		if(value instanceof AttributeDataType) {
			attributeValue=((AttributeDataType)value).getValue();
		}else {
			attributeValue = (String) value;
		}
		if (StringHelper.isEmpty(attributeValue)) {
			FacesMessage message = new FacesMessage("Value is required");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}

	}

}
