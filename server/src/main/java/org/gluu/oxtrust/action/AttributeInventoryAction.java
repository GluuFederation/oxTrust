/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.LdifService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;
import org.xdi.model.user.UserRole;
import org.xdi.service.security.Secure;

/**
 * Action class for displaying attributes
 * 
 * @author Yuriy Movchan Date: 10.17.2010
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('attribute', 'access')}")
public class AttributeInventoryAction implements Serializable {

	private static final long serialVersionUID = -3832167044333943686L;

	private boolean showInactive = false;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	private List<GluuAttribute> attributeList;

	@Inject
	private AttributeService attributeService;

	private List<GluuAttribute> activeAttributeList;
	
	@Inject
	private LdifService ldifService;

	private Map<String, Boolean> checked = new HashMap<String, Boolean>();
	private boolean initialized;

	public Map<String, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(Map<String, Boolean> checked) {
		this.checked = checked;
	}

	public String start() {
		if (attributeList == null) {
			try {
				this.attributeList = attributeService.getAllPersonAttributes(UserRole.ADMIN);
				this.setActiveAttributeList(attributeService.getAllActivePersonAttributes(UserRole.ADMIN));
			} catch (BasePersistenceException ex) {
				log.error("Failed to load attributes", ex);

				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load attributes");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_FAILURE;
			}
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<GluuAttribute> getAttributeList() {
		if (showInactive) {
			return attributeList;
		} else {
			return activeAttributeList;
		}

	}

	/**
	 * @return the showInactive
	 */
	public boolean isShowInactive() {
		return showInactive;
	}

	/**
	 * @param showInactive
	 *            the showInactive to set
	 */
	public void setShowInactive(boolean showInactive) {
		this.showInactive = showInactive;
	}

	public void toggleShowInactive() {
		this.showInactive = !showInactive;
		for (GluuAttribute attribute : attributeList) {
			if (attribute.getStatus().equals(GluuStatus.INACTIVE)) {

			}
		}
	}

	/**
	 * @return the activeAttributeList
	 */
	public List<GluuAttribute> getActiveAttributeList() {
		return activeAttributeList;
	}

	/**
	 * @param activeAttributeList
	 *            the activeAttributeList to set
	 */
	public void setActiveAttributeList(List<GluuAttribute> activeAttributeList) {
		this.activeAttributeList = activeAttributeList;
	}

    public void submit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
    	
    	List<String> checkedItems = new ArrayList<String>();

        for (GluuAttribute item : activeAttributeList) {
            if (checked.get(item.getInum())) {
                checkedItems.add(item.getInum());
            }
        }
        log.info("the selections are : {}", checkedItems.size());
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment; filename=\"attributes.ldif\"");
		try {
			ServletOutputStream os = response.getOutputStream();
			ldifService.exportLDIFFile(checkedItems,os);
			os.flush();
			os.close();
			facesContext.responseComplete();
		} catch (Exception e) {
			log.error("\nFailure : " + e.toString() + "\n");
		}
        checked.clear();
    }

	public boolean isInitialized() {
		return initialized;
	}

}
      