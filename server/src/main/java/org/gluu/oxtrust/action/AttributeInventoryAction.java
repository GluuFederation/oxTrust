/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.LdifService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Action class for displaying attributes
 * 
 * @author Yuriy Movchan Date: 10.17.2010
 */
@Scope(CONVERSATION)
@Name("attributeInventoryAction")
@Restrict("#{identity.loggedIn}")
public class AttributeInventoryAction implements Serializable {

	private static final long serialVersionUID = -3832167044333943686L;

	private boolean showInactive = false;

	@Logger
	private Log log;

	private List<GluuAttribute> attributeList;

	@In
	private AttributeService attributeService;
	
	@In(value = "#{facesContext.externalContext}")
	private ExternalContext extCtx;	

	@In(value = "#{facesContext}")
	FacesContext facesContext;

	private List<GluuAttribute> activeAttributeList;
	
	@In
	private LdifService ldifService;
	
	
	private Map<String, Boolean> checked = new HashMap<String, Boolean>();
	

	public Map<String, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(Map<String, Boolean> checked) {
		this.checked = checked;
	}

	@Restrict("#{s:hasPermission('attribute', 'access')}")
	public String start() {
		if (attributeList == null) {
			try {
				this.attributeList = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
				this.setActiveAttributeList(attributeService.getAllActivePersonAttributes(GluuUserRole.ADMIN));
			} catch (LdapMappingException ex) {
				log.error("Failed to load attributes", ex);

				return OxTrustConstants.RESULT_FAILURE;
			}
		}

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
        List<String> checkedItems = new ArrayList<String>();

        for (GluuAttribute item : activeAttributeList) {
            if (checked.get(item.getInum())) {
                checkedItems.add(item.getInum());
            }
        }
        log.info("the selections are : {0}", checkedItems.size());
        HttpServletResponse response = (HttpServletResponse) extCtx.getResponse();
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
}
      