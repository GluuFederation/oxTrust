package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.model.SamlAcr;
import org.gluu.oxtrust.service.SamlAcrService;
import org.gluu.oxtrust.service.Shibboleth3ConfService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@ConversationScoped
@Named("samlAcrAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class SamlAcrAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4373491307640582394L;
	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private SamlAcrService samlAcrService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	private SamlAcr samlAcr;

	private boolean edit;

	private List<SamlAcr> acrs = new ArrayList<>();

	private List<String> parents = new ArrayList<>();

	public List<SamlAcr> getAcrs() {
		return acrs;
	}
	
	public void setAcrs(List<SamlAcr> acrs) {
		this.acrs = acrs;
	}

	@PostConstruct
	public void init() {
		try {
			acrs.addAll(Arrays.asList(samlAcrService.getAll()));
		} catch (Exception e) {
			log.error("Error loading saml acrs", e);
		}
	}

	public void edit() {
		this.edit = true;
		this.samlAcr = new SamlAcr();
	}

	public void editEntry(SamlAcr samlAcr) {
		this.edit = true;
		this.samlAcr = samlAcr;
	}

	public void removeEntry(SamlAcr acr) {
		try {
			samlAcrService.remove(acr);
			this.acrs.remove(acr);
			shibboleth3ConfService.generateConfigurationFiles(samlAcrService.getAll());
			facesMessages.add(FacesMessage.SEVERITY_INFO, acr.getClassRef() + " removed!");
		} catch (Exception e) {
			log.info("", e);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, " Error removing " + acr.getClassRef());
		}

	}

	public void addEntry() {
		if (this.samlAcr.getInum() != null) {
			samlAcrService.update(this.samlAcr);
			shibboleth3ConfService.generateConfigurationFiles(samlAcrService.getAll());
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " updated!");
			this.samlAcr = null;
			this.edit = false;
		} else if (this.samlAcr.getParent() != null && this.samlAcr.getClassRef() != null
				&& this.samlAcr.getInum() == null) {
			String inum = samlAcrService.generateInumForSamlAcr();
			String dn = samlAcrService.getDn(inum);
			this.samlAcr.setDn(dn);
			this.samlAcr.setInum(inum);
			samlAcrService.add(this.samlAcr);
			this.acrs.add(samlAcr);
			this.edit = false;
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " added!");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "All fields are required!");
		}
		shibboleth3ConfService.generateConfigurationFiles(samlAcrService.getAll());
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public SamlAcr getSamlAcr() {
		return samlAcr;
	}

	public void setSamlAcr(SamlAcr samlAcr) {
		this.samlAcr = samlAcr;
	}

	public List<String> getParents() {
		this.parents.add("shibboleth.SAML2AuthnContextClassRef");
		return parents;
	}

	public void setParents(List<String> parents) {
		this.parents = parents;
	}
}
