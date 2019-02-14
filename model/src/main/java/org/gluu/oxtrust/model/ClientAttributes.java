package org.gluu.oxtrust.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

public class ClientAttributes implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6876529876454015750L;

	@JsonProperty("tlsClientAuthSubjectDn")
	private String tlsClientAuthSubjectDn="";

	public String getTlsClientAuthSubjectDn() {
		return tlsClientAuthSubjectDn;
	}

	public void setTlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {
		this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
	}

	@Override
	public String toString() {
		return "ClientAttributes{" + "tlsClientAuthSubjectDn='" + tlsClientAuthSubjectDn + '\'' + '}';
	}

}
