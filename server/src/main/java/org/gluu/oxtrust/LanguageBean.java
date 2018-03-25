package org.gluu.oxtrust;

import org.gluu.oxtrust.action.HomeAction;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by eugeniuparvan on 3/6/17.
 */
@Named("language")
@SessionScoped
public class LanguageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Identity identity;

	@Inject
	private PersonService personService;

	@Inject
	private HomeAction homeAction;

	private static Map<String, Object> countries;

	static {
		countries = new LinkedHashMap<String, Object>();
		countries.put("English", Locale.ENGLISH); // label, value
		countries.put("Russian", new Locale("ru"));
		countries.put("French", new Locale("fr"));
	}

	public Map<String, Object> getCountriesInMap() {
		return countries;
	}

	public String getLocaleCode() {
		GluuCustomPerson gluuCustomPerson = (GluuCustomPerson) identity.getUser();

		if (gluuCustomPerson == null) {
			return null;
		}
		GluuCustomAttribute locale = getLocaleOrNull(gluuCustomPerson);
		if (locale == null) {
			return null;
		} else {
			return locale.getValue();
		}
	}

	public void setLocaleCode(String localeCode) {
		if (!identity.isLoggedIn()) {
			return;
		}

		GluuCustomPerson gluuCustomPerson = (GluuCustomPerson) identity.getUser();

		if (gluuCustomPerson == null) {
			return;
		}
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
		homeAction.init();
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
