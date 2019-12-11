/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.status;

import java.io.Serializable;

/**
 * Configuration status
 * 
 * @author Yuriy Movchan Date: 04/20/2018
 */
public class ConfigurationStatus implements Serializable {

    private static final long serialVersionUID = -1499003894646725601L;

    private String hostname;
    private String gluuHttpStatus;
    private String gluuBandwidthTX;
    private String gluuBandwidthRX;
    private String sslExpiry;



    public final String getHostname() {
        return hostname;
    }

    public final void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public final String getGluuHttpStatus() {
        return gluuHttpStatus;
    }

    public final void setGluuHttpStatus(String gluuHttpStatus) {
        this.gluuHttpStatus = gluuHttpStatus;
    }

    public final String getGluuBandwidthTX() {
        return gluuBandwidthTX;
    }

    public final void setGluuBandwidthTX(String gluuBandwidthTX) {
        this.gluuBandwidthTX = gluuBandwidthTX;
    }

    public final String getGluuBandwidthRX() {
        return gluuBandwidthRX;
    }

    public final void setGluuBandwidthRX(String gluuBandwidthRX) {
        this.gluuBandwidthRX = gluuBandwidthRX;
    }

    public final String getSslExpiry() {
        return sslExpiry;
    }

    public final void setSslExpiry(String sslExpiry) {
        this.sslExpiry = sslExpiry;
    }

}
