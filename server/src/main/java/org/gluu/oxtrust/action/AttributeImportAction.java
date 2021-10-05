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

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.model.GluuAttribute;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.LdifService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

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
	private LdifService ldifService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private FacesMessages facesMessages;

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
		ResultCode result = null;
		try (InputStream is = new ByteArrayInputStream(fileDataToImport.getData());) {
			result = ldifService.importLdifFileInLdap(GluuAttribute.class, is);
		} catch (LDAPException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to import LDIF file");
		}
		removeFileToImport();
		if ((result != null) && result.equals(ResultCode.SUCCESS)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Attributes added successfully");
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
		ResultCode result = null;
		try (InputStream is = new ByteArrayInputStream(this.fileData);) {
			result = ldifService.validateLdifFile(is, dn);
		} catch (LDAPException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to parse LDIF file");
		} catch (IOException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to parse LDIF file");
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
		this.uploadedFile = event.getFile();
		this.fileData = this.uploadedFile.getContent();
	}

	public void removeFileToImport() {
		if (uploadedFile != null) {
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
