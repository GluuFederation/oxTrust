package org.gluu.oxtrust.api.authorization.casprotocol;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ShibbolethCASProtocolConfigurationDTO {

    @Size(min = 1)
    private String inum;

    private boolean enabled = true;

    private boolean extended = false;

    private boolean enableToProxyPatterns;

    @Size(min = 1)
    private String authorizedToProxyPattern;

    @Size(min = 1)
    private String unauthorizedToProxyPattern;

    @NotNull
    @JsonDeserialize(using = SessionStorageTypeDeserializer.class)
    @JsonSerialize(using = SessionStorageTypeSerializer.class)
    private SessionStorageType sessionStorageType;

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isEnableToProxyPatterns() {
        return enableToProxyPatterns;
    }

    public void setEnableToProxyPatterns(boolean enableToProxyPatterns) {
        this.enableToProxyPatterns = enableToProxyPatterns;
    }

    public String getAuthorizedToProxyPattern() {
        return authorizedToProxyPattern;
    }

    public void setAuthorizedToProxyPattern(String authorizedToProxyPattern) {
        this.authorizedToProxyPattern = authorizedToProxyPattern;
    }

    public String getUnauthorizedToProxyPattern() {
        return unauthorizedToProxyPattern;
    }

    public void setUnauthorizedToProxyPattern(String unauthorizedToProxyPattern) {
        this.unauthorizedToProxyPattern = unauthorizedToProxyPattern;
    }

    public SessionStorageType getSessionStorageType() {
        return sessionStorageType;
    }

    public void setSessionStorageType(SessionStorageType sessionStorageType) {
        this.sessionStorageType = sessionStorageType;
    }
}
