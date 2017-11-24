/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

/**
 * Some needed Constants
 */
public interface Constants {

    String MEDIA_TYPE_SCIM_JSON = "application/scim+json";

    String USER_EXT_SCHEMA_ID = "urn:ietf:params:scim:schemas:extension:gluu:2.0:User";
    String USER_EXT_SCHEMA_NAME = "GluuUserCustomExtension";
    String USER_EXT_SCHEMA_DESCRIPTION = "Gluu User Custom Extension";

    String BULK_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
    String BULK_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

    String LIST_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    String SEARCH_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:SearchRequest";

    String PATCH_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    String ERROR_RESPONSE_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

    String UTF8_CHARSET_FRAGMENT="; charset=utf-8";

    /**
     * The HTTP query parameter used in a URI to provide a filter expression.
     */
    String QUERY_PARAM_FILTER = "filter";

    /**
     * The HTTP query parameter used in a URI to select specific SCIM
     * attributes.
     */
    String QUERY_PARAM_ATTRIBUTES = "attributes";
    String QUERY_PARAM_EXCLUDED_ATTRS = "excludedAttributes";

    /**
     * The HTTP query parameter used in a URI to sort by a SCIM attribute.
     */
    String QUERY_PARAM_SORT_BY = "sortBy";

    /**
     * The HTTP query parameter used in a URI to specify the sort order.
     */
    String QUERY_PARAM_SORT_ORDER = "sortOrder";

    /**
     * The HTTP query parameter used in a URI to specify the starting index for
     * page results.
     */
    String QUERY_PARAM_START_INDEX = "startIndex";

    /**
     * The HTTP query parameter used in a URI to specify the maximum size of a
     * page of results.
     */
    String QUERY_PARAM_COUNT = "count";

    int MAX_COUNT = 200;    //Do not remove. This is used in SCIM-client project

    int MAX_BULK_OPERATIONS = 30;
    int MAX_BULK_PAYLOAD_SIZE = 3072000;  // 3 MB

}