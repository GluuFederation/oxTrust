/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.service.ImageService;
import org.gluu.oxtrust.service.uma.UmaScopeService;
import org.gluu.service.security.Secure;

/**
 * Action class for download scope descriptions
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@RequestScoped
@Named
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class UmaScopeDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Inject
	protected UmaScopeService scopeDescriptionService;

	@Inject
	protected ImageService imageService;

	private String scopeId;
	private boolean download;

	
	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

}
