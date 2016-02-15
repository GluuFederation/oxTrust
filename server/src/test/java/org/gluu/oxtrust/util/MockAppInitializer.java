package org.gluu.oxtrust.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
@Startup(depends = "oxTrustConfiguration")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("appInitializer")
public class MockAppInitializer { //extends AppInitializer {

}
