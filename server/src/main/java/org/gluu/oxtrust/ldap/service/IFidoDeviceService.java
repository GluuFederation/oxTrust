/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.util.List;

import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;

import org.gluu.search.filter.Filter;

/**
 * @author Val Pecaoco
 */
public interface IFidoDeviceService {

	String getDnForFidoDevice(String userId, String id);

	GluuCustomFidoDevice getGluuCustomFidoDeviceById(String userId, String id);

	void updateGluuCustomFidoDevice(GluuCustomFidoDevice gluuCustomFidoDevice);

	void removeGluuCustomFidoDevice(GluuCustomFidoDevice gluuCustomFidoDevice);

	List<GluuCustomFidoDevice> searchFidoDevices(String userInum, String ... returnAttributes) throws Exception;
}
