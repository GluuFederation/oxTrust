package org.gluu.oxtrust;

import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by eugeniuparvan on 3/6/17.
 */
@Named("language")
@Scope(ScopeType.SESSION)
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private Identity identity;

    @Inject
    private PersonService personService;


    private static Map<String, Object> countries;

    static {
        countries = new LinkedHashMap<String, Object>();
        countries.put("English", Locale.ENGLISH); //label, value
        countries.put("French", Locale.FRENCH);
        countries.put("Russian", new Locale("ru"));
    }

    public Map<String, Object> getCountriesInMap() {
        return countries;
    }


    public String getLocaleCode() {
        GluuCustomPerson gluuCustomPerson = (GluuCustomPerson) Contexts.getSessionContext().get(OxTrustConstants.CURRENT_PERSON);

        if (gluuCustomPerson == null)
            return null;
        GluuCustomAttribute locale = getLocaleOrNull(gluuCustomPerson);
        if (locale == null) {
            return null;
        } else {
            return locale.getValue();
        }
    }


    public void setLocaleCode(String localeCode) {
        if (!identity.isLoggedIn())
            return;


        GluuCustomPerson gluuCustomPerson = (GluuCustomPerson) Contexts.getSessionContext().get(OxTrustConstants.CURRENT_PERSON);

        if (gluuCustomPerson == null)
            return;

        GluuCustomAttribute locale = getLocaleOrNull(gluuCustomPerson);
        if (locale == null) {
            addLocale(gluuCustomPerson, localeCode);
        } else {
            locale.setValue(localeCode);
        }

        personService.updatePerson(gluuCustomPerson);
    }

    public void countryLocaleCodeChanged(ValueChangeEvent e) {
        String newLocaleValue = e.getNewValue().toString();
        for (Map.Entry<String, Object> entry : countries.entrySet()) {
            if (entry.getValue().toString().equals(newLocaleValue)) {
                FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale) entry.getValue());
            }
        }
    }

    private GluuCustomAttribute getLocaleOrNull(GluuCustomPerson gluuCustomPerson) {
        for (GluuCustomAttribute attribute : gluuCustomPerson.getCustomAttributes()) {
            if ("locale".equals(attribute.getName())) {
                return attribute;
            }
        }
        return null;
    }

    private void addLocale(GluuCustomPerson gluuCustomPerson, String localeCode) {
        GluuCustomAttribute locale = new GluuCustomAttribute();
        locale.setName("locale");
        locale.setValue(localeCode);
        gluuCustomPerson.getCustomAttributes().add(locale);
    }
}