/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;


import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

import org.gluu.oxtrust.service.external.ExternalIdGeneratorService;
import org.gluu.util.StringHelper;

/**
 * @author Yuriy Movchan
 * @version 0.1, 01/16/2015
 */

@Stateless
@Named
public class IdGenService {

    @Inject
    private ExternalIdGeneratorService externalIdGenerationService;

    public String generateId(String idType) {
		
		if (externalIdGenerationService.isEnabled()) {
    		final String generatedId = externalIdGenerationService.executeExternalDefaultGenerateIdMethod("oxtrust",idType, "");

    		if (StringHelper.isNotEmpty(generatedId)) {
    			return generatedId;
    		}
    	}
    	
    	return generateDefaultId();
    }
	public String generateDefaultId() {
		
		return UUID.randomUUID().toString();
	}

}
