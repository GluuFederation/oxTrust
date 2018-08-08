package org.gluu.oxtrust.api.authorization.defaultAuthenticationMethod;

import java.util.Map;

public class PassportConfigurationDTO {

    private String strategy;
    private Map<String, String> properties;

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}