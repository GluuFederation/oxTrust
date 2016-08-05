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

    String USER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:User";
    String USER_CORE_SCHEMA_NAME = "User";
    String USER_CORE_SCHEMA_DESCRIPTION = "User Account";

    String USER_EXT_SCHEMA_ID = "urn:ietf:params:scim:schemas:extension:gluu:2.0:User";
    String USER_EXT_SCHEMA_NAME = "GluuUserCustomExtension";
    String USER_EXT_SCHEMA_DESCRIPTION = "Gluu User Custom Extension";

    String GROUP_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:Group";
    String GROUP_CORE_SCHEMA_NAME = "Group";
    String GROUP_CORE_SCHEMA_DESCRIPTION = "Group";

    String BULK_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";
    String BULK_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

    String SERVICE_PROVIDER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

    String RESOURCE_TYPE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

    String LIST_RESPONSE_SCHEMA_ID = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    String SEARCH_REQUEST_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:SearchRequest";

    String ERROR_RESPONSE_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

    int DEFAULT_COUNT = 0;
    int MAX_COUNT = 200;

    int MAX_BULK_OPERATIONS = 30;
    int MAX_BULK_PAYLOAD_SIZE = 3072000;  // 3 MB
}