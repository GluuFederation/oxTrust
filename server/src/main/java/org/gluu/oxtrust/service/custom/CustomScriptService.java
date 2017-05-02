/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.custom;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.xdi.service.custom.script.AbstractCustomScriptService;

/**
 * Operations with custom scripts
 *
 * @author Yuriy Movchan Date: 12/03/2014
 */
@Stateless
@Named("customScriptService")
public class CustomScriptService extends AbstractCustomScriptService{

	private static final long serialVersionUID = -5283102477313448031L;

    public String baseDn() {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		return String.format("ou=scripts,%s", orgDn);
    }

}
