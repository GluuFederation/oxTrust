/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.gluu.oxtrust.model.GluuTreeModel;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.OxConstants;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private AbstractCustomScriptService customScriptService;

	private Map<CustomScriptType, List<CustomScript>> customScriptsByTypes;

	private boolean initialized;

	private static Set<String> allAcrs = new HashSet<>();

	private GluuTreeModel tree;

	private boolean edition = true;
	private boolean showAddButton = false;
	private CustomScript selectedScript;

	private CustomScriptType selectedScriptType = CustomScriptType.PERSON_AUTHENTICATION;

	@PostConstruct
	private void init() {
		tree = (GluuTreeModel) new GluuTreeModel().withText("root").withSelectable(false).withExpanded(false);
		CustomScriptType[] allowedCustomScriptTypes = this.configurationService.getCustomScriptTypes();
		Stream.of(allowedCustomScriptTypes).forEach(e -> {
			GluuTreeModel node = (GluuTreeModel) new GluuTreeModel().withText(e.getDisplayName()).withExpanded(false);
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
		setSelectedScript(
				customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.PERSON_AUTHENTICATION)).get(0));
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
	}

	public String modify() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		CustomScriptType[] allowedCustomScriptTypes = this.configurationService.getCustomScriptTypes();
		this.customScriptsByTypes = new HashMap<CustomScriptType, List<CustomScript>>();
		for (CustomScriptType customScriptType : allowedCustomScriptTypes) {
			this.customScriptsByTypes.put(customScriptType, new ArrayList<CustomScript>());
		}
		try {
			List<CustomScript> customScripts = customScriptService
					.findCustomScripts(Arrays.asList(allowedCustomScriptTypes));
			for (CustomScript customScript : customScripts) {
				// Automatic package update '.xdi' --> '.org'
				// TODO: Remove in CE 5.0
				String scriptCode = customScript.getScript();
				if (scriptCode != null) {
					scriptCode = scriptCode.replaceAll(".xdi", ".gluu");
					customScript.setScript(scriptCode);
				}
				CustomScriptType customScriptType = customScript.getScriptType();
				List<CustomScript> customScriptsByType = this.customScriptsByTypes.get(customScriptType);
				CustomScript typedCustomScript = customScript;
				if (CustomScriptType.PERSON_AUTHENTICATION == customScriptType) {
					typedCustomScript = new AuthenticationCustomScript(customScript);
				}
				if (typedCustomScript.getConfigurationProperties() == null) {
					typedCustomScript.setConfigurationProperties(new ArrayList<SimpleExtendedCustomProperty>());
				}
				if (typedCustomScript.getModuleProperties() == null) {
					typedCustomScript.setModuleProperties(new ArrayList<SimpleCustomProperty>());
				}
				customScriptsByType.add(typedCustomScript);
			}
		} catch (Exception ex) {
			log.error("Failed to load custom scripts ", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load custom scripts");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.initialized = true;
		return OxTrustConstants.RESULT_SUCCESS;
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
			String dn = customScript.getDn();
			String customScriptId = customScript.getInum();
			if (StringHelper.isEmpty(dn)) {
				customScriptId = UUID.randomUUID().toString();
				dn = customScriptService.buildDn(customScriptId);
				customScript.setDn(dn);
				customScript.setInum(customScriptId);
				this.edition = false;
			}
			customScript.setDn(dn);
			customScript.setInum(customScriptId);
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
			} else {
				customScriptService.add(customScript);
				this.edition = true;
				facesMessages.add(FacesMessage.SEVERITY_INFO, customScript.getName() + " added successfully");
			}
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Error when processing " + customScript.getName());
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String save() {
		try {
			List<CustomScript> oldCustomScripts = customScriptService
					.findCustomScripts(Arrays.asList(this.configurationService.getCustomScriptTypes()), "dn", "inum");
			List<String> updatedInums = new ArrayList<String>();
			for (Entry<CustomScriptType, List<CustomScript>> customScriptsByType : this.customScriptsByTypes
					.entrySet()) {
				List<CustomScript> customScripts = customScriptsByType.getValue();
				for (CustomScript customScript : customScripts) {
					String configId = customScript.getName();
					if (StringHelper.equalsIgnoreCase(configId, OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME)) {
						facesMessages.add(FacesMessage.SEVERITY_ERROR, "'%s' is reserved script name", configId);
						return OxTrustConstants.RESULT_FAILURE;
					}
					boolean nameValidation = NAME_PATTERN.matcher(customScript.getName()).matches();
					if (!nameValidation) {
						facesMessages.add(FacesMessage.SEVERITY_ERROR,
								"'%s' is invalid script name. Only alphabetic, numeric and underscore characters are allowed in Script Name",
								configId);
						return OxTrustConstants.RESULT_FAILURE;
					}
					customScript.setRevision(customScript.getRevision() + 1);
					boolean update = true;
					String dn = customScript.getDn();
					String customScriptId = customScript.getInum();
					if (StringHelper.isEmpty(dn)) {
						customScriptId = UUID.randomUUID().toString();
						dn = customScriptService.buildDn(customScriptId);
						customScript.setDn(dn);
						customScript.setInum(customScriptId);
						update = false;
					}
					customScript.setDn(dn);
					customScript.setInum(customScriptId);
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
					updatedInums.add(customScriptId);
					if (update) {
						customScriptService.update(customScript);
					} else {
						customScriptService.add(customScript);
					}
				}
			}
			for (CustomScript oldCustomScript : oldCustomScripts) {
				if (!updatedInums.contains(oldCustomScript.getInum())) {
					customScriptService.remove(oldCustomScript);
				}
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to update custom scripts", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update custom script configuration");
			return OxTrustConstants.RESULT_FAILURE;
		}
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Custom script configuration updated successfully");
		return OxTrustConstants.RESULT_SUCCESS;
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

	public void removeCustomScript(CustomScript removeCustomScript) {
		for (Entry<CustomScriptType, List<CustomScript>> customScriptsByType : this.customScriptsByTypes.entrySet()) {
			List<CustomScript> customScripts = customScriptsByType.getValue();
			for (Iterator<CustomScript> iterator = customScripts.iterator(); iterator.hasNext();) {
				CustomScript customScript = iterator.next();
				if (System.identityHashCode(removeCustomScript) == System.identityHashCode(customScript)) {
					iterator.remove();
					return;
				}
			}
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
			File file = new File("/opt/shibboleth-idp/conf/authn/general-authn.xml");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			document.getDocumentElement().normalize();
			NodeList nodes = document.getElementsByTagName("util:list");
			NodeList childNodes = nodes.item(0).getChildNodes();
			Element element = null;
			for (int index = 0; index < childNodes.getLength(); index++) {
				Node node = childNodes.item(index);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					String id = e.getAttribute("id");
					if (id.equalsIgnoreCase("authn/oxAuth")) {
						element = e;
						break;
					}
				}
			}
			if (element != null) {
				NodeList items = element.getElementsByTagName("bean");
				for (int i = 0; i < items.getLength(); i++) {
					Element node = (Element) items.item(i);
					allAcrs.add(node.getAttribute("c:classRef"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		List<CustomScript> scripts = customScriptsByTypes.get(CustomScriptType.PERSON_AUTHENTICATION);
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

	@Override
	public void processValueSelected(TreeNodeSelectionEvent event) {
		if (event.isSelected()) {
			GluuTreeModel node = (GluuTreeModel) event.getNode();
			if (node.getDn() != null && node.getInum() != null && !node.isParent()) {
				Optional<CustomScript> customScript = customScriptService.getCustomScriptByINum(node.getDn(),
						node.getInum(), null);
				if (customScript.isPresent()) {
					selectedScript = customScript.get();
				}
			}
			if (node.isParent()) {
				this.selectedScriptType = node.getCustomScriptType();
				setShowAddButton(true);
				facesMessages.add(FacesMessage.SEVERITY_INFO, "Current type:" + node.getCustomScriptType().getValue());
			}
		}
	}

	@Override
	public void processValueChecked(TreeNodeCheckedEvent event) {
	}

	@Override
	public void processValueExpanded(TreeNodeExpandedEvent event) {

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
}
