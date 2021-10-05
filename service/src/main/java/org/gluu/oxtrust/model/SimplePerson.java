package org.gluu.oxtrust.model;

import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.SimpleUser;

/**
 * Wrapper to add reired objectClass
 *
 * @author Yuriy Movchan Date: 05/24/2021
 */
@ObjectClass("gluuPerson")
public class SimplePerson extends SimpleUser {

	private static final long serialVersionUID = -7741095209704297164L;

}
