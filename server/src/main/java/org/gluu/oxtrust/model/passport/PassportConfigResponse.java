package org.gluu.oxtrust.model.passport;

import java.util.List;
import java.util.Map;


/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportConfigResponse {
	
	private String applicationEndpoint;
	private String applicationStartpoint;
	private String authenticationUrl;
	private Map  <String ,PassportStrategy> passportStrategies;	
	
	public String getApplicationStartpoint() {
		return applicationStartpoint;
	}
	public void setApplicationStartpoint(String applicationStartpoint) {
		this.applicationStartpoint = applicationStartpoint;
	}
	public String getAuthenticationUrl() {
		return authenticationUrl;
	}
	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}		
	
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
