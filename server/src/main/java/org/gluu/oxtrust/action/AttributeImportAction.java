/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.LdifService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.service.security.Secure;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

/**
 * Action class to load data from LDIF file
 * 
 * @author Shekhar L Date: 02.28.2017
 * @author Yuriy Movchan Date: 03/06/2017
 */
@Named("attributeImportAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('attribute', 'access')}")
public class AttributeImportAction implements Serializable {

	private static final long serialVersionUID = 8755036208872218664L;

	@Inject
	private Logger log;
	
	@Inject
	private LdifService ldifService;
	
	@Inject
	private AttributeService attributeService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	private UploadedFile uploadedFile;
	private FileDataToImport fileDataToImport;
	private byte[] fileData;

	private boolean isInitialized;

	public String init() {
		if (this.isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.fileDataToImport = new FileDataToImport();

		this.isInitialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String importAttributes() throws Exception {
		if (!fileDataToImport.isReady()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "File to import is invalid");
			return OxTrustConstants.RESULT_FAILURE;
		}

		InputStream is = new ByteArrayInputStream(fileDataToImport.getData());
		ResultCode result = null;
		try {
			result = ldifService.importLdifFileInLdap(is);
		} catch (LDAPException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to import LDIF file");
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		removeFileToImport();

		if ((result != null) && result.equals(ResultCode.SUCCESS)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO,"Attributes added successfully");
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to import LDIF file");
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public void validateFileToImport() {
		removeFileDataToImport();
		String dn = attributeService.getDnForAttribute(null);

		if (uploadedFile == null) {
			return;
		}

		InputStream is = new ByteArrayInputStream(this.fileData);
		ResultCode result = null;
		try {
			result = ldifService.validateLdifFile(is, dn);
		} catch (LDAPException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to parse LDIF file");
		} finally {
			IOUtils.closeQuietly(is);
		}

		if ((result != null) && result.equals(ResultCode.SUCCESS)) {
			this.fileDataToImport.setReady(true);
			this.fileDataToImport.setData(this.fileData);
		} else {
			removeFileDataToImport();
			this.fileDataToImport.setReady(false);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Invalid LDIF File. Validation failed");
		}
	}

	public String cancel() {
		destroy();

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@PreDestroy
	public void destroy() {
		removeFileDataToImport();
		removeFileToImport();
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public FileDataToImport getFileDataToImport() {
		return this.fileDataToImport;
	}

	public void removeFileDataToImport() {
		this.fileDataToImport.reset();
	}

	public void uploadFile(FileUploadEvent event) {
		removeFileToImport();

		this.uploadedFile = event.getUploadedFile();
		this.fileData = this.uploadedFile.getData();
	}

	public void removeFileToImport() {
		if (uploadedFile != null) {
			try {
				uploadedFile.delete();
			} catch (IOException ex) {
				log.error("Failed to remove temporary file", ex);
			}

			this.uploadedFile = null;
		}
		removeFileDataToImport();
	}


	public static class FileDataToImport implements Serializable {

		private static final long serialVersionUID = 7334362213305310293L;

		private String fileName;
		private byte[] data;
		private boolean ready;

		public FileDataToImport() {
		}

		public FileDataToImport(byte[] data) {
			this.data = data;
		}
		
		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public boolean isReady() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}

		public void reset() {
			this.fileName = null;
			this.data = null;
			this.ready = false;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	}
}
