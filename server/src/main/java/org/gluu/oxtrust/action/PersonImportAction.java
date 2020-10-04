/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuUserRole;
import org.gluu.model.attribute.AttributeDataType;
import org.gluu.oxtrust.ldap.load.conf.ImportPersonConfiguration;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.table.Table;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.ExcelService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.EntryPersistenceException;
import org.gluu.persist.model.AttributeData;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;

/**
 * Action class for load persons from Excel file
 * 
 * @author Yuriy Movchan Date: 02.14.2011
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('person', 'import')}")
public class PersonImportAction implements Serializable {

	private String UID = "uid";

	private String USER_PASSWORD = "userPassword";

	private String SEPARATOR = ";";

	private static final long serialVersionUID = -1270460481895022468L;

	private String[] PERSON_IMPORT_PERSON_LOCKUP_RETURN_ATTRIBUTES = { UID, "displayName", "mail" };
	private String PERSON_PASSWORD_ATTRIBUTE = USER_PASSWORD;

	@Inject
	private Logger log;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private PersonService personService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ExternalUpdateUserService externalUpdateUserService;

	@Inject
	private ExcelService excelService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private transient ImportPersonConfiguration importPersonConfiguration;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;
	private UploadedFile file;
	private FileDataToImport fileDataToImport;
	private List<GluuAttribute> attributes;
	private Map<String, GluuAttribute> attributesDisplayNameMap;
	private byte[] fileData;

	private boolean isInitialized;
	private GluuCustomPerson person;
	private String inum;

	public String init() {
		if (this.isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.attributes = importPersonConfiguration.getAttributes();
		this.attributesDisplayNameMap = getAttributesDisplayNameMap(this.attributes);
		this.fileDataToImport = new FileDataToImport();
		this.isInitialized = true;
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String importPersons() throws Exception {
		if (!fileDataToImport.isReady()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "File to import is invalid");
			return OxTrustConstants.RESULT_FAILURE;
		}
		log.debug("Attempting to add {} persons", fileDataToImport.getPersons().size());
		try {
			for (GluuCustomPerson person : fileDataToImport.getPersons()) {
				this.person = person;
				String result = initializePerson();
				if (result.equals(OxTrustConstants.RESULT_SUCCESS)) {
					result = save();
				}
				if (result.equals(OxTrustConstants.RESULT_SUCCESS)) {
					log.debug("Added new person: {}", person.getUid());
				} else {
					log.debug("Failed to add new person: {}", person.getUid());
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new person: '%s'", person.getUid());
				}
			}
		} catch (EntryPersistenceException ex) {
			log.error("Failed to add new person", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to import users");
			return OxTrustConstants.RESULT_FAILURE;
		}
		log.debug("All {} persons added successfully", fileDataToImport.getPersons().size());
		oxTrustAuditService.audit(fileDataToImport.getPersons().size() + " USERS IMPORTED ", identity.getUser(),
				(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Users successfully imported");
		removeFileToImport();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String validateFileToImport() {
		try {
			removeFileDataToImport();
			if (file == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (file.getFileName().contains(">") || file.getFileName().contains("<")) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Bad file.");
				return OxTrustConstants.RESULT_FAILURE;
			}
			Table table;
			try (InputStream is = new ByteArrayInputStream(this.fileData);) {
				table = excelService.read(is);
			} catch (Exception e) {
				return null;
			}
			this.fileDataToImport.setTable(table);
			if (table != null) {
				this.fileDataToImport.setFileName(FilenameUtils.getName(file.getFileName()));
				this.fileDataToImport.setImportAttributes(getAttributesForImport(table));
				this.fileDataToImport.setReady(true);
			}
			if (this.fileDataToImport.isReady()) {
				boolean valid = prepareAndValidateImportData(this.fileDataToImport.getTable(),
						this.fileDataToImport.getImportAttributes());
				this.fileDataToImport.setReady(valid);
				if (!valid) {
					removeFileDataToImport();
				}
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Invalid file content");
			log.error("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String cancel() {
		boolean cancel = this.file != null;
		destroy();

		if (cancel) {
			return OxTrustConstants.RESULT_CLEAR;
		}

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@PreDestroy
	public void destroy() {
		removeFileDataToImport();
		removeFileToImport();
	}

	public FileDataToImport getFileDataToImport() {
		return this.fileDataToImport;
	}

	public void removeFileDataToImport() {
		this.fileDataToImport.reset();
	}

	public void handleFileUpload(FileUploadEvent event) {
		this.file = null;
		this.fileDataToImport.reset();
		this.file = event.getFile();
		this.fileData = this.file.getContent();
	}

	public void removeFileToImport() {
		if (file != null) {
			this.file = null;
		}
		removeFileDataToImport();
	}

	private boolean prepareAndValidateImportData(Table table, List<ImportAttribute> importAttributes) throws Exception {
		String attributesString = getAttributesString(this.attributes);
		if ((table == null) || (importAttributes == null)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Import failed. Missing columns: %s", attributesString);
			return false;
		}

		List<GluuAttribute> mandatoryAttributes = getMandatoryAttributes(this.attributes);
		List<ImportAttribute> mandatoryImportAttributes = getMandatoryImportAttributes(importAttributes);
		if (mandatoryAttributes.size() != mandatoryImportAttributes.size()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Import failed. Required columns: %s", attributesString);
			return false;
		}

		if (table.getCountRows() < 1) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Import failed. No data found");
			return false;
		}

		// Convert Excel table to GluuCustomPersons
		List<GluuCustomPerson> persons = convertTableToPersons(table, importAttributes);
		if (persons == null) {
			return false;
		}

		// Check if person already exist
		if (!validatePersons(persons)) {
			return false;
		}

		// Fill persons with default values
		if (!setDefaultPersonAttributes(persons, importAttributes)) {
			return false;
		}

		// Store persons
		log.info("Prepared {} persons for creation", persons.size());
		this.fileDataToImport.setPersons(persons);

		return true;
	}

	private List<ImportAttribute> getMandatoryImportAttributes(List<ImportAttribute> importAttributes) {
		List<ImportAttribute> result = new ArrayList<ImportAttribute>();
		for (ImportAttribute importAttribute : importAttributes) {
			if ((importAttribute.getCol() != -1) && importAttribute.getAttribute().isRequred()) {
				result.add(importAttribute);
			}
		}

		return result;
	}

	private List<GluuAttribute> getMandatoryAttributes(List<GluuAttribute> attributes) {
		List<GluuAttribute> result = new ArrayList<GluuAttribute>();
		for (GluuAttribute attribute : attributes) {
			if (attribute.isRequred()) {
				result.add(attribute);
			}
		}

		return result;
	}

	private boolean setDefaultPersonAttributes(List<GluuCustomPerson> persons, List<ImportAttribute> importAttributes)
			throws Exception {
		boolean isGeneratePassword = false;
		for (ImportAttribute importAttribute : importAttributes) {
			if (importAttribute.getAttribute().getName().equalsIgnoreCase(PERSON_PASSWORD_ATTRIBUTE)
					&& !importAttribute.getAttribute().isRequred()) {
				isGeneratePassword = true;
				break;
			}
		}

		for (GluuCustomPerson person : persons) {
			if (StringHelper.isEmpty(person.getCommonName())) {
				person.setCommonName(person.getGivenName() + " " + person.getSurname());
			} else {
				person.setCommonName(person.getCommonName() + " " + person.getGivenName() + " " + person.getSurname());
			}
			person.setDisplayName(person.getCommonName());

			if (isGeneratePassword && StringHelper.isEmpty(person.getUserPassword())) {
				person.setUserPassword(RandomStringUtils.randomAlphanumeric(16));
			}

		}

		return true;
	}

	private boolean validatePersons(List<GluuCustomPerson> persons) throws Exception {
		Set<String> uids = new HashSet<String>();
		Set<String> mails = new HashSet<String>();
		for (GluuCustomPerson person : persons) {
			uids.add(person.getUid());
			mails.add(person.getMail());
		}

		if (uids.size() != persons.size()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Import failed. There are persons with simular uid(s) in input file");
			return false;
		}

		if (mails.size() != persons.size()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Import failed. There are persons with simular mail(s) in input file");
			return false;
		}

		List<GluuCustomPerson> existPersons = personService.findPersonsByUids(new ArrayList<String>(uids),
				PERSON_IMPORT_PERSON_LOCKUP_RETURN_ATTRIBUTES);
		if (existPersons.size() > 0) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Import failed. There are persons with existing uid(s): %s",
					personService.getPersonUids(existPersons));
			return false;
		}

		List<GluuCustomPerson> existEmailPersons = personService.findPersonsByMailids(new ArrayList<String>(mails),
				PERSON_IMPORT_PERSON_LOCKUP_RETURN_ATTRIBUTES);
		if (existEmailPersons.size() > 0) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Import failed. There are persons with existing mailid(s): %s",
					personService.getPersonMailids(existEmailPersons));
			return false;
		}

		return true;
	}

	protected List<GluuCustomPerson> convertTableToPersons(Table table, List<ImportAttribute> importAttributes)
			throws Exception {
		Map<String, List<AttributeData>> entriesAttributes = new HashMap<String, List<AttributeData>>();
		Map<String, String> uidPAsswords = new HashMap<String, String>();
		int rows = table.getCountRows();
		boolean validTable = true;
		for (int i = 1; i <= rows; i++) {
			List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
			String uid = null;
			String password = null;
			for (ImportAttribute importAttribute : importAttributes) {
				if (importAttribute.getCol() == -1) {
					continue;
				}
				GluuAttribute attribute = importAttribute.getAttribute();
				String cellValue = table.getCellValue(importAttribute.getCol(), i);
				boolean isMultiValue = attribute.getOxMultiValuedAttribute();
				if (StringHelper.isEmpty(cellValue)) {
					if (attribute.isRequred()) {
						facesMessages.add(FacesMessage.SEVERITY_ERROR, "Import failed. Empty '%s' not allowed",
								attribute.getDisplayName());
						validTable = false;
					}
					continue;
				}
				String ldapValue = getTypedValue(attribute, cellValue);
				if (StringHelper.isEmpty(ldapValue)) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"Invalid value '%s' in column '%s' at row %s were specified", cellValue,
							attribute.getDisplayName(), i + 1);
					validTable = false;
					continue;
				}
				if (attribute.getName().equalsIgnoreCase(UID)) {
					uid = ldapValue;
				}
				if (attribute.getName().equalsIgnoreCase(USER_PASSWORD)) {
					password = ldapValue;
				}
				if (isMultiValue) {
					AttributeData attributeData = new AttributeData(attribute.getName(), ldapValue.split(SEPARATOR));
					attributeDataList.add(attributeData);
				} else {
					AttributeData attributeData = new AttributeData(attribute.getName(), ldapValue);
					attributeDataList.add(attributeData);
				}
			}
			entriesAttributes.put(Integer.toString(i), attributeDataList);
			uidPAsswords.put(uid, password);
		}
		if (!validTable) {
			return null;
		}
		List<GluuCustomPerson> persons = personService.createEntities(entriesAttributes);
		log.trace("Found {} persons in input Excel file", persons.size());
		for (GluuCustomPerson person : persons) {
			if (person.getStatus() == null) {
				person.setStatus(appConfiguration.getSupportedUserStatus().get(1));
			}
			if (uidPAsswords.containsKey(person.getUid())) {
				String password = uidPAsswords.get(person.getUid());
				if (password != null) {
					person.setUserPassword(uidPAsswords.get(person.getUid().trim().toString()));
				} else {
					person.setUserPassword(person.getUid());
				}
			}
		}
		return persons;
	}

	private String getTypedValue(GluuAttribute attribute, String value) {
		if (AttributeDataType.STRING.equals(attribute.getDataType())
				|| attribute.getName().equalsIgnoreCase("gluuStatus")) {
			return value;
		} else if (AttributeDataType.BOOLEAN.equals(attribute.getDataType())) {
			Boolean gluuBoolean = Boolean.valueOf(value);
			if (gluuBoolean != null) {
				return gluuBoolean.toString();
			}
		}
		return null;
	}

	private String getAttributesString(List<GluuAttribute> attributes) {
		StringBuilder sb = new StringBuilder();

		for (Iterator<GluuAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
			GluuAttribute attribute = iterator.next();
			sb.append('\'').append(attribute.getDisplayName()).append('\'');
			if (!attribute.isRequred()) {
				sb.append(" (non mandatory)");
			}
			if (iterator.hasNext()) {
				sb.append(", ");
			}

		}

		return sb.toString();
	}

	private List<ImportAttribute> getAttributesForImport(Table table) {
		List<ImportAttribute> importAttributes = new ArrayList<ImportAttribute>();
		if ((table == null) || (table.getCountCols() < 1) || (table.getCountRows() < 1)) {
			return importAttributes;
		}

		int cols = table.getCountCols();
		List<String> addedAttributes = new ArrayList<String>(this.attributes.size());
		for (int i = 0; i <= cols; i++) {
			String cellValue = table.getCellValue(i, 0);
			if (StringHelper.isEmpty(cellValue)) {
				continue;
			}
			String attributeName = cellValue.toLowerCase();
			GluuAttribute attribute = attributesDisplayNameMap.get(attributeName);
			if (attribute != null) {
				addedAttributes.add(attributeName);
				ImportAttribute importAttribute = new ImportAttribute(i, attribute);
				importAttributes.add(importAttribute);
			}
		}
		for (GluuAttribute attribute : this.attributes) {
			if (!addedAttributes.contains(attribute.getName())) {
				ImportAttribute importAttribute = new ImportAttribute(-1, attribute);
				importAttributes.add(importAttribute);
			}
		}
		return importAttributes;
	}

	private Map<String, GluuAttribute> getAttributesDisplayNameMap(List<GluuAttribute> attributes) {
		Map<String, GluuAttribute> result = new HashMap<String, GluuAttribute>();
		for (GluuAttribute attribute : attributes) {
			result.put(attribute.getDisplayName().toLowerCase(), attribute);
		}

		return result;
	}

	private void initAttributes() {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		List<String> origins = attributeService.getAllAttributeOrigins(attributes);
		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		boolean newPerson = (customAttributes == null) || customAttributes.isEmpty();
		if (newPerson) {
			customAttributes = new ArrayList<GluuCustomAttribute>();
			this.person.setCustomAttributes(customAttributes);
		}
		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins,
				appConfiguration.getPersonObjectClassTypes(), appConfiguration.getPersonObjectClassDisplayNames());
		if (newPerson) {
			customAttributeAction.addCustomAttributes(personService.getMandatoryAtributes());
		}
	}

	public String save() {
		if (!organizationService.isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		personService.addCustomObjectClass(this.person);
		if (personService.getPersonByUid(this.person.getUid()) != null) {
			return OxTrustConstants.RESULT_DUPLICATE;
		}
		this.inum = personService.generateInumForNewPerson();
		String dn = personService.getDnForPerson(this.inum);
		// Save person
		this.person.setDn(dn);
		this.person.setInum(this.inum);
		List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
		if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
			List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
			changedAttributes.addAll(personAttributes);
			changedAttributes.add(
					new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getDisplayName()));
			this.person.setCustomAttributes(changedAttributes);
		} else {
			this.person.setCommonName(this.person.getCommonName() + " " + this.person.getGivenName());
		}
		try {
			boolean runScript = externalUpdateUserService.isEnabled();
			if (runScript) {
				externalUpdateUserService.executeExternalAddUserMethods(this.person);
			}
			personService.addPerson(this.person);
			if (runScript) {
				externalUpdateUserService.executeExternalPostAddUserMethods(this.person);
			}
		} catch (Exception ex) {
			log.error("Failed to add new person {}", this.person.getInum(), ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String initializePerson() {
		if (!organizationService.isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.person = new GluuCustomPerson();
		initAttributes();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public org.primefaces.model.file.UploadedFile getFile() {
		return file;
	}

	public void setFile(org.primefaces.model.file.UploadedFile file) {
		this.file = file;
	}

	public static class FileDataToImport implements Serializable {

		private static final long serialVersionUID = 7334362213305310293L;

		private String fileName;
		private Table table;
		private List<ImportAttribute> importAttributes;
		private List<GluuCustomPerson> persons;
		private boolean ready;

		public FileDataToImport() {
		}

		public FileDataToImport(Table table) {
			this.table = table;
		}

		public List<ImportAttribute> getImportAttributes() {
			return importAttributes;
		}

		public void setImportAttributes(List<ImportAttribute> importAttributes) {
			this.importAttributes = importAttributes;
		}

		public Table getTable() {
			return table;
		}

		public void setTable(Table table) {
			this.table = table;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public List<GluuCustomPerson> getPersons() {
			return persons;
		}

		public void setPersons(List<GluuCustomPerson> persons) {
			this.persons = persons;
		}

		public boolean isReady() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}

		public void reset() {
			this.fileName = null;
			this.table = null;
			this.importAttributes = null;
			this.persons = null;
			this.ready = false;
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