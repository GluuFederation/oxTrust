package org.gluu.oxtrust.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.auth.oauth.DefaultTestModeProtectionService;
import org.gluu.oxtrust.auth.uma.ApiUmaProtectionService;

import org.slf4j.Logger;

@ApplicationScoped
@BindingUrls({ "/api/v1" })
public class OxTrustApiService implements GluuRestService {
    
    @Inject
    private Logger log;
    
    @Inject
    private AppConfiguration appConfiguration;
    
    @Inject
    private ApiUmaProtectionService apiUmaProtectionService;
    
    @Inject
    private DefaultTestModeProtectionService testModeProtectionService; 

    @Override
    public String getName() {
        return "oxTrust API";
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
        
    @Override
    public IProtectionService getProtectionService() {        
        boolean testMode = appConfiguration.isOxTrustApiTestMode();
        log.info("oxTrust API protection mode is {}", testMode ? "TEST" : "UMA");
        
        return testMode ? testModeProtectionService : apiUmaProtectionService;
    }
    
}
