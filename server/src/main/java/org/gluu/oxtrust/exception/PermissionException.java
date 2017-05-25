package org.gluu.oxtrust.exception;

/**
 * Created by eugeniuparvan on 5/25/17.
 */
public class PermissionException extends Exception {

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }

}
