package org.gluu.oxtrust.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.gluu.persist.PersistenceEntryManager;
import org.slf4j.Logger;

@FacesConverter("org.gluu.jsf2.converter.DateTimeConverter")
public class DateTimeConverter implements Converter {

	@Inject
	PersistenceEntryManager manager;

	@Inject
	private Logger log;

	@Override
	public Object getAsObject(FacesContext ctx, UIComponent uiComponent, String value) {
		try {
			Date date = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(value);
			return manager.encodeTime(date);
		} catch (ParseException e) {
			log.info("", e);
			return manager.encodeTime(new Date());
		}
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
		Date result = manager.decodeTime((String) value);
		return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(result);
	}
}