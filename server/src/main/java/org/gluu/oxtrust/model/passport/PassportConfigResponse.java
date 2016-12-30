package org.gluu.oxtrust.model.passport;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportConfigResponse {

	private Map <String,Map> passportStrategies;

	public Map <String, Map> getPassportStrategies() {
		return passportStrategies;
	}

	public void setPassportStrategies(Map <String,Map> passportStrategies) {
		this.passportStrategies = passportStrategies;
	}

}
