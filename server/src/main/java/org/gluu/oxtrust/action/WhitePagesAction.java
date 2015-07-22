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
import java.util.Set;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuImage;
import org.xdi.model.GluuUserRole;

/**
 * Action class for view white pages
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Name("whitePagesAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class WhitePagesAction implements Serializable {

	private static final long serialVersionUID = 6730313815008211305L;

	private static final String PHOTO_NAME = "photo1";

	private List<String> tableAttributes;

	@Logger
	private Log log;

	@In
	private FacesMessages facesMessages;

	@In
	private AttributeService attributeService;

	@In
	private ImageService imageService;

	@In
	private PersonService personService;

	private String tableState;

	private List<GluuCustomPerson> persons;
	private Set<Integer> selectedPersons;

	@Create
	public void init() {
		this.tableAttributes = Arrays.asList("cn", "photo1", "mail", "phone");
	}

	public String start() {
		if (persons != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		return search();
	}

	public String search() {
		try {
			GluuCustomPerson person = new GluuCustomPerson();
			person.setGluuAllowPublication("true");
			this.persons = personService.findPersons(person, 0);
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuCustomPerson> getPersons() {
		return persons;
	}

	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}

	public Set<Integer> getSelectedPersons() {
		return selectedPersons;
	}

	public void setSelectedPersons(Set<Integer> selectedPersons) {
		this.selectedPersons = selectedPersons;
	}

	public GluuCustomPerson getSelectedPerson() {
		if ((this.selectedPersons == null) || (this.selectedPersons.isEmpty())) {
			return null;
		} else {
			int index = this.selectedPersons.iterator().next();
			return this.persons.get(index);
		}
	}

	public byte[] getPhotoThumbData(GluuCustomPerson person) {
		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
		GluuAttribute photoAttribute = attributeService.getAttributeByName(PHOTO_NAME, attributes);
		GluuCustomAttribute customAttribute = new GluuCustomAttribute(PHOTO_NAME, person.getAttribute(PHOTO_NAME));
		customAttribute.setMetadata(photoAttribute);
		GluuImage image = imageService.getImage(customAttribute);
		if (image == null || (person.getGluuOptOuts() != null && person.getGluuOptOuts().contains(PHOTO_NAME))) {
			return imageService.getBlankPhotoData();
		}
		return imageService.getThumImageData(image);
	}

	public List<GluuCustomAttribute> getReleasedAttributes(GluuCustomPerson person) {
		if (person == null) {
			return Arrays.asList(new GluuCustomAttribute[0]);
		}

		List<GluuCustomAttribute> releasedAttributes = new ArrayList<GluuCustomAttribute>();
		for (GluuCustomAttribute attribute : person.getCustomAttributes()) {
			List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.USER);
			GluuAttribute metadata = attributeService.getAttributeByName(attribute.getName(), attributes);
			if (metadata != null && metadata.isWhitePagesCanView() && !tableAttributes.contains(attribute.getName())) {
				attribute.setMetadata(metadata);
				releasedAttributes.add(attribute);
			}
		}

		return releasedAttributes;
	}

	public boolean released(GluuCustomPerson person, String attributeName) {
		return person.getGluuOptOuts() == null || !person.getGluuOptOuts().contains(attributeName);
	}

	public boolean canContact(GluuCustomPerson person) {
		return false;
	}
}
