package org.gluu.oxtrust.exception;

/**
 * Created by eugeniuparvan on 5/25/17.
 */
public class PermissionException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -797978261991962078L;

	public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }

}
