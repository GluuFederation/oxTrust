/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.HashMap;
import java.util.List;

import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Provides operations with view id
 * 
 * @author Yuriy Movchan
 * @version 0.1, 12/14/2012
 */
@Name("viewHandlerService")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ViewHandlerService {

	@In
	private FacesContext facesContext;

	@In(value = "#{facesContext.externalContext}")
	private ExternalContext externalContext;

	@In(value = "#{facesContext.application.viewHandler}")
	private ViewHandler viewHandler;

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