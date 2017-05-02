/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import com.unboundid.ldap.sdk.Filter;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.seam.annotations.*;
import org.slf4j.Logger;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import java.io.Serializable;
import java.util.List;

/**
 * @author Val Pecaoco
 */
@Stateless
@Named("fidoDeviceService")
public class FidoDeviceService implements IFidoDeviceService, Serializable {

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private LdapEntryManager ldapEntryManager;

	public static IFidoDeviceService instance() {
		return (IFidoDeviceService) Component.getInstance(FidoDeviceService.class);
	}

	@Override
	public String getDnForFidoDevice(String userId, String id) {

		personService = PersonService.instance();

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

		personService = PersonService.instance();

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
