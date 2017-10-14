/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.util;

import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.fido.FidoDeviceResource;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.xdi.service.cdi.util.CdiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Val Pecaoco
 * Updated by jgomer on 2017-10-06.
 */
public class FilterUtil {

    private static List<String> urns;

    static {
        urns = new ArrayList<String>();
        ExtensionService extService= CdiUtil.bean(ExtensionService.class);

        List<Class<? extends BaseScimResource>> list = Arrays.asList(UserResource.class, GroupResource.class, FidoDeviceResource.class);

        for (Class <? extends BaseScimResource> cls : list) {
            for (Extension ext : extService.getResourceExtensions(cls))
                urns.add(ext.getUrn());
            urns.add(extService.getDefaultSchema(cls));
        }

    }

    //TODO: there should be stripping depending on resource type - not general as in this case
    public static String stripScim2Schema(String uri) {

        for (String urn : urns)
            if (uri.startsWith(urn + ":")) {
                uri = uri.substring(urn.length()+1);
                break;
            }
        return uri;

    }

}
