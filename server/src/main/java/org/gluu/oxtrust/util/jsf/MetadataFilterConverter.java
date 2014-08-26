package org.gluu.oxtrust.util.jsf;

import java.io.Serializable;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.gluu.oxtrust.ldap.service.FilterService;
import org.gluu.oxtrust.model.MetadataFilter;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Converter()
@Name("metadataFilterConverter")
@BypassInterceptors
public class MetadataFilterConverter implements javax.faces.convert.Converter, Serializable {

	private static final long serialVersionUID = 3376046924407678310L;

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String metadataFilterName) {
		List<MetadataFilter> filters = FilterService.instance().getAvailableMetadataFilters();
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
