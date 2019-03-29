/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.python.interfaces;

import java.util.List;
import java.util.Map;

import org.gluu.model.SimpleCustomProperty;
import org.gluu.oxtrust.model.GluuCustomPerson;

/**
 * Base interface for registration python script
 *
 * @author Oleksiy Tataryn Date: 04.22.2014
 */
public interface RegistrationScript {
	public boolean execute(List<SimpleCustomProperty> list, GluuCustomPerson person, Map<String, String[]> requestParameters);
}
