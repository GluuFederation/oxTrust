/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.gluu.oxtrust.model.MetadataFilter;
import org.gluu.oxtrust.service.FilterService;

@FacesConverter("metadataFilterConverter")
public class MetadataFilterConverter implements Converter {

	@Inject
	private FilterService filterService;

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String metadataFilterName) {
		List<MetadataFilter> filters = filterService.getAvailableMetadataFilters();
		for (MetadataFilter filter : filters) {
			if (filter.getName().equals(metadataFilterName)) {
				return filter;
			}
		}
		return null;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object metadataFilter) {
		return ((MetadataFilter) metadataFilter).getName();
	}

}
