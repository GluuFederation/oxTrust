/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;

/**
 * Action class for helping with menu
 * 
 * @author Yuriy Movchan Date: 12/18/2012
 */
@Scope(ScopeType.STATELESS)
@Named("menuAction")
public class MenuAction implements Serializable {

	private static final long serialVersionUID = -172441515451149801L;

	@End(beforeRedirect = true)
	public String endConversation(final String viewId) {
		return viewId;
	}
}
