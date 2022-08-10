package org.gluu.oxtrust;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.action.HomeAction;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.util.StringHelper;

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
			return locale.getStringValue();
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

		try {
			personService.updatePerson(gluuCustomPerson);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			if ("locale".equalsIgnoreCase(attribute.getName())) {
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
