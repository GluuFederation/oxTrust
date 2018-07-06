/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.gluu.oxtrust.ws.rs.scim2.BulkWebService;
import org.gluu.oxtrust.ws.rs.scim2.GroupWebService;
import org.gluu.oxtrust.ws.rs.scim2.ResourceTypeWS;
import org.gluu.oxtrust.ws.rs.scim2.SchemaWebService;
import org.gluu.oxtrust.ws.rs.scim2.ServiceProviderConfigWS;
import org.gluu.oxtrust.ws.rs.scim2.UserWebService;

/**
 * Integration with Resteasy
 *
 * @author Yuriy Movchan
 * @version June 6, 2017
 */
@Provider
public class TestResteasyInitializer extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(UserWebService.class);
        classes.add(GroupWebService.class);
        classes.add(BulkWebService.class);
        classes.add(ResourceTypeWS.class);
        classes.add(SchemaWebService.class);
        classes.add(ServiceProviderConfigWS.class);

        return classes;
    }

}