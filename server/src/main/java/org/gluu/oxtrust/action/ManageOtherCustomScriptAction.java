package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.SamlAcrService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.INumGenerator;
import org.gluu.util.OxConstants;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

@Named("manageOtherScriptAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ManageOtherCustomScriptAction
		implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3741983528514587310L;

	private Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\:\\/\\.]+$");

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

	private boolean showActive;

	private List<String> allAcrs = new ArrayList<>();

	@Inject
	private SamlAcrService samlAcrService;

	public String modify() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		CustomScriptType[] allowedCustomScriptTypes = getScriptType();
		this.customScriptsByTypes = new HashMap<CustomScriptType, List<CustomScript>>();
		for (CustomScriptType customScriptType : allowedCustomScriptTypes) {
			this.customScriptsByTypes.put(customScriptType, new ArrayList<CustomScript>());
		}
		try {
			List<CustomScript> customScripts = customScriptService
					.findCustomScripts(Arrays.asList(allowedCustomScriptTypes));
			for (CustomScript customScript : customScripts) {
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

	public boolean isShowActive() {
		return showActive;
	}

	public void setShowActive(boolean showActive) {
		this.showActive = showActive;
	}

	public boolean isShowCurrentScript(CustomScript script) {
		return (showActive)? script.isEnabled(): true;
	}

	public String save() {
		try {
			List<CustomScript> oldCustomScripts = customScriptService.findCustomScripts(Arrays.asList(getScriptType()),
					"dn", "inum");
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
						customScriptId = INumGenerator.generate(2);
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

			// Remove removed scripts
			for (CustomScript oldCustomScript : oldCustomScripts) {
				if (!updatedInums.contains(oldCustomScript.getInum())) {
					customScriptService.remove(oldCustomScript);
				}
			}
		} catch (Exception ex) {
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

	public void setAllAcrs(List<String> allAcrs) {
		this.allAcrs = allAcrs;
	}

	public void resetAcrs(CustomScript script) {
		script.setAliases(new ArrayList<>());
	}

	public List<String> getAllAcrs() {
		return allAcrs;
	}

	public CustomScriptType[] getScriptType() {
		return this.configurationService.getOthersCustomScriptTypes();
	}

}
