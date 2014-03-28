package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuOrganization;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Components Authorization Mode Action
 * 
 * @author Reda Zerrad Date: 05.18.2012
 */
@Scope(ScopeType.STATELESS)
@Name("compAuthMode")
public class CompAuthMode implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 6411825264007429787L;

	@Logger
	private Log log;

	public String confAuthMode() {
		try {
			log.info(" Request received ");
			OrganizationService orgService;
			orgService = OrganizationService.instance();

			GluuOrganization org = orgService.getOrganization();

			if (org.getScimAuthMode().equalsIgnoreCase("bearer")) {

				return "bearer";
			} else if (org.getScimAuthMode().equalsIgnoreCase("basic")) {

				return "basic";
			}
			return "Error";
		} catch (Exception ex) {
			log.error("Could not get ScimAuthMode : ", ex);

			return "Error";
		}
	}

}
