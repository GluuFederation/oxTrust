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

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuImage;
import org.xdi.model.GluuUserRole;

/**
 * Action class for view and update profile actions.
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Name("userProfileAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UserProfileAction implements Serializable {

	private static final long serialVersionUID = -8238855019631152823L;

	private static final String tabName = "Attributes";

	@Logger
	private Log log;

	@In
	private PersonService personService;

	@In
	private AttributeService attributeService;

	@In
	private ImageService imageService;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private CustomAttributeAction customAttributeAction;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private UserPasswordAction userPasswordAction;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private WhitePagesAction whitePagesAction;

	@In
	private GluuCustomPerson currentPerson;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private GluuCustomPerson person;

	private List<String> optOuts;

	private static final String photoAttributes[][] = new String[][] { { "gluuPerson", "photo1" }, };

	@Restrict("#{s:hasPermission('profile', 'access')}")
	public String show() {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.person = personService.getPersonByInum(currentPerson.getInum());
		} catch (LdapMappingException ex) {
			log.error("Failed to find person {0}", ex, currentPerson.getInum());
		}

		if (this.person == null) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		initAttributes();
		addOpts();
		addPhotoAttribute();
		userPasswordAction.setPerson(this.person);

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('profile', 'access')}")
	public String update() {
		try {
			GluuCustomPerson person = this.person;
			// TODO: Reffactor
			person.setGluuOptOuts(optOuts.size() == 0 ? null : optOuts);
			personService.updatePerson(person);
		} catch (LdapMappingException ex) {
			log.error("Failed to update profile {0}", ex, person.getInum());
			return OxTrustConstants.RESULT_FAILURE;
		}

		customAttributeAction.savePhotos();

		return OxTrustConstants.RESULT_SUCCESS;
	}

//	@Restrict("#{s:hasPermission('person', 'access')}")
	public void cancel() {
	}

	private void initAttributes() {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
		List<String> origins = Arrays.asList(tabName);

		List<GluuCustomAttribute> customAttributes = this.person.getCustomAttributes();

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins, applicationConfiguration
				.getPersonObjectClassTypes(), applicationConfiguration.getPersonObjectClassDisplayNames());
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
}
