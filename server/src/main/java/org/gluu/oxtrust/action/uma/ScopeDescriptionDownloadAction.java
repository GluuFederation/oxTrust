package org.gluu.oxtrust.action.uma;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.ViewHandlerService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.model.GluuImage;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.util.io.FileDownloader;
import org.xdi.util.io.FileDownloader.ContentDisposition;
import org.xdi.util.io.ResponseHelper;

/**
 * Action class for download scope descriptions
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@Name("scopeDescriptionDownloadAction")
@Scope(ScopeType.EVENT)
public class ScopeDescriptionDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Logger
	private Log log;

	@In
	protected ScopeDescriptionService scopeDescriptionService;

	@In
	protected ImageService imageService;

	@In(value = "#{facesContext.externalContext}")
	private ExternalContext externalContext;

	@In(value = "#{facesContext}")
	private FacesContext facesContext;

	@In
	private ViewHandlerService viewHandlerService;

	private String scopeId;
	private boolean download;

	public void downloadFile() {
		byte resultFile[] = null;

		ScopeDescription scopeDescription = getScopeDescription();

		if (scopeDescription != null) {
			JSONObject jsonObject = new JSONObject();
			try {
				HashMap<String, List<String>> pageParams = new HashMap<String, List<String>>();
				pageParams.put("scope", Arrays.asList(scopeDescription.getId()));
				String umaScope = viewHandlerService.getBookmarkableURL("/uma/scope/scopeDescriptionFile.xhtml", pageParams);

				jsonObject.put("name", scopeDescription.getId());
				jsonObject.put("icon_uri", umaScope);

				resultFile = jsonObject.toString().getBytes("UTF-8");
			} catch (Exception ex) {
				log.error("Failed to generate json response", ex);
			}
		}

		if (resultFile == null) {
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
			FileDownloader.sendError(response, "Failed to generate json file");
		} else {
			ContentDisposition contentDisposition = download ? ContentDisposition.ATTACHEMENT : ContentDisposition.NONE;
			ResponseHelper.downloadFile(scopeDescription.getId() + ".json", "application/json;charset=UTF-8", resultFile,
					contentDisposition, facesContext);
		}
	}

	public void downloadIcon() {
		byte resultFile[] = null;

		ScopeDescription scopeDescription = getScopeDescription();

		if (scopeDescription != null) {
			GluuImage gluuImage = imageService.getGluuImageFromXML(scopeDescription.getFaviconImageAsXml());
			try {
				resultFile = imageService.getThumImageData(gluuImage);
			} catch (Exception ex) {
				log.error("Failed to generate image response", ex);
			}
		}

		if (resultFile == null) {
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
			FileDownloader.sendError(response, "Failed to prepare icon");
		} else {
			ContentDisposition contentDisposition = download ? ContentDisposition.ATTACHEMENT : ContentDisposition.NONE;
			ResponseHelper.downloadFile(scopeDescription.getId() + ".jpg", "image/jpeg", resultFile, contentDisposition, facesContext);
		}
	}

	private ScopeDescription getScopeDescription() {
		try {
			scopeDescriptionService.prepareScopeDescriptionBranch();
		} catch (Exception ex) {
			log.error("Failed to initialize download action", ex);
			return null;
		}

		log.debug("Loading UMA scope description '{0}'", this.scopeId);
		ScopeDescription scopeDescription;
		try {
			List<ScopeDescription> scopeDescriptions = scopeDescriptionService.findScopeDescriptionsById(this.scopeId);
			if (scopeDescriptions.size() != 1) {
				log.error("Failed to find scope description '{0}'. Found: '{1}'", this.scopeId, scopeDescriptions.size());
				return null;
			}

			scopeDescription = scopeDescriptions.get(0);
		} catch (LdapMappingException ex) {
			log.error("Failed to find scope description '{0}'", ex, this.scopeId);
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
