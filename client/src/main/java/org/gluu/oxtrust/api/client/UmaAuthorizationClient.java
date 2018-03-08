/*
 * SCIM-Client is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.client;

import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xdi.oxauth.client.TokenRequest;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaTokenService;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.crypto.OxAuthCryptoProvider;
import org.xdi.oxauth.model.token.ClientAssertionType;
import org.xdi.oxauth.model.uma.UmaMetadata;
import org.xdi.oxauth.model.uma.UmaTokenResponse;
import org.xdi.util.StringHelper;

/**
 * UMA token receiving.
 * 
 * @author Yuriy Movchan
 * @author Dmitry Ognyannikov
 */
public class UmaAuthorizationClient {

    private static final Logger logger = LogManager.getLogger(UmaAuthorizationClient.class);
    
    private String rpt;

    private String umaAatClientId;
    private String umaAatClientKeyId;
    private String umaAatClientJksPath;
    private String umaAatClientJksPassword;
    
    /**
     * Constructs a UmaAuthorizationClient object with the specified parameters and service contract.
     * @param domain The root URL of the SCIM service. Usually in the form {@code https://your.gluu-server.com/identity/restv1}
     * @param umaAatClientId Requesting party Client Id
     * @param umaAatClientJksPath Path to requesting party jks file in local filesystem
     * @param umaAatClientJksPassword Keystore password
     * @param umaAatClientKeyId Key Id in the keystore. Pass an empty string to use the first key in keystore
     */
    public UmaAuthorizationClient(String domain, String umaAatClientId, String umaAatClientJksPath, String umaAatClientJksPassword, String umaAatClientKeyId) {
        //TODO: domain
        
        this.umaAatClientId = umaAatClientId;
        this.umaAatClientJksPath = umaAatClientJksPath;
        this.umaAatClientJksPassword = umaAatClientJksPassword;
        this.umaAatClientKeyId = umaAatClientKeyId;
    }
    
    

    /**
     * Builds a string suitable for being passed as an authorization header. It does so by prefixing the current Requesting
     * Party Token this object has with the word "Bearer ".
     * @return String built or null if this instance has no RPT yet
     */
    //@Override
    String getAuthenticationHeader() {
    	return StringHelper.isEmpty(rpt) ?  null : "Bearer " + rpt;
    }

    /**
     * Recomputes a new RPT according to UMA workflow if the response passed as parameter has status code 401 (unauthorized).
     * @param response A Response object corresponding to the request obtained in the previous call to a service method
     * @return If the parameter passed has a status code different to 401, it returns false. Otherwise it returns the success
     * of the attempt made to get a new RPT
     */
    //@Override
    boolean authorize(Response response) throws OxTrustAuthorizationException {

        boolean value = false;

        if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {

            try {
                String permissionTicketResponse = response.getHeaderString("WWW-Authenticate");
                String permissionTicket = null;
                String asUri = null;

                String[] headerKeyValues = StringHelper.split(permissionTicketResponse, ",");
                for (String headerKeyValue : headerKeyValues) {
                    if (headerKeyValue.startsWith("ticket=")) {
                        permissionTicket = headerKeyValue.substring(7);
                    }
                    if (headerKeyValue.startsWith("as_uri=")) {
                        asUri = headerKeyValue.substring(7);
                    }
                }
                value= !StringHelper.isEmpty(asUri) && !StringHelper.isEmpty(permissionTicket) && obtainAuthorizedRpt(asUri, permissionTicket);
            }
            catch (Exception e) {
                throw new OxTrustAuthorizationException(e.getMessage(), e);
            }
        }

        return value;
    }

    private boolean obtainAuthorizedRpt(String asUri, String ticket) throws OxTrustAuthorizationException {

        try {
            return StringUtils.isNotBlank(getAuthorizedRpt(asUri, ticket));
        }
        catch (Exception e) {
            throw new OxTrustAuthorizationException(e.getMessage(), e);
        }

    }

    private String getAuthorizedRpt(String asUri, String ticket) throws OxTrustAuthorizationException {

        try {
        	// Get metadata configuration
        	UmaMetadata umaMetadata = UmaClientFactory.instance().createMetadataService(asUri).getMetadata();
            if (umaMetadata == null) {
                throw new OxTrustAuthorizationException(String.format("Failed to load valid UMA metadata configuration from: %s", asUri));
            }

        	TokenRequest tokenRequest = getAuthorizationTokenRequest(umaMetadata);
            //No need for claims token. See comments on issue https://github.com/GluuFederation/SCIM-Client/issues/22

            UmaTokenService tokenService = UmaClientFactory.instance().createTokenService(umaMetadata);
            UmaTokenResponse rptResponse = tokenService.requestJwtAuthorizationRpt(ClientAssertionType.JWT_BEARER.toString(), tokenRequest.getClientAssertion(), GrantType.OXAUTH_UMA_TICKET.getValue(), ticket, null, null, null, null, null); //ClaimTokenFormatType.ID_TOKEN.getValue()

            if (rptResponse == null) {
                throw new OxTrustAuthorizationException("UMA RPT token response is invalid");
            }

            if (StringUtils.isBlank(rptResponse.getAccessToken())) {
                throw new OxTrustAuthorizationException("UMA RPT is invalid");
            }
            
            this.rpt = rptResponse.getAccessToken();

            return rpt;
        }
        catch (Exception ex) {
            throw new OxTrustAuthorizationException(ex.getMessage(), ex);
        }

    }

    private TokenRequest getAuthorizationTokenRequest(UmaMetadata umaMetadata) throws OxTrustAuthorizationException {

        try {
            if (StringHelper.isEmpty(umaAatClientJksPath) || StringHelper.isEmpty(umaAatClientJksPassword)) {
                throw new OxTrustAuthorizationException("UMA JKS keystore path or password is empty");
            }
            OxAuthCryptoProvider cryptoProvider;
            try {
                cryptoProvider = new OxAuthCryptoProvider(umaAatClientJksPath, umaAatClientJksPassword, null);
            }
            catch (Exception ex) {
                throw new OxTrustAuthorizationException("Failed to initialize crypto provider");
            }

            String keyId = umaAatClientKeyId;
            if (StringHelper.isEmpty(keyId)) {
                // Get first key
                List<String> aliases = cryptoProvider.getKeyAliases();
                if (aliases.size() > 0) {
                    keyId = aliases.get(0);
                }
            }

            if (StringHelper.isEmpty(keyId)) {
                throw new OxTrustAuthorizationException("UMA keyId is empty");
            }

            TokenRequest tokenRequest = new TokenRequest(GrantType.CLIENT_CREDENTIALS);
            tokenRequest.setAuthenticationMethod(AuthenticationMethod.PRIVATE_KEY_JWT);
            tokenRequest.setAuthUsername(umaAatClientId);
            tokenRequest.setCryptoProvider(cryptoProvider);
            tokenRequest.setAlgorithm(cryptoProvider.getSignatureAlgorithm(keyId));
            tokenRequest.setKeyId(keyId);
            tokenRequest.setAudience(umaMetadata.getTokenEndpoint());

            return tokenRequest;
        }
        catch (Exception ex) {
            throw new OxTrustAuthorizationException("Failed to get client token", ex);
        }

    }
}
