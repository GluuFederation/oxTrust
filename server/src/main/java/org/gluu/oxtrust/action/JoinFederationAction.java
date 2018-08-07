/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.io.ResponseHelper;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.FederationService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.xdi.model.GluuStatus;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;
import org.xdi.util.io.ExcludeFilterInputStream;
import org.xdi.util.io.FileUploadWrapper;

@ConversationScoped
@Named("joinFederationAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class JoinFederationAction implements Serializable {

	private static final long serialVersionUID = -1032167044333943680L;

	private GluuSAMLFederationProposal federationProposal;

	private String inum;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private FederationService federationService;

	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private TrustContactsAction trustContactsAction;

	private FileUploadWrapper fileWrapper = new FileUploadWrapper();

	public String add() {
		if (this.federationProposal != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.federationProposal = new GluuSAMLFederationProposal();
		this.federationProposal.setOwner(organizationService.getOrganization().getDn());
		this.federationProposal.setStatus(GluuStatus.INACTIVE);

		init();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String view() {
		if (this.federationProposal != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.federationProposal = federationService.getProposalByInum(inum);

		this.fileWrapper = new FileUploadWrapper();
		this.fileWrapper.setFileName(this.federationProposal.getSpMetaDataFN());

		init();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void init() {
		trustContactsAction.initContacts(this.federationProposal);
	}

	public boolean isActive() {
		return GluuStatus.ACTIVE.equals(this.federationProposal.getStatus());
	}

	public String acceptToggle() {
		if (isActive()) {
			this.federationProposal.setStatus(GluuStatus.INACTIVE);
			federationService.updateFederationProposal(federationProposal);
		} else {
			this.federationProposal.setStatus(GluuStatus.ACTIVE);
			federationService.updateFederationProposal(federationProposal);
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String delete() {

		federationService.removeFederationProposal(this.federationProposal);

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save(boolean federation) {
		if (this.federationProposal.isRulesAccepted() || federation) {
			if (inum == null) {
				this.inum = federationService.generateInumForNewFederationProposal();
				String dn = federationService.getDnForFederationProposal(this.getInum());

				this.federationProposal.setInum(this.getInum());
				this.federationProposal.setDn(dn);
				if (!federation && !saveSpMetaDataFile()) {
					return OxTrustConstants.RESULT_FAILURE;
				}
				trustContactsAction.saveContacts();
				this.federationProposal.setFederation(federation);
				federationService.addFederationProposal(federationProposal);
			} else {
				if (!federation && !saveSpMetaDataFile()) {
					return OxTrustConstants.RESULT_FAILURE;
				}
				trustContactsAction.saveContacts();
				federationService.updateFederationProposal(federationProposal);
			}

			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "You should accept Federation Policies and Operating Procedures");
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String cancel() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private boolean saveSpMetaDataFile() {
		boolean result = false;
		if (GluuMetadataSourceType.FILE.equals(federationProposal.getSpMetaDataSourceType())) {
			result = saveSpMetaDataFileSourceTypeFile();
		} else if (GluuMetadataSourceType.URI.equals(federationProposal.getSpMetaDataSourceType())) {
			result = saveSpMetaDataFileSourceTypeURI();
		}

		if (!result) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to save meta-data file. Please check if you provide correct file");
			return result;
		}

		if (shibboleth3ConfService.isCorrectMetadataFile(federationProposal.getSpMetaDataFN())) {
			return true;
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to parse meta-data file. Please check if you provide correct file");
		shibboleth3ConfService.removeMetadataFile(federationProposal.getSpMetaDataFN());

		return false;
	}

	private boolean saveSpMetaDataFileSourceTypeFile() {
		String metadataFileName = federationProposal.getSpMetaDataFN();
		boolean emptySpMetadataFileName = StringHelper.isEmpty(metadataFileName);

		if (fileWrapper.getStream() == null) {
			if (emptySpMetadataFileName) {
				return false;
			}

			// Admin doesn't provide new file. Check if we already has this file
			String filePath = shibboleth3ConfService.getMetadataFilePath(metadataFileName);
			if (filePath == null) {
				return false;
			}

			File file = new File(filePath);
			if (!file.exists()) {
				return false;
			}

			// File already exist
			return true;
		}

		if (emptySpMetadataFileName) {
			// Generate new file name
			metadataFileName = shibboleth3ConfService.getNewMetadataFileName(this.federationProposal,
					federationService.getAllFederationProposals());
		}

		// Save new file
		boolean result = shibboleth3ConfService.saveMetadataFile(metadataFileName, fileWrapper.getStream());
		if (result) {
			federationProposal.setSpMetaDataFN(metadataFileName);
		}

		return result;

	}

	private boolean saveSpMetaDataFileSourceTypeURI() {
		String metadataFileName = federationProposal.getSpMetaDataFN();
		boolean emptyMetadataFileName = StringHelper.isEmpty(metadataFileName);

		if (emptyMetadataFileName) {
			// Generate new file name
			metadataFileName = shibboleth3ConfService.getNewMetadataFileName(this.federationProposal,
					federationService.getAllFederationProposals());
		}

		boolean result = shibboleth3ConfService.saveMetadataFile(federationProposal.getSpMetaDataURL(), metadataFileName);
		if (result) {
			federationProposal.setSpMetaDataFN(metadataFileName);
		}

		return result;
	}

	public GluuSAMLFederationProposal getFederationProposal() {
		return federationProposal;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getInum() {
		return inum;
	}

	public void setFileWrapper(FileUploadWrapper fileWrapper) {
		this.fileWrapper = fileWrapper;
	}

	public FileUploadWrapper getFileWrapper() {
		return fileWrapper;
	}

	public String getMetadata() throws IOException {
		if (federationProposal == null) {
			return null;
		}

		String filename = federationProposal.getSpMetaDataFN();
		File metadataFile = null;
		if (!StringUtils.isEmpty(filename)) {
			metadataFile = new File(shibboleth3ConfService.getMetadataFilePath(filename));

			if (metadataFile.exists()) {
				return FileUtils.readFileToString(metadataFile);
			}
		}

		return null;
	}

	public String downloadFederation() throws IOException {
		boolean result = false;
		if (StringHelper.isNotEmpty(inum)) {
			GluuSAMLFederationProposal federation = federationService.getProposalByInum(inum);
			if (!federation.isFederation() || !federation.getStatus().equals(GluuStatus.ACTIVE)) {
				return OxTrustConstants.RESULT_FAILURE;
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream(16384);
			String head = String
					.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<EntitiesDescriptor Name=\"%s\"  xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\">\n",
							StringHelper.removePunctuation(federation.getInum()));
			bos.write(head.getBytes());
			for (GluuSAMLFederationProposal proposal : federationService.getAllActiveFederationProposals()) {
				if (proposal.getContainerFederation() != null && proposal.getContainerFederation().equals(federation)) {
					String filename = proposal.getSpMetaDataFN();
					if (!StringUtils.isEmpty(filename)) {
						File metadataFile = new File(shibboleth3ConfService.getMetadataFilePath(filename));
						InputStream is = FileUtils.openInputStream(metadataFile);
						ExcludeFilterInputStream filtered = new ExcludeFilterInputStream(is, "<?", "?>");
						IOUtils.copy(filtered, bos);
					}
				}
			}
			String tail = "</EntitiesDescriptor>";
			bos.write(tail.getBytes());

			FacesContext facesContext = FacesContext.getCurrentInstance();
			result = ResponseHelper.downloadFile("federation.xml", OxTrustConstants.CONTENT_TYPE_TEXT_PLAIN, bos.toByteArray(), facesContext);
		}
		return result ? OxTrustConstants.RESULT_SUCCESS : OxTrustConstants.RESULT_FAILURE;
	}

	public void setRules(String rules) {
		this.federationProposal.setFederationRules(rules);
	}

	public String getRules() {
		String rules = null;
		if (this.federationProposal.isFederation()) {
			rules = this.federationProposal.getFederationRules();
		} else if (this.federationProposal.getContainerFederation() != null) {
			rules = this.federationProposal.getContainerFederation().getFederationRules();
		}
		return rules;
	}

}
