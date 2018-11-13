package org.gluu.oxtrust.api.organization;

public class SmtpConfigurationDTO {

    private String host;

    private int port;

    private boolean requiresSsl;

    private boolean trustHost;

    private String fromName;

    private String fromEmailAddress;

    private boolean requiresAuthentication;

    private String userName;

    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isRequiresSsl() {
        return requiresSsl;
    }

    public void setRequiresSsl(boolean requiresSsl) {
        this.requiresSsl = requiresSsl;
    }

    public boolean isTrustHost() {
        return trustHost;
    }

    public void setTrustHost(boolean trustHost) {
        this.trustHost = trustHost;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
