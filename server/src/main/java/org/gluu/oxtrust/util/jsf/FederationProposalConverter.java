package org.gluu.oxtrust.util.jsf;

import java.io.Serializable;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.gluu.oxtrust.ldap.service.FederationService;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Converter()
@Name("federationProposalConverter")
@BypassInterceptors
public class FederationProposalConverter implements javax.faces.convert.Converter, Serializable {

	private static final long serialVersionUID = 3376046924407678311L;

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String federationName) {
		List<GluuSAMLFederationProposal> federations = FederationService.instance().getAllFederations();
		for (GluuSAMLFederationProposal federation : federations) {
			if (federation.getDisplayName().equals(federationName)) {
				return federation;
			}
		}
		return null;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object federation) {
		if (federation == null) {
			return null;
		}

		return ((GluuSAMLFederationProposal) federation).getDisplayName();
	}

}
