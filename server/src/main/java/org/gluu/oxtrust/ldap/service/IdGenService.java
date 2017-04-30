/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;


import org.gluu.oxtrust.service.external.ExternalIdGeneratorService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.log.Log;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

/**
 * @author Yuriy Movchan
 * @version 0.1, 01/16/2015
 */

@Scope(ScopeType.STATELESS)
@Named("idGenService")
@AutoCreate
public class IdGenService {

    @Logger
    private Log log;
    
    @Inject
    private ExternalIdGeneratorService externalIdGenerationService;

    public static IdGenService instance() {
		return (IdGenService) Component.getInstance(IdGenService.class);
    }

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
