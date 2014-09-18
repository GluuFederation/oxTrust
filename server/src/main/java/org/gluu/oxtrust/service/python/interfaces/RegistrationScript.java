package org.gluu.oxtrust.service.python.interfaces;

import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.xdi.model.SimpleCustomProperty;

/**
 * Base interface for registration python script
 *
 * @author Oleksiy Tataryn Date: 04.22.2014
 */
public interface RegistrationScript {
	public boolean execute(List<SimpleCustomProperty> list, GluuCustomPerson person, Map<String, String[]> requestParameters);
}
