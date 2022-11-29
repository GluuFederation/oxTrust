package org.gluu.oxtrust.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.OxTrustApiMode;
import org.gluu.oxtrust.auth.oauth.DefaultOAuthProtectionService;
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
    
    @Inject
    private DefaultOAuthProtectionService oAuthProtectionService;

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
      OxTrustApiMode mode = appConfiguration.getOxTrustProtectionMode();
      log.info("oxTrust API protection mode is {}", mode);
      
      if (mode != null) {
          switch (mode) {
              case UMA: return apiUmaProtectionService;
              case OAUTH: return oAuthProtectionService;
              case TEST: return testModeProtectionService;
          }
      }
      
      return null;
  }
    
}
