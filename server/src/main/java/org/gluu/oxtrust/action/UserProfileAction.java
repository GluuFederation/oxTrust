/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.ImapDataService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuIMAPData;
import org.xdi.model.GluuImage;
import org.xdi.model.ImapPassword;
import org.xdi.model.user.UserRole;
import org.xdi.service.security.Secure;

/**
 * Action class for view and update profile actions.
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Named("userProfileAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('profile', 'access')}")
public class UserProfileAction implements Serializable {

	private static final long serialVersionUID = -8238855019631152823L;

	private static final String tabName = "Attributes";

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private IPersonService personService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private ImageService imageService;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private UserPasswordAction userPasswordAction;

	@Inject
	private WhitePagesAction whitePagesAction;
	
	@Inject
	private ImapDataService imapDataService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private Identity identity;

	@Inject
	private ExternalUpdateUserService externalUpdateUserService;

	private GluuCustomPerson person;

	private List<String> optOuts;
	
	
	private GluuIMAPData imapData;

    public GluuIMAPData getImapData() {
		return imapData;
	}

	public void setImapData(GluuIMAPData imapData) {
		this.imapData = imapData;
	}
	
	@PostConstruct
	public void init() {
		this.imapData = new GluuIMAPData();
		this.imapData.setImapPassword(new ImapPassword());
		
	}

	private static final String photoAttributes[][] = new String[][] { { "gluuPerson", "photo1" }, };

	public String show() {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.person = identity.getUser();
		} catch (BasePersistenceException ex) {
			log.error("Failed to find person {}", identity.getUser().getInum(), ex);
		}

		if (this.person == null) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load profile");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		initAttributes();
		addOpts();
		addPhotoAttribute();
		userPasswordAction.setPerson(this.person);
		if(this.person.getGluuIMAPData() != null){
			this.imapData=imapDataService.getGluuIMAPDataFromJson(this.person.getGluuIMAPData());
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() {
		try {
			if (this.imapData != null) {
				List<GluuCustomAttribute> customAttributes = this.person
						.getCustomAttributes();
				for (GluuCustomAttribute gluuCustomAttribute : customAttributes) {
					if (gluuCustomAttribute.getName().equals("gluuIMAPData")) {
						gluuCustomAttribute.setValue(imapDataService
								.getJsonStringFromImap(this.imapData));
					}
				}
			}
			
			GluuCustomPerson person = this.person;
			// TODO: Reffactor
			person.setGluuOptOuts(optOuts.size() == 0 ? null : optOuts);

			boolean runScript = externalUpdateUserService.isEnabled();
			if (runScript) {
				externalUpdateUserService.executeExternalUpdateUserMethods(this.person);
			}
			personService.updatePerson(this.person);
			if (runScript) {
				externalUpdateUserService.executeExternalPostUpdateUserMethods(this.person);
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to update profile {}", person.getInum(), ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update profile '#{userProfileAction.person.displayName}'");

			return OxTrustConstants.RESULT_FAILURE;
		}

		customAttributeAction.savePhotos();

		facesMessages.add(FacesMessage.SEVERITY_INFO, "Profile '#{userProfileAction.person.displayName}' updated successfully");

		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public void removeImapData(String inum) {		
		this.imapData = null;
		customAttributeAction.removeCustomAttribute(inum);
	}

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Profile modification canceled");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initAttributes() {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(UserRole.USER);
		List<String> origins = Arrays.asList(tabName);

		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins, appConfiguration
				.getPersonObjectClassTypes(), appConfiguration.getPersonObjectClassDisplayNames());
	}

	public void addOpts() {
		optOuts = new ArrayList<String>();
		if (this.person.getGluuOptOuts() != null) {
			optOuts.addAll(this.person.getGluuOptOuts());
		}
	}

	private void addPhotoAttribute() {
		for (String[] photoAttribute : photoAttributes) {
			GluuAttribute attribute = customAttributeAction.getCustomAttribute(photoAttribute[0], photoAttribute[1]);
			if (attribute != null) {
				customAttributeAction.addCustomAttribute(attribute.getInum());
			}
		}
	}

	public GluuCustomPerson getPerson() {
		return person;
	}

	public void setPerson(GluuCustomPerson person) {
		this.person = person;
	}

	public List<GluuCustomAttribute> getMandatoryAttributes() {
		return personService.getMandatoryAtributes();
	}

	protected String getOriginForAttribute(GluuAttribute attribute) {
		return tabName;
	}

	protected boolean allowEditAttribute(GluuAttribute attribute) {
		return attribute.isUserCanEdit();
	}

	public boolean getAllowPublication() {
		return this.person.isAllowPublication();
	}

	public void setAllowPublication(boolean allowPublication) {
		this.person.setAllowPublication(allowPublication);
	}

	public void toggle(String attributeName) {
		if (optOuts.contains(attributeName)) {
			optOuts.remove(attributeName);
		} else {
			optOuts.add(attributeName);
		}
	}

	public boolean released(String attributeName) {
		return !optOuts.contains(attributeName);
	}

	public void configureListingOptions() {
		this.person.setGluuOptOuts(optOuts);
	}

	public byte[] getPhotoThumbData() {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(UserRole.USER);
		GluuAttribute photoAttribute = attributeService.getAttributeByName("photo1", attributes);
		GluuCustomAttribute customAttribute = new GluuCustomAttribute("photo1", this.person.getAttribute("photo1"));
		customAttribute.setMetadata(photoAttribute);
		GluuImage image = imageService.getImage(customAttribute);
		if (image == null) {
			return imageService.getBlankPhotoData();
		}
		return imageService.getThumImageData(image);
	}
}
