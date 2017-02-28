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

import org.gluu.oxtrust.ldap.load.conf.ImportPersonConfiguration;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ExcelService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.LdifService;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.LdifDataUtility;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.GluuAttribute;

import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldif.LDIFReader;

/**
 * Action class for load data from LDIF file
 * 
 * @author Shekhar Laad Date: 02.14.2011
 */
@Name("attributeImportAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class AttributeImportAction implements Serializable {

	private static final long serialVersionUID = -1270460481895022468L;

	public static final String PERSON_PASSWORD_ATTRIBUTE = "userPassword";

	@Logger
	private Log log;

	@In
	StatusMessages statusMessages;

	@In
	private IPersonService personService;
	
	@In
	private LdifService ldifService;
	
	@In
	private AttributeService attributeService;
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In
	private ExternalUpdateUserService externalUpdateUserService;

	@In
	private transient ExcelService excelService;

	@In
	private FacesMessages facesMessages;

	@In
	private transient ImportPersonConfiguration importPersonConfiguration;
	
	@In
    protected LdapEntryManager ldapEntryManager;
	
	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private CustomAttributeAction customAttributeAction;

	private UploadedFile uploadedFile;
	private FileDataToImport fileDataToImport;
	private byte[] fileData;

	private boolean isInitialized;
	LdifDataUtility ldifDataUtility;

	public String init() {
		if (this.isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		ldifDataUtility = LdifDataUtility.instance();
		this.fileDataToImport = new FileDataToImport();

		this.isInitialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String importAttributes() throws Exception {
		if (!fileDataToImport.isReady()) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		
		if (uploadedFile != null) {
			//Table table;
			InputStream is = new ByteArrayInputStream(this.fileData);
			
			ResultCode result = ldifService.importLdifFileInLdap( is);
			
			if(!result.equals(ResultCode.SUCCESS)){
				removeFileDataToImport();
				this.fileDataToImport.setReady(false);
				facesMessages.add(Severity.ERROR, "Invalid LDIF File. import Failed");
				return OxTrustConstants.RESULT_FAILURE;
			}
			
			is.close();
			this.fileDataToImport.setReady(true);
			this.fileDataToImport.setIs(is);
		}

		removeFileToImport();
		facesMessages.add(Severity.INFO,"LDIF File is successfully Added");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void validateFileToImport() throws Exception {
		removeFileDataToImport();

		if (uploadedFile == null) {
			return;
		}

		if (uploadedFile != null) {
			//Table table;
			InputStream is = new ByteArrayInputStream(this.fileData);
			ResultCode result = ldifService.importLdifFileInLdap( is);
			if(!result.equals(ResultCode.SUCCESS)){
				log.info("LDIFReader   --- : ");
				removeFileDataToImport();
				this.fileDataToImport.setReady(false);
				facesMessages.add(Severity.ERROR, "Invalid LDIF File. validation Failed");
				return;
			}
			is.close();
			this.fileDataToImport.setReady(true);
			this.fileDataToImport.setIs(is);
		}
	}

	@Restrict("#{s:hasPermission('import', 'person')}")
	public void cancel() {
		destroy();
	}

	@Destroy
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

	@Restrict("#{s:hasPermission('import', 'person')}")
	public void uploadFile(FileUploadEvent event) {
		removeFileToImport();

		this.uploadedFile = event.getUploadedFile();
		this.fileData = this.uploadedFile.getData();
	}

	@Restrict("#{s:hasPermission('import', 'person')}")
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
		private InputStream is;
		private boolean ready;

		public FileDataToImport() {
		}

		public FileDataToImport(InputStream is) {
			this.is = is;
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
			this.is = null;
			this.ready = false;
		}

		public InputStream getIs() {
			return is;
		}

		public void setIs(InputStream is) {
			this.is = is;
		}
	}

	public static class ImportAttribute implements Serializable {

		private static final long serialVersionUID = -5640983196565086530L;

		private GluuAttribute attribute;
		private int col;

		public ImportAttribute(int col, GluuAttribute attribute) {
			this.col = col;
			this.attribute = attribute;
		}

		public int getCol() {
			return col;
		}

		public void setCol(int col) {
			this.col = col;
		}

		public GluuAttribute getAttribute() {
			return attribute;
		}

		public void setAttribute(GluuAttribute attribute) {
			this.attribute = attribute;
		}
	}

}
