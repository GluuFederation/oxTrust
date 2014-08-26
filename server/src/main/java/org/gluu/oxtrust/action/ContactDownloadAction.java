package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.gluu.oxtrust.ldap.service.DownloadService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

@Name("contactDownloadAction")
@Scope(ScopeType.EVENT)
@Restrict("#{identity.loggedIn}")
public class ContactDownloadAction implements Serializable {

	private static final long serialVersionUID = 6486111971437252913L;

	@Logger
	private Log log;

	@In(value = "#{facesContext.externalContext}")
	private ExternalContext extCtx;

	@In
	protected GluuCustomPerson currentPerson;

	@In(value = "#{facesContext}")
	FacesContext facesContext;

	@In
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
