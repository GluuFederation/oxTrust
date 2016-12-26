package org.gluu.oxtrust.model.passport;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportConfigResponse {

	private List <Map> passportStrategies;

	public List <Map> getPassportStrategies() {
		return passportStrategies;
	}

	public void setPassportStrategies(List <Map> passportStrategies) {
		this.passportStrategies = passportStrategies;
	}

}
