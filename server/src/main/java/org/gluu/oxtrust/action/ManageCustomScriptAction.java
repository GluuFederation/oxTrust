/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.AuthenticationScriptUsageType;
import org.gluu.model.ProgrammingLanguage;
import org.gluu.model.ScriptLocationType;
import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.SimpleExtendedCustomProperty;
import org.gluu.model.SimpleProperty;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.model.custom.script.model.auth.AuthenticationCustomScript;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.ldap.service.SamlAcrService;
import org.gluu.oxtrust.model.GluuTreeModel;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.OxConstants;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.google.common.base.Optional;

import net.bootsfaces.component.tree.event.TreeNodeCheckedEvent;
import net.bootsfaces.component.tree.event.TreeNodeEventListener;
import net.bootsfaces.component.tree.event.TreeNodeExpandedEvent;
import net.bootsfaces.component.tree.event.TreeNodeSelectionEvent;

/**
 * Add/Modify custom script configurations
 * 
 * @author Yuriy Movchan Date: 12/29/2014
 */
@Named("manageCustomScriptAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ManageCustomScriptAction
		implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, Serializable, TreeNodeEventListener {

	private static final long serialVersionUID = -3823022039248381963L;

	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\:\\/\\.]+$");

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private Logger log;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private SamlAcrService samlAcrService;

	@Inject
	private AbstractCustomScriptService customScriptService;

	private Map<CustomScriptType, List<CustomScript>> customScriptsByTypes;

	private boolean initialized;

	private static List<String> allAcrs = new ArrayList<>();

	private GluuTreeModel tree;

	private boolean edition = true;
	private boolean showAddButton = false;
	private CustomScript selectedScript;
	private String dn;
	private String inum;
	private CustomScriptType selectedScriptType = CustomScriptType.PERSON_AUTHENTICATION;

	public void init(boolean isInitial) {
		try {
			tree = (GluuTreeModel) new GluuTreeModel().withText("root").withSelectable(false).withExpanded(false);
			CustomScriptType[] allowedCustomScriptTypes = this.configurationService.getCustomScriptTypes();
			Stream.of(allowedCustomScriptTypes).forEach(e -> {
				GluuTreeModel node = (GluuTreeModel) new GluuTreeModel().withText(e.getDisplayName())
						.withExpanded(false);
				node.setParent(true);
				node.setCustomScriptType(CustomScriptType.getByValue(e.getValue()));
				List<CustomScript> customScripts = customScriptService.findCustomScripts(Arrays.asList(e));
				customScripts.forEach(k -> {
					GluuTreeModel scriptNode = (GluuTreeModel) new GluuTreeModel().withText(k.getName())
							.withIcon("fa fa-info").withExpanded(false);
					scriptNode.setInum(k.getInum());
					scriptNode.setDn(k.getDn());
					node.withSubnode(scriptNode);
				});
				tree.withSubnode(node);
			});
			if (isInitial) {
				this.selectedScript = customScriptService
						.findCustomScripts(Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION)).get(0);
			} else {
				this.selectedScript = customScriptService.getCustomScriptByINum(dn, inum, null).get();
			}
			fillEmptyListProperty();
			tree.expandParentOfNode(selectedScript);
			setShowAddButton(true);
		} catch (Exception e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error  building custom script tree structure");
			log.error("", e);
		}
	}

	private void fillEmptyListProperty() {
		if (this.selectedScript.getConfigurationProperties() == null) {
			this.selectedScript.setConfigurationProperties(new ArrayList<SimpleExtendedCustomProperty>());
		}
		if (this.selectedScript.getModuleProperties() == null) {
			this.selectedScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
		}
	}

	public void initAddForm() {
		this.showAddButton = false;
		this.edition = false;
		if (CustomScriptType.PERSON_AUTHENTICATION == selectedScriptType) {
			AuthenticationCustomScript authenticationCustomScript = new AuthenticationCustomScript();
			authenticationCustomScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
			authenticationCustomScript.setUsageType(AuthenticationScriptUsageType.INTERACTIVE);
			this.selectedScript = authenticationCustomScript;
		} else {
			this.selectedScript = new CustomScript();
			this.selectedScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
		}
		this.selectedScript.setLocationType(ScriptLocationType.LDAP);
		this.selectedScript.setScriptType(selectedScriptType);
		this.selectedScript.setProgrammingLanguage(ProgrammingLanguage.PYTHON);
		this.selectedScript.setConfigurationProperties(new ArrayList<SimpleExtendedCustomProperty>());
		facesMessages.add(FacesMessage.SEVERITY_WARN, "This custom script will be added to '%s' category",
				selectedScriptType.getDisplayName());
	}

	public String saveScript() {
		CustomScript customScript = selectedScript;
		try {
			if (StringHelper.equalsIgnoreCase(customScript.getName(), OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME)) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "'%s' is reserved script name", customScript.getName());
				return OxTrustConstants.RESULT_FAILURE;
			}
			boolean nameValidation = NAME_PATTERN.matcher(customScript.getName()).matches();
			if (!nameValidation) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"'%s' is invalid script name. Only alphabetic, numeric and underscore characters are allowed in Script Name",
						customScript.getName());
				return OxTrustConstants.RESULT_FAILURE;
			}
			customScript.setRevision(customScript.getRevision() + 1);
			this.dn = customScript.getDn();
			this.inum = customScript.getInum();
			if (StringHelper.isEmpty(dn)) {
				this.inum = UUID.randomUUID().toString();
				this.dn = customScriptService.buildDn(this.inum);
				customScript.setDn(this.dn);
				customScript.setInum(this.inum);
				this.edition = false;
			}
			customScript.setDn(this.dn);
			customScript.setInum(this.inum);
			if (ScriptLocationType.LDAP == customScript.getLocationType()) {
				customScript.removeModuleProperty(CustomScript.LOCATION_PATH_MODEL_PROPERTY);
			}
			if ((customScript.getConfigurationProperties() != null)
					&& (customScript.getConfigurationProperties().size() == 0)) {
				customScript.setConfigurationProperties(null);
			}
			if ((customScript.getConfigurationProperties() != null)
					&& (customScript.getModuleProperties().size() == 0)) {
				customScript.setModuleProperties(null);
			}
			if (this.isEdition()) {
				customScriptService.update(customScript);
				facesMessages.add(FacesMessage.SEVERITY_INFO, customScript.getName() + " updated successfully");
				init(false);
			} else {
				customScriptService.add(customScript);
				this.selectedScript = customScriptService
						.getCustomScriptByINum(customScript.getDn(), customScript.getInum(), null).get();
				this.edition = true;
				facesMessages.add(FacesMessage.SEVERITY_INFO, customScript.getName() + " added successfully");
				init(false);
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error when processing " + customScript.getName());
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String cancel() throws Exception {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Custom script configuration not updated");
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public Map<CustomScriptType, List<CustomScript>> getCustomScriptsByTypes() {
		return this.customScriptsByTypes;
	}

	public String getId(Object obj) {
		return "c" + System.identityHashCode(obj) + "Id";
	}

	public void addCustomScript(CustomScriptType scriptType) {
		List<CustomScript> customScriptsByType = this.customScriptsByTypes.get(scriptType);
		CustomScript customScript;
		if (CustomScriptType.PERSON_AUTHENTICATION == scriptType) {
			AuthenticationCustomScript authenticationCustomScript = new AuthenticationCustomScript();
			authenticationCustomScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
			authenticationCustomScript.setUsageType(AuthenticationScriptUsageType.INTERACTIVE);

			customScript = authenticationCustomScript;
		} else {
			customScript = new CustomScript();
			customScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
		}
		customScript.setLocationType(ScriptLocationType.LDAP);
		customScript.setScriptType(scriptType);
		customScript.setProgrammingLanguage(ProgrammingLanguage.PYTHON);
		customScript.setConfigurationProperties(new ArrayList<SimpleExtendedCustomProperty>());
		customScriptsByType.add(customScript);
	}

	public void removeCustomScript() {
		if (this.selectedScript != null && this.selectedScript.getInum() != null) {
			customScriptService.remove(this.selectedScript);
			facesMessages.add(FacesMessage.SEVERITY_INFO, this.selectedScript.getName() + " removed successfully");
			init(true);
		}
	}

	@Override
	public void addItemToSimpleProperties(List<SimpleProperty> simpleProperties) {
		if (simpleProperties != null) {
			simpleProperties.add(new SimpleProperty(""));
		}
	}

	@Override
	public void removeItemFromSimpleProperties(List<SimpleProperty> simpleProperties, SimpleProperty simpleProperty) {
		if (simpleProperties != null) {
			simpleProperties.remove(simpleProperty);
		}
	}

	@Override
	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.add(new SimpleExtendedCustomProperty("", ""));
		}
	}

	@Override
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.remove(simpleCustomProperty);
		}
	}

	public boolean hasCustomScriptError(CustomScript customScript) {
		String error = getCustomScriptError(customScript);
		return error != null;
	}

	public String getCustomScriptError(CustomScript customScript) {
		if ((customScript == null) || (customScript.getDn() == null)) {
			return null;
		}
		CustomScript currentScript = customScriptService.getCustomScriptByDn(customScript.getDn(), "oxScriptError");
		if ((currentScript != null) && (currentScript.getScriptError() != null)) {
			return currentScript.getScriptError().getStackTrace();
		}
		return null;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public List<String> getAvailableAcrs(String scriptName) {
		return new ArrayList<>(cleanAcrs(scriptName));
	}

	public void initAcrs() {
		try {
			allAcrs.clear();
			allAcrs = Stream.of(samlAcrService.getAll()).map(e -> e.getClassRef()).collect(Collectors.toList());
		} catch (Exception e) {
			log.info("", e);
		}
	}

	public String getDisplayName(String value) {
		return value;
	}

	public boolean isPersonScript(CustomScript script) {
		if (script.getScriptType() != null) {
			return script.getScriptType().getValue()
					.equalsIgnoreCase(CustomScriptType.PERSON_AUTHENTICATION.getValue());
		}
		return false;
	}

	private Set<String> cleanAcrs(String name) {
		Set<String> result = new HashSet<>();
		result.addAll(allAcrs);
		List<CustomScript> scripts = customScriptService
				.findCustomScripts(Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION));
		for (CustomScript customScript : scripts) {
			if (null == customScript.getAliases())
				customScript.setAliases(new ArrayList<>());
			if (customScript.getName() != null) {
				if (!customScript.getName().equals(name)) {
					List<String> existing = customScript.getAliases();
					if (existing != null && existing.size() > 0) {
						for (String value : existing) {
							result.remove(value);
						}
					}
				}
			}
		}
		return result;
	}

	public GluuTreeModel getTree() {
		return tree;
	}

	public void setTree(GluuTreeModel tree) {
		this.tree = tree;
	}

	public CustomScript getSelectedScript() {
		return selectedScript;
	}

	public void setSelectedScript(CustomScript selectedScript) {
		this.selectedScript = selectedScript;
	}

	public boolean isEdition() {
		return edition;
	}

	public void setEdition(boolean edition) {
		this.edition = edition;
	}

	public CustomScriptType getSelectedScriptType() {
		return selectedScriptType;
	}

	public void setSelectedScriptType(CustomScriptType selectedScriptType) {
		this.selectedScriptType = selectedScriptType;
	}

	public boolean isShowAddButton() {
		return showAddButton;
	}

	public void setShowAddButton(boolean showAddButton) {
		this.showAddButton = showAddButton;
	}

	@Override
	public void processValueSelected(TreeNodeSelectionEvent event) {
		GluuTreeModel node = (GluuTreeModel) event.getNode();
		if (event.isSelected()) {
			if (node.getDn() != null && node.getInum() != null && !node.isParent()) {
				Optional<CustomScript> customScript = customScriptService.getCustomScriptByINum(node.getDn(),
						node.getInum(), null);
				if (customScript.isPresent()) {
					this.selectedScript = customScript.get();
					this.selectedScriptType = node.getCustomScriptType();
					fillEmptyListProperty();
				}
				tree.expandParentOfNode(this.selectedScript);
			} else if (node.isParent()) {
				if (node.isExpanded()) {
					node.setExpanded(false);
				} else {
					node.setExpanded(true);
				}
				this.selectedScriptType = node.getCustomScriptType();
				setShowAddButton(true);
			}
		} else {
			if (node.isParent()) {
				node.setExpanded(false);
			} else {
				tree.closeParentOfNode(node);
			}
			setShowAddButton(false);
		}
	}

	@Override
	public void processValueExpanded(TreeNodeExpandedEvent event) {
	}

	@Override
	public void processValueChecked(TreeNodeCheckedEvent event) {
	}
}
