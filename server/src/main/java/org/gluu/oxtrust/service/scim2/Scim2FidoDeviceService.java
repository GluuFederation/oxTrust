/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import org.gluu.oxtrust.ldap.service.FidoDeviceService;
import org.gluu.oxtrust.ldap.service.IFidoDeviceService;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.fido.FidoDevice;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Val Pecaoco
 */
@Named("scim2FidoDeviceService")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class Scim2FidoDeviceService implements Serializable {

	@Logger
	private Log log;

	@Inject
	private IFidoDeviceService fidoDeviceService;

	public FidoDevice updateFidoDevice(String id, FidoDevice fidoDevice) throws Exception {

		fidoDeviceService = FidoDeviceService.instance();

		GluuCustomFidoDevice gluuCustomFidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(fidoDevice.getUserId(), id);
		if (gluuCustomFidoDevice == null) {
			throw new EntryPersistenceException("Scim2FidoDeviceService.updateFidoDevice(): Resource " + id + " not found");
		}

		GluuCustomFidoDevice updatedGluuCustomFidoDevice = CopyUtils2.updateGluuCustomFidoDevice(fidoDevice, gluuCustomFidoDevice);

		log.info(" Setting meta: update device ");
		DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
		Date dateLastModified = DateTime.now().toDate();
		updatedGluuCustomFidoDevice.setMetaLastModified(dateTimeFormatter.print(dateLastModified.getTime()));
		if (updatedGluuCustomFidoDevice.getMetaLocation() == null || (updatedGluuCustomFidoDevice.getMetaLocation() != null && updatedGluuCustomFidoDevice.getMetaLocation().isEmpty())) {
			String relativeLocation = "/scim/v2/FidoDevices/" + id;
			updatedGluuCustomFidoDevice.setMetaLocation(relativeLocation);
		}

		fidoDeviceService.updateGluuCustomFidoDevice(gluuCustomFidoDevice);

		FidoDevice updatedFidoDevice = CopyUtils2.copy(gluuCustomFidoDevice, new FidoDevice());

		return updatedFidoDevice;
	}

	public void deleteFidoDevice(String id) throws Exception {

		fidoDeviceService = FidoDeviceService.instance();

		GluuCustomFidoDevice gluuCustomFidoDevice = fidoDeviceService.getGluuCustomFidoDeviceById(null, id);
		if (gluuCustomFidoDevice == null) {
			throw new EntryPersistenceException("Scim2FidoDeviceService.deleteFidoDevice(): Resource " + id + " not found");
		}

		fidoDeviceService.removeGluuCustomFidoDevice(gluuCustomFidoDevice);
	}
}
