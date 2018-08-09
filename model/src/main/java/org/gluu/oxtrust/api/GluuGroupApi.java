package org.gluu.oxtrust.api;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.gluu.oxtrust.model.GluuGroup;
import org.xdi.model.GluuStatus;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gluugroup")
public class GluuGroupApi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5914998360095334159L;
	private String inum;
	private String iname;
	private String displayName;
	private String description;
	private String owner;
	private List<String> members;
	private String organization;
	private GluuStatus status;

	public GluuGroupApi() {
	}

	public GluuGroupApi(GluuGroup gluuGroup) {
		this.inum = gluuGroup.getInum();
		this.iname = gluuGroup.getIname();
		this.displayName = gluuGroup.getDisplayName();
		this.description = gluuGroup.getDescription();
		this.owner = gluuGroup.getOwner();
		this.status = gluuGroup.getStatus();
		this.members = gluuGroup.getMembers();
		this.organization = gluuGroup.getOrganization();
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getIname() {
		return iname;
	}

	public void setIname(String iname) {
		this.iname = iname;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public GluuStatus getStatus() {
		return status;
	}

	public void setStatus(GluuStatus status) {
		this.status = status;
	}
}
