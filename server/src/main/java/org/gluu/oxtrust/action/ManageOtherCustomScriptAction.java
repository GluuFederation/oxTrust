package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.SimpleExtendedCustomProperty;
import org.gluu.model.SimpleProperty;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.model.SimplePropertiesListModel;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
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

}
