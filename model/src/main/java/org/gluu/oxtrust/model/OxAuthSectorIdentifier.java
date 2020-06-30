package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Sector Identifier
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@DataEntry(sortBy = { "description" })
@ObjectClass(value = "oxSectorIdentifier")
@JsonInclude(Include.NON_NULL)
public class OxAuthSectorIdentifier extends Entry implements Serializable {

	private static final long serialVersionUID = -2812480357430436514L;

	private transient boolean selected;

	@AttributeName(name = "oxId", ignoreDuringUpdate = true)
	private String id;
	@NotNull
	@Size(min = 0, max = 250, message = "Length of the Description should not exceed 250")
	@AttributeName(name = "description")
	private String description;

	@AttributeName(name = "oxAuthRedirectURI")
	private List<String> redirectUris = new ArrayList<>();

	@AttributeName(name = "oxAuthClientId")
	private List<String> clientIds = new ArrayList<>();

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(List<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	public List<String> getClientIds() {
		return clientIds;
	}

	public void addNewClient(String clientInum) {

		List<String> clients = new ArrayList<>();
		clients.addAll(this.clientIds);
		clients.remove(clientInum);
		clients.add(clientInum);
		this.clientIds = clients;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	public String getLoginUri() {
		return null;
	}

	public String getDescription() {
		if (description == null) {
			description = "Default description";
		}
		return description;
	}

	public void setDescription(String des) {
		this.description = des;
	}

	@Override
	public String toString() {
		return String.format("OxAuthSectorIdentifier [id=%s, toString()=%s]", id, super.toString());
	}
}
