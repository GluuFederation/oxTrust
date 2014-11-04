/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;

@Name("clientPasswordAction")
@Scope(ScopeType.EVENT)
@Restrict("#{identity.loggedIn}")
public class ClientPasswordAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	private String newPassword;
	private String newPasswordConfirmation;
	private String passwordMessage;
	
	@In
	private UpdateClientAction updateClientAction;

	@In
	private ClientService clientService;

	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	@Logger
	private Log log;

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

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String update() {
		OxAuthClient client = clientService.getClientByDn(updateClientAction.getClient().getDn());
		try {
			client.setOxAuthClientSecret(newPassword);
		} catch (EncryptionException e) {
			log.error("Failed to encrypt password", e);
		}
		
		clientService.updateClient(client);
		
		// Update client password in action class
		updateClientAction.getClient().setEncodedClientSecret(client.getEncodedClientSecret());

		return OxTrustConstants.RESULT_SUCCESS;
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
