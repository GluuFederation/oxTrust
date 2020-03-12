/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

@RequestScoped
@Named
@Secure("#{permissionService.hasPermission('client', 'access')}")
public class ClientPasswordAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	private String newPassword;
	private String newPasswordConfirmation;
	private String passwordMessage;

	@Inject
	private UpdateClientAction updateClientAction;

	@Inject
	private ClientService clientService;

	@Inject
	private EncryptionService encryptionService;

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

	public String update() {
		OxAuthClient client = clientService.getClientByDn(updateClientAction.getClient().getDn());
		try {
			client.setOxAuthClientSecret(newPassword);
			client.setEncodedClientSecret(encryptionService.encrypt(newPassword));
		} catch (EncryptionException e) {
			log.error("Failed to encrypt password", e);
		}
		clientService.updateClient(client);
		updateClientAction.getClient().setEncodedClientSecret(client.getEncodedClientSecret());
		updateClientAction.getClient().setOxAuthClientSecret(newPassword);
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
