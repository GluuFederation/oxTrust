/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;


import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.service.external.ExternalIdGeneratorService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * @author Yuriy Movchan
 * @version 0.1, 01/16/2015
 */

@Stateless
@Named
public class IdGenService {

    @Inject
    private Logger log;
    
    @Inject
    private ExternalIdGeneratorService externalIdGenerationService;

    public String generateId(String orgInum, String prefix) {
		if (StringHelper.isEmptyString(orgInum) || StringHelper.isEmptyString(prefix)) {
			return "";
		}

		String newPrefix = orgInum + OxTrustConstants.inumDelimiter + prefix;

		if (externalIdGenerationService.isEnabled()) {
    		final String generatedId = externalIdGenerationService.executeExternalDefaultGenerateIdMethod("oxtrust", "", newPrefix);

    		if (StringHelper.isNotEmpty(generatedId)) {
    			return generatedId;
    		}
    	}
    	
    	return generateDefaultId(newPrefix);
    }
	public String generateDefaultId(String newPrefix) {
		return newPrefix + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

}
