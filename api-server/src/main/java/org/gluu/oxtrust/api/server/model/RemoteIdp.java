package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteIdp")
public class RemoteIdp implements Serializable {

	public RemoteIdp(String id, String name, String host, List<String> signingCertificates,
			List<SingleSignOnServices> supportedSingleSignOnServices) {
		super();
		this.id = id;
		this.name = name;
		this.host = host;
		this.signingCertificates = signingCertificates;
		this.supportedSingleSignOnServices = supportedSingleSignOnServices;
	}

	private static final long serialVersionUID = -5914998360095334159L;
	private String id;
	private String name;
	private String host;
	private List<String> signingCertificates;
	private List<SingleSignOnServices> supportedSingleSignOnServices;

	public RemoteIdp() {
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<String> getSigningCertificates() {
		return signingCertificates;
	}

	public void setSigningCertificates(List<String> signingCertificates) {
		this.signingCertificates = signingCertificates;
	}

	public List<SingleSignOnServices> getSupportedSingleSignOnServices() {
		return supportedSingleSignOnServices;
	}

	public void setSupportedSingleSignOnServices(List<SingleSignOnServices> supportedSingleSignOnServices) {
		this.supportedSingleSignOnServices = supportedSingleSignOnServices;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		//result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((signingCertificates == null) ? 0 : signingCertificates.hashCode());
		result = prime * result
				+ ((supportedSingleSignOnServices == null) ? 0 : supportedSingleSignOnServices.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteIdp other = (RemoteIdp) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;

		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (signingCertificates == null) {
			if (other.signingCertificates != null)
				return false;
		} else if (!signingCertificates.equals(other.signingCertificates))
			return false;
		if (supportedSingleSignOnServices == null) {
			if (other.supportedSingleSignOnServices != null)
				return false;
		} else if (!supportedSingleSignOnServices.equals(other.supportedSingleSignOnServices))
			return false;
		return true;
	}


}
