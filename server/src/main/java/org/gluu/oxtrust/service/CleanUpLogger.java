package org.gluu.oxtrust.service;

import java.io.Serializable;

import javax.inject.Inject;

import org.slf4j.Logger;

public class CleanUpLogger implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 717536440969366892L;

	@Inject
	private Logger logger;

	public void addNewLogLine(String message) {
		logger.info(message);
	}

	public void addNewLogLineAsError(String message) {
		logger.error(message);
	}

	public void addNewLogLineAsWarning(String message) {
		logger.warn(message);
	}

}
