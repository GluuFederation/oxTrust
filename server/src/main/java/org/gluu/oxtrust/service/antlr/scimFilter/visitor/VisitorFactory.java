/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.visitor;

import org.gluu.oxtrust.service.antlr.scimFilter.MainScimFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.GroupFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.UserFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.fido.FidoDeviceFilterVisitor;

/**
 * @author Val Pecaoco
 */
public class VisitorFactory {

    public static MainScimFilterVisitor getVisitorInstance(Class clazz) {

        MainScimFilterVisitor visitor = null;

        if (clazz.getName().equals(org.gluu.oxtrust.model.scim2.User.class.getName())) {
            visitor = new UserFilterVisitor();
        } else if (clazz.getName().equals(org.gluu.oxtrust.model.scim2.Group.class.getName())) {
            visitor = new GroupFilterVisitor();
        } else if (clazz.getName().equals(org.gluu.oxtrust.model.scim2.fido.FidoDevice.class.getName())) {
            visitor = new FidoDeviceFilterVisitor();
        }

        return visitor;
    }
}
