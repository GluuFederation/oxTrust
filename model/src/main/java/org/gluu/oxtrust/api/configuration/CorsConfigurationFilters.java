package org.gluu.oxtrust.api.configuration;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import javax.validation.constraints.Size;

public class CorsConfigurationFilters {
    @Required
    @Size(min = 1)
    private String corsAllowedHeaders;

    private int corsPreflightMaxAge;

    @Required
    @Size(min = 1)
    private String corsAllowedOrigins;

    private boolean corsLoggingEnabled;

    @Required
    @Size(min = 1)
    private String filterName;

    private boolean corsSupportCredentials;

    private boolean corsRequestDecorate;

    @Required
    @Size(min = 1)
    private String corsExposedHeaders;

    @Required
    @Size(min = 1)
    private String corsAllowedMethods;

    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    public int getCorsPreflightMaxAge() {
        return corsPreflightMaxAge;
    }

    public void setCorsPreflightMaxAge(int corsPreflightMaxAge) {
        this.corsPreflightMaxAge = corsPreflightMaxAge;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public boolean isCorsLoggingEnabled() {
        return corsLoggingEnabled;
    }

    public void setCorsLoggingEnabled(boolean corsLoggingEnabled) {
        this.corsLoggingEnabled = corsLoggingEnabled;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isCorsSupportCredentials() {
        return corsSupportCredentials;
    }

    public void setCorsSupportCredentials(boolean corsSupportCredentials) {
        this.corsSupportCredentials = corsSupportCredentials;
    }

    public boolean isCorsRequestDecorate() {
        return corsRequestDecorate;
    }

    public void setCorsRequestDecorate(boolean corsRequestDecorate) {
        this.corsRequestDecorate = corsRequestDecorate;
    }

    public String getCorsExposedHeaders() {
        return corsExposedHeaders;
    }

    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }

    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }
}