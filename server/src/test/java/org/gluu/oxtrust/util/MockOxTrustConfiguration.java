package org.gluu.oxtrust.util;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Scope(ScopeType.APPLICATION)
@Name("oxTrustConfiguration")
@Install(precedence = Install.MOCK)
@Startup
public class MockOxTrustConfiguration extends OxTrustConfiguration {

	@Override
	public void init() {
		// do not init
	}

	@Override
	public void create() {
		// do not create
	}
	

}
