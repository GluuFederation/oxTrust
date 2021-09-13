package org.gluu.oxtrust.auth;

public interface GluuRestService {
    
    String getName();
    boolean isEnabled();
    IProtectionService getProtectionService();
    
}
