/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.gluu.oxtrust.model.scim2.meta.MapAttribute;
import org.gluu.oxtrust.model.scim2.meta.SetAttribute;
import org.gluu.oxtrust.model.scim2.meta.SingularAttribute;

public class User_ extends Resource_ {
    
    private User_(){
    }

    public static SingularAttribute<User, String> userName = new SingularAttribute<User, String>("userName", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, Name> name = new SingularAttribute<User, Name>("name", User.class, Name.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> displayName = new SingularAttribute<User, String>("displayName", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> nickName = new SingularAttribute<User, String>("nickName", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> profileUrl = new SingularAttribute<User, String>("profileUrl", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> title = new SingularAttribute<User, String>("title", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> userType = new SingularAttribute<User, String>("userType", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> preferredLanguage = new SingularAttribute<User, String>("preferredLanguage", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> locale = new SingularAttribute<User, String>("locale", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> timezone = new SingularAttribute<User, String>("timezone", User.class, String.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, Boolean> active = new SingularAttribute<User, Boolean>("active", User.class, Boolean.class); // NOSONAR : not finished yet
    public static SingularAttribute<User, String> password = new SingularAttribute<User, String>("password", User.class, String.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> emails = new SetAttribute<User, MultiValuedAttribute>("emails", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> phoneNumbers = new SetAttribute<User, MultiValuedAttribute>("phoneNumbers", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> ims = new SetAttribute<User, MultiValuedAttribute>("ims", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> photos = new SetAttribute<User, MultiValuedAttribute>("photos", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, Address> addresses = new SetAttribute<User, Address>("addresses", User.class, Address.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> groups = new SetAttribute<User, MultiValuedAttribute>("groups", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> entitlements = new SetAttribute<User, MultiValuedAttribute>("entitlements", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> roles = new SetAttribute<User, MultiValuedAttribute>("roles", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static SetAttribute<User, MultiValuedAttribute> x509Certificates = new SetAttribute<User, MultiValuedAttribute>("x509Certificates", User.class, MultiValuedAttribute.class); // NOSONAR : not finished yet
    public static MapAttribute<User, String, Extension> extensions = new MapAttribute<User, String, Extension>("extensions", User.class, Extension.class); // NOSONAR : not finished yet
}