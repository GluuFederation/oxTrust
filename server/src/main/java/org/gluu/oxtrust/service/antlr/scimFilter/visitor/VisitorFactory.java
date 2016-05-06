/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.visitor;

import org.gluu.oxtrust.service.antlr.scimFilter.MainScimFilterVisitor;

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
        }

        return visitor;
    }
}
