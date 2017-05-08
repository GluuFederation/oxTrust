/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.CentralLdapService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

@Named("appliancePasswordAction")
@RequestScoped
//TODO CDI @Restrict("#{identity.loggedIn}")
public class AppliancePasswordAction implements Serializable {

    private static final long serialVersionUID = 6486111971437252913L;

    private String newPassword;
    private String newPasswordConfirmation;
    private String passwordMessage;

    @Inject
    private ApplianceService applianceService;

    @Inject
    private CentralLdapService centralLdapService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject(value = "#{configurationFactory.cryptoConfigurationSalt}")
    private String cryptoConfigurationSalt;

    @Inject
    private Logger log;

    public String validatePassword() {
        String result;
        if (newPasswordConfirmation == null || !newPasswordConfirmation.equals(newPassword)) {
            this.passwordMessage = "Passwords Must be equal";
            result = OxTrustConstants.RESULT_VALIDATION_ERROR;
        } else {
            this.passwordMessage = "";
            result = OxTrustConstants.RESULT_SUCCESS;
        }

        return result;

    }

    //TODO CDI @Restrict("#{s:hasPermission('profile', 'access')}")
    public String update() {
        String result;

        GluuAppliance appliance = applianceService.getAppliance();
        try {
			appliance.setBlowfishPassword(StringEncrypter.defaultInstance().encrypt(newPassword, cryptoConfigurationSalt));
        } catch (EncryptionException e) {
            log.error("Failed to encrypt password", e);
        }
        appliance.setUserPassword(newPassword);

        if (centralLdapService.isUseCentralServer()) {
            GluuAppliance tmpAppliance = new GluuAppliance();
            tmpAppliance.setDn(appliance.getDn());
            boolean existAppliance = centralLdapService.containsAppliance(tmpAppliance);

            if (existAppliance) {
                centralLdapService.updateAppliance(appliance);
            } else {
                centralLdapService.addAppliance(appliance);
            }
        }

        applianceService.updateAppliance(appliance);
        result = OxTrustConstants.RESULT_SUCCESS;
        return result;
    }

    public void cancel() {
    }

    public void setNewPasswordConfirmation(String newPasswordConfirmation) {
        this.newPasswordConfirmation = newPasswordConfirmation;
    }

    public String getNewPasswordConfirmation() {
        return newPasswordConfirmation;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setPasswordMessage(String passwordMessage) {
        this.passwordMessage = passwordMessage;
    }

    public String getPasswordMessage() {
        return passwordMessage;
    }

}
