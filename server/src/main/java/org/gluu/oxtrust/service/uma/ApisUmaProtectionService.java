/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.service.uma;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.xdi.config.oxtrust.AppConfiguration;

/**
 * Provides service to protect APIs UMA Rest service endpoints
 * 
 * @author Dmitry Ognyannikov
 */
@ApplicationScoped
@Named("apisUmaProtectionService")
public class ApisUmaProtectionService extends BaseUmaProtectionService implements Serializable {
    

    @Inject
    private AppConfiguration appConfiguration;

    protected String getClientId() {
        return appConfiguration.getApisUmaClientId();
    }

    protected String getClientKeyStorePassword() {
        return appConfiguration.getApisUmaClientKeyStorePassword();
    }

    protected String getClientKeyStoreFile() {
        return appConfiguration.getApisUmaClientKeyStoreFile();
    }

    protected String getClientKeyId() {
        return appConfiguration.getApisUmaClientKeyId();
    }

    public String getUmaResourceId() {
        return appConfiguration.getApisUmaResourceId();
    }

    public String getUmaScope() {
        return appConfiguration.getApisUmaScope();
    }

    public boolean isEnabled() {
        return isEnabledUmaAuthentication();
    }
}
