/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

@Named("fileViewerAction")
@Stateless
@Secure("#{identity.loggedIn}")
public class FileViewerAction implements Serializable {

	private static final long serialVersionUID = 3968626531612060143L;

	@Inject
	private Logger log;

	public String getString(String fileName) {
		if (StringHelper.isNotEmpty(fileName)) {
			try {
				return FileUtils.readFileToString(new File(fileName),"UTF-8");
			} catch (IOException ex) {
				log.error("Failed to read file: '{}'", fileName, ex);
			}
		}
		return "invalid file name: " + fileName;
	}
}
