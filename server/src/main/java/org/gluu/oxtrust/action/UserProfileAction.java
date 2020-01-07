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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuIMAPData;
import org.gluu.model.GluuImage;
import org.gluu.model.GluuUserRole;
import org.gluu.model.ImapPassword;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.ldap.service.DataSourceTypeService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.ImapDataService;
import org.gluu.oxtrust.ldap.service.OxTrustAuditService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

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
	private PersonService personService;

	@Inject
	private AttributeService attributeService;

	@Inject
	private ImageService imageService;

	@Inject
	private CustomAttributeAction customAttributeAction;

	@Inject
	private UserPasswordAction userPasswordAction;

	@Inject
	private ImapDataService imapDataService;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private Identity identity;

	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@Inject
	private ExternalUpdateUserService externalUpdateUserService;

	@Inject
	private DataSourceTypeService dataSourceTypeService;

	private GluuCustomPerson person;

	private boolean isEditable;

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
		if (this.person.getGluuIMAPData() != null) {
			this.imapData = imapDataService.getGluuIMAPDataFromJson(this.person.getGluuIMAPData());
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() {
		try {
			if (appConfiguration.getEnforceEmailUniqueness()
					&& !dataSourceTypeService.isLDAP(personService.getDnForPerson(null))) {
				if (!userEmailIsUniqAtEditionTime(this.person.getAttribute("mail"))) {
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"#{msg['UpdatePersonAction.faileUpdateUserMailidExist']} %s", person.getMail());
					return OxTrustConstants.RESULT_FAILURE;
				}
			}
			if (this.imapData != null) {
				List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
				for (GluuCustomAttribute gluuCustomAttribute : customAttributes) {
					if (gluuCustomAttribute.getName().equals("gluuIMAPData")) {
						gluuCustomAttribute.setValue(imapDataService.getJsonStringFromImap(this.imapData));
						break;
					}
				}
			}

			GluuCustomPerson person = this.person;
			person.setGluuOptOuts(optOuts.size() == 0 ? null : optOuts);
			boolean runScript = externalUpdateUserService.isEnabled();
			if (runScript) {
				externalUpdateUserService.executeExternalUpdateUserMethods(this.person);
			}
			personService.updatePerson(this.person);
			oxTrustAuditService.audit(
					this.person.getInum() + " **" + this.person.getDisplayName() + "** PROFILE UPDATED",
					identity.getUser(),
					(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
			if (runScript) {
				externalUpdateUserService.executeExternalPostUpdateUserMethods(this.person);
			}
		} catch (DuplicateEmailException ex) {
			log.error("Failed to update profile {}", person.getInum(), ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, ex.getMessage());
			return OxTrustConstants.RESULT_FAILURE;
		} catch (BasePersistenceException ex) {
			log.error("Failed to update profile {}", person.getInum(), ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to update profile '#{userProfileAction.person.displayName}'");
			return OxTrustConstants.RESULT_FAILURE;
		} catch (Exception ex) {
			log.error("Failed to update profile {}", person.getInum(), ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to update profile '#{userProfileAction.person.displayName}'");
			return OxTrustConstants.RESULT_FAILURE;
		}

		customAttributeAction.savePhotos();

		facesMessages.add(FacesMessage.SEVERITY_INFO,
				"Profile '#{userProfileAction.person.displayName}' updated successfully");

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
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
		List<String> origins = Arrays.asList(tabName);
		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();
		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins,
				appConfiguration.getPersonObjectClassTypes(), appConfiguration.getPersonObjectClassDisplayNames());
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
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
		GluuAttribute photoAttribute = attributeService.getAttributeByName("photo1", attributes);
		GluuCustomAttribute customAttribute = new GluuCustomAttribute("photo1", this.person.getAttribute("photo1"));
		customAttribute.setMetadata(photoAttribute);
		GluuImage image = imageService.getImage(customAttribute);
		if (image == null) {
			return imageService.getBlankPhotoData();
		}
		return imageService.getThumImageData(image);
	}

	public boolean userEmailIsUniqAtEditionTime(String email) {
		boolean emailIsUniq = false;
		List<GluuCustomPerson> gluuCustomPersons = personService.getPersonsByEmail(email);
		if (gluuCustomPersons == null || gluuCustomPersons.isEmpty()) {
			emailIsUniq = true;
		}
		if (gluuCustomPersons.size() == 1 && gluuCustomPersons.get(0).getAttribute("mail").equalsIgnoreCase(email)
				&& gluuCustomPersons.get(0).getInum().equalsIgnoreCase(this.person.getInum())) {
			emailIsUniq = true;
		}
		return emailIsUniq;
	}

	public boolean isEditable() {
		this.isEditable = configurationService.getConfiguration().isProfileManagment();
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
}
