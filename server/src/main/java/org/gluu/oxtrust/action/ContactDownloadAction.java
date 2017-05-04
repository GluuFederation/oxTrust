/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.ldap.service.DownloadService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.slf4j.Logger;

@Named("contactDownloadAction")
@Scope(ScopeType.EVENT)
//TODO CDI @Restrict("#{identity.loggedIn}")
public class ContactDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Inject
	private Logger log;

	@Inject(value = "#{facesContext.externalContext}")
	private ExternalContext extCtx;

	@SuppressWarnings("seam-unresolved-variable")
	@Inject
	protected GluuCustomPerson currentPerson;

	@Inject(value = "#{facesContext}")
	FacesContext facesContext;

	@Inject
	private DownloadService downloadService;

	public String download() {
		HttpServletResponse response = (HttpServletResponse) extCtx.getResponse();
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment; filename=\"" + currentPerson.getDisplayName() + "_contacts.csv\"");
		try {
			ServletOutputStream os = response.getOutputStream();
			os.write(downloadService.contactsAsCSV(currentPerson));
			os.flush();
			os.close();
			facesContext.responseComplete();
		} catch (Exception e) {
			log.error("\nFailure : " + e.toString() + "\n");
		}

		return null;
	}
}
