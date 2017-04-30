/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.push;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.model.push.PushApplication;
import org.hibernate.annotations.common.util.StringHelper;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.log.Log;

/**
 * Allows to prepare oxPush platform specifis configurations
 * 
 * @author Yuriy Movchan Date: 02/03/2014
 */
@ApplicationScoped
@Named("pushApplicationConfigurationService")
@AutoCreate
public class PushApplicationConfigurationService implements Serializable {

	private static final long serialVersionUID = -3486468321593831158L;

	Map<String, String> supportedPlatforms;

	@Logger
	private Log log;

	@Create
	public void init() {
		this.supportedPlatforms = new HashMap<String, String>();
		
		this.supportedPlatforms.put("android", "Android");
		this.supportedPlatforms.put("ios", "Apple");
	}

	public List<String> getPlatformDescriptionList(PushApplication pushApplication) {
		List<String> result = new ArrayList<String>();
		
		List<HashMap<String, String>> platformConfigurations = pushApplication.getApplicationConfiguration().getPlatforms();
		for (HashMap<String, String> platformConfiguration : platformConfigurations) {
			String platformId = platformConfiguration.get("name");
			String platform = this.supportedPlatforms.get(platformId);
			
			if (StringHelper.isNotEmpty(platform)) {
				result.add(platform);
			}
		}

		return result;
	}

}
