package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "trustedIdp")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustedIDPApi implements Serializable {

	private static final long serialVersionUID = -5914998360095334159L;
	private RemoteIdp remoteIdp;
	private SingleSignOnServices selectedSingleSignOnService;
	
	
	
	public SingleSignOnServices getSelectedSingleSignOnService() {
		return selectedSingleSignOnService;
	}
	public void setSelectedSingleSignOnService(SingleSignOnServices selectedSingleSignOnService) {
		this.selectedSingleSignOnService = selectedSingleSignOnService;
	}
	public RemoteIdp getRemoteIdp() {
		return remoteIdp;
	}
	public void setRemoteIdp(RemoteIdp remoteIdp) {
		this.remoteIdp = remoteIdp;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getRemoteIdp() == null) ? 0 : getRemoteIdp().hashCode());
		result = prime * result + ((selectedSingleSignOnService == null) ? 0 : selectedSingleSignOnService.hashCode());
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
		TrustedIDPApi other = (TrustedIDPApi) obj;
		if (getRemoteIdp() == null) {
			if (other.getRemoteIdp() != null)
				return false;
		} else if (!getRemoteIdp().equals(other.getRemoteIdp()))
			return false;
		if (selectedSingleSignOnService == null) {
			if (other.selectedSingleSignOnService != null)
				return false;
		} else if (!selectedSingleSignOnService.equals(other.selectedSingleSignOnService))
			return false;
		return true;
	}

}
