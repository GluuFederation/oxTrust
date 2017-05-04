/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import com.unboundid.ldap.sdk.Filter;

/**
 * @author Val Pecaoco
 */
@Stateless
@Named
public class FidoDeviceService implements IFidoDeviceService, Serializable {

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private LdapEntryManager ldapEntryManager;

	@Override
	public String getDnForFidoDevice(String userId, String id) {
		String baseDn;
		if (userId != null && !userId.isEmpty()) {
			baseDn = "ou=fido," + personService.getDnForPerson(userId);
			if (id != null && !id.isEmpty()) {
				baseDn = "oxId=" + id + "," + baseDn;
			}
		} else {
			baseDn = personService.getDnForPerson(null);
		}

		return baseDn;
	}

	@Override
	public GluuCustomFidoDevice getGluuCustomFidoDeviceById(String userId, String id) {

		GluuCustomFidoDevice gluuCustomFidoDevice = null;

		try {

			Filter filter = Filter.create("oxId=" + id);
			gluuCustomFidoDevice = searchFidoDevice(filter, userId, id);

		} catch (Exception e) {
			log.error("Failed to find device by id " + id, e);
		}

		return gluuCustomFidoDevice;
	}

	private GluuCustomFidoDevice searchFidoDevice(Filter filter, String userId, String id) throws Exception {
		GluuCustomFidoDevice gluuCustomFidoDevice = null;

		VirtualListViewResponse vlvResponse = new VirtualListViewResponse();

		List<GluuCustomFidoDevice> gluuCustomFidoDevices = ldapEntryManager.findEntriesVirtualListView(getDnForFidoDevice(userId, id), GluuCustomFidoDevice.class, filter, 1, 1, "oxId", SortOrder.ASCENDING, vlvResponse, null);

		if (gluuCustomFidoDevices != null && !gluuCustomFidoDevices.isEmpty() && vlvResponse.getTotalResults() > 0) {
			gluuCustomFidoDevice = gluuCustomFidoDevices.get(0);
		}

		return gluuCustomFidoDevice;
	}

	@Override
	public void updateGluuCustomFidoDevice(GluuCustomFidoDevice gluuCustomFidoDevice) {
		ldapEntryManager.merge(gluuCustomFidoDevice);
	}

	@Override
	public void removeGluuCustomFidoDevice(GluuCustomFidoDevice gluuCustomFidoDevice) {
		ldapEntryManager.removeWithSubtree(gluuCustomFidoDevice.getDn());
	}
}
