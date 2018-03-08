package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.SectorIdentifierService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.DisplayNameEntry;
import org.xdi.service.LookupService;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update sector identifier form.
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@ConversationScoped
@Named("updateSectorIdentifierAction")
@Secure("#{permissionService.hasPermission('sectorIdentifier', 'access')}")
public class UpdateSectorIdentifierAction implements Serializable {

    private static final long serialVersionUID = 572441515451149802L;

    @Inject
    private Logger log;

    private String id;
    private boolean update;

    private OxAuthSectorIdentifier sectorIdentifier;

    private List<String> loginUris;

    private List<DisplayNameEntry> clientDisplayNameEntries;

    @NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
    private String searchAvailableClientPattern;

    private String oldSearchAvailableClientPattern;

    private String availableLoginUri = "https://";

    private List<OxAuthClient> availableClients;

    @Inject
    private SectorIdentifierService sectorIdentifierService;

    @Inject
    private LookupService lookupService;

    @Inject
    private ClientService clientService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

    @Inject
    private AppConfiguration appConfiguration;

    public String add() throws Exception {
        if (this.sectorIdentifier != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }

        this.update = false;
        this.sectorIdentifier = new OxAuthSectorIdentifier();

        try {
            this.loginUris = getNonEmptyStringList(sectorIdentifier.getRedirectUris());
            if(sectorIdentifier.getClientIds() != null && sectorIdentifier.getClientIds().size()>0)
            	this.loginUris.addAll(clientRedirectUriList(sectorIdentifier.getClientIds()));
            this.clientDisplayNameEntries = loadClientDisplayNameEntries();
        } catch (BaseMappingException ex) {
            log.error("Failed to load login Uris", ex);

            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new sector identifier");
			conversationService.endConversation();

            return OxTrustConstants.RESULT_FAILURE;
        }

        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    public String update() {
    	String outcome = updateImpl();
    	
    	if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find sector identifier");
			conversationService.endConversation();
    	}
    	
    	return outcome;
    }

