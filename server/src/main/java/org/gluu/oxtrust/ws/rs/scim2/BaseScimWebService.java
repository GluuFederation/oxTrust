package org.gluu.oxtrust.ws.rs.scim2;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.gluu.oxtrust.service.scim2.serialization.ListResponseJsonSerializer;
import org.gluu.oxtrust.service.scim2.serialization.ScimResourceSerializer;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;
import static org.gluu.oxtrust.model.scim2.Constants.SEARCH_REQUEST_SCHEMA_ID;

/**
 * Base methods for SCIM web services
 *
 * @author Yuriy Movchan Date: 08/23/2013
 * Updated by jgomer on 2017-09-14.
 */
public class BaseScimWebService {

    @Inject
    Logger log;

    @Inject
    AppConfiguration appConfiguration;

    @Inject
    ScimResourceSerializer resourceSerializer;

    @Inject
    ExtensionService extService;

    public static final String SEARCH_SUFFIX = ".search";

    String endpointUrl;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public boolean isAttributeRecognized(Class<? extends BaseScimResource> cls, String attribute){

        boolean valid;

        Extension ext=extService.extensionOfAttribute(cls, attribute);
        valid=ext!=null;

        if (!valid) {
            attribute = extService.stripDefaultSchema(cls, attribute);
            Field f= IntrospectUtil.findFieldFromPath(cls, attribute);
            valid= f!=null;
        }
        return valid;

    }

    protected Response prepareSearchRequest(List<String> schemas, String filter, String sortBy, String sortOrder, Integer startIndex,
                                         Integer count, String attrsList, String excludedAttrsList, SearchRequest request, String sortByDefault){

        Response response=null;

        if (schemas!=null && schemas.size()==1 && schemas.get(0).equals(SEARCH_REQUEST_SCHEMA_ID)) {
            count = count == null ? MAX_COUNT : count;
            if (count >= 0) {
                if (count <= MAX_COUNT) {
                    startIndex = (startIndex == null || startIndex < 1) ? 1 : startIndex;

                    if (StringUtils.isEmpty(sortOrder) || !sortOrder.equals(SortOrder.DESCENDING.getValue()))
                        sortOrder = SortOrder.ASCENDING.getValue();

                    request.setSchemas(schemas);
                    request.setAttributes(attrsList);
                    request.setExcludedAttributes(excludedAttrsList);
                    request.setFilter(filter);
                    request.setSortBy(StringUtils.isEmpty(sortBy) ? sortByDefault : sortBy);
                    request.setSortOrder(sortOrder);
                    request.setStartIndex(startIndex);
                    request.setCount(count);
                }
                else
                    response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.TOO_MANY, "Maximum number of results per page is " + MAX_COUNT);
            }
            else
                response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_VALUE, "count parameter must be non-negative");
        }
        else
            response = getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_SYNTAX, "Wrong schema(s) supplied in Search Request");

        return response;

    }

    public String getListResponseSerialized(VirtualListViewResponse vlv, List<BaseScimResource> resources, String attrsList, String excludedAttrsList) throws IOException{

        ListResponse listResponse = new ListResponse(vlv.getStartIndex(), vlv.getItemsPerPage(), vlv.getTotalResults());
        listResponse.setResources(resources);

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ListResponseModule", Version.unknownVersion());
        module.addSerializer(ListResponse.class, new ListResponseJsonSerializer(resourceSerializer, attrsList, excludedAttrsList));
        mapper.registerModule(module);

        return mapper.writeValueAsString(listResponse);

    }

    public Response getErrorResponse(Response.Status status, String detail) {
        return getErrorResponse(status.getStatusCode(), null, detail);
    }

    public Response getErrorResponse(Response.Status status, ErrorScimType scimType, String detail) {
        return getErrorResponse(status.getStatusCode(), scimType, detail);
    }

    public Response getErrorResponse(int statusCode, String detail) {
        return getErrorResponse(statusCode, null, detail);
    }

    public Response getErrorResponse(int statusCode, ErrorScimType scimType, String detail) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(String.valueOf(statusCode));
        errorResponse.setScimType(scimType);
        errorResponse.setDetail(detail);

        return Response.status(statusCode).entity(errorResponse).build();
    }

}
