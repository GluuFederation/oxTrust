package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.DeleteNotifier;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.config.oxtrust.ApplicationConfiguration;

public class LdifArchiver implements DeleteNotifier {

	LdapEntryManager ldapEntryManager;

	private static final Logger log = LoggerFactory.getLogger(LdifArchiver.class);

	private String storeDir;

	private boolean disable;

	public LdifArchiver(LdapEntryManager ldapEntryManager) {
		ApplicationConfiguration applicationConfiguration = OxTrustConfiguration.instance().getApplicationConfiguration();
		storeDir = applicationConfiguration.getLdifStore();
		if (storeDir != null) {
			File store = new File(storeDir);
			store.mkdirs();
		} else {
			disable = true;
		}

		this.ldapEntryManager = ldapEntryManager;
	}

	public void onBeforeRemove(String dn) {
		if (!disable) {
			File file = new File(storeDir + dn + Calendar.getInstance().getTimeInMillis());
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