    public String updateImpl() {
        if (this.sectorIdentifier != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }

        this.update = true;
        log.info("this.update : " + this.update);
        try {
            log.info("id : " + id);
            this.sectorIdentifier = sectorIdentifierService.getSectorIdentifierById(id);
        } catch (BaseMappingException ex) {
            log.error("Failed to find sector identifier {}", id, ex);
        }

        if (this.sectorIdentifier == null) {
            log.info("Sector identifier is null ");
            return OxTrustConstants.RESULT_FAILURE;
        }

        try {
            this.loginUris = getNonEmptyStringList(sectorIdentifier.getRedirectUris());
            this.clientDisplayNameEntries = loadClientDisplayNameEntries();
        } catch (Exception ex) {
            log.error("Failed to load person display names", ex);

            return OxTrustConstants.RESULT_FAILURE;
		}

        log.info("returning Success");

        return OxTrustConstants.RESULT_SUCCESS;
    }

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New sector identifier not added");
		}
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

    public String save() throws Exception {
        List<DisplayNameEntry> oldClientDisplayNameEntries = null;
        try {
            oldClientDisplayNameEntries = loadClientDisplayNameEntries();
        } catch (BaseMappingException ex) {
            log.info("error getting old clients");
            log.error("Failed to load client display names", ex);

            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update sector identifier");
            return OxTrustConstants.RESULT_FAILURE;
        }

        updateLoginURIs();
        updateClientDisplayNameEntries();
        if (update) {
            // Update sectorIdentifier
            try {
                sectorIdentifierService.updateSectorIdentifier(this.sectorIdentifier);
                updateClients(oldClientDisplayNameEntries, this.clientDisplayNameEntries);
            } catch (BaseMappingException ex) {
                log.info("error updating sector identifier ", ex);
                log.error("Failed to update sector identifier {}", this.id, ex);

                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}'");
                return OxTrustConstants.RESULT_FAILURE;
            } catch (Exception ex) {
				log.error("Failed to update sector identifier {}", this.id, ex);
            }

            facesMessages.add(FacesMessage.SEVERITY_INFO, "Sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}' updated successfully");
        } else {
            this.id = sectorIdentifierService.generateIdForNewSectorIdentifier();
            String dn = sectorIdentifierService.getDnForSectorIdentifier(this.id);

            // Save sectorIdentifier
            this.sectorIdentifier.setDn(dn);
            this.sectorIdentifier.setId(this.id);
            try {
                sectorIdentifierService.addSectorIdentifier(this.sectorIdentifier);
                updateClients(oldClientDisplayNameEntries, this.clientDisplayNameEntries);
            } catch (BaseMappingException ex) {
                log.info("error saving sector identifier ");
                log.error("Failed to add new sector identifier {}", this.sectorIdentifier.getId(), ex);

                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new sector identifier");
                return OxTrustConstants.RESULT_FAILURE;
            }

			facesMessages.add(FacesMessage.SEVERITY_INFO, "New sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}' added successfully");
			conversationService.endConversation();

            this.update = true;
        }

        log.info(" returning success updating or saving sector identifier");

        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String delete() throws Exception {
        if (update) {
            // Remove sectorIdentifier
            try {
                sectorIdentifierService.removeSectorIdentifier(this.sectorIdentifier);

                facesMessages.add(FacesMessage.SEVERITY_INFO, "Sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
            } catch (BaseMappingException ex) {
                log.error("Failed to remove sector identifier {}", this.sectorIdentifier.getId(), ex);
            }
        }

        facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove sector identifier '#{updateSectorIdentifierAction.sectorIdentifier.id}'");

        return OxTrustConstants.RESULT_FAILURE;
    }

    private List<DisplayNameEntry> loadClientDisplayNameEntries() throws Exception {
        List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
        List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null), this.sectorIdentifier.getClientIds());
        if (tmp != null) {
            result.addAll(tmp);
        }

        return result;
    }

    private List<String> getNonEmptyStringList(List<String> currentList) {
        if (currentList != null && currentList.size() > 0) {
            return new ArrayList<String>(currentList);
        } else {
            return new ArrayList<String>();
        }
    }

    public void addClient(OxAuthClient client) {
        DisplayNameEntry displayNameEntry = new DisplayNameEntry(client.getDn(), client.getInum(), client.getDisplayName());
        this.clientDisplayNameEntries.add(displayNameEntry);
    }

    public void removeClient(String inum) throws Exception {
        if (StringHelper.isEmpty(inum)) {
            return;
        }

        String removeClientInum = clientService.getDnForClient(inum);

        for (Iterator<DisplayNameEntry> iterator = this.clientDisplayNameEntries.iterator(); iterator.hasNext(); ) {
            DisplayNameEntry displayNameEntry = iterator.next();
            if (removeClientInum.equals(displayNameEntry.getDn())) {
                iterator.remove();
                break;
            }
        }
    }

    public String getSearchAvailableClientPattern() {
        return this.searchAvailableClientPattern;
    }

    public void setSearchAvailableClientPattern(String searchAvailableClientPattern) {
        this.searchAvailableClientPattern = searchAvailableClientPattern;
    }

    public List<OxAuthClient> getAvailableClients() {
        return this.availableClients;
    }

    public void searchAvailableClients() {
        if (Util.equals(this.oldSearchAvailableClientPattern, this.searchAvailableClientPattern)) {
            return;
        }

        try {
            this.availableClients = clientService.searchClients(this.searchAvailableClientPattern, OxTrustConstants.searchClientsSizeLimit);
            this.oldSearchAvailableClientPattern = this.searchAvailableClientPattern;
            selectAddedClients();
        } catch (Exception ex) {
            log.error("Failed to find clients", ex);
        }
    }

    public void selectAddedClients() {
        if (this.availableClients == null) {
            return;
        }

        Set<String> addedClientInums = new HashSet<String>();
        for (DisplayNameEntry entry : clientDisplayNameEntries) {
            addedClientInums.add(entry.getInum());
        }

        for (OxAuthClient client : this.availableClients) {
            client.setSelected(addedClientInums.contains(client.getInum()));
        }
    }

    public void acceptSelectClients() {
        if (this.availableClients == null) {
            return;
        }

        Set<String> addedClientInums = new HashSet<String>();
        for (DisplayNameEntry entry : clientDisplayNameEntries) {
            addedClientInums.add(entry.getInum());
        }

        for (OxAuthClient client : this.availableClients) {
            if (client.isSelected() && !addedClientInums.contains(client.getInum())) {
                addClient(client);
                if(client.getOxAuthRedirectURIs() != null && client.getOxAuthRedirectURIs().size()>0)
                	this.loginUris.addAll(client.getOxAuthRedirectURIs());
            }
        }
    }

    public void cancelSelectClients() {
    }

    private void updateClientDisplayNameEntries() {
        List<String> clientDisplayNameEntries = new ArrayList<String>();
        this.sectorIdentifier.setClientIds(clientDisplayNameEntries);

        for (DisplayNameEntry displayNameEntry : this.clientDisplayNameEntries) {
            clientDisplayNameEntries.add(displayNameEntry.getDn());
        }
    }

    private void updateClients(List<DisplayNameEntry> oldClientDisplayNameEntries, List<DisplayNameEntry> newClientDisplayNameEntries) throws Exception {
        log.debug("Old clients: {}", oldClientDisplayNameEntries);
        log.debug("New clients: {}", newClientDisplayNameEntries);

        String sectorIdentifierDn = this.sectorIdentifier.getDn();

        // Convert members to array of DNs
        String[] oldClientDns = convertToDNsArray(oldClientDisplayNameEntries);
        String[] newClientDns = convertToDNsArray(newClientDisplayNameEntries);

        Arrays.sort(oldClientDns);
        Arrays.sort(newClientDns);

        boolean[] retainOldClients = new boolean[oldClientDns.length];
        Arrays.fill(retainOldClients, false);

        List<String> addedMembers = new ArrayList<String>();
        List<String> removedMembers = new ArrayList<String>();
        List<String> existingMembers = new ArrayList<String>();

        // Add new values
        for (String value : newClientDns) {
            int idx = Arrays.binarySearch(oldClientDns, value);
            if (idx >= 0) {
                // Old members array contains member. Retain member
                retainOldClients[idx] = true;
            } else {
                // This is new member
                addedMembers.add(value);
            }
        }

        // Remove clients which we don't have in new clients
        for (int i = 0; i < oldClientDns.length; i++) {
            if (retainOldClients[i]) {
                existingMembers.add(oldClientDns[i]);
            } else {
                removedMembers.add(oldClientDns[i]);
            }
        }

        for (String dn : addedMembers) {
            OxAuthClient client = clientService.getClientByDn(dn);
            log.debug("Adding sector identifier {} to client {}", sectorIdentifierDn, client.getDisplayName());

            client.setSectorIdentifierUri(getSectorIdentifierUrl());

            clientService.updateClient(client);
        }

        for (String dn : removedMembers) {
            OxAuthClient client = clientService.getClientByDn(dn);
            log.debug("Removing sector identifier {} from client {}", sectorIdentifierDn, client.getDisplayName());

            client.setSectorIdentifierUri(null);

            clientService.updateClient(client);
        }
    }

    private String[] convertToDNsArray(List<DisplayNameEntry> clientDisplayNameEntries) {
        String[] dns = new String[clientDisplayNameEntries.size()];
        int i = 0;
        for (DisplayNameEntry displayNameEntry : clientDisplayNameEntries) {
            dns[i++] = displayNameEntry.getDn();
        }

        return dns;
    }

    public void acceptSelectLoginUri() {
        if (StringHelper.isEmpty(this.availableLoginUri)) {
            return;
        }

        if (!this.loginUris.contains(this.availableLoginUri)) {
            this.loginUris.add(this.availableLoginUri);
        }

        this.availableLoginUri = "https://";
    }

    public void cancelSelectLoginUri() {
        this.availableLoginUri = "http://";
    }

    private void updateLoginURIs() {
        if (this.loginUris == null || this.loginUris.size() == 0) {
            this.sectorIdentifier.setRedirectUris(null);
            return;
        }

        List<String> tmpUris = new ArrayList<String>();
        for (String uri : this.loginUris) {
            tmpUris.add(uri);
        }

        this.sectorIdentifier.setRedirectUris(tmpUris);
    }

    public void removeLoginURI(String uri) {
        removeFromList(this.loginUris, uri);
    }

    private void removeFromList(List<String> uriList, String uri) {
        if (StringUtils.isEmpty(uri)) {
            return;
        }

        for (Iterator<String> iterator = uriList.iterator(); iterator.hasNext(); ) {
            String tmpUri = iterator.next();
            if (uri.equals(tmpUri)) {
                iterator.remove();
                break;
            }
        }
    }

    public String getSectorIdentifierUrl() {
        return appConfiguration.getOxAuthSectorIdentifierUrl() + "/" + id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OxAuthSectorIdentifier getSectorIdentifier() {
        return sectorIdentifier;
    }

    public List<String> getLoginUris() {
        return loginUris;
    }

    public void setLoginUris(List<String> loginUris) {
        this.loginUris = loginUris;
    }

    public List<DisplayNameEntry> getClientDisplayNameEntries() {
        return clientDisplayNameEntries;
    }

    public boolean isUpdate() {
        return update;
    }

    public String getAvailableLoginUri() {
        return availableLoginUri;
    }

    public void setAvailableLoginUri(String availableLoginUri) {
        this.availableLoginUri = availableLoginUri;
    }
    
	private List<String> clientRedirectUriList(List<String> clientInum) {
		List<String> clientRedirectUri = new  ArrayList <String>();
		for (int i = 0; i < clientInum.size(); i++) {
			OxAuthClient OxAuthClient = clientService.getClientByInum(clientInum
					.get(i));
			clientRedirectUri.addAll(OxAuthClient.getOxAuthRedirectURIs());
		}		
		return clientRedirectUri;
	}
    
}
