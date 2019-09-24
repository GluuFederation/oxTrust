package org.gluu.oxtrust.api.server.api.impl;

import org.slf4j.Logger;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;


@OpenAPIDefinition (
	info = @Info (
		title = "oxTrust API",
		version = "4.0.0",
		description = "This is an API for Gluu Server's oxTrust administrative interface. Go to https://gluu.org for more information",
		termsOfService = "https://www.gluu.org/terms/",
		contact = @Contact(url="https://gluu.org",name="Gluu Support",email="contact@gluu.org"),
		license = @License(name = "Gluu Support License", url = "https://www.gluu.org/support-license/") 
	)
)
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
