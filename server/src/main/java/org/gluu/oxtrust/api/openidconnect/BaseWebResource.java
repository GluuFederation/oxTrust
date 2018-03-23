package org.gluu.oxtrust.api.openidconnect;

import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BaseWebResource {
	protected ObjectMapper mapper;

	public BaseWebResource() {
		this.mapper = new ObjectMapper();
		this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	public String handleError(Logger logger, Exception e, String information, HttpServletResponse response) {
		logger.error(information, e);
		try {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
		} catch (Exception ex) {
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

}
