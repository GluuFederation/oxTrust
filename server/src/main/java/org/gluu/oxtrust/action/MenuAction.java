/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.service.ConversationService;
import org.gluu.jsf2.service.FacesService;

/**
 * Action class for helping with menu
 * 
 * @author Yuriy Movchan Date: 12/18/2012
 */
@Stateless
@Named
public class MenuAction implements Serializable {

	private static final long serialVersionUID = -172441515451149801L;
	
	@Inject
	private ConversationService conversationService;

	@Inject
	private FacesService facesService;

	public String endConversation(final String viewId) {
		conversationService.endConversation();

		facesService.redirect(viewId + "?faces-redirect=true");

		return viewId;
	}
}
