package org.gluu.oxtrust.model;

public class Device {
	private String addedOn;

    private String id;

    private String nickName;

    
    public String getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(String addedOn) {
        this.addedOn = addedOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
        updateHash();
    }

    public void updateHash() {

        if (nickName == null) {
            id = "";
        } else {
            String str = nickName.replaceFirst("hotp:", "").replaceFirst("totp:", "");
            int idx = str.indexOf(";");
            if (idx > 0) {
                str = str.substring(0, idx);
            }
            id = str;
        }
    }

}
