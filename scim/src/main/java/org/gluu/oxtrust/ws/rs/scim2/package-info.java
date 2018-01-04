/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */

/**
 * Interfaces and annotations shared by both the server side code and the Java client
 * (<a href="https://github.com/GluuFederation/SCIM-Client">SCIM-Client</a>).
 * Client code uses the Resteasy Proxy Framework and thus we are sharing the same interface for client and server.
 */
/*
 * Interfaces found here are common "contracts" so if a service method is added or changes signature, both projects
 * (oxtrust-server and scim-client) will consistently "see" the same contract.
 * Important: do not use @Inject in these classes (the client is not a weld project)
 */
package org.gluu.oxtrust.ws.rs.scim2;