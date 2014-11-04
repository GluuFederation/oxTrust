package org.gluu.oxtrust.util.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.RegexValidator;

@FacesValidator("extendedRegexValidator")
public class ExtendedRegexValidator extends RegexValidator{

	public ExtendedRegexValidator() {
		super();
		String pattern = ".*";
		
		setPattern(pattern);
	}
	
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        String pattern = (String) component.getAttributes().get("pattern");

        if (pattern != null) {
            setPattern(pattern);
            super.validate(context, component, value);
        }
    }

}