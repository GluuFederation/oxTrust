/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

/**
 * Some needed Constants
 */
//TODO: remove unneeded constants
public interface Constants {

    String MEDIA_TYPE_SCIM_JSON = "application/scim+json";

    String SCIM1_CORE_SCHEMA_ID = "urn:scim:schemas:core:1.0";

    String USER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:User";
    String USER_CORE_SCHEMA_NAME = "User";
    String USER_CORE_SCHEMA_DESCRIPTION = "User Account";

    String USER_EXT_SCHEMA_ID = "urn:ietf:params:scim:schemas:extension:gluu:2.0:User";
    String USER_EXT_SCHEMA_NAME = "GluuUserCustomExtension";
    String USER_EXT_SCHEMA_DESCRIPTION = "Gluu User Custom Extension";

    String GROUP_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:Group";
    String GROUP_CORE_SCHEMA_NAME = "Group";
    String GROUP_CORE_SCHEMA_DESCRIPTION = "Group";

    String FIDO_DEVICES_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:FidoDevice";
    String FIDO_DEVICES_CORE_SCHEMA_NAME = "FidoDevice";
    String FIDO_DEVICES_CORE_SCHEMA_DESCRIPTION = "Fido Device";

    String BULK_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
    String BULK_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

    String SERVICE_PROVIDER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

    String RESOURCE_TYPE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

    String LIST_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    String SEARCH_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:SearchRequest";

    String ERROR_RESPONSE_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

    String UTF8_CHARSET_FRAGMENT="; charset=utf-8";

    /**
     * The HTTP query parameter used in a URI to select specific SCIM
     * attributes.
     */
    String QUERY_PARAM_ATTRIBUTES = "attributes";
    String QUERY_PARAM_EXCLUDED_ATTRS = "excludedAttributes";

    int DEFAULT_COUNT = 0;
    int MAX_COUNT = 200;

    int MAX_BULK_OPERATIONS = 30;
    int MAX_BULK_PAYLOAD_SIZE = 3072000;  // 3 MB

}