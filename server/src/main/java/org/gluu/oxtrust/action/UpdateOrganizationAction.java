/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import org.apache.commons.beanutils.PropertyUtils;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshConfiguration;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.MailUtils;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Version;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.richfaces.event.DropEvent;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.GluuImage;
import org.xdi.model.SmtpConfiguration;
import org.xdi.util.FileUtil;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for configuring application
 * 
 * @author Yuriy Movchan Date: 11.16.2010
 */
@Name("updateOrganizationAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UpdateOrganizationAction implements Serializable {

	private static final long serialVersionUID = -4470460481895022468L;

	@Logger
	private Log log;

	@In
	private StatusMessages statusMessages;

	@In
	private GluuCustomPerson currentPerson;

	@In
	private ImageService imageService;

	@In
	private OrganizationService organizationService;

	@In
	private ApplianceService applianceService;

	@In(required = false)
	private CacheRefreshConfiguration cacheRefreshConfiguration;

	@In
	private FacesMessages facesMessages;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private GluuOrganization organization;

	protected GluuImage oldLogoImage, curLogoImage;
	protected String loginPageCustomMessage;
	protected String welcomePageCustomMessage;
	protected String welcomeTitleText;

	private String buildDate;
	private String buildNumber;

	private GluuImage curFaviconImage, oldFaviconImage;

	private GluuAppliance appliance;

	private boolean cacheRefreshEnabled;
	
	private List<GluuAppliance> clusterPartners;
	private String clusterType;
	private String memcachedServerAddress;
	
	private List<GluuAppliance> appliances;

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String modify()  {
		String resultOrganization = modifyOrganization();
		String resultApplliance = modifyApplliance();

		if (StringHelper.equals(OxTrustConstants.RESULT_SUCCESS, resultOrganization)
				&& StringHelper.equals(OxTrustConstants.RESULT_SUCCESS, resultApplliance)) {
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	private String modifyOrganization()  {
		if (this.organization != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			GluuOrganization tmpOrganization = organizationService.getOrganization();
			this.organization = new GluuOrganization();

			// Clone shared instance
			try {
				PropertyUtils.copyProperties(this.organization, tmpOrganization);
			} catch (Exception ex) {
				log.error("Failed to load organization", ex);
				this.organization = null;
			}
		} catch (LdapMappingException ex) {
			log.error("Failed to load organization", ex);
		}

		if (this.organization == null) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		initLogoImage();
		initFaviconImage();

		this.loginPageCustomMessage = organizationService.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_LOGIN_PAGE);
		this.welcomePageCustomMessage = organizationService.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_WELCOME_PAGE);
		this.welcomeTitleText = organizationService.getOrganizationCustomMessage(OxTrustConstants.CUSTOM_MESSAGE_TITLE_TEXT);

		this.cacheRefreshEnabled = applicationConfiguration.isCacheRefreshEnabled() && (cacheRefreshConfiguration != null);

		appliances = new ArrayList<GluuAppliance>();
		try {
			appliances.addAll(ApplianceService.instance().getAppliances());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clusterPartners = new ArrayList<GluuAppliance>();
		
		
		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * 
	 */
//	private void initClusterConfig() {
//		clusterType = appliance.getClusterType();
//		memcachedServerAddress = appliance.getMemcachedServerAddress();
//		List<String> oxClusterPartners = appliance.getOxClusterPartners();
//		clusterPartners = new ArrayList<SimpleCustomProperty>();
//		if(oxClusterPartners != null && !oxClusterPartners.isEmpty()){
//			for(String partner : oxClusterPartners){
//				if(partner != null && ! partner.isEmpty()){
//					SimpleCustomProperty partnerProperty = new SimpleCustomProperty(partner, "");
//					clusterPartners.add(partnerProperty);
//				}
//			}
//		}
//		
//	}

	public String clusterUpdate(DropEvent event){
		if("Cluster".equals(event.getDropValue()) && ! clusterPartners.contains((GluuAppliance) event.getDragValue())){
			appliances.remove((GluuAppliance) event.getDragValue());
			clusterPartners.add((GluuAppliance) event.getDragValue());
		}else if("Standalone".equals(event.getDropValue()) && ! appliances.contains((GluuAppliance) event.getDragValue())){
			clusterPartners.remove((GluuAppliance) event.getDragValue());
			appliances.add((GluuAppliance) event.getDragValue());
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}
	private void initLogoImage() {
		this.oldLogoImage = imageService.getGluuImageFromXML(this.organization.getLogoImage());
		if (this.oldLogoImage != null) {
			this.oldLogoImage.setLogo(true);
		}
		this.curLogoImage = this.oldLogoImage;
	}

	private void initFaviconImage() {
		this.oldFaviconImage = imageService.getGluuImageFromXML(this.organization.getFaviconImage());
		this.curFaviconImage = this.oldFaviconImage;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String save() {
		// Update organization
		try {
			saveLogoImage();
			saveFaviconImage();

			setCustomMessages();
//			saveClusterConfiguration();
			organizationService.updateOrganization(this.organization);

			appliance.setClusterType(clusterType);
			List<String> oxClusterPartners = new ArrayList<String>();
			for(GluuAppliance partner: clusterPartners){
				oxClusterPartners.add(partner.getInum());
			}
			appliance.setOxClusterPartners(oxClusterPartners);
			appliance.setMemcachedServerAddress(memcachedServerAddress);
			
			updateSmptConfiguration(this.appliance);
			
			applianceService.updateAppliance(this.appliance);

			Events.instance().raiseEvent(OxTrustConstants.EVENT_CLEAR_ORGANIZATION);

			/* Resolv.conf update */
			// saveDnsInformation(); // This will be handled by puppet.
			/* Resolv.conf update */
		} catch (LdapMappingException ex) {
			log.error("Failed to update organization", ex);
			facesMessages.add(Severity.ERROR, "Failed to update organization");
			return OxTrustConstants.RESULT_FAILURE;
		}

		return modify();
	}

	private void updateSmptConfiguration(GluuAppliance appliance) {
		SmtpConfiguration smtpConfiguration = new SmtpConfiguration();
		smtpConfiguration.setHost(appliance.getSmtpHost());
		smtpConfiguration.setPort(StringHelper.toInteger(appliance.getSmtpPort(), 25));
		smtpConfiguration.setRequiresSsl(StringHelper.toBoolean(appliance.getSmtpRequiresSsl(), false));
		smtpConfiguration.setFromName(appliance.getSmtpFromName());
		smtpConfiguration.setFromEmailAddress(appliance.getSmtpFromEmailAddress());
		smtpConfiguration.setRequiresAuthentication(StringHelper.toBoolean(appliance.getSmtpRequiresAuthentication(), false));
		smtpConfiguration.setUserName(appliance.getSmtpUserName());
		smtpConfiguration.setPassword(appliance.getSmtpPassword());
		
		appliance.setSmtpConfiguration(smtpConfiguration);
	}

	/**
	 * 
	 */
//	private void saveClusterConfiguration() {
//		appliance.setClusterType(clusterType);
//		appliance.setMemcachedServerAddress(memcachedServerAddress);
//		List<String> applianceClusterPartners = new ArrayList<String>();
//		if(! clusterPartners.isEmpty()){
//			for(SimpleCustomProperty property: clusterPartners){
//				if(property.getValue1() != null && ! property.getValue1().isEmpty()){
//					applianceClusterPartners.add(property.getValue1());
//				}
//			}
//		}
//		appliance.setOxClusterPartners(applianceClusterPartners);
//	}

	// Resolv.conf file update will be handled by puppet.
	private void saveDnsInformation() {
		if (Util.empty(appliance.getApplianceDnsServer())) {
			appliance.setApplianceDnsServer("");
		}
		String filePath = "/etc/resolv.conf";
		FileUtil fileUtil = new FileUtil();
		String startTag = "##CONFIGURED-FROM-APPLIANCE-START##";
		String endTag = "##CONFIGURED-FROM-APPLIANCE-END##";
		String fileContent = FileUtil.readFile(filePath).toString();

		int startTagPositon = fileContent.indexOf(startTag);
		int endTagPositon = fileContent.indexOf(endTag);

		if (startTagPositon >= 0 && endTagPositon >= 0) {
			String startContent = fileContent.substring(0, startTagPositon - 1);
			String endContent = fileContent.substring(endTagPositon + endTag.length());
			fileContent = startContent + endContent;
		}

		fileContent = fileContent.replace(startTag, "");
		fileContent = fileContent.replace(endTag, "");

		String[] entries = appliance.getApplianceDnsServer().split(",");
		String servers = "";
		if (entries != null && entries.length > 0) {
			for (int i = 0; i < entries.length; i++) {
				entries[i] = entries[i].trim();
				servers += "\n" + entries[i];
			}
		}

		fileContent += "\n" + startTag + servers + "\n" + endTag;

		boolean success = fileUtil.writeLargeFile(filePath, 0, fileContent);
		if (!success) {
			log.error("Failed to update resolv.conf");
			facesMessages
					.add(Severity.ERROR,
							"Failed to update DNS Server information in /etc/resolv.conf. Please make sure that you have the permission to modify that file.");
		}
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String verifySmtpConfiguration() {
		log.info("HostName: " + appliance.getSmtpHost() + " Port: " + appliance.getSmtpPort() + " RequireSSL: " + appliance.isRequiresSsl()
				+ " RequireSSL: " + appliance.isRequiresAuthentication());
		log.info("UserName: " + appliance.getSmtpUserName() + " Password: " + appliance.getSmtpPasswordStr());

		try {
			MailUtils mail = new MailUtils(appliance.getSmtpHost(), appliance.getSmtpPort(), appliance.isRequiresSsl(),
					appliance.isRequiresAuthentication(), appliance.getSmtpUserName(), appliance.getSmtpPasswordStr());
			mail.sendMail(appliance.getSmtpFromName() + " <" + appliance.getSmtpFromEmailAddress() + ">",
					appliance.getSmtpFromEmailAddress(), "SMTP Server Configuration Verification",
					"SMTP Server Configuration Verification Successful.");
		} catch (AuthenticationFailedException ex) {
			log.error("SMTP Authentication Error: ", ex);
			return OxTrustConstants.RESULT_FAILURE;
		} catch (MessagingException ex) {
			log.error("SMTP Host Connection Error", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		log.info("Connection Successful");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String modifyApplliance() {
		if (this.appliance != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			this.appliance = applianceService.getAppliance();

			if (this.appliance == null) {
				return OxTrustConstants.RESULT_FAILURE;
			}

//			initClusterConfig();
			clusterType = appliance.getClusterType();
			memcachedServerAddress = appliance.getMemcachedServerAddress();
			clusterPartners = new ArrayList<GluuAppliance>();
			List<String> oxClusterPartners = appliance.getOxClusterPartners();
			if(oxClusterPartners != null){
				for(String partner: oxClusterPartners){
					clusterPartners.add(applianceService.getApplianceByInum(partner));
				}
			}else{
				clusterPartners.add(appliance);
			}
 
			appliances.removeAll(clusterPartners);
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

	public boolean isCacheRefreshEnabled() {
		return cacheRefreshEnabled;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String getBuildDate() {
		if (this.buildDate != null) {
			return this.buildDate;
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMddhhmm");
		try {
			final Date date = formatter.parse(Version.GLUU_BUILD_DATE);
			this.buildDate = new SimpleDateFormat("hh:mm MMM dd yyyy").format(date) + " UTC";
		} catch (ParseException e) {
			log.error("Error formating date. Build process is invalid.", e);

		}
		return this.buildDate;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String getBuildNumber() {
		if (this.buildNumber != null) {
			return this.buildNumber;
		}

		this.buildNumber = Version.GLUU_HUDSON_BUILDNO;
		return this.buildNumber;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() throws Exception {
		cancelLogoImage();
		cancelFaviconImage();
	}

	public void setCustLogoImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getUploadedFile();
		try {
			setCustLogoImageImpl(uploadedFile);
		} finally {
			try {
				uploadedFile.delete();
			} catch (IOException ex) {
				log.error("Failed to remove temporary image", ex);
			}
		}
	}

	private void setCustLogoImageImpl(UploadedFile uploadedFile) {
		removeLogoImage();

		GluuImage newLogoImage = imageService.constructImage(currentPerson, uploadedFile);
		newLogoImage.setStoreTemporary(true);
		newLogoImage.setLogo(true);
		try {
			if (imageService.createImageFiles(newLogoImage)) {
				this.curLogoImage = newLogoImage;
			}

			this.organization.setLogoImage(imageService.getXMLFromGluuImage(newLogoImage));
		} catch (Exception ex) {
			log.error("Failed to store icon image: '{0}'", ex, newLogoImage);
		}
	}

	public boolean isCustLogoImageExist() {
		return this.curLogoImage != null;
	}

	public void removeLogoImage() {
		cancelLogoImage();

		this.curLogoImage = null;
		this.organization.setLogoImage(null);
	}

	public void cancelLogoImage() {
		if ((this.curLogoImage != null) && this.curLogoImage.isStoreTemporary()) {
			try {
				imageService.deleteImage(this.curLogoImage);
			} catch (Exception ex) {
				log.error("Failed to delete temporary icon image: '{0}'", ex, this.curLogoImage);
			}
		}
	}

	public byte[] getLogoImageThumbData() throws Exception {
		if (this.curLogoImage != null) {
			return imageService.getThumImageData(this.curLogoImage);
		}

		return imageService.getBlankImageData();
	}

	public String getLogoImageSourceName() {
		if (this.curLogoImage != null) {
			return this.curLogoImage.getSourceName();
		}

		return null;
	}

	public void saveLogoImage() {
		// Remove old logo image if user upload new logo
		if ((this.oldLogoImage != null)
				&& ((this.curLogoImage == null) || !this.oldLogoImage.getUuid().equals(this.curLogoImage.getUuid()))) {
			try {
				imageService.deleteImage(this.oldLogoImage);
			} catch (Exception ex) {
				log.error("Failed to remove old icon image: '{0}'", ex, this.oldLogoImage);
			}
		}

		// Move added photo to persistent location
		if ((this.curLogoImage != null) && this.curLogoImage.isStoreTemporary()) {
			try {
				imageService.moveLogoImageToPersistentStore(this.curLogoImage);
				this.organization.setLogoImage(imageService.getXMLFromGluuImage(curLogoImage));
			} catch (Exception ex) {
				log.error("Failed to move new icon image to persistence store: '{0}'", ex, this.curLogoImage);
			}
		}

		this.oldLogoImage = this.curLogoImage;
	}

	public void setFaviconImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getUploadedFile();
		try {
			setFaviconImageImpl(uploadedFile);
		} finally {
			try {
				uploadedFile.delete();
			} catch (IOException ex) {
				log.error("Failed to remove temporary image", ex);
			}
		}
	}

	public void setFaviconImageImpl(UploadedFile uploadedFile) {
		removeFaviconImage();

		GluuImage newFaviconImage = imageService.constructImage(currentPerson, uploadedFile);
		newFaviconImage.setStoreTemporary(true);
		newFaviconImage.setLogo(false);
		try {
			if (imageService.createFaviconImageFiles(newFaviconImage)) {
				this.curFaviconImage = newFaviconImage;
			}

			this.organization.setFaviconImage(imageService.getXMLFromGluuImage(newFaviconImage));
		} catch (Exception ex) {
			log.error("Failed to store favicon image: '{0}'", ex, newFaviconImage);
		}
	}

	public boolean isFaviconImageExist() {
		return this.curFaviconImage != null;
	}

	public void removeFaviconImage() {
		cancelFaviconImage();

		this.curFaviconImage = null;
		this.organization.setFaviconImage(null);
	}

	public void cancelFaviconImage() {
		if ((this.curFaviconImage != null) && this.curFaviconImage.isStoreTemporary()) {
			try {
				imageService.deleteImage(this.curFaviconImage);
			} catch (Exception ex) {
				log.error("Failed to delete temporary favicon image: '{0}'", ex, this.curFaviconImage);
			}
		}
	}

	public byte[] getFaviconImage() throws Exception {
		if (this.curFaviconImage != null) {
			return imageService.getThumImageData(this.curFaviconImage);
		}

		return imageService.getBlankImageData();
	}

	public String getFaviconImageSourceName() {
		if (this.curFaviconImage != null) {
			return this.curFaviconImage.getSourceName();
		}

		return null;
	}

	public void saveFaviconImage() {
		// Remove old favicon image if user upload new image
		if ((this.oldFaviconImage != null)
				&& ((this.curFaviconImage == null) || !this.oldFaviconImage.getUuid().equals(this.curFaviconImage.getUuid()))) {
			try {
				imageService.deleteImage(this.oldFaviconImage);
			} catch (Exception ex) {
				log.error("Failed to remove old favicon image: '{0}'", ex, this.oldFaviconImage);
			}
		}

		// Move added photo to persistent location
		if ((this.curFaviconImage != null) && this.curFaviconImage.isStoreTemporary()) {
			try {
				imageService.moveImageToPersistentStore(this.curFaviconImage);
			} catch (Exception ex) {
				log.error("Failed to move new favicon image to persistence store: '{0}'", ex, this.curFaviconImage);
			}
		}

		this.oldFaviconImage = this.curFaviconImage;
	}

	public void removeThemeColor() {
		this.organization.setThemeColor(null);
	}

	public GluuOrganization getOrganization() {
		return organization;
	}

	@Destroy
	public void destroy() throws Exception {
		// When user decided to leave form without saving we must remove added
		// logo image from disk
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

	public GluuAppliance getAppliance() {
		return this.appliance;
	}

	/**
	 * @return the clusterPartners
	 */
	public List<GluuAppliance> getClusterPartners() {
		return clusterPartners;
	}

	/**
	 * @param clusterPartners the clusterPartners to set
	 */
	public void setClusterPartners(List<GluuAppliance> clusterPartners) {
		this.clusterPartners = clusterPartners;
	}

	/**
	 * @return the clusterType
	 */
	public String getClusterType() {
		return clusterType;
	}

	/**
	 * @param clusterType the clusterType to set
	 */
	public void setClusterType(String clusterType) {
		this.clusterType = clusterType;
	}

	/**
	 * @return the memcachedServerAddress
	 */
	public String getMemcachedServerAddress() {
		return memcachedServerAddress;
	}

	/**
	 * @param memcachedServerAddress the memcachedServerAddress to set
	 */
	public void setMemcachedServerAddress(String memcachedServerAddress) {
		this.memcachedServerAddress = memcachedServerAddress;
	}

	/**
	 * @return the appliances
	 */
	public List<GluuAppliance> getAppliances() {
		return appliances;
	}

	/**
	 * @param appliances the appliances to set
	 */
	public void setAppliances(List<GluuAppliance> appliances) {
		this.appliances = appliances;
	}
}
