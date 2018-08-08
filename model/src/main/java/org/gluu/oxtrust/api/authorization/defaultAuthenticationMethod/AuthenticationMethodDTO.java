package org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod;

public class AuthenticationMethodDTO {
    private String authenticationMode;
    private String oxTrustAuthenticationMode;
    private PassportAuthenticationMethodDTO passportAuthenticationMethod;

    public AuthenticationMethodDTO() {
        // default-Ctor
    }

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getOxTrustAuthenticationMode() {
        return oxTrustAuthenticationMode;
    }

    public void setOxTrustAuthenticationMode(String oxTrustAuthenticationMode) {
        this.oxTrustAuthenticationMode = oxTrustAuthenticationMode;
    }

    public PassportAuthenticationMethodDTO getPassportAuthenticationMethod() {
        return passportAuthenticationMethod;
    }

    public void setPassportAuthenticationMethod(PassportAuthenticationMethodDTO passportAuthenticationMethod) {
        this.passportAuthenticationMethod = passportAuthenticationMethod;
    }
}