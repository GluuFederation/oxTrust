/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuCustomPerson;

@ApplicationScoped
@Named("downloadService")
public class DownloadService implements Serializable {

	private static final long serialVersionUID = -6847131971095468865L;

	public byte[] contactsAsCSV(GluuCustomPerson currentPerson) {
		String output = "";
		return output.replace("\n,", "\n").substring(1).getBytes();
	}

}
