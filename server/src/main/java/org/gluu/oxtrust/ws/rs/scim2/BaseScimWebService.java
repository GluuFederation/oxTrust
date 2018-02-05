/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ws.rs.scim2;

import static org.gluu.oxtrust.model.scim2.Constants.DEFAULT_COUNT;
import static org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.GroupFilterVisitor.getGroupLdapAttributeName;
import static org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.UserFilterVisitor.getUserLdapAttributeName;
import static org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.fido.FidoDeviceFilterVisitor.getFidoDeviceLdapAttributeName;

import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ErrorResponse;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.oxtrust.service.uma.ScimUmaProtectionService;
import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.service.uma.UmaPermissionService;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.model.ListViewResponse;
import org.gluu.persist.model.SortOrder;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.oxauth.client.ClientInfoClient;
import org.xdi.oxauth.client.ClientInfoResponse;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.Pair;

import org.gluu.search.filter.Filter;

/**
 * Base methods for SCIM web services
 * 
 * @author Yuriy Movchan Date: 08/23/2013
 */
public class BaseScimWebService {

	@Inject
	private Logger log;

	@Inject
	private Identity identity;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private ScimUmaProtectionService scimUmaProtectionService;
	
	@Inject
	private UmaPermissionService umaPermissionService;

	@Inject
	private OpenIdService openIdService;

	@Inject
	private LdapEntryManager ldapEntryManager;

	@Inject
	private ScimFilterParserService scimFilterParserService;

    @Inject
    private AppInitializer appInitializer;

	public int getMaxCount(){
	    //return Constants.MAX_COUNT;
	    return appConfiguration.getScimProperties().getMaxCount();
	}

    protected Response processTestModeAuthorization(String token) throws Exception {

        Response response=null;

        try {
            token=token.replaceFirst("Bearer\\s+","");
            log.debug("Validating token {}", token);

            String clientInfoEndpoint=openIdService.getOpenIdConfiguration().getClientInfoEndpoint();
            ClientInfoClient clientInfoClient = new ClientInfoClient(clientInfoEndpoint);
            ClientInfoResponse clientInfoResponse = clientInfoClient.execClientInfo(token);

            if (clientInfoResponse.getErrorType()!=null) {
                response=getErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "Invalid token "+ token);
                log.debug("Error validating access token: {}", clientInfoResponse.getErrorDescription());
            }
        }
        catch (Exception e) {
            log.error("Failed to check test token", e);
            response=getErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "Invalid token");
        }
        return response;

    }

	protected Response processAuthorization(String authorization) throws Exception {
		if (!scimUmaProtectionService.isEnabled()) {
			log.info("UMA SCIM authentication is disabled");
			return getErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "SCIM was disabled");
		}

		Token patToken;
		try {
			patToken = scimUmaProtectionService.getPatToken();
		} catch (UmaProtectionException ex) {
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to obtain PAT token");
		}
		Pair<Boolean, Response> rptTokenValidationResult = umaPermissionService.validateRptToken(patToken, authorization, scimUmaProtectionService.getUmaResourceId(), scimUmaProtectionService.getUmaScope());
		if (rptTokenValidationResult.getFirst()) {
			if (rptTokenValidationResult.getSecond() != null) {
				return rptTokenValidationResult.getSecond();
			}
		} else {
			return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Invalid GAT/RPT token");
		}

		return null;
	}

	public <T> ListViewResponse<T> search(String dn, Class<T> entryClass, String filterString, int startIndex, int count, String sortBy, String sortOrder, String attributesArray) throws Exception {

		log.info("----------");
		log.info(" ### RAW PARAMS ###");
		log.info(" filter string = " + filterString);
		log.info(" startIndex = " + startIndex);
		log.info(" count = " + count);
		log.info(" sortBy = " + sortBy);
		log.info(" sortOrder = " + sortOrder);
		log.info(" attributes = " + attributesArray);

		Filter filter;
		if (filterString == null || (filterString != null && filterString.isEmpty())) {
			if (entryClass.getName().equals(GluuCustomFidoDevice.class.getName())) {
				filter = Filter.create("oxId=*");
			} else {
				filter = Filter.create("inum=*");
			}
		} else {
			Class clazz = null;
			if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
				clazz = User.class;
			} else if (entryClass.getName().equals(GluuGroup.class.getName())) {
				clazz = Group.class;
			} else if (entryClass.getName().equals(GluuCustomFidoDevice.class.getName())) {
				clazz = FidoDevice.class;
			}
			filter = scimFilterParserService.createFilter(filterString, clazz);
		}

		startIndex = (startIndex < 1) ? 1 : startIndex;

		count = (count < 1) ? DEFAULT_COUNT : count;
		count = (count >getMaxCount()) ? getMaxCount() : count;

		if (entryClass.getName().equals(GluuCustomFidoDevice.class.getName())) {
			sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
			sortBy = getFidoDeviceLdapAttributeName(sortBy);
		} else {
			sortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "displayName";
			if (entryClass.getName().equals(GluuCustomPerson.class.getName())) {
				sortBy = getUserLdapAttributeName(sortBy);
			}  else if (entryClass.getName().equals(GluuGroup.class.getName())) {
				sortBy = getGroupLdapAttributeName(sortBy);
			}
		}

		SortOrder sortOrderEnum;
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

					attribute = FilterUtil.stripScim2Schema(attribute);

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
			if (entryClass.getName().equals(GluuGroup.class.getName())) {
				attributesSet.add("displayName");
			}
			if (entryClass.getName().equals(GluuCustomFidoDevice.class.getName())) {
				attributesSet.add("creationDate");  // For meta.created
				attributesSet.add("displayName");
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
					} else if (entryClass.getName().equals(GluuCustomFidoDevice.class.getName())) {
						attributes[i] = getFidoDeviceLdapAttributeName(attributes[i].trim());
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

		// List<T> result = ldapEntryManager.findEntriesVirtualListView(dn, entryClass, filter, startIndex, count, sortBy, sortOrderEnum, vlvResponse, attributes);
		ListViewResponse<T> result = ldapEntryManager.findListViewResponse(dn, entryClass, filter, startIndex, count, getMaxCount(), sortBy, sortOrderEnum, attributes);

		log.info(" ### RESULTS INFO ###");
		log.info(" totalResults = " + result.getTotalResults());
		log.info(" itemsPerPage = " + result.getItemsPerPage());
		log.info(" startIndex = " + result.getStartIndex());
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
