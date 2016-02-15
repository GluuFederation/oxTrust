package org.gluu.oxtrust.util.test;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("imageRepository")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Startup
public class MockImageRepository {//extends ImageRepository {

}
