/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.event.DeleteNotifier;
import org.gluu.persist.model.AttributeData;
import org.slf4j.Logger;

@Stateless
@Named
public class LdifArchiver implements DeleteNotifier {

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager persistenceManager;	

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

	public void onBeforeRemove(String dn, String[] objectClasses) {
		if (!disable) {
			String dnForRemoval = dn;
			int dnForRemovalLenght = dnForRemoval.length();
			if (dnForRemovalLenght > 200) {
				dnForRemoval = dn.substring(dnForRemovalLenght - 200, dnForRemovalLenght);
			}
			File file = new File(storeDir + File.separator + dnForRemoval + Calendar.getInstance().getTimeInMillis());

			try (PrintWriter writer = new PrintWriter(file);) {
				List<AttributeData> exportEntry = persistenceManager.exportEntry(dn, objectClasses[0]);
				if (exportEntry != null && exportEntry.size() >= 0) {
					writer.println("dn: " + dn);
					exportEntry.forEach(v -> {
						String key = v.getName();
						for (Object value : v.getValues()) {
							writer.println(key + ": " + value);
						}
					});
				}

				writer.println();
				writer.flush();
			} catch (FileNotFoundException e) {
				log.error("Failed to write into log file", e);
			}
		}

	}

	public void onAfterRemove(String dn, String[] objectClasses) {
		// TODO Auto-generated method stub

	}

}
