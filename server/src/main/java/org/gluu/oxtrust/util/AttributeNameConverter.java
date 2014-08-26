package org.gluu.oxtrust.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.xdi.model.GluuAttribute;

@FacesConverter("AttributeNameConverter")
public class AttributeNameConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        if (value == null) {
            return null;
        }
        AttributeService attributeService = AttributeService.instance();
        GluuAttribute attribute = attributeService.getAttributeByName(value);
        return attribute;
    }
 
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        String string = null;
        if (value instanceof GluuAttribute) {
            string = ((GluuAttribute) value).getName();
        }
        return string;
    }

}