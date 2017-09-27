/**
 * Provides classes that model and compose the different resource types defined by SCIM 2 spec,
 * for instance, User, Group, ServiceProviderConfig, etc.
 * The root of hierarchy is the class {@link org.gluu.oxtrust.model.scim2.BaseScimResource}
 * This package also contains utility classes to model SCIM errors, data validations, attribute
 * characteristics, etc.
 * Do not use @Inject in these classes (the client is not a weld project)
 */

package org.gluu.oxtrust.model.scim2;
