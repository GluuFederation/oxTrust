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

import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.service.TrustService;

@FacesConverter("federationConverter")
public class FederationConverter implements Converter {
	
	@Inject
	private TrustService trustService;

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String federationName) {
		List<GluuSAMLTrustRelationship> federations = trustService.getAllFederations();
		for (GluuSAMLTrustRelationship federation : federations) {
			if (federation.getDisplayName().equals(federationName)) {
				return federation;
			}
		}
		return null;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object federation) {
		if (federation == null) {
			return null;
		} else {
			return ((GluuSAMLTrustRelationship) federation).getDisplayName();
		}
	}

}
