/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import org.gluu.model.GluuAttributeUsageType;
import org.gluu.model.GluuStatus;
import org.gluu.model.GluuUserRole;
import org.gluu.model.SchemaEntry;
import org.gluu.model.attribute.AttributeDataType;
import org.gluu.model.attribute.AttributeValidation;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.fido.Fido2DeviceResource;
import org.gluu.oxtrust.model.scim2.fido.FidoDeviceResource;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.TrustService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.DataSourceTypeService;
import org.gluu.service.SchemaService;
import org.gluu.service.security.Secure;
import org.gluu.util.ArrayHelper;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Action class for updating attribute metadata
 *
 * @author Yuriy Movchan Date: 10.19.2010
 */
@ConversationScoped
@Named("updateAttributeAction")
@Secure("#{permissionService.hasPermission('attribute', 'access')}")
public class UpdateAttributeAction implements Serializable {

    private static final long serialVersionUID = -2932167044333943687L;

    @Inject
    private Logger log;

    @Inject
    private AttributeService attributeService;

    @Inject
    private TrustService trustService;

    @Inject
    private DataSourceTypeService dataSourceTypeService;

    @Inject
    private SchemaService schemaService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private ConversationService conversationService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Identity identity;

    @Inject
    private OxTrustAuditService oxTrustAuditService;

    private String inum;
    private GluuAttribute attribute;
    private boolean update;
    private boolean showAttributeDeleteConfirmation;

    private boolean validationToggle;
    private boolean tooltipToggle;
    private static final Map<String, String> ldapScim = new HashMap<>();

    private boolean canEdit;

    static {
        Map<Class<? extends BaseScimResource>, Map<String, String>> refs = new HashMap<>(IntrospectUtil.storeRefs);
        List<Class<? extends BaseScimResource>> resourceTypes = Arrays.asList(UserResource.class,
                FidoDeviceResource.class, GroupResource.class, Fido2DeviceResource.class);
        Predicate<Class<? extends BaseScimResource>> p = resourceTypes::contains;
        refs.keySet().removeIf(p.negate());

        for (Class<? extends BaseScimResource> resourceType : refs.keySet()) {
            Map<String, String> scimLdap = refs.get(resourceType);
            String resourceName = ScimResourceUtil.getType(resourceType);

            for (String scimAttr : scimLdap.keySet()) {
                String ldapAttr = scimLdap.get(scimAttr);
                String val = ldapScim.getOrDefault(ldapAttr, String.format("%s (", scimAttr));
                val += String.format("%s/", resourceName);
                ldapScim.put(ldapAttr, val);
            }
        }
        ldapScim.keySet()
                .forEach(ldapAttr -> ldapScim.compute(ldapAttr, (k, v) -> v.substring(0, v.length() - 1).concat(")")));
    }

    public String add() {
        if (this.attribute != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }
        this.update = false;
        this.showAttributeDeleteConfirmation = false;
        this.attribute = new GluuAttribute();
        attribute.setAttributeValidation(new AttributeValidation());
        this.attribute.setStatus(GluuStatus.ACTIVE);
        this.attribute.setEditType(new GluuUserRole[]{GluuUserRole.ADMIN});
        this.canEdit = true;
        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String update() {
        if (this.attribute != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }
        this.update = true;
        this.showAttributeDeleteConfirmation = false;
        if (!loadAttribute(this.inum)) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find attribute");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    private boolean loadAttribute(String inum) {
        try {
            this.attribute = attributeService.getAttributeByInum(inum);
        } catch (BasePersistenceException ex) {
            log.error("Failed to find attribute {}", inum, ex);
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
        }
        if (attribute.getAttributeValidation().getRegexp() == null || StringHelper.isEmpty(attribute.getAttributeValidation().getRegexp())) {
            this.validationToggle = false;
        } else {
            this.validationToggle = true;
        }

        if (attribute.getGluuTooltip() != null) {
            this.tooltipToggle = true;
        }
    }

    private boolean isAllowEdit() {
        return this.attribute.isAdminCanEdit() || this.attribute.getEditType() == null;
    }

    public String getScimValue(String key) {
        return ldapScim.get(key);
    }

    public boolean isScimAttribute(String key) {
        return ldapScim.get(key) != null ? true : false;
    }

    public String cancel() {
        if (update) {
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    "Attribute '#{updateAttributeAction.attribute.displayName}' not updated");
        } else {
            facesMessages.add(FacesMessage.SEVERITY_INFO, "New attribute not added");
        }

        conversationService.endConversation();

        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String save() {
        String regexp = attribute.getAttributeValidation().getRegexp();
        if (regexp != null) {
            try {
                Pattern.compile(regexp);
            } catch (PatternSyntaxException e) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        "The regular expression assign to this attribute is not valid.");
                return OxTrustConstants.RESULT_SUCCESS;
            }
        }
        String outcome = saveImpl();

