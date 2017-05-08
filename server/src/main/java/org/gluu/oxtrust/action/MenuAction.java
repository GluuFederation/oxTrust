/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Named;

import javax.faces.application.FacesMessage;
import org.jboss.seam.annotations.End;

/**
 * Action class for helping with menu
 * 
 * @author Yuriy Movchan Date: 12/18/2012
 */
@Stateless
@Named("menuAction")
public class MenuAction implements Serializable {

	private static final long serialVersionUID = -172441515451149801L;

	@End(beforeRedirect = true)
	public String endConversation(final String viewId) {
		return viewId;
	}
}
