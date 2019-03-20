package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.xdi.model.passport.Provider;

public class PassportProvider implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3916910813086334777L;

	public PassportProvider(Provider provider) {
		this.provider = provider;
	}

	private transient boolean checked;

	private Provider provider;

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

}
