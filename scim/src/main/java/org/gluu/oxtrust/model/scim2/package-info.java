/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */

/**
 * Provides necessary classes to model and compose the different types of resources defined by SCIM2 spec,
 * for instance, User, Group, ServiceProviderConfig, etc. See sections 4-7 of RFC 7643 for more info.
 * <p>The root of the resource hierarchy is the class {@link org.gluu.oxtrust.model.scim2.BaseScimResource BaseScimResource}.</p>
 * <p>This package also contains utility classes to model SCIM errors, data validations, and describe meta information
 * such as attribute characteristics.</p>
 */
/*
 * Note:</b> Do not use @Inject in these classes (the SCIM-Client is not a weld project)
 */
package org.gluu.oxtrust.model.scim2;
