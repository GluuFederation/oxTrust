/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.service.external.ExternalUpdateUserService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.oxtrust.util.Utils;
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
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;

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
	private IPersonService personService;

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
	
	private GluuStatus gluuStatus ;

	private String password;
	private String confirmPassword;

	public GluuStatus getGluuStatus() {
		return gluuStatus;
	}

	public void setGluuStatus(GluuStatus gluuStatus) {
		this.gluuStatus = gluuStatus;
	}

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
		this.gluuStatus=this.person.getStatus();

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
	public String save() throws Exception {
		if (!OrganizationService.instance().isAllowPersonModification()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateCustomObjectClasses();

		List<GluuCustomAttribute> removedAttributes = customAttributeAction.detectRemovedAttributes();
		customAttributeAction.updateOriginCustomAttributes();
		
		List<GluuCustomAttribute>  customAttributes = customAttributeAction.getCustomAttributes();
		for(GluuCustomAttribute customAttribute: customAttributes){
			if(customAttribute.getName().equalsIgnoreCase("gluuStatus")){
				customAttribute.setValue(gluuStatus.getValue());
			}
			
		}

		this.person.setCustomAttributes(customAttributeAction.getCustomAttributes());
		this.person.getCustomAttributes().addAll(removedAttributes);

		// Sync email, in reverse ("oxTrustEmail" <- "mail")
		this.person = Utils.syncEmailReverse(this.person, true);

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
			this.person.setUserPassword(this.password);

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
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateCustomObjectClasses() {
		personService.addCustomObjectClass(this.person);

		// Update objectClasses
		String[] allObjectClasses = ArrayHelper.arrayMerge(applicationConfiguration.getPersonObjectClassTypes(), this.person.getCustomObjectClasses());
		String[] resultObjectClasses = new HashSet<String>(Arrays.asList(allObjectClasses)).toArray(new String[0]);
		
		this.person.setCustomObjectClasses(resultObjectClasses);
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
				if (externalUpdateUserService.isEnabled()) {
					externalUpdateUserService.executeExternalDeleteUserMethods(this.person);
                }
				personService.removePerson(this.person);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove person {0}", ex, this.person.getInum());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	private void initAttributes() {
		if (externalUpdateUserService.isEnabled()) {
			externalUpdateUserService.executeExternalNewUserMethods(this.person);
        }
		
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
	
	public GluuStatus[] getActiveInactiveStatuses() {
		return new GluuStatus[] { GluuStatus.ACTIVE, GluuStatus.INACTIVE };
	}
	
	public void validateConfirmPassword(FacesContext context, UIComponent comp,
			Object value){
		if (comp.getClientId().endsWith("custpasswordId")) {
			this.password = (String) value;
		} else if (comp.getClientId().endsWith("custconfirmpasswordId")) {
			this.confirmPassword = (String) value;
		}

		if (!StringHelper.equalsIgnoreCase(password, confirmPassword)) {	
			((UIInput) comp).setValid(false);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password and Confirm Password should be same!", "Password and Confirm Password should be same!");
			context.addMessage(comp.getClientId(context), message);
}		
	}

}
