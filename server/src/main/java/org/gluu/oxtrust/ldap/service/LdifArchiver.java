/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.persist.event.DeleteNotifier;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;

@Stateless
@Named
public class LdifArchiver implements DeleteNotifier {

	@Inject
	private Logger log;

	@Inject
	private LdapEntryManager ldapEntryManager;	
	@Inject
	private AppConfiguration appConfiguration;

	private String storeDir;

	private boolean disable;

	public void init() {
		storeDir = appConfiguration.getLdifStore();
		if (storeDir != null) {
			File store = new File(storeDir);
			store.mkdirs();
		} else {
			disable = true;
		}
	}

	public void onBeforeRemove(String dn) {
		if (!disable) {
			String dnForRemoval = dn;
			int dnForRemovalLenght = dnForRemoval.length();
			if (dnForRemovalLenght > 200) {
				dnForRemoval = dn.substring(dnForRemovalLenght - 200, dnForRemovalLenght);
			}
			File file = new File(storeDir + File.separator + dnForRemoval + Calendar.getInstance().getTimeInMillis());
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(file);
			} catch (FileNotFoundException e) {

				log.error("Failed to write into log file", e);
			}
			String[] ldif = ldapEntryManager.getLDIF(dn);
			for (String ldifValue : ldif) {
				writer.println(ldifValue);
			}
			writer.flush();
		}

	}

	public void onAfterRemove(String dn) {
		// TODO Auto-generated method stub

	}

}