        if (update) {
            if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
                facesMessages.add(FacesMessage.SEVERITY_INFO,
                        "Attribute '#{updateAttributeAction.attribute.displayName}' updated successfully");
            } else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new attribute");
            }
        } else {
            if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
                facesMessages.add(FacesMessage.SEVERITY_INFO,
                        "New attribute '#{updateAttributeAction.attribute.displayName}' added successfully");
                conversationService.endConversation();
            } else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        "Failed to update attribute '#{updateAttributeAction.attribute.displayName}'");
            }
        }

        return outcome;
    }

    public String saveImpl() {
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
                oxTrustAuditService.audit(
                        "ATTRIBUTE " + this.attribute.getInum() + " **" + this.attribute.getDisplayName()
                                + "** UPDATED",
                        identity.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            } catch (BasePersistenceException ex) {
                log.error("Failed to update attribute {}", inum, ex);
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update attribute");
                return OxTrustConstants.RESULT_FAILURE;
            }
        } else {
            if (!validateName(attributeName)) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "An attribute with the same name already exist.");
                return OxTrustConstants.RESULT_FAILURE;
            }
            boolean result = addNewAttribute(attributeName, false);
            if (!result) {
                return OxTrustConstants.RESULT_FAILURE;
            }

        }

        return OxTrustConstants.RESULT_SUCCESS;
    }

    private boolean addNewAttribute(String attributeName, boolean addToSchema) {
        boolean attributeValidation = validateAttributeDefinition(attributeName);
        if (!attributeValidation) {
            return false;
        }

        this.inum = attributeService.generateInumForNewAttribute();
        String dn = attributeService.getDnForAttribute(inum);
        if (attribute.getSaml1Uri() == null || attribute.getSaml1Uri().equals("")) {
            attribute.setSaml1Uri("urn:gluu:dir:attribute-def:" + attributeName);
        }
        if (attribute.getSaml2Uri() == null || attribute.getSaml2Uri().equals("")) {
            attribute.setSaml2Uri("urn:oid:" + attributeName);
        }

        if (dataSourceTypeService.isLDAP(attributeService.getDnForAttribute(null))) {
            String attributeOrigin = determineOrigin(attributeName);
            if (StringHelper.isEmpty(attributeOrigin)) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to determine object class by attribute name");
                return false;
            }
            this.attribute.setOrigin(attributeOrigin);
        } else {
            this.attribute.setOrigin("gluuPerson");
        }
        this.attribute.setDn(dn);
        this.attribute.setInum(inum);
        this.attribute.setDisplayName(this.attribute.getDisplayName().trim());
        this.attribute.setName(this.attribute.getName().trim());

        try {
            attributeService.addAttribute(this.attribute);
            oxTrustAuditService.audit(
                    "ATTRIBUTE " + this.attribute.getInum() + " **" + this.attribute.getDisplayName() + "** ADDED",
                    identity.getUser(),
                    (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        } catch (BasePersistenceException ex) {
            log.error("Failed to add new attribute {}", this.attribute.getInum(), ex);

            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new attribute");
            return false;
        }

        return true;
    }

    private boolean validateAttributeDefinition(String attributeName) {
        if (dataSourceTypeService.isLDAP(attributeService.getDnForAttribute(null))) {
            boolean containsAttribute = schemaService.containsAttributeTypeInSchema(attributeName);
            if (!containsAttribute) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        "The attribute type '#{updateAttributeAction.attribute.name}' not defined in LDAP schema");
                return false;
            }
            boolean containsAttributeInGluuObjectClasses = containsAttributeInGluuObjectClasses(attributeName);
            if (!containsAttributeInGluuObjectClasses) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        "Attribute type '#{updateAttributeAction.attribute.name}' definition not belong to list of allowed object classes");
                return false;
            }

            return true;
        } else {
            return true;
        }

    }

    private String determineOrigin(String attributeName) {
        String[] objectClasses = ArrayHelper.arrayMerge(new String[]{"gluuPerson"},
                appConfiguration.getPersonObjectClassTypes());

        SchemaEntry schemaEntry = schemaService.getSchema();

        for (String objectClass : objectClasses) {
            Set<String> attributeNames = schemaService.getObjectClassesAttributes(schemaEntry,
                    new String[]{objectClass});
            String atributeNameToSearch = StringHelper.toLowerCase(attributeName);
            boolean contains = attributeNames.contains(atributeNameToSearch);
            if (contains) {
                return objectClass;
            }
        }

        log.error("Failed to determine object class by attribute name '{}'", attributeName);
        return null;
    }

    private boolean containsAttributeInGluuObjectClasses(String attributeName) {
        String[] objectClasses = ArrayHelper.arrayMerge(new String[]{"gluuPerson"},
                appConfiguration.getPersonObjectClassTypes());

        SchemaEntry schemaEntry = schemaService.getSchema();
        Set<String> attributeNames = schemaService.getObjectClassesAttributes(schemaEntry, objectClasses);

        String atributeNameToSearch = StringHelper.toLowerCase(attributeName);
        boolean result = attributeNames.contains(atributeNameToSearch);

        return result;
    }

    public String delete() {
        showAttributeDeleteConfirmation = true;
        return deleteAndAcceptUpdate();
    }

    public void cancelDeleteAndAcceptUpdate() {
        showAttributeDeleteConfirmation = false;
    }

    public String deleteAndAcceptUpdate() {
        if (update && showAttributeDeleteConfirmation) {
            showAttributeDeleteConfirmation = false;
            if (trustService.removeAttribute(this.attribute)) {
                oxTrustAuditService.audit(
                        "ATTRIBUTE " + this.attribute.getInum() + " **" + this.attribute.getDisplayName()
                                + "** REMOVED",
                        identity.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
                facesMessages.add(FacesMessage.SEVERITY_INFO,
                        "Attribute '#{updateAttributeAction.attribute.displayName}' removed successfully");
                conversationService.endConversation();
                return OxTrustConstants.RESULT_SUCCESS;
            } else {
                log.error("Failed to remove attribute {}", this.attribute.getInum());
            }
        }
        showAttributeDeleteConfirmation = false;
        facesMessages.add(FacesMessage.SEVERITY_ERROR,
                "Failed to remove attribute '#{updateAttributeAction.attribute.displayName}'");

        return OxTrustConstants.RESULT_FAILURE;
    }

    public boolean validateEditType() {
        if (!(this.attribute.allowEditBy(GluuUserRole.USER) || this.attribute.allowEditBy(GluuUserRole.ADMIN))) {
            facesMessages.add(FacesMessage.SEVERITY_WARN, "Please select Edit Type.");
            return false;
        }

        return true;
    }

    public boolean validateName(String attributeName) {
        GluuAttribute tmpAttribute = new GluuAttribute();
        tmpAttribute.setBaseDn(attributeService.getDnForAttribute(null));
        tmpAttribute.setName(attributeName);
        if (attributeService.containsAttribute(tmpAttribute)) {
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

    public AttributeDataType[] getDataTypes() {
        return attributeService.getDataTypes();
    }

    public GluuUserRole[] getAttributeEditTypes() {
        return attributeService.getAttributeEditTypes();
    }

    public GluuAttributeUsageType[] getAttributeUsageTypes() {
        return attributeService.getAttributeUsageTypes();
    }
    
    public String getPersistenceType() {
		return attributeService.getPersistenceType();
	}

}
