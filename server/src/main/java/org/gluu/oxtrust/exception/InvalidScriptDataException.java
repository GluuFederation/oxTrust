package org.gluu.oxtrust.exception;

public class InvalidScriptDataException extends RuntimeException {

    public InvalidScriptDataException() {
    }

    public InvalidScriptDataException(String message) {
        super(message);
    }

    public InvalidScriptDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidScriptDataException(Throwable cause) {
        super(cause);
    }

}
