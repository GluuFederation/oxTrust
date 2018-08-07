/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.gluu.jsf2.io.ResponseHelper;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.model.GluuImage;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;
import org.xdi.service.security.Secure;
import org.xdi.util.io.FileDownloader;
import org.xdi.util.io.FileDownloader.ContentDisposition;

/**
 * Action class for download scope descriptions
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@RequestScoped
@Named
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class ScopeDescriptionDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Inject
	private Logger log;

	@Inject
	protected ScopeDescriptionService scopeDescriptionService;

	@Inject
	protected ImageService imageService;

	private String scopeId;
	private boolean download;

	public void downloadIcon() {
		byte resultFile[] = null;

        UmaScopeDescription scopeDescription = getScopeDescription();

		if (scopeDescription != null) {
			GluuImage gluuImage = imageService.getGluuImageFromXML(scopeDescription.getFaviconImageAsXml());
			try {
				resultFile = imageService.getThumImageData(gluuImage);
			} catch (Exception ex) {
				log.error("Failed to generate image response", ex);
			}
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();

		if (resultFile == null) {
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
			FileDownloader.sendError(response, "Failed to prepare icon");
		} else {
			ContentDisposition contentDisposition = download ? ContentDisposition.ATTACHEMENT : ContentDisposition.NONE;
			ResponseHelper.downloadFile(scopeDescription.getId() + ".jpg", "image/jpeg", resultFile, contentDisposition, facesContext);
		}
	}

	private UmaScopeDescription getScopeDescription() {
		try {
			scopeDescriptionService.prepareScopeDescriptionBranch();
		} catch (Exception ex) {
			log.error("Failed to initialize download action", ex);
			return null;
		}

		log.debug("Loading UMA scope description '{}'", this.scopeId);
        UmaScopeDescription scopeDescription;
		try {
			List<UmaScopeDescription> scopeDescriptions = scopeDescriptionService.findScopeDescriptionsById(this.scopeId);
			if (scopeDescriptions.size() != 1) {
				log.error("Failed to find scope description '{}'. Found: '{}'", this.scopeId, scopeDescriptions.size());
				return null;
			}

			scopeDescription = scopeDescriptions.get(0);
		} catch (BasePersistenceException ex) {
			log.error("Failed to find scope description '{}'", this.scopeId, ex);
			return null;
		}

		return scopeDescription;
	}

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
