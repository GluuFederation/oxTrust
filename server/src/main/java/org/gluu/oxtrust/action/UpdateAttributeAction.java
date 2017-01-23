/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.AttributeValidation;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.model.SchemaEntry;
import org.xdi.service.SchemaService;
import org.xdi.util.StringHelper;

/**
 * Action class for updating attribute metadata
 * 
 * @author Yuriy Movchan Date: 10.19.2010
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateAttributeAction")
@Restrict("#{identity.loggedIn}")
public class UpdateAttributeAction implements Serializable {

	private static final long serialVersionUID = -2932167044333943687L;

	@Logger
	private Log log;

	@In
	private AttributeService attributeService;

	@In
	private SchemaService schemaService;

	@In
	private FacesMessages facesMessages;
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	private String inum;
	private GluuAttribute attribute;
	private boolean update;
	private boolean showAttributeDeleteConfirmation;
	
	private boolean validationToggle;
	private boolean tooltipToggle;

	private boolean canEdit;
	
	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String add() {
		if (this.attribute != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;

		this.showAttributeDeleteConfirmation = false;

		this.attribute = new GluuAttribute();
		attribute.setAttributeValidation(new AttributeValidation());
		
		this.attribute.setStatus(GluuStatus.ACTIVE);
		this.attribute.setEditType(new GluuUserRole[] { GluuUserRole.ADMIN });

		this.canEdit = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String update() {
		if (this.attribute != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;

		this.showAttributeDeleteConfirmation = false;

		if (!loadAttribute(this.inum)) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private boolean loadAttribute(String inum) {
		try {
			this.attribute = attributeService.getAttributeByInum(inum);
		} catch (LdapMappingException ex) {
			log.error("Failed to find attribute {0}", ex, inum);
		}

		if (this.attribute == null) {
			return false;
		}

		initAttribute();

		this.canEdit = isAllowEdit();

		return true;
	}

	private void initAttribute() {
		if (StringHelper.isEmpty(this.attribute.getSaml1Uri())) {
			String namespace;
			if (attribute.isCustom() || StringHelper.isEmpty(attribute.getUrn())
					&& attribute.getUrn().startsWith("urn:gluu:dir:attribute-def:")) {
				namespace = "gluu";
			} else {
				namespace = "mace";
			}
			this.attribute.setSaml1Uri(String.format("urn:%s:dir:attribute-def:%s", namespace, attribute.getName()));
		}

		if (StringHelper.isEmpty(this.attribute.getSaml2Uri())) {
			this.attribute.setSaml2Uri(attributeService.getDefaultSaml2Uri(attribute.getName()));
		}

		if (attribute.getAttributeValidation() == null) {
			attribute.setAttributeValidation(new AttributeValidation());
		} else {
			this.validationToggle = true;
		}
		
		if(attribute.getGluuTooltip() != null){
			this.tooltipToggle = true;
		}
	}

	private boolean isAllowEdit() {
		return this.attribute.isAdminCanEdit();
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public void cancel() {
	}


	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String save() {
		if (!tooltipToggle) {
			attribute.setGluuTooltip(null);
		}

		if ((attribute.getEditType() != null) && (attribute.getEditType().length == 0)) {
			attribute.setEditType(null);
		}

		if ((attribute.getViewType() != null) && (attribute.getViewType().length == 0)) {
			attribute.setViewType(null);
		}
		
		String attributeName = this.attribute.getName();
		if (this.update) {
			try {

				boolean attributeValidation = validateAttributeDefinition(attributeName);
				if (!attributeValidation) {
					return OxTrustConstants.RESULT_VALIDATION_ERROR;
				}

				attributeService.updateAttribute(this.attribute);
			} catch (LdapMappingException ex) {
				log.error("Failed to update attribute {0}", ex, inum);
				facesMessages.add(Severity.ERROR, "Failed to update attribute");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			if (!validateName(attributeName)) {
				return OxTrustConstants.RESULT_FAILURE;
			}

			boolean result = addNewAttribute(attributeName, false);
			if (!result) {
				return OxTrustConstants.RESULT_FAILURE;
			}
		}

		this.update = true;
		if (!loadAttribute(this.attribute.getInum())) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private boolean addNewAttribute(String attributeName, boolean addToSchema) {
		boolean attributeValidation = validateAttributeDefinition(attributeName);
		if (!attributeValidation) {
			return false;
		}

		String inum = attributeService.generateInumForNewAttribute();
		String dn = attributeService.getDnForAttribute(inum);
		if (attribute.getSaml1Uri() == null || attribute.getSaml1Uri().equals("")) {
			attribute.setSaml1Uri("urn:gluu:dir:attribute-def:" + attributeName);
		}
		if (attribute.getSaml2Uri() == null || attribute.getSaml2Uri().equals("")) {
			attribute.setSaml2Uri("urn:oid:" + attributeName);
		}

		String attributeOrigin = determineOrigin(attributeName);
		if (StringHelper.isEmpty(attributeOrigin)) {
			facesMessages.add(Severity.ERROR, "Failed to determine object class by attribute name");
			return false;
		}

		this.attribute.setOrigin(attributeOrigin);

		// Save attribute metadata
		this.attribute.setDn(dn);
		this.attribute.setInum(inum);

		try {
			attributeService.addAttribute(this.attribute);
		} catch (LdapMappingException ex) {
			log.error("Failed to add new attribute {0}", ex, this.attribute.getInum());

			facesMessages.add(Severity.ERROR, "Failed to add new attribute");
			return false;
		}

		return true;
	}

	private boolean validateAttributeDefinition(String attributeName) {
		boolean containsAttribute = schemaService.containsAttributeTypeInSchema(attributeName);
		if (!containsAttribute) {
			facesMessages.add(Severity.ERROR, "The attribute type '{0}' not defined in LDAP schema", attributeName);
			return false;
		}

		// Check if attribute defined in gluuPerson or in custom object class
		boolean containsAttributeInGluuObjectClasses = containsAttributeInGluuObjectClasses(attributeName);
		if (!containsAttributeInGluuObjectClasses) {
			facesMessages.add(Severity.ERROR, "Attribute type '{0}' definition not belong to list of allowed object classes", attributeName);
			return false;
		}
		
		return true;
	}

	private String determineOrigin(String attributeName) {
		String[] objectClasses = ArrayHelper.join(new String[] { "gluuPerson" }, applicationConfiguration.getPersonObjectClassTypes());

		SchemaEntry schemaEntry = schemaService.getSchema();
		
		for (String objectClass : objectClasses) { 
			Set<String> attributeNames = schemaService.getObjectClassesAttributes(schemaEntry, new String[] { objectClass });
			String atributeNameToSearch = StringHelper.toLowerCase(attributeName);
			boolean contains = attributeNames.contains(atributeNameToSearch);
			if (contains) {
				return objectClass;
			}
		}

		log.error("Failed to determine object class by attribute name '{0}'", attributeName);
		return null;
	}

	private boolean containsAttributeInGluuObjectClasses(String attributeName) {
		String[] objectClasses = ArrayHelper.join(new String[] { "gluuPerson" }, applicationConfiguration.getPersonObjectClassTypes());

		SchemaEntry schemaEntry = schemaService.getSchema();
		Set<String> attributeNames = schemaService.getObjectClassesAttributes(schemaEntry, objectClasses);

		String atributeNameToSearch = StringHelper.toLowerCase(attributeName);
		boolean result = attributeNames.contains(atributeNameToSearch);

		return result;
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String delete() {
		showAttributeDeleteConfirmation = true;
		return deleteAndAcceptUpdate();
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public void cancelDeleteAndAcceptUpdate() {
		showAttributeDeleteConfirmation = false;
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String deleteAndAcceptUpdate() {
		if (update && showAttributeDeleteConfirmation && this.attribute.isCustom()) {
			showAttributeDeleteConfirmation = false;

			if (attributeService.removeAttribute(this.attribute)) {
				return OxTrustConstants.RESULT_SUCCESS;
			} else {
				log.error("Failed to remove attribute {0}", this.attribute.getInum());
			}
		}

		showAttributeDeleteConfirmation = false;

		return OxTrustConstants.RESULT_FAILURE;
	}

	public boolean validateEditType() {
		if (!(this.attribute.allowEditBy(GluuUserRole.USER) || this.attribute.allowEditBy(GluuUserRole.ADMIN))) {
			facesMessages.add(Severity.WARN, "Please select Edit Type.");
			return false;
		}

		return true;
	}

	public boolean validateName(String attributeName) {
		GluuAttribute tmpAttribute = new GluuAttribute();
		tmpAttribute.setBaseDn(attributeService.getDnForAttribute(null));
		tmpAttribute.setName(attributeName);

		if (attributeService.containsAttribute(tmpAttribute)) {
			facesMessages.addToControl("nameId", Severity.ERROR, "Attribute with specified name already exist");
			return false;
		}

		return true;
	}

	public String getInum() {
		return inum;
	}
	public void setInum(String inum) {
		this.inum = inum;
	}

	public GluuAttribute getAttribute() {
		return attribute;
	}

	public boolean isUpdate() {
		return update;
	}

	public boolean isShowAttributeDeleteConfirmation() {
		return showAttributeDeleteConfirmation;
	}

	public boolean canEdit() {
		return canEdit;
	}

	public boolean isValidationToggle() {
		return validationToggle;
	}

	public void setValidationToggle(boolean validationToggle) {
		this.validationToggle = validationToggle;
	}

	public boolean isTooltipToggle() {
		return tooltipToggle;
	}

	public void setTooltipToggle(boolean tooltipToggle) {
		this.tooltipToggle = tooltipToggle;
	}

    /**
     * @param update the update to set
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

    /**
     * @param showAttributeDeleteConfirmation the showAttributeDeleteConfirmation to set
     */
    public void setShowAttributeDeleteConfirmation(boolean showAttributeDeleteConfirmation) {
        this.showAttributeDeleteConfirmation = showAttributeDeleteConfirmation;
    }

    /**
     * @return the canEdit
     */
    public boolean isCanEdit() {
        return canEdit;
    }

    /**
     * @param canEdit the canEdit to set
     */
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

}
