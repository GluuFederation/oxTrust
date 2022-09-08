package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import org.gluu.oxtrust.model.GluuBoolean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8570151265449586475L;

	private String allowPasswordReset = GluuBoolean.DISABLED.getValue();
	private String enablePassport = GluuBoolean.DISABLED.getValue();
	private String enableScim = GluuBoolean.DISABLED.getValue();
	private String enableSaml = GluuBoolean.DISABLED.getValue();
	private String enableRadius = GluuBoolean.DISABLED.getValue();
	private String allowProfileManagement = GluuBoolean.DISABLED.getValue();

	public String getEnableSaml() {
		return enableSaml;
	}

	public void setEnableSaml(String enableSaml) {
		this.enableSaml = enableSaml;
	}

	public String getEnableRadius() {
		return enableRadius;
	}

	public void setEnableRadius(String enableRadius) {
		this.enableRadius = enableRadius;
	}

	public String getAllowPasswordReset() {
		return allowPasswordReset;
	}

	public void setAllowPasswordReset(String allowPasswordReset) {
		this.allowPasswordReset = allowPasswordReset;
	}

	public String getEnablePassport() {
		return enablePassport;
	}

	public void setEnablePassport(String enablePassport) {
		this.enablePassport = enablePassport;
	}

	public String getEnableScim() {
		return enableScim;
	}

	public void setEnableScim(String enableScim) {
		this.enableScim = enableScim;
	}

	public String getAllowProfileManagement() {
		return allowProfileManagement;
	}

	public void setAllowProfileManagement(String allowProfileManagement) {
		this.allowProfileManagement = allowProfileManagement;
	}

	@Override
	public String toString() {
		return "OxtrustSetting [allowPasswordReset=" + allowPasswordReset + ", enablePassport=" + enablePassport
				+ ", enableScim=" + enableScim + ", allowProfileManagement=" + allowProfileManagement + "]";
	}

}
