package org.gluu.oxtrust.model.passport;

import java.util.List;
import java.util.Map;


/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportConfigResponse {
	
	private String applicationEndpoint;
	
	private Map  <String ,PassportStrategy> passportStrategies;	
	
	
	public String getApplicationEndpoint() {
		return applicationEndpoint;
	}
	public void setApplicationEndpoint(String applicationEndpoint) {
		this.applicationEndpoint = applicationEndpoint;
	}
	public Map  <String ,PassportStrategy> getPassportStrategies() {
		return passportStrategies;
	}
	public void setPassportStrategies(Map  <String ,PassportStrategy> passportStrategies) {
		this.passportStrategies = passportStrategies;
	}

}
