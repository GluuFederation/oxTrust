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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.model.GluuUserRole;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.LdifService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;
import org.slf4j.Logger;

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
	
	private static final String gluuAttributeObjectClass = "gluuAttribute";

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

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be between 0 and 30")
	private String searchPattern;

	private String oldSearchPattern;

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
				this.attributeList = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
				this.setActiveAttributeList(attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN));
			} catch (Exception ex) {
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

	public String search() {
		if ((this.searchPattern != null) && Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				try {
					this.attributeList = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
					this.setActiveAttributeList(attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN));
				} catch (Exception ex) {
					log.error("Failed to load attributes", ex);
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load attributes");
				}
			}
			if (searchPattern != null && !searchPattern.isEmpty() && isShowInactive()) {
				this.attributeList = attributeService.searchAttributes(this.searchPattern,
						OxTrustConstants.searchPersonsSizeLimit);
			}
			if (searchPattern != null && !searchPattern.isEmpty() && !isShowInactive()) {
				this.activeAttributeList = attributeService.searchAttributes(this.searchPattern,
						OxTrustConstants.searchPersonsSizeLimit);
			}
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find attributes", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find attributes");
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
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
			if (checked.get(item.getDn())) {
				checkedItems.add(item.getDn());
			}
		}
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment; filename=\"attributes.ldif\"");
		try (ServletOutputStream os = response.getOutputStream()) {
			ldifService.exportLDIFFile(checkedItems, os, gluuAttributeObjectClass);
			os.flush();
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
