/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs;

import java.net.URI;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.IdGenService;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.InumConf;
import org.gluu.oxtrust.model.InumResponse;
import org.gluu.oxtrust.model.scim.Error;
import org.gluu.oxtrust.model.scim.Errors;
import org.gluu.oxtrust.model.sql.InumSqlEntry;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.GluuBoolean;

/**
 * Inum Generation WebService Implementation
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
@Name("inumGenerationWebService")
public class InumGenerationWebServiceImpl implements InumGenerationWebService {
	@Logger
	Log log;

	@In
	OrganizationService organizationService;

	@In
	InumService inumService;

	@In
	private ApplianceService applianceService;

	@Override
	public Response getInum(HttpServletRequest request, String prefix) throws Exception {

		boolean authorized = getAuthorizedUser();

		if (!authorized) {
			return getErrorResponse("User isn't authorized", Response.Status.FORBIDDEN.getStatusCode());
		}

		try {
			organizationService = OrganizationService.instance();
			GluuOrganization org = organizationService.getOrganization();
			IdGenService idGenService = IdGenService.instance();

			InumConf conf = null;
			conf = (InumConf) jsonToObject(org.getOxInumConfig(), InumConf.class);

			String entityPrefix = null;

			if (prefix.equalsIgnoreCase("person")) {
				entityPrefix = conf.getPersonPrefix();
			} else if (prefix.equalsIgnoreCase("group")) {
				entityPrefix = conf.getGroupPrefix();
			} else if (prefix.equalsIgnoreCase("client")) {
				entityPrefix = conf.getClientPrefix();
			} else if (prefix.equalsIgnoreCase("scope")) {
				entityPrefix = conf.getScopePrefix();
			} else {
				entityPrefix = prefix;
			}

			String inum = idGenService.generateId(org.getInum(), entityPrefix);

			EntityManager inumEntryManager = (EntityManager) Component.getInstance("InumEntryManager");
			inumService.addInum(inumEntryManager, inum, prefix);

			InumSqlEntry foundInum = inumService.findInum(inumEntryManager, "@!1111!0000!D4E7");
			System.out.println("foundInum : " + foundInum.getInum());

			URI location = new URI("/InumGenerator/" + inum);

			InumResponse inumObject = new InumResponse();
			inumObject.setGeneratedInum(inum);

			return Response.ok(inumObject).location(location).build();
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			ex.printStackTrace();
			return getErrorResponse("Unexpected processing error, please check the input parameters",
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

	}

	private Object jsonToObject(String json, Class<?> clazz) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		Object clazzObject = mapper.readValue(json, clazz);
		return clazzObject;
	}

	private Response getErrorResponse(String errMsg, int statusCode) {
		Errors errors = new Errors();
		Error error = new org.gluu.oxtrust.model.scim.Error(errMsg, statusCode, "");
		errors.getErrors().add(error);
		return Response.status(statusCode).entity(errors).build();
	}

	private boolean getAuthorizedUser() {
		try {
			GluuCustomPerson authUser = (GluuCustomPerson) Contexts.getSessionContext().get(OxTrustConstants.CURRENT_PERSON);

			if (authUser == null) {
				return false;
			}

			GluuAppliance appliance = applianceService.getAppliance();
			if (appliance == null) {
				return false;
			}

			if (!(GluuBoolean.TRUE.equals(appliance.getScimEnabled()) || GluuBoolean.ENABLED.equals(appliance.getScimEnabled()))) {
				return false;
			}

			return true;
		} catch (Exception ex) {
			log.error("Exception: ", ex);
			return false;
		}
	}

}
