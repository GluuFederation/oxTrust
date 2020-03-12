/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.DownloadService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@RequestScoped
@Named
@Secure("#{identity.loggedIn}")
public class ContactDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Inject
	private Logger log;

	@Inject
	private Identity identity;;

	@Inject
	private DownloadService downloadService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private ExternalContext externalContext;

	public String download() {
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment; filename=\"" + identity.getUser().getDisplayName() + "_contacts.csv\"");
		try {
			ServletOutputStream os = response.getOutputStream();
			os.write(downloadService.contactsAsCSV(identity.getUser()));
			os.flush();
			os.close();
			facesContext.responseComplete();
		} catch (Exception e) {
			log.error("\nFailure : " + e.toString() + "\n");
		}

		return null;
	}
}
