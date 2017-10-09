/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.visitor;

import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.service.antlr.scimFilter.MainScimFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.GroupFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.UserFilterVisitor;

/**
 * @author Val Pecaoco
 * Updated by jgomer on 2017-09-12.
 */
public class VisitorFactory {

    public static MainScimFilterVisitor getVisitorInstance(Class clazz) {

        MainScimFilterVisitor visitor = null;

        if (clazz.getName().equals(UserResource.class.getName())) {
            visitor = new UserFilterVisitor();
        } else if (clazz.getName().equals(GroupResource.class.getName())) {
            visitor = new GroupFilterVisitor();
        }
        //TODO: add fido part
        /*
        else if (clazz.getName().equals(org.gluu.oxtrust.model.scim2.fido.FidoDevice.class.getName())) {
            visitor = new FidoDeviceFilterVisitor();
        }
*/
        return visitor;
    }
}
