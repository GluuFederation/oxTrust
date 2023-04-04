package org.gluu.oxtrust.auth.uma;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.gluu.oxauth.client.uma.wrapper.UmaClient;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxauth.model.uma.wrapper.Token;
import org.gluu.oxtrust.auth.IProtectionService;
import org.gluu.oxtrust.exception.UmaProtectionException;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.util.Pair;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;

import org.slf4j.Logger;

/**
 * Provide base methods to simplify work with UMA Rest services
 */
public abstract class BaseUmaProtectionService implements IProtectionService, Serializable {

	private static final long serialVersionUID = -1147131971095468865L;

	@Inject
	private Logger log;

	@Inject
	private EncryptionService encryptionService;

	@Inject
	private UmaMetadata umaMetadata;

	@Inject
	protected UmaPermissionService umaPermissionService;

	private Token umaPat;
	private long umaPatAccessTokenExpiration = 0l; // When the "accessToken" will expire;

	private final ReentrantLock lock = new ReentrantLock();

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
		return (umaMetadata != null) && isExistPatToken();
	}

	public boolean isExistPatToken() {
		try {
			return getPatToken() != null;
		} catch (UmaProtectionException ex) {
			log.error("Failed to check UMA PAT token status", ex);
		}

		return false;
	}

	public String getIssuer() {
		if (umaMetadata == null) {
			return "";
		}
		return umaMetadata.getIssuer();
	}

	private void retrievePatToken() throws UmaProtectionException {
		this.umaPat = null;
		if (umaMetadata == null) {
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
			this.umaPat = UmaClient.requestPat(umaMetadata.getTokenEndpoint(), umaClientKeyStoreFile,
					umaClientKeyStorePassword, getClientId(), getClientKeyId());
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
		return !((validatePatToken == null) || (validatePatToken.getAccessToken() == null)
				|| (validatePatTokenExpiration <= now));
	}

	Response processUmaAuthorization(String authorization, ResourceInfo resourceInfo) throws Exception {
		List<String> scopes = null;
//		scopes = getRequestedScopes(resourceInfo);
		Token patToken = null;
		try {
			patToken = getPatToken();
		} catch (UmaProtectionException ex) {
			return IProtectionService.simpleResponse(Response.Status.INTERNAL_SERVER_ERROR,
                                "Failed to obtain PAT token");
		}

		Pair<Boolean, Response> rptTokenValidationResult;
		if ((scopes != null) && !scopes.isEmpty()) {
			rptTokenValidationResult = umaPermissionService.validateRptToken(patToken, authorization,
					getUmaResourceId(), scopes);
		} else {
			rptTokenValidationResult = umaPermissionService.validateRptToken(patToken, authorization,
					getUmaResourceId(), Arrays.asList(getUmaScope()));
		}

		if (rptTokenValidationResult.getFirst()) {
			if (rptTokenValidationResult.getSecond() != null) {
				return rptTokenValidationResult.getSecond();
			}
		} else {
			return IProtectionService.simpleResponse(Response.Status.UNAUTHORIZED,
                                "Invalid GAT/RPT token");
		}
		return null;

	}

	public List<String> getRequestedScopes(ResourceInfo resourceInfo) {
		Class<?> resourceClass = resourceInfo.getResourceClass();
		ProtectedApi typeAnnotation = resourceClass.getAnnotation(ProtectedApi.class);
		List<String> scopes = new ArrayList<String>();
		if (typeAnnotation == null) {
			addMethodScopes(resourceInfo, scopes);
		} else {
			scopes.addAll(Stream.of(typeAnnotation.scopes()).collect(Collectors.toList()));
			addMethodScopes(resourceInfo, scopes);
		}
		return scopes;
	}

	private void addMethodScopes(ResourceInfo resourceInfo, List<String> scopes) {
		Method resourceMethod = resourceInfo.getResourceMethod();
		ProtectedApi methodAnnotation = resourceMethod.getAnnotation(ProtectedApi.class);
		if (methodAnnotation != null) {
			scopes.addAll(Stream.of(methodAnnotation.scopes()).collect(Collectors.toList()));
		}
	}

	protected abstract String getClientId();

	protected abstract String getClientKeyStorePassword();

	protected abstract String getClientKeyStoreFile();

	protected abstract String getClientKeyId();

	public abstract String getUmaResourceId();

	public abstract String[] getUmaScope();

    /**
     * This method checks whether the authorization header is present and valid
     * before service methods can be actually called.
     * @param headers An object holding HTTP headers
     * @param resourceInfo An object that allows access to the involved JAX-RS resource method or class
     * @return A null value if the authorization was successful, otherwise a Response object is returned signaling an
     * authorization error
     */
    @Override
    public Response processAuthorization(HttpHeaders headers, ResourceInfo resourceInfo) {

        Response authorizationResponse;
        String authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header {} found", StringUtils.isEmpty(authorization) ? "not" : "");

        try {
            if (isEnabledUmaAuthentication()) {
                authorizationResponse = processUmaAuthorization(authorization, resourceInfo);
            } else {
                authorizationResponse = IProtectionService.simpleResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Failed to setup UMA authentication");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            authorizationResponse = IProtectionService.simpleResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
        return authorizationResponse;

    }

}
