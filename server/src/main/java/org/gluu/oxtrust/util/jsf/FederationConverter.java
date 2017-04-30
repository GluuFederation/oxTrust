/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util.jsf;

import java.io.Serializable;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import javax.inject.Named;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Converter()
@Named("federationConverter")
@BypassInterceptors
public class FederationConverter implements javax.faces.convert.Converter, Serializable {

	private static final long serialVersionUID = 3376046924407678310L;

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String federationName) {
		List<GluuSAMLTrustRelationship> federations = TrustService.instance().getAllFederations();
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
