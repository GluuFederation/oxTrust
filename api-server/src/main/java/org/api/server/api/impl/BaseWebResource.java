package org.api.server.api.impl;

import org.slf4j.Logger;

public class BaseWebResource {

	public BaseWebResource() {
	}

	public void log(Logger logger, Exception e) {
		logger.debug("", e);
	}

}
