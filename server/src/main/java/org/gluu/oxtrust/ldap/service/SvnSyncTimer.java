/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.SubversionFile;
import org.javatuples.Pair;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.xdi.util.StringHelper;

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("svnSyncTimer")
public class SvnSyncTimer {

	@Logger
	Log log;

	@In
	private Shibboleth3ConfService shibboleth3ConfService;

	@In
	private SubversionService subversionService;

	@In
	private FacesMessages facesMessages;

	private LinkedBlockingQueue<Pair<GluuSAMLTrustRelationship, String>> removedTrustRelationship;

	private String svnComment = "";

	List<Pair<GluuSAMLTrustRelationship, String>> alteredTrustRelations;

	@In
	private TrustService trustService;

	@Create
	public void create() {
		removedTrustRelationship = new LinkedBlockingQueue<Pair<GluuSAMLTrustRelationship, String>>();
		alteredTrustRelations = new ArrayList<Pair<GluuSAMLTrustRelationship, String>>();
	}

	@Asynchronous
	public QuartzTriggerHandle scheduleSvnSync(@Expiration Date when, @IntervalDuration Long interval) {
		process(when, interval);
		return null;
	}

	private void process(Date when, Long interval) {
		commitShibboleth3Configuration(TrustService.instance().getAllActiveTrustRelationships());
	}

	private void commitShibboleth3Configuration(List<GluuSAMLTrustRelationship> trustRelationships) {
		synchronized (this) {
			List<SubversionFile> subversionFiles = new ArrayList<SubversionFile>();
			try {
				subversionFiles = subversionService.getDifferentFiles(shibboleth3ConfService
						.getConfigurationFilesForSubversion(trustRelationships));
			} catch (IOException e) {
				log.error("Failed to prepare files list to be persisted in svn", e);
			}

			List<SubversionFile> removeSubversionFiles = new ArrayList<SubversionFile>();
			while (!removedTrustRelationship.isEmpty()) {
				Pair<GluuSAMLTrustRelationship, String> removedRelationship = removedTrustRelationship.poll();

				SubversionFile file = shibboleth3ConfService.getConfigurationFileForSubversion(removedRelationship.getValue0());
				if (file != null) {
					removeSubversionFiles.add(file);
				}
			}
			String idpSvnComment = "";
			// Find all TRs modified not by user.
			for (SubversionFile file : subversionFiles) {
				String filename = file.getLocalFile();
				if (filename.matches(".*/DA[0-9A-F]*-sp-metadata\\.xml")) {
					boolean found = false;
					String inum = filename.replaceAll("-sp-metadata\\.xml", "").replaceAll(".*/", "");
					for (Pair<GluuSAMLTrustRelationship, String> trust : alteredTrustRelations) {
						if (StringHelper.removePunctuation(trust.getValue0().getInum()).equals(inum)) {
							found = true;
							break;
						}
					}

					if (!found) {
						GluuSAMLTrustRelationship unknownTrust = trustService.getTrustByUnpunctuatedInum(inum);
						if (unknownTrust != null) {
							idpSvnComment += "Trust relationship '" + unknownTrust.getDisplayName() + "' was updated automatically\n";
						} else {
							idpSvnComment += "Appliance have no information about  '" + filename
									+ "'. Please report this issue to appliance admin.\n";
						}
					}
				}
			}
			log.debug("Files to be persisted in repository: " + StringHelper.toString(subversionFiles.toArray(new SubversionFile[] {})));
			log.debug("Files to be removed from repository: "
					+ StringHelper.toString(removeSubversionFiles.toArray(new SubversionFile[] {})));
			if (!subversionService.commitShibboleth3ConfigurationFiles(OrganizationService.instance().getOrganization(), subversionFiles,
					removeSubversionFiles, svnComment + idpSvnComment)) {
				log.error("Failed to commit Shibboleth3 configuration to SVN repository");
				facesMessages.add(Severity.ERROR, "Failed to commit Shibboleth3 configuration to SVN repository");
			} else {
				svnComment = "";
				alteredTrustRelations.clear();
				log.info("Shibboleth3 configuration commited successfully to SVN repository");
				facesMessages.add(Severity.INFO, "Shibboleth3 configuration comited successfully to SVN repository");
			}
		}
	}

	public void removeTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) throws InterruptedException {
		removedTrustRelationship.put(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " removed trust relationship " + trustRelationship.getDisplayName() + "\n";
	}

	public void addTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) {
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " added trust relationship " + trustRelationship.getDisplayName() + "\n";
	}

	public void updateTrustRelationship(GluuSAMLTrustRelationship trustRelationship, String user) {
		alteredTrustRelations.add(new Pair<GluuSAMLTrustRelationship, String>(trustRelationship, user));
		svnComment += "User " + user + " updated trust relationship " + trustRelationship.getDisplayName() + "\n";
	}
}
