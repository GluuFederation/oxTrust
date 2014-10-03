/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.util.StringHelper;

@Name("fileViewerAction")
@Scope(ScopeType.STATELESS)
@Restrict("#{identity.loggedIn}")
public class FileViewerAction implements Serializable {

	private static final long serialVersionUID = 3968626531612060143L;

	@Logger
	private Log log;

	public String getString(String fileName) {
		if (StringHelper.isNotEmpty(fileName)) {
			try {
				return FileUtils.readFileToString(new File(fileName));
			} catch (IOException ex) {
				log.error("Failed to read file: '{0}'", ex, fileName);
			}
		}

		return "invalid file name: " + fileName;
	}
}
