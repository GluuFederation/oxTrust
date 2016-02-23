/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.gluu.oxtrust.model.scim2.meta.SetAttribute;
import org.gluu.oxtrust.model.scim2.meta.SingularAttribute;

public class Group_ extends Resource_ {
    
    private Group_(){
    }

    public static SingularAttribute<Group, String> displayName = new SingularAttribute<Group, String>("displayName", Group.class, String.class); // NOSONAR : not finished yet
    public static SetAttribute<Group, MemberRef> members = new SetAttribute<Group, MemberRef>("members", Group.class, MemberRef.class); // NOSONAR : not finished yet
}