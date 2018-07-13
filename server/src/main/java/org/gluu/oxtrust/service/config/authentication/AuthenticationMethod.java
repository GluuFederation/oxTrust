package org.gluu.oxtrust.service.config.authentication;

public class AuthenticationMethod {

    private String authenticationMode;
    private String oxTrustAuthenticationMode;
    private PassportAuthenticationMethod passportAuthenticationMethod;

    public AuthenticationMethod() {
        // default-Ctor
    }

    public AuthenticationMethod(String authenticationMode, String oxTrustAuthenticationMode, PassportAuthenticationMethod passportAuthenticationMethod) {
        this.authenticationMode = authenticationMode;
        this.oxTrustAuthenticationMode = oxTrustAuthenticationMode;
        this.passportAuthenticationMethod = passportAuthenticationMethod;
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

    public void setPassportAuthenticationMethod(PassportAuthenticationMethod passportAuthenticationMethod) {
        this.passportAuthenticationMethod = passportAuthenticationMethod;
    }

    public PassportAuthenticationMethod getPassportAuthenticationMethod() {
        return passportAuthenticationMethod;
    }

    public boolean hasOxTrustAuthenticationMode(String oxTrustAuthenticationMode) {
        return oxTrustAuthenticationMode.equals(this.oxTrustAuthenticationMode);
    }

    public boolean hasAuthenticationMode(String authenticationMode) {
        return authenticationMode.equals(this.authenticationMode);
    }

    public AuthenticationMethod mergeWith(AuthenticationMethod other) {
        return new AuthenticationMethod(other.authenticationMode, other.oxTrustAuthenticationMode,
                passportAuthenticationMethod.mergeWith(other.passportAuthenticationMethod));
    }
}