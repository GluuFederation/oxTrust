/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.fido;

import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.Resource;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Val Pecaoco
 */
public class FidoDevice extends Resource {

    @LdapAttribute(name = "oxId")
    private String id;

    private String userId;

    @LdapAttribute(name = "creationDate")
    private String creationDate;

    @LdapAttribute(name = "oxApplication")
    private String application;

    @LdapAttribute(name = "oxCounter")
    private String counter;

    @LdapAttribute(name = "oxDeviceData")
    private String deviceData;

    @LdapAttribute(name = "oxDeviceHashCode")
    private String deviceHashCode;

    @LdapAttribute(name = "oxDeviceKeyHandle")
    private String deviceKeyHandle;

    @LdapAttribute(name = "oxDeviceRegistrationConf")
    private String deviceRegistrationConf;

    @LdapAttribute(name = "oxLastAccessTime")
    private String lastAccessTime;

    @LdapAttribute(name = "oxStatus")
    private String status;

    @LdapAttribute(name = "displayName")
    private String displayName;

    @LdapAttribute(name = "description")
    private String description;

    public FidoDevice() {
        Meta fidoDeviceMeta = new Meta();
        fidoDeviceMeta.setResourceType("FidoDevice");
        setMeta(fidoDeviceMeta);
        Set<String> fidoDeviceSchemas = new HashSet<String>();
        fidoDeviceSchemas.add(Constants.FIDO_DEVICES_CORE_SCHEMA_ID);
        setSchemas(fidoDeviceSchemas);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public String getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(String deviceData) {
        this.deviceData = deviceData;
    }

    public String getDeviceHashCode() {
        return deviceHashCode;
    }

    public void setDeviceHashCode(String deviceHashCode) {
        this.deviceHashCode = deviceHashCode;
    }

    public String getDeviceKeyHandle() {
        return deviceKeyHandle;
    }

    public void setDeviceKeyHandle(String deviceKeyHandle) {
        this.deviceKeyHandle = deviceKeyHandle;
    }

    public String getDeviceRegistrationConf() {
        return deviceRegistrationConf;
    }

    public void setDeviceRegistrationConf(String deviceRegistrationConf) {
        this.deviceRegistrationConf = deviceRegistrationConf;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(String lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
