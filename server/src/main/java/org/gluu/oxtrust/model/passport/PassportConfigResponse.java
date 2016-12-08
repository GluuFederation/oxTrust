package org.gluu.oxtrust.model.passport;

import java.util.Map;

/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportConfigResponse {

	private Map<String, PassportStrategy> passportStrategies;

	public Map<String, PassportStrategy> getPassportStrategies() {
		return passportStrategies;
	}

	public void setPassportStrategies(Map<String, PassportStrategy> passportStrategies) {
		this.passportStrategies = passportStrategies;
	}

}
