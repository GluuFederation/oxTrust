/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.gluu.oxtrust.model.scim2.meta.SetAttribute;
import org.gluu.oxtrust.model.scim2.meta.SingularAttribute;

public class Resource_ {  

    protected Resource_(){
    }

    public static SingularAttribute<Resource, String> id = new SingularAttribute<Resource, String>("id", Resource.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<Resource, String> externalId = new SingularAttribute<Resource, String>("externalId", Resource.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<Resource, Meta> meta = new SingularAttribute<Resource, Meta>("meta", Resource.class, Meta.class); // NOSONAR : not finished yet
    public static SetAttribute<Resource, String> schemas = new SetAttribute<Resource, String>("schemas", Resource.class, String.class); // NOSONAR : not finished yet

}