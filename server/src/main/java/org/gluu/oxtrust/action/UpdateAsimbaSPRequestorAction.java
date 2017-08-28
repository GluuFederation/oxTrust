/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.asimba.util.ldap.sp.RequestorEntry;
import org.gluu.asimba.util.ldap.sp.RequestorPoolEntry;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AsimbaService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.asimba.AsimbaXMLConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.ServiceUtil;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.security.Secure;

/**
 * Action class for updating and adding the SAML SP Requestor (=client
 * application) to Asimba
 * 
 * @author Dmitry Ognyannikov
 */
@SessionScoped
@Named("updateAsimbaSPRequestorAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class UpdateAsimbaSPRequestorAction implements Serializable {

	private static final long serialVersionUID = -1342167044333943680L;

	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private Identity identity;

	@Inject
	private SvnSyncTimer svnSyncTimer;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private AsimbaService asimbaService;

	@Inject
	private AsimbaXMLConfigurationService asimbaXMLConfigurationService;
    
        @Inject
        private ConversationService conversationService;

	@Produces
	private RequestorEntry spRequestor;

	private boolean newEntry = true;

	private String editEntryInum = null;

	private String spRequestorAdditionalProperties = "";

	private List<RequestorEntry> spRequestorList = new ArrayList<RequestorEntry>();

	private ArrayList<SelectItem> spPoolList;

	private byte uploadedCertBytes[] = null;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern = "";

	public UpdateAsimbaSPRequestorAction() {

	}

	@PostConstruct
	public void init() {
		log.info("init() SPRequestor call");

		clearEdit();
		spRequestor.setPoolID("requestorpool.1");

		refresh();
	}

	public void refresh() {
		log.info("refresh() SPRequestor call");

		if (searchPattern == null || "".equals(searchPattern)) {
			// list loading
			spRequestorList = asimbaService.loadRequestors();
		} else {
			// search mode, clear pattern
			searchPattern = null;
		}

		// fill spPoolList
		spPoolList = new ArrayList<SelectItem>();
		List<RequestorPoolEntry> spPoolListEntries = asimbaService.loadRequestorPools();
		for (RequestorPoolEntry entry : spPoolListEntries) {
			spPoolList.add(new SelectItem(entry.getId(), entry.getId(), entry.getFriendlyName()));
		}
	}

	public void clearEdit() {
		log.info("clearEdit() SPRequestor call");
		spRequestor = new RequestorEntry();
		editEntryInum = null;
		newEntry = true;
		uploadedCertBytes = null;
	}

    /**
     * Set "add new" or "edit" mode by inum request parameter.
     */
	public void edit() {
            log.info("edit() SPRequestor call, inum: " + editEntryInum);
            if (editEntryInum == null || "".equals(editEntryInum) || "new".equals(editEntryInum)) {
                // no inum, new entry mode
                clearEdit();
            } else if ((editEntryInum != null) && (spRequestor != null) && (editEntryInum != spRequestor.getInum())) {
                // edit entry
                newEntry = false;
                spRequestor = asimbaService.readRequestorEntry(editEntryInum);
                if (spRequestor != null) {
                    setProperties(spRequestor.getProperties());
                }
            }
	}

	public String add() {
		log.info("add new Requestor", spRequestor);
		spRequestor.setProperties(getProperties());
		synchronized (svnSyncTimer) {
			asimbaService.addRequestorEntry(spRequestor);
		}
		// save certificate
		try {
			if (uploadedCertBytes != null) {
				String message = asimbaXMLConfigurationService.addCertificateFile(uploadedCertBytes, spRequestor.getId());
				log.info("Add CertificateFile: " + message);
			}
		} catch (Exception e) {
			log.error("Requestor certificate - add CertificateFile exception", e);
                        facesMessages.add(FacesMessage.SEVERITY_ERROR, "Requestor certificate - add CertificateFile exception");
                        conversationService.endConversation();
                        return OxTrustConstants.RESULT_FAILURE;
		}
		clearEdit();
                conversationService.endConversation();
        
                asimbaService.restartAsimbaService();
        
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() {
		log.info("update() Requestor", spRequestor);
		spRequestor.setId(spRequestor.getId().trim());

		synchronized (svnSyncTimer) {
			asimbaService.updateRequestorEntry(spRequestor);
		}
		// save certificate
		try {
			if (uploadedCertBytes != null) {
				String message = asimbaXMLConfigurationService.addCertificateFile(uploadedCertBytes, spRequestor.getId());
				log.info("Add CertificateFile: " + message);
			}
		} catch (Exception e) {
			log.error("Requestor certificate - add CertificateFile exception", e);
                        facesMessages.add(FacesMessage.SEVERITY_ERROR, "Requestor certificate - add CertificateFile exception");
                        conversationService.endConversation();
                        return OxTrustConstants.RESULT_FAILURE;
		}
		newEntry = false;
                conversationService.endConversation();
        
                asimbaService.restartAsimbaService();
        
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		log.info("cancel() Requestor", spRequestor);
		clearEdit();
                conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String delete() {
		log.info("delete() Requestor", spRequestor);
		synchronized (svnSyncTimer) {
			asimbaService.removeRequestorEntry(spRequestor);
		}
		clearEdit();
                conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String uploadMetadataFile(FileUploadEvent event) {
		log.info("uploadMetadataFile() Requestor", spRequestor);
		try {
			UploadedFile uploadedFile = event.getUploadedFile();
			String filepath = asimbaService.saveSPRequestorMetadataFile(uploadedFile);
			spRequestor.setMetadataFile(filepath);
			spRequestor.setMetadataUrl("");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "File uploaded");
		} catch (Exception e) {
			log.error("Requestor metadata - uploadFile() exception", e);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Requestor metadata - uploadFile exception", e);
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String uploadCertificateFile(FileUploadEvent event) {
		log.info("uploadCertificateFile() Requestor", spRequestor);
		try {
			UploadedFile uploadedFile = event.getUploadedFile();
			uploadedCertBytes = ServiceUtil.readFully(uploadedFile.getInputStream());

			// check alias for valid url
			String id = spRequestor.getId();
			if (id != null && id.trim().toLowerCase().startsWith("http")) {
				id = id.trim();
				URL u = new URL(id); // this would check for the protocol
				u.toURI(); // does the extra checking required for validation of
							// URI

				String message = asimbaXMLConfigurationService.addCertificateFile(uploadedFile, spRequestor.getId());
				// display message
				if (!OxTrustConstants.RESULT_SUCCESS.equals(message)) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Add Certificate ERROR: ", message);
				} else {
					facesMessages.add(FacesMessage.SEVERITY_INFO, "Certificate uploaded");
				}
			} else {
				facesMessages.add(FacesMessage.SEVERITY_INFO, "Add valid URL to ID");
			}
		} catch (Exception e) {
			log.info("Requestor certificate - uploadCertificateFile() exception", e);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Add Certificate ERROR: ", e.getMessage());
			return OxTrustConstants.RESULT_VALIDATION_ERROR;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String search() {
		log.info("search() Requestor searchPattern:", searchPattern);
		synchronized (svnSyncTimer) {
			if (searchPattern != null && !"".equals(searchPattern)) {
				try {
					spRequestorList = asimbaService.searchRequestors(searchPattern, 0);
				} catch (Exception ex) {
					log.error("LDAP search exception", ex);
                                        return OxTrustConstants.RESULT_FAILURE;
				}
			} else {
				// list loading
				spRequestorList = asimbaService.loadRequestors();
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private Properties getProperties() {
		if (spRequestorAdditionalProperties == null || "".equals(spRequestorAdditionalProperties)) {
			// empty set
			return new Properties();
		}
		try {
			Properties p = new Properties();
			p.load(new StringReader(spRequestorAdditionalProperties));
			return p;
		} catch (Exception ex) {
			log.error("cannot parse SPRequestor properties: " + spRequestorAdditionalProperties);
			return new Properties();
		}
	}

	private void setProperties(Properties properties) {
		if (properties != null && properties.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String propertyName : properties.stringPropertyNames()) {
				String value = properties.getProperty(propertyName);
				sb.append(propertyName + "=" + value + "\n");
			}
			spRequestorAdditionalProperties = sb.toString();
		} else {
			spRequestorAdditionalProperties = "";
		}
	}

	/**
	 * @return the spRequestor
	 */
	public RequestorEntry getSpRequestor() {
		return spRequestor;
	}

	/**
	 * @param spRequestor
	 *            the spRequestor to set
	 */
	public void setSpRequestor(RequestorEntry spRequestor) {
		this.spRequestor = spRequestor;
	}

	/**
	 * @return the spRequestorList
	 */
	public List<RequestorEntry> getSpRequestorList() {
		return spRequestorList;
	}

	/**
	 * @param spRequestorList
	 *            the spRequestorList to set
	 */
	public void setSpRequestorList(List<RequestorEntry> spRequestorList) {
		this.spRequestorList = spRequestorList;
	}

	/**
	 * @return the searchPattern
	 */
	public String getSearchPattern() {
		return searchPattern;
	}

	/**
	 * @param searchPattern
	 *            the searchPattern to set
	 */
	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	/**
	 * @return the spPoolList
	 */
	public ArrayList<SelectItem> getSpPoolList() {
		return spPoolList;
	}

	/**
	 * @param spPoolList
	 *            the spPoolList to set
	 */
	public void setSpPoolList(ArrayList<SelectItem> spPoolList) {
		this.spPoolList = spPoolList;
	}

	/**
	 * @return the spRequestorAdditionalProperties
	 */
	public String getSpRequestorAdditionalProperties() {
		return spRequestorAdditionalProperties;
	}

	public Properties getSpRequestorAdditionalPropertiesAsProperties() throws IOException {
		Properties result = new Properties();
		result.load(new StringReader(spRequestorAdditionalProperties));
		return result;
	}

	/**
	 * @param spRequestorAdditionalProperties
	 *            the spRequestorAdditionalProperties to set
	 */
	public void setSpRequestorAdditionalProperties(String spRequestorAdditionalProperties) {
		this.spRequestorAdditionalProperties = spRequestorAdditionalProperties;
	}

	public void setSpRequestorAdditionalProperties(Properties additionalProperties) {
		StringWriter writer = new StringWriter();
		for (String property : additionalProperties.stringPropertyNames()) {
			writer.write(property);
			writer.write("=");
			writer.write(additionalProperties.getProperty(property));
			writer.write("\n");
		}
		this.spRequestorAdditionalProperties = writer.toString();
	}

	/**
	 * @return the newEntry
	 */
	public boolean isNewEntry() {
		return newEntry;
	}

	/**
	 * @param newEntry
	 *            the newEntry to set
	 */
	public void setNewEntry(boolean newEntry) {
		this.newEntry = newEntry;
	}

	/**
	 * @return the editEntryInum
	 */
	public String getEditEntryInum() {
		return editEntryInum;
	}

	/**
	 * @param editEntryInum
	 *            the editEntryInum to set
	 */
	public void setEditEntryInum(String editEntryInum) {
		this.editEntryInum = editEntryInum;
	}
}
