/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.TrustContact;
import org.gluu.oxtrust.model.OxTrustedIdp;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.TrustedIDPService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.JsonService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;
import org.gluu.oxtrust.model.SingleSignOnServices;

/**
 * Action class for view and update oxTrustedIdp form.
 * 
 * @author Shekhar L. Date: 11.09.2022
 */
@ConversationScoped
@Named("updateTrustedIdpAction")
@Secure("#{permissionService.hasPermission('group', 'access')}")
public class UpdateTrustedIdpAction implements Serializable {

	private static final long serialVersionUID = 572441515451149801L;

	@Inject
	private Logger log;
	
	@Inject
	private OxTrustAuditService oxTrustAuditService;

	private String inum;
	private boolean update;

	@Inject
	private Identity identity;
	
	private OxTrustedIdp oxTrustedIdp;
	
	private List <SingleSignOnServices> singleSignOnServices;
	
	private SingleSignOnServices selectedSingleSignOnService;

	@Inject
	private TrustedIDPService trustedIdpService;	

	@Inject
	private JsonService jsonService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	public String add() throws Exception {
		if (this.getOxTrustedIdp() != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.singleSignOnServices = new ArrayList<SingleSignOnServices>();
		this.selectedSingleSignOnService = new SingleSignOnServices();
		this.update = false;
		this.setOxTrustedIdp(new OxTrustedIdp());		
		
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() throws Exception {
		if (this.getOxTrustedIdp() != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = true;
		try {
			OxTrustedIdp oxTrustedIdp = trustedIdpService.getTrustedIDPByInum(inum);
			this.setOxTrustedIdp(oxTrustedIdp);
			if(oxTrustedIdp.getSupportedSingleSignOnServices() != null)
				singleSignOnServices = (List<SingleSignOnServices>)jsonService.jsonToObject(oxTrustedIdp.getSupportedSingleSignOnServices(),List.class);
			else {
				this.singleSignOnServices = new ArrayList<SingleSignOnServices>();
			}
			
			if(oxTrustedIdp.getSelectedSingleSignOnService() != null)
				selectedSingleSignOnService = (SingleSignOnServices)jsonService.jsonToObject(oxTrustedIdp.getSelectedSingleSignOnService(),SingleSignOnServices.class);
			else {
				this.selectedSingleSignOnService = new SingleSignOnServices();
			}		
		} catch (BasePersistenceException ex) {
			log.error("Failed to find oxTrustedIdp {}", inum, ex);
		}

		if (this.getOxTrustedIdp() == null) {
			log.error("Failed to load oxTrustedIdp {}", inum);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find oxTrustedIdp");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		log.debug("returning Success");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrustedIdp '#{updateTrustedIdpAction.oxTrustedIdp.remoteIdpName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New oxTrustedIdp not added");
		}
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws Exception {
		this.oxTrustedIdp.setSupportedSingleSignOnServices(jsonService.objectToJson(singleSignOnServices));
		this.oxTrustedIdp.setSelectedSingleSignOnService(jsonService.objectToJson(this.selectedSingleSignOnService));
		
		if (update) {
			try {
				trustedIdpService.updateTrustedIDP(getOxTrustedIdp());
				
			} catch (BasePersistenceException ex) {
				log.error("Failed to update oxTrustedIdp {}", this.inum, ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxTrustedIdp " + oxTrustedIdp.getRemoteIdpHost() );
				return OxTrustConstants.RESULT_FAILURE;
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrustedIdp " + oxTrustedIdp.getRemoteIdpHost() + "updated successfully");
		} else {
			OxTrustedIdp existingoxTrustedIdp = trustedIdpService.getTrustedIDPByRemoteIdpHost(getOxTrustedIdp().getRemoteIdpHost());
			if(existingoxTrustedIdp != null) {
				log.error("Already exist Trusted IDP {}", this.getOxTrustedIdp().getRemoteIdpHost());
				facesMessages.add(FacesMessage.SEVERITY_ERROR, " Trusted IDP already exist with Host : "+ this.getOxTrustedIdp().getRemoteIdpHost());
				return OxTrustConstants.RESULT_FAILURE;
			}
			this.inum = trustedIdpService.generateInumForTrustedIDP();
			this.getOxTrustedIdp().setDn(trustedIdpService.getDnForTrustedIDP(this.inum));
			this.getOxTrustedIdp().setInum(this.inum);
			try {
				trustedIdpService.addTrustedIDP(getOxTrustedIdp());
				oxTrustAuditService.audit("Trusted IDP " + this.getOxTrustedIdp().getInum() + " "+this.getOxTrustedIdp().getRemoteIdpName()+ " ADDED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
			} catch (BasePersistenceException ex) {
				log.error("Failed to add new oxTrustedIdp {}", this.getOxTrustedIdp().getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new oxTrustedIdp");
				return OxTrustConstants.RESULT_FAILURE;
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New Trusted IDP '#{updateTrustedIdpAction.oxTrustedIdp.remoteIdpName}' added successfully");
			conversationService.endConversation();
			this.update = true;
		}
		log.debug(" returning success updating or saving Trusted IDP");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String delete() throws Exception {
		if (update) {
			try {
				trustedIdpService.removeTrustedIDP(this.getOxTrustedIdp());
				oxTrustAuditService.audit("oxTrustedIdp " + this.getOxTrustedIdp().getInum() + " **"+this.getOxTrustedIdp().getRemoteIdpName()+ "** REMOVED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrustedIdp '#{updateTrustedIdpAction.oxTrustedIdp.remoteIdpName}' removed successfully");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BasePersistenceException ex) {
				log.error("Failed to remove oxTrustedIdp {}", this.getOxTrustedIdp().getInum(), ex);
			}
		}
		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove oxTrustedIdp '#{updateTrustedIdpAction.oxTrustedIdp.remoteIdpName}'");
		return OxTrustConstants.RESULT_FAILURE;
	}
	
	public void addEmptyContact() {
		if(singleSignOnServices == null)
			singleSignOnServices = new ArrayList<SingleSignOnServices>();
		singleSignOnServices.add(new SingleSignOnServices());
	}
	
	public void removeSingleSignOnServices(SingleSignOnServices singleSignOnService) {
		this.singleSignOnServices.remove(singleSignOnService);
	}
	
	public void removeSingleSignOnServices1(String singleSignOnService) {
		//this.singleSignOnServices.remove();
		log.info("inside remove ");
	}


	public void cancelSelectMembers() {
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public boolean isUpdate() {
		return update;
	}

	public OxTrustedIdp getOxTrustedIdp() {
		return oxTrustedIdp;
	}

	public void setOxTrustedIdp(OxTrustedIdp oxTrustedIdp) {
		this.oxTrustedIdp = oxTrustedIdp;
	}

	public List <SingleSignOnServices> getSingleSignOnServices() {
		return singleSignOnServices;
	}

	public void setSingleSignOnServices(List <SingleSignOnServices> singleSignOnServices) {
		this.singleSignOnServices = singleSignOnServices;
	}

	public SingleSignOnServices getSelectedSingleSignOnService() {
		return selectedSingleSignOnService;
	}

	public void setSelectedSingleSignOnService(SingleSignOnServices selectedSingleSignOnService) {
		this.selectedSingleSignOnService = selectedSingleSignOnService;
	}

}
