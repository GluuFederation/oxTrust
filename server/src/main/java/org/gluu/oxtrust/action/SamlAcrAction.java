package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.SamlAcrService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.SamlAcr;
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

	public List<SamlAcr> getAcrs() {
		return acrs;
	}

	public void setAcrs(List<SamlAcr> acrs) {
		this.acrs = acrs;
	}

	@PostConstruct
	public void init() {

		try {
			acrs = samlAcrService.getAll();
		} catch (Exception e) {
			log.error("Error loading saml acrs", e);
		}
	}

	public void save() {
		for (SamlAcr samlAcr : acrs) {
			samlAcrService.update(samlAcr);
		}
		
		shibboleth3ConfService.generateConfigurationFiles(acrs.stream().toArray(SamlAcr[]::new));
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Save succesfully!");
	}

	public void edit() {
		this.edit = true;
		this.samlAcr = new SamlAcr();
	}

	public void removeEntry(SamlAcr acr) {
		acrs.remove(acr);
		facesMessages.add(FacesMessage.SEVERITY_INFO, acr.getClassRef() + " removed!");
	}

	public void addEntry() {
		if (this.samlAcr.getParent() != null && this.samlAcr.getClassRef() != null) {
			acrs.add(this.samlAcr);
			this.edit = false;
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " added!");
		} else {
			this.edit = false;
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "All fields are required!");
		}
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public SamlAcr getSamlAcr() {
		if (this.samlAcr.getParent() == null) {
			this.samlAcr.setParent("shibboleth.SAML2AuthnContextClassRef");
		}
		return samlAcr;
	}

	public void setSamlAcr(SamlAcr samlAcr) {
		this.samlAcr = samlAcr;
	}
}
