/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import javax.ws.rs.core.Response;

import com.unboundid.ldap.sdk.Filter;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.UmaAuthenticationService;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;
import org.xdi.util.Pair;

import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.DEFAULT_COUNT;
import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;
import static org.gluu.oxtrust.service.antlr.scimFilter.visitor.GroupFilterVisitor.getGroupLdapAttributeName;
import static org.gluu.oxtrust.service.antlr.scimFilter.visitor.UserFilterVisitor.getUserLdapAttributeName;

/**
 * Base methods for SCIM web services
 * 
 * @author Yuriy Movchan Date: 08/23/2013
 */
public class BaseScimWebService {

	@Logger
	private Log log;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	protected ApplicationConfiguration applicationConfiguration;

	@In
	protected JsonConfigurationService jsonConfigurationService;

	@In
	private ApplianceService applianceService;

	@In
	private UmaAuthenticationService umaAuthenticationService;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private ScimFilterParserService scimFilterParserService;

	protected Response processTestModeAuthorization(String token) throws Exception {

		try {

			String validateTokenEndpoint = jsonConfigurationService.getOxTrustApplicationConfiguration().getOxAuthTokenValidationUrl();

			ValidateTokenClient validateTokenClient = new ValidateTokenClient(validateTokenEndpoint);
			ValidateTokenResponse validateTokenResponse = validateTokenClient.execValidateToken(token);

			log.info(" (BaseScimWebService) validateToken token = " + token);
			log.info(" (BaseScimWebService) validateToken status = " + validateTokenResponse.getStatus());
			log.info(" (BaseScimWebService) validateToken entity = " + validateTokenResponse.getEntity());
			log.info(" (BaseScimWebService) validateToken isValid = " + validateTokenResponse.isValid());
			log.info(" (BaseScimWebService) validateToken expires = " + validateTokenResponse.getExpiresIn());

			if (!validateTokenResponse.isValid() ||
				(validateTokenResponse.getExpiresIn() == null || (validateTokenResponse.getExpiresIn() != null && validateTokenResponse.getExpiresIn() <= 0)) ||
				(validateTokenResponse.getStatus() != Response.Status.OK.getStatusCode())) {
				return getErrorResponse(Response.Status.FORBIDDEN, "User isn't authorized");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return getErrorResponse(Response.Status.FORBIDDEN, "User isn't authorized");
		}

		return null;
	}

	protected Response processAuthorization(String authorization) throws Exception {
		boolean authorized = getAuthorizedUser();
		if (!authorized) {
			if (!umaAuthenticationService.isEnabledUmaAuthentication()) {
				log.info("UMA authentication is disabled");
				return getErrorResponse(Response.Status.FORBIDDEN, "User isn't authorized");
			}
			Pair<Boolean, Response> rptTokenValidationResult = umaAuthenticationService.validateRptToken(authorization, applicationConfiguration.getUmaResourceId(), applicationConfiguration.getUmaScope());
			if (rptTokenValidationResult.getFirst()) {
				if (rptTokenValidationResult.getSecond() != null) {
					return rptTokenValidationResult.getSecond();
				}
			} else {
				return getErrorResponse(Response.Status.FORBIDDEN, "User isn't authorized");
			}
		}
		return null;
	}

	protected boolean getAuthorizedUser() {
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

	public <T> List<T> search(String dn, Class<T> entryClass, String filterString, int startIndex, int count, String sortBy, String sortOrder, VirtualListViewResponse vlvResponse, String attributesArray) throws Exception {

		log.info("----------");
		log.info(" ### RAW PARAMS ###");
		log.info(" filter string = " + filterString);
		log.info(" startIndex = " + startIndex);
		log.info(" count = " + count);
		log.info(" sortBy = " + sortBy);
		log.info(" sortOrder = " + sortOrder);
		log.info(" attributes = " + attributesArray);

		Filter filter = null;
		if (filterString == null || (filterString != null && filterString.isEmpty())) {
			filter = Filter.create("inum=*");
		} else {
			Class clazz = null;
			if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
				clazz = User.class;
			} else if (entryClass.getName().equals(GluuGroup.class.getName())) {
				clazz = Group.class;
			}
			filter = scimFilterParserService.createFilter(filterString, clazz);
		}

		startIndex = (startIndex < 1) ? 1 : startIndex;

		count = (count < 1) ? DEFAULT_COUNT : count;
		count = (count > MAX_COUNT) ? MAX_COUNT : count;

		sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "displayName";
		if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
			sortBy = getUserLdapAttributeName(sortBy);
		}  else if (entryClass.getName().equals(GluuGroup.class.getName())) {
			sortBy = getGroupLdapAttributeName(sortBy);
		}

		SortOrder sortOrderEnum = null;
		if (sortOrder != null && !sortOrder.isEmpty()) {
			sortOrderEnum = SortOrder.getByValue(sortOrder);
		} else if (sortBy != null && (sortOrder == null || (sortOrder != null && sortOrder.isEmpty()))) {
			sortOrderEnum = SortOrder.ASCENDING;
		} else {
			sortOrderEnum = SortOrder.ASCENDING;
		}

		// String[] attributes = (attributesArray != null && !attributesArray.isEmpty()) ? mapper.readValue(attributesArray, String[].class) : null;
		String[] attributes = (attributesArray != null && !attributesArray.isEmpty()) ? attributesArray.split("\\,") : null;
		if (attributes != null && attributes.length > 0) {

			// Add the attributes which are returned by default

			List<String> attributesList = new ArrayList<String>(Arrays.asList(attributes));
			Set<String> attributesSet = new LinkedHashSet<String>();

			for (String attribute : attributesList) {

				if (attribute != null && !attribute.isEmpty()) {

					attribute = FilterUtil.stripScimSchema(attribute);

					if (entryClass.getName().equals(GluuCustomPerson.class.getName()) && attribute.toLowerCase().startsWith("name.")) {

						if (!attributesSet.contains("name.familyName")) {
							attributesSet.add("name.familyName");
							attributesSet.add("name.middleName");
							attributesSet.add("name.givenName");
							attributesSet.add("name.honorificPrefix");
							attributesSet.add("name.honorificSuffix");
							attributesSet.add("name.formatted");
						}

					} else {
						attributesSet.add(attribute);
					}
				}
			}

			attributesSet.add("id");
			if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
				attributesSet.add("userName");
			}
			attributesSet.add("meta.created");
			attributesSet.add("meta.lastModified");
			attributesSet.add("meta.location");
			attributesSet.add("meta.version");

			attributes = attributesSet.toArray(new String[attributesSet.size()]);

			for (int i = 0; i < attributes.length; i++) {
				if (attributes[i] != null && !attributes[i].isEmpty()) {
					if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
						attributes[i] = getUserLdapAttributeName(attributes[i].trim());
					} else if (entryClass.getName().equals(GluuGroup.class.getName())) {
						attributes[i] = getGroupLdapAttributeName(attributes[i].trim());
					}
				}
			}
		}

		log.info(" ### CONVERTED PARAMS ###");
		log.info(" parsed filter = " + filter.toString());
		log.info(" startIndex = " + startIndex);
		log.info(" count = " + count);
		log.info(" sortBy = " + sortBy);
		log.info(" sortOrder = " + sortOrderEnum.getValue());
		log.info(" attributes = " + ((attributes != null && attributes.length > 0) ? new ObjectMapper().writeValueAsString(attributes) : null));

		List<T> result = ldapEntryManager.findEntriesVirtualListView(dn, entryClass, filter, startIndex, count, sortBy, sortOrderEnum, vlvResponse, attributes);

		log.info(" ### RESULTS INFO ###");
		log.info(" totalResults = " + vlvResponse.getTotalResults());
		log.info(" itemsPerPage = " + vlvResponse.getItemsPerPage());
		log.info(" startIndex = " + vlvResponse.getStartIndex());
		log.info("----------");

		return result;
	}

	/*
	protected Response getErrorResponse(String errMsg, int statusCode) {
		Errors errors = new Errors();
		Error error = new org.gluu.oxtrust.model.scim.Error(errMsg, statusCode, "");
		errors.getErrors().add(error);
		return Response.status(statusCode).entity(errors).build();
	}
	*/

	protected Response getErrorResponse(Response.Status status, String detail) {
		return getErrorResponse(status.getStatusCode(), null, detail);
	}

    protected Response getErrorResponse(Response.Status status, ErrorScimType scimType, String detail) {
        return getErrorResponse(status.getStatusCode(), scimType, detail);
    }

    protected Response getErrorResponse(int statusCode, String detail) {
        return getErrorResponse(statusCode, null, detail);
    }

	protected Response getErrorResponse(int statusCode, ErrorScimType scimType, String detail) {

		ErrorResponse errorResponse = new ErrorResponse();

		List<String> schemas = new ArrayList<String>();
		schemas.add(Constants.ERROR_RESPONSE_URI);
		errorResponse.setSchemas(schemas);

		errorResponse.setStatus(String.valueOf(statusCode));
		errorResponse.setScimType(scimType);
		errorResponse.setDetail(detail);

		return Response.status(statusCode).entity(errorResponse).build();
	}
}
