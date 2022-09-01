package org.gluu.oxtrust;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.action.HomeAction;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.model.LocaleSupported;

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
	
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private List<LocaleSupported> adminUiLocaleSupported;

	

	public List<org.gluu.model.LocaleSupported> getCountriesInMap() {
		adminUiLocaleSupported = jsonConfigurationService.getOxTrustappConfiguration().getAdminUiLocaleSupported();
		if(adminUiLocaleSupported == null) {
			adminUiLocaleSupported = new ArrayList<LocaleSupported>();
			adminUiLocaleSupported.add(new LocaleSupported("en","English"));			
		}
		
		return adminUiLocaleSupported;

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
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale( newLocaleValue));
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

	public List<org.gluu.model.LocaleSupported> getAdminUiLocaleSupported() {
		return adminUiLocaleSupported;
	}

	public void setAdminUiLocaleSupported(List<org.gluu.model.LocaleSupported> adminUiLocaleSupported) {
		this.adminUiLocaleSupported = adminUiLocaleSupported;
	}
}
