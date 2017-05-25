/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.inject.Named;

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
	private Conversation conversation;

	public String endConversation(final String viewId) {
		// TODO: CDI Review
		if (!conversation.isTransient()) {
			conversation.end();
		}
		return viewId;
	}
}
