/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.io.Serializable;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * @author: Yuriy Movchan Date: 11.24.2010
 */
@Converter()
@Name("uptimeConverter")
@BypassInterceptors
public class UptimeConverter implements javax.faces.convert.Converter, Serializable {

	private static final long serialVersionUID = -4292643644104278424L;

	private static final String[] dateFormats = { "D 'days' HH 'hours' mm 'mins' ss 'seconds'" };

	public Object getAsObject(FacesContext context, UIComponent comp, String value) throws ConverterException {
		if ((value == null) || value.trim().length() == 0) {
			return null;
		}

		try {
			return DateUtils.parseDate(value, dateFormats);
		} catch (ParseException e) {
			throw new ConverterException("Unable to convert " + value + " to seconds!");
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object instanceof String) {
			try {
				return getSecondsAsString(Long.valueOf((String) object));
			} catch (NumberFormatException ex) {
				throw new ConverterException("Unable to convert " + object + " to date!");
			}
		}

		return null;
	}
	
	private String getSecondsAsString(long seconds) {
		int days = (int) TimeUnit.SECONDS.toDays(seconds);
		long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24);
		long mins = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
		long secondsInMinute = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

		return days + " days " + hours + " hours " + mins + " mins " + secondsInMinute + " seconds";
	}

}