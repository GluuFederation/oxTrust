/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.render;

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Components;
import org.slf4j.Logger;

/**
 * Custom JSF2 renderer
 * 
 * @author Yuriy Movchan Date: 11/19/2017
 */
@Stateless
@Named("renderService")
public class RenderService implements Serializable {

	private static final long serialVersionUID = -820746838757282684L;

	@Inject
	private Logger log;

	public String renderView(String viewId) {
		String html;
		try {
			html = Components.encodeHtml(Components.buildView(viewId));
		} catch (IOException ex) {
			log.error("Failed to render viewId: '{}'", ex, viewId);
			
			return null;
		}

		return html;
	}
}
