package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.STATELESS)
@Name("downloadService")
@AutoCreate
public class DownloadService implements Serializable {

	private static final long serialVersionUID = -6847131971095468865L;

	@In
	private AttributeService attributeService;

	public byte[] contactsAsCSV(GluuCustomPerson currentPerson) {
		String output = "";
		return output.replace("\n,", "\n").substring(1).getBytes();
	}

}
