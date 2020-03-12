/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides operations with view id
 * 
 * @author Yuriy Movchan
 * @version 0.1, 12/14/2012
 */
@Named("viewHandlerService")
@Stateless
public class ViewHandlerService {

	@Inject
	private ViewHandler viewHandler;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ExternalContext externalContext;

	public String getBookmarkableURL(String viewId, HashMap<String, List<String>> pageParams) {
		StringBuilder sb = new StringBuilder(externalContext.getRequestScheme()).append("://").append(
				externalContext.getRequestServerName());

		int port = externalContext.getRequestServerPort();
		if ((port != 80) && (port != 443)) {
			sb.append(":").append(port);
		}

		sb.append(viewHandler.getBookmarkableURL(facesContext, viewId, pageParams, true));

		return sb.toString();
	}

}