package org.gluu.oxtrust.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.oxtrust.auth.uma.PassportUmaProtectionService;
import org.gluu.oxtrust.service.ConfigurationService;

@ApplicationScoped
@BindingUrls({"/passport/config"})
public class PassportConfigService implements GluuRestService {

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private PassportUmaProtectionService passportUmaProtectionService;
    
    @Override
    public String getName() {
        return "Passport configuration";
    }
    
    @Override
    public boolean isEnabled() {
        return configurationService.getConfiguration().isPassportEnabled();
    }
    
    @Override    
    public IProtectionService getProtectionService() {
        //there is only UMA protection for passport
        return passportUmaProtectionService;
    }
    
}
