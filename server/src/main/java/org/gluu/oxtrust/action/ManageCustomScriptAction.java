/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.model.SimpleProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.custom.script.model.auth.AuthenticationCustomScript;
import org.xdi.service.custom.script.AbstractCustomScriptService;
import org.xdi.service.security.Secure;
import org.xdi.util.INumGenerator;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Add/Modify custom script configurations
 * 
 * @author Yuriy Movchan Date: 12/29/2014
 */
@Named("manageCustomScriptAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ManageCustomScriptAction implements SimplePropertiesListModel, SimpleCustomPropertiesListModel, Serializable {

	private static final long serialVersionUID = -3823022039248381963L;

	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final String STR_RESERVED_NAME_ERR = "'%s' is reserved script name";

    private static final String STR_INVALID_SCRIPT_NAME_ERR = "'%s' is invalid script name. Only alphabetic, numeric and underscore characters are allowed in Script Name";


    @Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private AbstractCustomScriptService customScriptService;
	
	@Inject
	private AppConfiguration appConfiguration;

	private Map<CustomScriptType, List<CustomScript>> customScriptsByTypes;

	private boolean initialized;
	
	public String modify() {
		if (this.initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		
		CustomScriptType[] allowedCustomScriptTypes = this.applianceService.getCustomScriptTypes();
        this.customScriptsByTypes = new HashMap<CustomScriptType, List<CustomScript>>();

		try {

            final List<CustomScript> customScripts = customScriptService.findCustomScripts(Arrays.asList(allowedCustomScriptTypes));
			customScriptsByTypes = loadScriptsMap(allowedCustomScriptTypes, customScripts );

		} catch (Exception ex) {
			log.error("Failed to load custom scripts ", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load custom scripts");

			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public static Map<CustomScriptType, List<CustomScript>> loadScriptsMap(CustomScriptType[] allowedCustomScriptTypes, List<CustomScript> customScriptList) {

		final Map<CustomScriptType, List<CustomScript>> customScriptsByTypes = new HashMap<CustomScriptType, List<CustomScript>>();

		for (CustomScriptType customScriptType : allowedCustomScriptTypes) {
			customScriptsByTypes.put(customScriptType, new ArrayList<CustomScript>());
		}

		for (CustomScript customScript : customScriptList) {
			CustomScriptType customScriptType = customScript.getScriptType();
			List<CustomScript> customScriptsByType = customScriptsByTypes.get(customScriptType);

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
		return customScriptsByTypes;
	}

	public String save() {
		try {

			final List<CustomScript> oldCustomScripts = customScriptService.findCustomScripts(Arrays.asList(this.applianceService.getCustomScriptTypes()), "dn", "inum");

            final Set<Entry<CustomScriptType, List<CustomScript>>> scriptByTypeEntrySet = this.customScriptsByTypes.entrySet();

            final List<String> updatedInums = saveCustomScriptList(scriptByTypeEntrySet, organizationService, customScriptService);

            removeOldCustomScripts(oldCustomScripts, updatedInums, customScriptService);


        } catch (BasePersistenceException ex) {

			log.error("Failed to update custom scripts", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update custom script configuration");
			return OxTrustConstants.RESULT_FAILURE;

		} catch (IllegalArgumentException ex) {

		    log.error(ex.getMessage(), ex);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, ex.getMessage());
		    return OxTrustConstants.RESULT_FAILURE;

        }

		facesMessages.add(FacesMessage.SEVERITY_INFO, "Custom script configuration updated successfully");

		return OxTrustConstants.RESULT_SUCCESS;
	}

    /**
     *
     * @param scriptByTypeEntrySet
     * @param organizationService
     * @param customScriptService
     * @return
     */
    public static List<String> saveCustomScriptList( Set<Entry<CustomScriptType, List<CustomScript>>> scriptByTypeEntrySet, OrganizationService organizationService, AbstractCustomScriptService customScriptService) {
        List<String> updatedInums = new ArrayList<String>();

        for (Entry<CustomScriptType, List<CustomScript>> customScriptsByType : scriptByTypeEntrySet) {
            List<CustomScript> customScripts = customScriptsByType.getValue();

            for (CustomScript customScript : customScripts) {
                validateScriptName(customScript.getName());
                saveScript(updatedInums, customScript, organizationService, customScriptService);
            }
        }

        return updatedInums;
    }

    private static boolean isValidName(String configId) {

	    if (configId == null) throw new IllegalArgumentException("Custom script name is null");

        return NAME_PATTERN.matcher(configId).matches();
    }

    public static boolean isReservedName(String configId) {

        return StringHelper.equalsIgnoreCase(configId, OxConstants.SCRIPT_TYPE_INTERNAL_RESERVED_NAME);
    }

    public static void saveScript(List<String> updatedInums, CustomScript customScript, OrganizationService organizationService, AbstractCustomScriptService customScriptService) {
        customScript.setRevision(customScript.getRevision() + 1);

        boolean update = true;
        String dn = customScript.getDn();
        String customScriptId = customScript.getInum();
        if (StringHelper.isEmpty(dn)) {
            String basedInum = organizationService.getOrganizationInum();
            customScriptId = basedInum + "!" + INumGenerator.generate(2);
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

        if ((customScript.getConfigurationProperties() != null) && (customScript.getConfigurationProperties().size() == 0)) {
            customScript.setConfigurationProperties(null);
        }

        if ((customScript.getConfigurationProperties() != null) && (customScript.getModuleProperties().size() == 0)) {
            customScript.setModuleProperties(null);
        }

        updatedInums.add(customScriptId);

        if (update) {
            customScriptService.update(customScript);
        } else {
            customScriptService.add(customScript);
        }
    }

    public static void removeOldCustomScripts(List<CustomScript> oldCustomScripts, List<String> updatedInums, AbstractCustomScriptService customScriptService) {
        // Remove removed scripts
        for (CustomScript oldCustomScript : oldCustomScripts) {
            if (!updatedInums.contains(oldCustomScript.getInum())) {
                customScriptService.remove(oldCustomScript);
            }
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
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties, SimpleCustomProperty simpleCustomProperty) {
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

	public static void validateScriptName(String configId) {

		if (isReservedName(configId)) {
            String errMsg = String.format(STR_RESERVED_NAME_ERR, configId);
            throw new IllegalArgumentException(errMsg);
        }

        boolean nameValidation = isValidName(configId);
        if (!nameValidation) {
            String errMsg = String.format(STR_INVALID_SCRIPT_NAME_ERR, configId);
            throw new IllegalArgumentException(errMsg);
        }
    }

}
