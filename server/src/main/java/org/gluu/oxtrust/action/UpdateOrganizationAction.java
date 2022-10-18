/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.gluu.config.oxauth.WebKeysSettings;
import org.gluu.config.oxtrust.LdapOxAuthConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SmtpConfiguration;
import org.gluu.orm.util.ArrayHelper;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.servlet.FaviconImageServlet;
import org.gluu.oxtrust.servlet.IdpFaviconServlet;
import org.gluu.oxtrust.servlet.IdpLogoServlet;
import org.gluu.oxtrust.servlet.LogoImageServlet;
import org.gluu.oxtrust.servlet.OxAuthFaviconServlet;
import org.gluu.oxtrust.servlet.OxAuthLogoServlet;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.MailService;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Action class for configuring application
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 */
@Named("updateOrganizationAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class UpdateOrganizationAction implements Serializable {

	private static final long serialVersionUID = -4470460481895022468L;

	private String THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING = "The change may take up to 30min to be effective due to caching.You can use Ctrl+F5 to force cache reload.";
	private String DEFAULT_CONTACT_EMAIL = "example@orgname.com";

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private MailService mailService;

	private GluuOrganization organization;

	protected String loginPageCustomMessage;
	protected String welcomePageCustomMessage;
	protected String welcomeTitleText;

	private GluuConfiguration configuration;

	private List<GluuConfiguration> configurations;

	private boolean initialized;
	private WebKeysSettings webKeysSettings;

	private LdapOxAuthConfiguration ldapOxAuthConfiguration;

	private SmtpConfiguration smtpConfiguration;

	private String smtpPasswordDecrypted;
	private String keyStorePasswordDecrypted;

	private String contactEmail;

	@PostConstruct
	void init() {
			GluuConfiguration configurationUpdate = configurationService.getConfiguration();
			smtpConfiguration = configurationUpdate.getSmtpConfiguration();

			configurationService.decryptSmtpPassword(smtpConfiguration);
			configurationService.decryptKeyStorePassword(smtpConfiguration);

			smtpPasswordDecrypted = smtpConfiguration.getPasswordDecrypted();
			keyStorePasswordDecrypted = smtpConfiguration.getKeyStorePasswordDecrypted();
	}

	public String modify() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		String resultOrganization = modifyOrganization();
		String resultApplliance = modifyApplliance();
		if (!StringHelper.equals(OxTrustConstants.RESULT_SUCCESS, resultOrganization)
				|| !StringHelper.equals(OxTrustConstants.RESULT_SUCCESS, resultApplliance)) {

			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['organization.prepareUpdateFailed']}"));
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}
		this.initialized = true;
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String modifyOrganization() {
		if (this.organization != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			GluuOrganization tmpOrganization = organizationService.getOrganization();
			this.organization = new GluuOrganization();
			try {
				PropertyUtils.copyProperties(this.organization, tmpOrganization);
			} catch (Exception ex) {
				log.error("Failed to load organization", ex);
				this.organization = null;
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to load organization", ex);
		}

		if (this.organization == null) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.loginPageCustomMessage = organizationService
				.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_LOGIN_PAGE);
		this.welcomePageCustomMessage = organizationService
				.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_WELCOME_PAGE);
		this.welcomeTitleText = organizationService
				.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_TITLE_TEXT);
		initOxAuthSetting();
		configurations = new ArrayList<GluuConfiguration>();
		configurations.addAll(configurationService.getConfigurations());
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initOxAuthSetting() {
		String configurationDn = configurationFactory.getConfigurationDn();
		try {
			ldapOxAuthConfiguration = organizationService.getOxAuthSetting(configurationDn);
			this.webKeysSettings = ldapOxAuthConfiguration.getOxWebKeysSettings();
			if (webKeysSettings == null) {
				webKeysSettings = new WebKeysSettings();
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP");
		}
	}

	public String save() {
		try {
			setCustomMessages();
			organizationService.updateOrganization(this.organization);
			if(StringUtils.isNotEmpty(smtpPasswordDecrypted)){
				smtpConfiguration.setPasswordDecrypted(smtpPasswordDecrypted);
			}
			if(StringUtils.isNotEmpty(keyStorePasswordDecrypted)){
				smtpConfiguration.setKeyStorePasswordDecrypted(keyStorePasswordDecrypted);
			}
			configuration.setContactEmail(new String[] { getContactEmail()} );
			configurationService.encryptSmtpPassword(smtpConfiguration);
			configurationService.encryptKeyStorePassword(smtpConfiguration);
			updateConfiguration();
			saveWebKeySettings();
		} catch (BasePersistenceException ex) {
			log.error("Failed to update organization", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					facesMessages.evalResourceAsString("#{msgs['organization.UpdateFailed']}"));
			return OxTrustConstants.RESULT_FAILURE;
		}
		facesMessages.add(FacesMessage.SEVERITY_INFO,
				facesMessages.evalResourceAsString("#{msgs['organization.UpdateSucceed']}"));
		return modify();
	}

	private void updateConfiguration() {
		GluuConfiguration configurationUpdate = configurationService.getConfiguration();
		configurationUpdate.setPasswordResetAllowed(configuration.isPasswordResetAllowed());
		configurationUpdate.setPassportEnabled(configuration.isPassportEnabled());
		configurationUpdate.setRadiusEnabled(configuration.isRadiusEnabled());
		configurationUpdate.setScimEnabled(configuration.isScimEnabled());
		configurationUpdate.setProfileManagment(configuration.isProfileManagment());
		configurationUpdate.setConfigurationDnsServer(configuration.getConfigurationDnsServer());
		configurationUpdate.setMaxLogSize(configuration.getMaxLogSize());
		configurationUpdate.setContactEmail(new String[] { contactEmail });
		configurationUpdate.setSamlEnabled(configuration.isSamlEnabled());
		configurationUpdate.setSmtpConfiguration(smtpConfiguration);
		configurationService.updateConfiguration(configurationUpdate);
	}

	public void saveWebKeySettings() {
		String configurationDn = configurationFactory.getConfigurationDn();
		ldapOxAuthConfiguration = organizationService.getOxAuthSetting(configurationDn);
		WebKeysSettings oldwebKeysSettings = ldapOxAuthConfiguration.getOxWebKeysSettings();
		if ((oldwebKeysSettings != null) && !oldwebKeysSettings.equals(webKeysSettings)) {
			webKeysSettings.setUpdateAt(new Date());
			ldapOxAuthConfiguration.setOxWebKeysSettings(webKeysSettings);
			organizationService.saveLdapOxAuthConfiguration(ldapOxAuthConfiguration);
		}
	}

	public String verifySmtpConfiguration() {
		if (StringUtils.isNotEmpty(smtpPasswordDecrypted)) {
			smtpConfiguration.setPasswordDecrypted(smtpPasswordDecrypted);
		}
		configurationService.encryptSmtpPassword(smtpConfiguration);

		if (StringUtils.isNotEmpty(keyStorePasswordDecrypted)) {
			smtpConfiguration.setKeyStorePasswordDecrypted(keyStorePasswordDecrypted);
		}
		configurationService.encryptKeyStorePassword(smtpConfiguration);

		boolean result = false;

		String keystoreFile = smtpConfiguration.getKeyStore();
		String keystoreSecret = smtpConfiguration.getKeyStorePasswordDecrypted();
		String kjeyStoreAlias = smtpConfiguration.getKeyStoreAlias();

		if (StringUtils.isNotEmpty(keystoreFile) &&
				StringUtils.isNotEmpty(keystoreSecret) &&
				StringUtils.isNotEmpty(kjeyStoreAlias)) {
			result = mailService.sendMailSigned(smtpConfiguration, smtpConfiguration.getFromEmailAddress(),
					smtpConfiguration.getFromName(), smtpConfiguration.getFromEmailAddress(), null,
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.subject']}"),
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.plain.body']}"),
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.html.body']}"));
		}
		else {
			result = mailService.sendMail(smtpConfiguration, smtpConfiguration.getFromEmailAddress(),
					smtpConfiguration.getFromName(), smtpConfiguration.getFromEmailAddress(), null,
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.subject']}"),
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.plain.body']}"),
					facesMessages.evalResourceAsString("#{msgs['mail.verify.message.html.body']}"));
		}
		if (result) {
			log.info("Connection Successful");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "SMTP Test succeeded!");
			return OxTrustConstants.RESULT_SUCCESS;
		}
		facesMessages.add(FacesMessage.SEVERITY_ERROR, "SMTP Test Failed");
		return OxTrustConstants.RESULT_FAILURE;
	}

	private String modifyApplliance() {
		if (this.configuration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			this.configuration = configurationService.getConfiguration();
			if (ArrayHelper.isEmpty(configuration.getContactEmail())) {
				this.contactEmail = DEFAULT_CONTACT_EMAIL;
			} else {
				this.contactEmail = configuration.getContactEmail()[0];
			}
			if (this.configuration == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}
			this.smtpConfiguration = this.configuration.getSmtpConfiguration();
			if (this.smtpConfiguration == null) {
				this.smtpConfiguration = new SmtpConfiguration();
				this.configuration.setSmtpConfiguration(smtpConfiguration);
			}
			configurationService.decryptSmtpPassword(smtpConfiguration);
			configurationService.decryptKeyStorePassword(smtpConfiguration);
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("an error occured", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	private void setCustomMessages() {
		String[][] customMessages = { { OxTrustConstants.CUSTOM_MESSAGE_LOGIN_PAGE, loginPageCustomMessage },
				{ OxTrustConstants.CUSTOM_MESSAGE_WELCOME_PAGE, welcomePageCustomMessage },
				{ OxTrustConstants.CUSTOM_MESSAGE_TITLE_TEXT, welcomeTitleText } };
		String[] customMessagesArray = organizationService.buildOrganizationCustomMessages(customMessages);
		if (customMessagesArray.length > 0) {
			this.organization.setCustomMessages(customMessagesArray);
		} else {
			this.organization.setCustomMessages(null);
		}
	}

	public String cancel() throws Exception {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Organization configuration not updated");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public void setOxTrustLogoImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveLogo(uploadedFile);
	}

	public void setOxTrustFaviconImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveFavIcon(uploadedFile);
	}

	public void setOxAuthLogoImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveOxAuthLogo(uploadedFile);
	}

	public void setOxAuthFaviconImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveOxAuthFavIcon(uploadedFile);
	}

	public void setIdpLogoImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveIdpLogo(uploadedFile);
	}

	public void setdpFaviconImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		saveIdpFavIcon(uploadedFile);
	}

	public void addNewOxtrustLib(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		addLib(uploadedFile, true);
	}

	public void addNewOxauthLib(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		addLib(uploadedFile, false);
	}

	private void addLib(UploadedFile uploadedFile, boolean isOxTrust) {
		String LIB_PATH = "/opt/gluu/jetty/identity/custom/libs/";
		String XML_PATH = "/opt/gluu/jetty/identity/webapps/identity.xml";
		String BASE_FILE_NAME = LIB_PATH;
		if (!isOxTrust) {
			LIB_PATH = "/opt/gluu/jetty/oxauth/custom/libs/";
			XML_PATH = "/opt/gluu/jetty/oxauth/webapps/oxauth.xml";
			BASE_FILE_NAME = LIB_PATH;
		}
		String fileName = saveFile(uploadedFile, LIB_PATH);
		BASE_FILE_NAME=BASE_FILE_NAME+fileName;
		boolean result = updateXml(XML_PATH, isOxTrust, BASE_FILE_NAME);
		if (result) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Library " + fileName + " added");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error encountered while adding " + fileName + " library");
		}
	}

	private boolean updateXml(String XML_PATH, boolean isOxTrust, String fileName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(XML_PATH));
			document.getDocumentElement().normalize();
			NodeList configures = document.getElementsByTagName("Configure");
			Element configure = (Element) configures.item(0);
			Element library = document.createElement("Set");
			library.setAttribute("name", "extraClasspath");
			library.appendChild(document.createTextNode(fileName));
			configure.appendChild(library);
			document.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			File file = new File(XML_PATH);
			file.setWritable(true);
			StreamResult result = new StreamResult(file);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);
			return true;
		} catch (Exception e) {
			log.info("=========" + e);
			return false;
		}
	}

	private void saveLogo(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, LogoImageServlet.BASE_OXTRUST_LOGO_PATH);
		if (fileName != null) {
			this.organization.setOxTrustLogoPath(LogoImageServlet.BASE_OXTRUST_LOGO_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrust logo loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading oxTrust logo");
		}
	}

	private void saveFavIcon(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, FaviconImageServlet.BASE_OXTRUST_FAVICON_PATH);
		if (fileName != null) {
			this.organization.setOxTrustFaviconPath(FaviconImageServlet.BASE_OXTRUST_FAVICON_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxTrust favicon loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading oxTrust favicon");
		}
	}

	private void saveOxAuthLogo(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, OxAuthLogoServlet.BASE_OXAUTH_LOGO_PATH);
		if (fileName != null) {
			this.organization.setOxAuthLogoPath(OxAuthLogoServlet.BASE_OXAUTH_LOGO_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxAuth logo loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading oxAuth logo");
		}
	}

	private void saveOxAuthFavIcon(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, OxAuthFaviconServlet.BASE_OXAUTH_FAVICON_PATH);
		if (fileName != null) {
			this.organization.setOxAuthFaviconPath(OxAuthFaviconServlet.BASE_OXAUTH_FAVICON_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "oxAuth favicon loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading oxAuth favicon");
		}
	}

	private void saveIdpLogo(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, IdpLogoServlet.BASE_IDP_LOGO_PATH);
		if (fileName != null) {
			this.organization.setIdpLogoPath(IdpLogoServlet.BASE_IDP_LOGO_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "IDP logo loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading IDP logo");
		}
	}

	private void saveIdpFavIcon(UploadedFile uploadedFile) {
		String fileName = saveFile(uploadedFile, IdpFaviconServlet.BASE_IDP_FAVICON_PATH);
		if (fileName != null) {
			this.organization.setIdpFaviconPath(IdpFaviconServlet.BASE_IDP_FAVICON_PATH + fileName);
			this.organizationService.updateOrganization(this.organization);
			facesMessages.add(FacesMessage.SEVERITY_INFO, "IDP favicon loaded successfully");
			facesMessages.add(FacesMessage.SEVERITY_WARN,
					THE_CHANGE_MAY_TAKE_UP_TO_30MIN_TO_BE_EFFECTIVE_DUE_TO_CACHING);
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error loading IDP favicon");
		}
	}

	private String saveFile(UploadedFile uploadedFile, String basePath) {
		String fileName = uploadedFile.getFileName();
		try {
			File logo = new File(basePath, fileName);
			if (!logo.exists()) {
				File dir = new File(basePath);
				if (!dir.exists()) {
					dir.mkdir();
				}
				logo.createNewFile();
				logo = new File(basePath, fileName);
			}
			Files.copy(uploadedFile.getInputStream(), logo.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			log.debug("Error loading custom idp favicon", ex);
			return null;
		}finally {
			uploadedFile=null;
		}
		return fileName;
	}

	public void removeThemeColor() {
		this.organization.setThemeColor(null);
	}

	public GluuOrganization getOrganization() {
		return organization;
	}

	@PreDestroy
	public void destroy() throws Exception {
		cancel();
	}

	public String getLoginPageCustomMessage() {
		return loginPageCustomMessage;
	}

	public void setLoginPageCustomMessage(String loginPageCustomMessage) {
		this.loginPageCustomMessage = loginPageCustomMessage;
	}

	public String getWelcomePageCustomMessage() {
		return welcomePageCustomMessage;
	}

	public void setWelcomePageCustomMessage(String welcomePageCustomMessage) {
		this.welcomePageCustomMessage = welcomePageCustomMessage;
	}
	public String getWelcomeTitleText() {
		return welcomeTitleText;
	}

	public void setWelcomeTitleText(String welcomeTitleText) {
		this.welcomeTitleText = welcomeTitleText;
	}

	public String getSmtpPasswordDecrypted() {
		return smtpPasswordDecrypted;
	}

	public void setSmtpPasswordDecrypted(String smtpPasswordDecrypted) {
		this.smtpPasswordDecrypted = smtpPasswordDecrypted;
	}

	public String getKeyStorePasswordDecrypted() {
		return keyStorePasswordDecrypted;
	}

	public void setKeyStorePasswordDecrypted(String keyStorePasswordDecrypted) {
		this.keyStorePasswordDecrypted = keyStorePasswordDecrypted;
	}

	public GluuConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * @return the configurations
	 */
	public List<GluuConfiguration> getConfigurations() {
		return configurations;
	}

	/**
	 * @param configurations
	 *            the configurations to set
	 */
	public void setConfigurations(List<GluuConfiguration> configurations) {
		this.configurations = configurations;
	}

	public WebKeysSettings getWebKeysSettings() {
		return webKeysSettings;
	}

	public void setWebKeysSettings(WebKeysSettings webKeysSettings) {
		this.webKeysSettings = webKeysSettings;
	}

	public SmtpConfiguration getSmtpConfiguration() {
		return smtpConfiguration;
	}

	public String getRandonRnd() {
		return "&rnd=" + UUID.randomUUID().toString().substring(0, 8);
	}

}
