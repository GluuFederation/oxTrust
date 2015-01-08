/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.custom;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xdi.service.custom.script.CustomScriptManager;

/**
 * Provides actual versions of scrips
 *
 * @author Yuriy Movchan Date: 12/03/2014
 */
@Scope(ScopeType.APPLICATION)
@Name("customScriptManager")
@AutoCreate
// Remove this class after CE 1.7 release
public class ExtendedCustomScriptManager extends CustomScriptManager {

	private static final long serialVersionUID = -3225890597520443390L;

    public static ExtendedCustomScriptManager instance() {
        return (ExtendedCustomScriptManager) Component.getInstance(ExtendedCustomScriptManager.class);
    }

}
