/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.IFidoDeviceService;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

/**
 * @author Val Pecaoco
 */
@Stateless
@Named("scim2FidoDeviceService")
public class Scim2FidoDeviceService implements Serializable {

	@Inject
	private Logger log;

	@Inject
	private IFidoDeviceService fidoDeviceService;

	@Inject
	private CopyUtils2 copyUtils2;

	public FidoDevice updateFidoDevice(String id, FidoDevice fidoDevice) throws Exception {
		GluuCustomFidoDevice gluuCustomFidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(fidoDevice.getUserId(), id);
		if (gluuCustomFidoDevice == null) {
			throw new EntryPersistenceException("Scim2FidoDeviceService.updateFidoDevice(): Resource " + id + " not found");
		}

		GluuCustomFidoDevice updatedGluuCustomFidoDevice = copyUtils2.updateGluuCustomFidoDevice(fidoDevice, gluuCustomFidoDevice);

		log.info(" Setting meta: update device ");
		DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
		Date dateLastModified = DateTime.now().toDate();
		updatedGluuCustomFidoDevice.setMetaLastModified(dateTimeFormatter.print(dateLastModified.getTime()));
		if (updatedGluuCustomFidoDevice.getMetaLocation() == null || (updatedGluuCustomFidoDevice.getMetaLocation() != null && updatedGluuCustomFidoDevice.getMetaLocation().isEmpty())) {
			String relativeLocation = "/scim/v2/FidoDevices/" + id;
			updatedGluuCustomFidoDevice.setMetaLocation(relativeLocation);
		}

		fidoDeviceService.updateGluuCustomFidoDevice(gluuCustomFidoDevice);

		FidoDevice updatedFidoDevice = copyUtils2.copy(gluuCustomFidoDevice, new FidoDevice());

		return updatedFidoDevice;
	}

	public void deleteFidoDevice(String id) throws Exception {
		GluuCustomFidoDevice gluuCustomFidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(null, id);
		if (gluuCustomFidoDevice == null) {
			throw new EntryPersistenceException("Scim2FidoDeviceService.deleteFidoDevice(): Resource " + id + " not found");
		}

		fidoDeviceService.removeGluuCustomFidoDevice(gluuCustomFidoDevice);
	}
}
