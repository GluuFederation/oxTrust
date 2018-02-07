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

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.model.ListViewResponse;
import org.gluu.persist.model.SortOrder;
import org.gluu.persist.model.base.SimpleBranch;
import org.slf4j.Logger;
import org.gluu.search.filter.Filter;

/**
 * @author Val Pecaoco
 * Updated by jgomer on 2017-10-22
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
		    String dn=getDnForFidoDevice(userId, id);
		    if (StringUtils.isNotEmpty(userId))
                gluuCustomFidoDevice = ldapEntryManager.find(GluuCustomFidoDevice.class, dn);
		    else{
		        Filter filter=Filter.createEqualityFilter("oxId", id);
		        gluuCustomFidoDevice = ldapEntryManager.findEntries(dn, GluuCustomFidoDevice.class, filter).get(0);
            }
			//Filter filter = Filter.create("oxId=" + id);
			//gluuCustomFidoDevice = searchFidoDevice(filter, userId, id);
		}
		catch (Exception e) {
			log.error("Failed to find device by id " + id, e);
		}

		return gluuCustomFidoDevice;
	}

	public GluuCustomFidoDevice searchFidoDevice(Filter filter, String userId, String id) throws Exception {
		GluuCustomFidoDevice gluuCustomFidoDevice = null;

		List<GluuCustomFidoDevice> gluuCustomFidoDevices = ldapEntryManager.findEntries(getDnForFidoDevice(userId, id), GluuCustomFidoDevice.class, filter, 1);
		if (gluuCustomFidoDevices != null && !gluuCustomFidoDevices.isEmpty()) {
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
		ldapEntryManager.removeRecursively(gluuCustomFidoDevice.getDn());
	}
	
	@Override
	public 	List<GluuCustomFidoDevice> searchFidoDevices(String userInum, String ... returnAttributes) {
		
		if(containsBranch(userInum)){	
			String baseDnForU2fDevices = getDnForFidoDevice(userInum,null);	
			return ldapEntryManager.findEntries(baseDnForU2fDevices, GluuCustomFidoDevice.class, null, returnAttributes);
		}
		return null;
	}
	
	private boolean containsBranch(final String userInum) {
		return ldapEntryManager.contains(SimpleBranch.class, getDnForFidoDevice(userInum,null));
	}

}
