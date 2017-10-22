/**
 * All classes residing in this package are shared between the server side code and the Java client code "SCIM-Client"
 * https://github.com/GluuFederation/SCIM-Client) in terms of Resteasy.
 * They are a common "contract" so when a new service method is added or changes signature, there is no need to update
 * both projects. Just recompile...
 * Client code uses the Resteasy Proxy Framework and thus, we are sharing an interface between client and server
 * Important: do not use @Inject in these classes (the client is not a weld project)
 */

package org.gluu.oxtrust.ws.rs.scim2;