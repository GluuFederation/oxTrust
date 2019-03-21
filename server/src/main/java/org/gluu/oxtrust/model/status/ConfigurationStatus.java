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

    private String freeDiskSpace;
    private String freeMemory;
    private String freeSwap;
    private String hostname;
    private String ipAddress;
    private String systemUptime;
    private String gluuHttpStatus;
    private String gluuBandwidthTX;
    private String gluuBandwidthRX;
    private String sslExpiry;
    private String loadAvg;

    public final String getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public final void setFreeDiskSpace(String freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }

    public final String getFreeMemory() {
        return freeMemory;
    }

    public final void setFreeMemory(String freeMemory) {
        this.freeMemory = freeMemory;
    }

    public final String getFreeSwap() {
        return freeSwap;
    }

    public final void setFreeSwap(String freeSwap) {
        this.freeSwap = freeSwap;
    }

    public final String getHostname() {
        return hostname;
    }

    public final void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public final String getIpAddress() {
        return ipAddress;
    }

    public final void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public final String getSystemUptime() {
        return systemUptime;
    }

    public final void setSystemUptime(String systemUptime) {
        this.systemUptime = systemUptime;
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

    public final String getLoadAvg() {
        return loadAvg;
    }

    public final void setLoadAvg(String loadAvg) {
        this.loadAvg = loadAvg;
    }

}
