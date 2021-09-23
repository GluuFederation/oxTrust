package org.gluu.oxtrust.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.config.oxtrust.ScimMode;
import org.gluu.oxtrust.auth.none.NoProtectionService;
import org.gluu.oxtrust.auth.oauth.DefaultOAuthProtectionService;
import org.gluu.oxtrust.auth.oauth.DefaultTestModeProtectionService;
import org.gluu.oxtrust.auth.uma.ScimUmaProtectionService;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.JsonConfigurationService;

import org.slf4j.Logger;

@ApplicationScoped
@BindingUrls({"/scim"})
public class ScimService implements GluuRestService {
    
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private JsonConfigurationService jsonConfigurationService;
    
    @Inject
    private ScimUmaProtectionService scimUmaProtectionService;
    
    @Inject
    private DefaultOAuthProtectionService oauthProtectionService;
    
    @Inject
    private DefaultTestModeProtectionService testModeProtectionService;
    
    @Inject
    private NoProtectionService noProtectionService;

    @Override
    public String getName() {
        return "SCIM";
    }
    
    @Override
    public boolean isEnabled() {
        boolean enabled = configurationService.getConfiguration().isScimEnabled();
        if (!enabled) {
            log.debug("SCIM API is disabled. Read the Gluu SCIM docs to learn more");    
        }
        return enabled;
    }
        
    @Override    
    public IProtectionService getProtectionService() {
        
        ScimMode mode = jsonConfigurationService.getOxTrustappConfiguration().getScimProperties()
                .getProtectionMode();
        log.debug("SCIM protection mode is: {}", mode);
        
        if (mode != null) {
            switch (mode) {
                case UMA: return scimUmaProtectionService;
                case OAUTH: return oauthProtectionService;
                case TEST: return testModeProtectionService;
                case BYPASS: return noProtectionService;
            }
        }
        return null;
        
    }
    
}
