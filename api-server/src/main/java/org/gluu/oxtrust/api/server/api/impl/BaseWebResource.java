package org.gluu.oxtrust.api.server.api.impl;

import org.slf4j.Logger;

public class BaseWebResource {
	protected static final String READ_ACCESS = "oxtrust-api-read";
	protected static final String WRITE_ACCESS = "oxtrust-api-write";

	public BaseWebResource() {
	}

	public void log(Logger logger, Exception e) {
		logger.debug("++++++++++API-ERROR", e);
	}

	public void log(Logger logger, String message) {
		logger.info(message);
	}

}
