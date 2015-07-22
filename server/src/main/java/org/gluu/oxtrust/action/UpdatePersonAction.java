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
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;

/**
 * Action class for updating person's attributes
 * 
 * @author Yuriy Movchan Date: 10.23.2010
 */
@Scope(ScopeType.CONVERSATION)
@Name("updatePersonAction")
@Restrict("#{identity.loggedIn}")
public class UpdatePersonAction implements Serializable {

	private static final long serialVersionUID = -3242167044333943689L;

	@Logger
	private Log log;

	private String inum;
	private boolean update;

	private GluuCustomPerson person;

	@In
	private AttributeService attributeService;

	@In
	private PersonService personService;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private CustomAttributeAction customAttributeAction;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private UserPasswordAction userPasswordAction;

	@In(create = true)
	@Out(scope = ScopeType.CONVERSATION)
	private WhitePagesAction whitePagesAction;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In
	private ExternalUpdateUserService externalUpdateUserService;

	/**
	 * Initializes attributes for adding new person
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	@Restrict("#{s:hasPermission('person', 'access')}")
	public String add() {
		if (!OrganizationService.instance().isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.person = new GluuCustomPerson();

		initAttributes();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Initializes attributes for updating person
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	@Restrict("#{s:hasPermission('person', 'access')}")
	public String update() {
		if (this.person != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;
		try {
			this.person = personService.getPersonByInum(inum);
		} catch (LdapMappingException ex) {
			log.error("Failed to find person {0}", ex, inum);
			return OxTrustConstants.RESULT_FAILURE;
		}

		initAttributes();

		userPasswordAction.setPerson(this.person);

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('person', 'access')}")
	public void cancel() {
	}

	/**
	 * Saves person to ldap
	 * 
	 * @return String describing success of the operation
	 */
	@Restrict("#{s:hasPermission('person', 'access')}")
	public String save() {
		if (!OrganizationService.instance().isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		String customObjectClass = attributeService.getCustomOrigin();
		this.person.setStatus(GluuStatus.ACTIVE);
		String[] customObjectClassesArray = this.person.getCustomObjectClasses();
		if(customObjectClassesArray != null && customObjectClassesArray.length != 0){
			List<String> customObjectClassesList = Arrays.asList(customObjectClassesArray);
			if(! customObjectClassesList.contains(customObjectClass)){
				List<String> customObjectClassesListUpdated = new ArrayList<String>();
				customObjectClassesListUpdated.addAll(customObjectClassesList);
				customObjectClassesListUpdated.add(customObjectClass);
				customObjectClassesList = customObjectClassesListUpdated;
			}
			this.person.setCustomObjectClasses(customObjectClassesList.toArray(new String[0]));
			
		}else{
			this.person.setCustomObjectClasses(new String[] { customObjectClass });
		}
		this.person.setCustomAttributes(customAttributeAction.getCustomAttributes());

		if (update) {
			try {
				if (externalUpdateUserService.isEnabled()) {
					externalUpdateUserService.executeExternalUpdateUserMethods(this.person);
                }
				personService.updatePerson(this.person);
			} catch (LdapMappingException ex) {
				log.error("Failed to update person {0}", ex, inum);
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			if (personService.getPersonByUid(this.person.getUid()) != null) {
				return OxTrustConstants.RESULT_DUPLICATE;
			}

			this.inum = personService.generateInumForNewPerson();
			String iname = personService.generateInameForNewPerson(this.person.getUid());
			String dn = personService.getDnForPerson(this.inum);

			// Save person
			this.person.setDn(dn);
			this.person.setInum(this.inum);
			this.person.setIname(iname);

			List<GluuCustomAttribute> personAttributes = this.person.getCustomAttributes();
			if (!personAttributes.contains(new GluuCustomAttribute("cn", ""))) {
				List<GluuCustomAttribute> changedAttributes = new ArrayList<GluuCustomAttribute>();
				changedAttributes.addAll(personAttributes);
				changedAttributes.add(new GluuCustomAttribute("cn", this.person.getGivenName() + " " + this.person.getDisplayName()));
				this.person.setCustomAttributes(changedAttributes);
			} else {
				this.person.setCommonName(this.person.getCommonName() + " " + this.person.getGivenName());
			}

			try {
				if (externalUpdateUserService.isEnabled()) {
					externalUpdateUserService.executeExternalAddUserMethods(this.person);
                }
				personService.addPerson(this.person);
			} catch (Exception ex) {
				log.error("Failed to add new person {0}", ex, this.person.getInum());

				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;

			userPasswordAction.setPerson(this.person);
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	/**
	 * Delete selected person from ldap
	 * 
	 * @return String describing success of the operation
	 * @throws Exception
	 */
	@Restrict("#{s:hasPermission('person', 'access')}")
	public String delete() {
		if (!OrganizationService.instance().isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (update) {
			// Remove person
			try {
				Events.instance().raiseEvent(OxTrustConstants.EVENT_PERSON_DELETED, this.person);
				personService.removePerson(this.person);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove person {0}", ex, this.person.getInum());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
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

		customAttributeAction.initCustomAttributes(attributes, customAttributes, origins, applicationConfiguration
				.getPersonObjectClassTypes(), applicationConfiguration.getPersonObjectClassDisplayNames());

		if (newPerson) {
			customAttributeAction.addCustomAttributes(personService.getMandatoryAtributes());
		}
	}
	
	public String getGroupName(String dn){
		if(dn != null){
			GluuGroup group = GroupService.instance().getGroupByDn(dn);
			if( group != null ){
				String groupName = group.getDisplayName();
				if(groupName != null && ! groupName.isEmpty()){
					return groupName;
				}
			}
		}
		return "invalid group name"; 
		
	}

	/**
	 * Returns person's inum
	 * 
	 * @return inum
	 */
	public String getInum() {
		return inum;
	}

	/**
	 * Sets person's inum
	 * 
	 * @param inum
	 */
	public void setInum(String inum) {
		this.inum = inum;
	}

	/**
	 * Returns person
	 * 
	 * @return GluuCustomPerson
	 */
	public GluuCustomPerson getPerson() {
		return person;
	}

	/**
	 * Return true if person is being updated, false if adding new person
	 * 
	 * @return
	 */
	public boolean isUpdate() {
		return update;
	}

}
