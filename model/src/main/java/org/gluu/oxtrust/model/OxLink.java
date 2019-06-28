/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.gluu.persist.model.base.Entry;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

/**
 * @author "Oleksiy Tataryn"
 *
 */
@DataEntry
@ObjectClass(value = "oxLink")
public class OxLink extends Entry implements Serializable {

	private static final long serialVersionUID = -2129922260303558907L;

	@AttributeName(name = "oxGuid")
	private String guid;

	@AttributeName(name = "oxLinkExpirationDate")
	private Date linkExpirationDate;

	@AttributeName(name = "oxLinkModerated")
	private Boolean linkModerated;

	@AttributeName(name = "oxLinkModerators")
	private List<String> linkModerators;

	@AttributeName(name = "oxLinkCreator")
	private String linkCreator;

	@AttributeName(name = "oxLinkPending")
	private List<String> linkPending;

	@AttributeName(name = "oxLinkLinktrack")
	private String linktrackLink;

	@AttributeName(name = "description")
	private String description;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getLinkExpirationDate() {
		return linkExpirationDate;
	}

	public void setLinkExpirationDate(Date linkExpirationDate) {
		this.linkExpirationDate = linkExpirationDate;
	}

	public Boolean getLinkModerated() {
		return linkModerated;
	}

	public void setLinkModerated(Boolean linkModerated) {
		this.linkModerated = linkModerated;
	}

	public List<String> getLinkModerators() {
		return linkModerators;
	}

	public void setLinkModerators(List<String> linkModerators) {
		this.linkModerators = linkModerators;
	}

	public String getLinkCreator() {
		return linkCreator;
	}

	public void setLinkCreator(String linkCreator) {
		this.linkCreator = linkCreator;
	}

	public List<String> getLinkPending() {
		return linkPending;
	}

	public void setLinkPending(List<String> linkPending) {
		this.linkPending = linkPending;
	}

	public String getLinktrackLink() {
		return linktrackLink;
	}

	public void setLinktrackLink(String linktrackLink) {
		this.linktrackLink = linktrackLink;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result
				+ ((linkCreator == null) ? 0 : linkCreator.hashCode());
		result = prime
				* result
				+ ((linkExpirationDate == null) ? 0 : linkExpirationDate
						.hashCode());
		result = prime * result
				+ ((linkModerated == null) ? 0 : linkModerated.hashCode());
		result = prime * result
				+ ((linkModerators == null) ? 0 : linkModerators.hashCode());
		result = prime * result
				+ ((linkPending == null) ? 0 : linkPending.hashCode());
		result = prime * result
				+ ((linktrackLink == null) ? 0 : linktrackLink.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OxLink other = (OxLink) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (linkCreator == null) {
			if (other.linkCreator != null)
				return false;
		} else if (!linkCreator.equals(other.linkCreator))
			return false;
		if (linkExpirationDate == null) {
			if (other.linkExpirationDate != null)
				return false;
		} else if (!linkExpirationDate.equals(other.linkExpirationDate))
			return false;
		if (linkModerated == null) {
			if (other.linkModerated != null)
				return false;
		} else if (!linkModerated.equals(other.linkModerated))
			return false;
		if (linkModerators == null) {
			if (other.linkModerators != null)
				return false;
		} else if (!linkModerators.equals(other.linkModerators))
			return false;
		if (linkPending == null) {
			if (other.linkPending != null)
				return false;
		} else if (!linkPending.equals(other.linkPending))
			return false;
		if (linktrackLink == null) {
			if (other.linktrackLink != null)
				return false;
		} else if (!linktrackLink.equals(other.linktrackLink))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("OxLink [guid=%s, linkExpirationDate=%s, linkModerated=%s, linkModerators=%s, linkCreator=%s, linkPending=%s, linktrackLink=%s, description=%s]",
						guid, linkExpirationDate, linkModerated,
						linkModerators, linkCreator, linkPending,
						linktrackLink, description);
	}

}
