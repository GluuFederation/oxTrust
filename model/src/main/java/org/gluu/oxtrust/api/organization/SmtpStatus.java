package org.gluu.oxtrust.api.organization;

public class SmtpStatus {

    private boolean isUp;

    public SmtpStatus(boolean isUp) {
        this.isUp = isUp;
    }

    public SmtpStatus() {
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }
}
