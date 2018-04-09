package org.gluu.oxtrust.api.openidconnect;

import org.slf4j.Logger;

public class BaseWebResource {

	public BaseWebResource() {
	}

	public void log(Logger logger, Exception e) {
		logger.debug("", e);
	}

}
