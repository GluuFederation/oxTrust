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

    String USER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:User";
    String USER_CORE_SCHEMA_NAME = "User";
    String USER_CORE_SCHEMA_DESCRIPTION = "User Account";

    String USER_EXT_SCHEMA_ID = "urn:ietf:params:scim:schemas:extension:gluu:2.0:User";
    String USER_EXT_SCHEMA_NAME = "GluuCustomExtension";
    String USER_EXT_SCHEMA_DESCRIPTION = "Gluu Custom Extension";

    String GROUP_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:Group";
    String SERVICE_PROVIDER_CORE_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

    String ERROR_RESPONSE_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

    int DEFAULT_COUNT = 10;
    int MAX_COUNT = 100;
}