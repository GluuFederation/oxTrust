/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.exception;

/**
 * This class represents the communication error
 * 
 * @author Yuriy Movchan 11/13/2014
 */
public class CommunicationException extends RuntimeException {

	private static final long serialVersionUID = -155309858406251977L;

	public CommunicationException(final String message) {
		super(message);
	}

	public CommunicationException(final Throwable cause) {
		super(cause);
	}

	public CommunicationException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
