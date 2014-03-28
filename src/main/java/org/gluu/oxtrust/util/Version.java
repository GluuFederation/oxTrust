package org.gluu.oxtrust.util;

/**
 * Constants with current build info
 * 
 * @author Yuriy Movchan Date: 12.17.2010
 */
public final class Version {

	public static final String GLUU_SVN_REVISION_VERSION = "${revisionVersion}";
	public static final String GLUU_SVN_REVISION_DATE = "${revisionDate}";
	public static final String GLUU_BUILD_DATE = "201110301211";
	public static final String GLUU_HUDSON_BUILDNO = "${env.BUILD_NUMBER}";

}
