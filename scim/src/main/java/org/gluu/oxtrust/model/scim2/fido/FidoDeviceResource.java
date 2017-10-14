package org.gluu.oxtrust.model.scim2.fido;

import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.*;

/**
 * Created by jgomer on 2017-10-09.
 * Based on former org.gluu.oxtrust.model.scim2.fido.FidoDevice from Val Pecaoco
 *
 * Fido device resource.
 * Notes: Other classes may depend on this one via reflection. Do not add members whose names are already at parent
 * org.gluu.oxtrust.model.scim2.BaseScimResource
 */
@Schema(id = "urn:ietf:params:scim:schemas:core:2.0:FidoDevice", name = "FidoDevice", description = "Fido Device")
public class FidoDeviceResource extends BaseScimResource {

    @Attribute(description = "Username of device owner",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    private String userId;

    /*
    //This is commented: the aim is the same as Meta.created
    @Attribute(description = "Date of enrollment",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    @StoreReference(ref = "creationDate")
    private String creationDate;
       */
    @Attribute(description = "Application ID that enrolled the device",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    @StoreReference(ref = "oxApplication")
    private String application;

    @Attribute(description = "A counter aimed at being used by the FIDO endpoint",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE,
            type = AttributeDefinition.Type.INTEGER)
    @StoreReference(ref = "oxCounter")
    private String counter;

    @Attribute(description = "A Json representation of low-level attributes of this device",
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    @StoreReference(ref = "oxDeviceData")
    private String deviceData;

    @Attribute(description = "",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE,
            type = AttributeDefinition.Type.INTEGER)
    @StoreReference(ref = "oxDeviceHashCode")
    private String deviceHashCode;

    @Attribute(description = "",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    @StoreReference(ref = "oxDeviceKeyHandle")
    private String deviceKeyHandle;

    @Attribute(description = "",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.IMMUTABLE)
    @StoreReference(ref = "oxDeviceRegistrationConf")
    private String deviceRegistrationConf;

    @Attribute(description = "The most recent dateTime when this device was used for authentication",
            type = AttributeDefinition.Type.DATETIME)
    @StoreReference(ref = "oxLastAccessTime")
    private String lastAccessTime;

    @Attribute(description = "",
            isRequired = true,
            canonicalValues = {"active", "compromised"})
    @StoreReference(ref = "oxStatus")
    private String status;

    @Attribute(description = "")
    @StoreReference(ref = "displayName")
    private String displayName;

    @Attribute(description = "")
    @StoreReference(ref = "description")
    private String description;

    @Attribute(description = "")
    @StoreReference(ref = "oxNickName")
    private String nickname;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
/*
    //See comment above on creationDate member
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
*/
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
