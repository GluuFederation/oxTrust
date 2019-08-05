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
import org.gluu.oxtrust.model.SamlAcr;
import org.gluu.oxtrust.util.OxTrustConstants;
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

	private boolean update = false;

	private String inum;

	private List<SamlAcr> acrs = new ArrayList<>();
	private List<String> parents = new ArrayList<>();

	private SamlAcr samlAcr = new SamlAcr();

	public SamlAcr getSamlAcr() {
		return samlAcr;
	}

	public void setSamlAcr(SamlAcr samlAcr) {
		this.samlAcr = samlAcr;
	}

	public List<SamlAcr> getAcrs() {
		return acrs;
	}

	public void setAcrs(List<SamlAcr> acrs) {
		this.acrs = acrs;
	}

	@PostConstruct
	public String init() {
		try {
			acrs = samlAcrService.getAll();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.error("Error loading saml acrs", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public void removeEntry(SamlAcr acr) {
		samlAcrService.remove(this.samlAcr);
		acrs.remove(acr);
		facesMessages.add(FacesMessage.SEVERITY_INFO, acr.getClassRef() + " removed!");
	}

	public void update() {
		log.info("+++++++++++++++++++++");
		if (this.inum != null) {
			log.info("===========================Updating");
			this.samlAcr = samlAcrService.getByInum(this.inum);
			this.update=true;
		}else {
			this.update=false;
		}
	}

	public String save() {
		if (this.samlAcr.getInum() != null) {
			samlAcrService.update(this.samlAcr);
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " updated!");
			this.samlAcr = null;
			return OxTrustConstants.RESULT_SUCCESS;
		} else if (this.samlAcr.getParent() != null && this.samlAcr.getClassRef() != null
				&& this.samlAcr.getInum() == null) {
			String inum = samlAcrService.generateInumForSamlAcr();
			String dn = samlAcrService.getDn(inum);
			this.samlAcr.setDn(dn);
			this.samlAcr.setInum(inum);
			samlAcrService.add(this.samlAcr);
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.samlAcr.getClassRef() + " added!");
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "All fields are required!");
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public List<String> getParents() {
		parents.add("shibboleth.SAML2AuthnContextClassRef");
		return parents;
	}

	public void setParents(List<String> parents) {
		this.parents = parents;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}
}
