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
 * Provides service to protect APIs Rest service endpoints with UMA scope.
 * 
 * @author Dmitry Ognyannikov
 */
@ApplicationScoped
@Named("apiUmaProtectionService")
public class ApiUmaProtectionService extends BaseUmaProtectionService implements Serializable {
    

    @Inject
    private AppConfiguration appConfiguration;

    @Override
    protected String getClientId() {
        return appConfiguration.getApiUmaClientId();
    }

    @Override
    protected String getClientKeyStorePassword() {
        return appConfiguration.getApiUmaClientKeyStorePassword();
    }

    @Override
    protected String getClientKeyStoreFile() {
        return appConfiguration.getApiUmaClientKeyStoreFile();
    }

    @Override
    protected String getClientKeyId() {
        return appConfiguration.getApiUmaClientKeyId();
    }

    @Override
    public String getUmaResourceId() {
        return appConfiguration.getApiUmaResourceId();
    }

    @Override
    public String getUmaScope() {
        return appConfiguration.getApiUmaScope();
    }

    @Override
    public boolean isEnabled() {
        return isEnabledUmaAuthentication();
    }
}
