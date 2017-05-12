package org.gluu.oxtrust.service.uma;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.slf4j.Logger;
import org.xdi.oxauth.client.uma.wrapper.UmaClient;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.StringHelper;
import org.xdi.util.security.StringEncrypter.EncryptionException;

/**
 * Provide base methods to simplify work with UMA Rest services
 * 
 * @author Yuriy Movchan Date: 12/06/2016
 */
public abstract class BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -1147131971095468865L;

	@Inject
	private Logger log;

	@Inject
	private EncryptionService encryptionService;

	@Inject
	private UmaConfiguration umaMetadataConfiguration;

	private Token umaPat;
	private long umaPatAccessTokenExpiration = 0l; // When the "accessToken" will expire;

	private final ReentrantLock lock = new ReentrantLock();

	@Inject @Named("configurationFactory.cryptoConfigurationSalt")
	private String cryptoConfigurationSalt;

	public Token getPatToken() throws UmaProtectionException {
		if (isValidPatToken(this.umaPat, this.umaPatAccessTokenExpiration)) {
			return this.umaPat;
		}

		lock.lock();
		try {
			if (isValidPatToken(this.umaPat, this.umaPatAccessTokenExpiration)) {
				return this.umaPat;
			}

			retrievePatToken();
		} finally {
		  lock.unlock();
		}


		return this.umaPat;
	}

	protected boolean isEnabledUmaAuthentication() {
        return (umaMetadataConfiguration != null) && isExistPatToken();
	}

	public boolean isExistPatToken() {
		try {
			return getPatToken() != null;
		} catch (UmaProtectionException ex) {
			log.error("Failed to check UMA PAT token status", ex);
		}

		return false;
	}

	private void retrievePatToken() throws UmaProtectionException {
		this.umaPat = null;
		if (umaMetadataConfiguration == null) {
			return;
		}

		String umaClientKeyStoreFile = getClientKeyStoreFile();
		String umaClientKeyStorePassword = getClientKeyStorePassword();
		if (StringHelper.isEmpty(umaClientKeyStoreFile) || StringHelper.isEmpty(umaClientKeyStorePassword)) {
			throw new UmaProtectionException("UMA JKS keystore path or password is empty");
		}

		if (umaClientKeyStorePassword != null) {
			try {
				umaClientKeyStorePassword = encryptionService.decrypt(umaClientKeyStorePassword);
			} catch (EncryptionException ex) {
				log.error("Failed to decrypt UmaClientKeyStorePassword password", ex);
			}
		}
		

		try {
			this.umaPat = UmaClient.requestPat(umaMetadataConfiguration.getTokenEndpoint(), umaClientKeyStoreFile, umaClientKeyStorePassword, getClientId(), getClientKeyId());
			if (this.umaPat == null) {
				this.umaPatAccessTokenExpiration = 0l;
			} else {
				this.umaPatAccessTokenExpiration = computeAccessTokenExpirationTime(this.umaPat.getExpiresIn());
			}
		} catch (Exception ex) {
			throw new UmaProtectionException("Failed to obtain valid UMA PAT token", ex);
		}
		
		if ((this.umaPat == null) || (this.umaPat.getAccessToken() == null)) {
			throw new UmaProtectionException("Failed to obtain valid UMA PAT token");
		}
	}

	protected long computeAccessTokenExpirationTime(Integer expiresIn) {
		// Compute "accessToken" expiration timestamp
		Calendar calendar = Calendar.getInstance();
		if (expiresIn != null) {
			calendar.add(Calendar.SECOND, expiresIn);
			calendar.add(Calendar.SECOND, -10); // Subtract 10 seconds to avoid expirations during executing request
		}

		return calendar.getTimeInMillis();
	}

	private boolean isValidPatToken(Token validatePatToken, long validatePatTokenExpiration) {
		final long now = System.currentTimeMillis();

		// Get new access token only if is the previous one is missing or expired
        return !((validatePatToken == null) || (validatePatToken.getAccessToken() == null) ||
                (validatePatTokenExpiration <= now));
    }

	protected abstract String getClientId();
	protected abstract String getClientKeyStorePassword();
	protected abstract String getClientKeyStoreFile();

	protected abstract String getClientKeyId();

	public abstract String getUmaResourceId();
	public abstract String getUmaScope();

	public abstract boolean isEnabled();

}