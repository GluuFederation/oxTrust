/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.model.SubversionFile;
import org.gluu.oxtrust.util.svn.SvnHelper;
import org.slf4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.admin.ISVNAdminAreaFactorySelector;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.util.StringHelper;

/**
 * Provides operations with SVN
 * 
 * @author Yuriy Movchan Date: 11.25.2010
 */
@Stateless
@Named("subversionService")
public class SubversionService {

	@Inject
	private Logger log;

	@Inject
	private AppConfiguration appConfiguration;
	
	@Inject
	private EncryptionService encryptionService;

	final private static String baseSvnDir = "/var/gluu/svn";

	public boolean commitShibboleth3ConfigurationFiles(GluuOrganization organization, List<SubversionFile> newSubversionFiles,
													   List<SubversionFile> removeSubversionFiles, String svnComment) {
		// Retrieve properties and derive applianceSvnHome
		String svnUrl = appConfiguration.getSvnConfigurationStoreRoot();
		String inumFN = StringHelper.removePunctuation(appConfiguration.getApplianceInum());
		String svnPassword = appConfiguration.getSvnConfigurationStorePassword();
		String applianceSvnHomePath = String.format("%s/%s", baseSvnDir, inumFN);

		if (StringHelper.isEmpty(svnUrl) || StringHelper.isEmpty(inumFN) || StringHelper.isEmpty(svnPassword)) {
			// log.error("Failed to commit files to repository. Please check SVN related properties in gluuAppliance.properties file");
			return false;
		}

		SVNClientManager clientManager = null;
		try {
			// Decrypt password
			svnPassword = encryptionService.decrypt(svnPassword);

			// Create an instance of SVNClientManager
			log.debug("Creating an instance of SVNClientManager");
			SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl);
			clientManager = SvnHelper.getSVNClientManager(inumFN, svnPassword);

			// Check root path exists
			boolean result = checkRootSvnPath(clientManager, repositoryURL);
			if (!result) {
				return result;
			}

			File applianceSvnHome = new File(applianceSvnHomePath);

			removeFilesFromLocalRepository(applianceSvnHome, removeSubversionFiles);
			// Copy files to temporary repository folder
			copyFilesToLocalRepository(applianceSvnHome, newSubversionFiles);

			// Add files
			log.debug("Adding files if neccessary");
			SvnHelper.addNewFiles(clientManager, applianceSvnHome);

			// Commit updates to repository
			log.debug("Commiting updates to repository");
			String message = String.format("Automatic update of Shibboleth configuration files for organization %s",
					organization.getDisplayName());
			message += "\n Changes List:\n" + svnComment;
			SvnHelper.commit(clientManager, applianceSvnHome, false, message);

			return true;
		} catch (Exception ex) {
			// log.error("Failed to commit files to repository", ex);
		} finally {
			if (clientManager != null) {
				clientManager.dispose();
			}
		}

		return false;
	}

	private void removeFilesFromLocalRepository(File dir, List<SubversionFile> removeSubversionFiles) throws IOException {
		log.debug("Removing files from temporary repository folder");
		String dirFn = dir.getAbsolutePath();
		for (SubversionFile subversionFile : removeSubversionFiles) {
			String fn = getLocalRepositoryFilePath(dirFn, subversionFile);
			File f = new File(fn);
			try {
				FileUtils.forceDelete(f);
			} catch (IOException ex) {
				log.error("Failed to delete file {} from local repository folder", subversionFile.getLocalFile());
				throw ex;
			}
		}

	}

	private boolean checkRootSvnPath(SVNClientManager clientManager, SVNURL repositoryURL) throws SVNException {
		SVNRepository repository = SvnHelper.getSVNRepository(clientManager, repositoryURL);

		// Check if root path exist
		log.debug("Checking if root path exist");
		SVNNodeKind nodeKind = SvnHelper.exist(repository, "");
		if (nodeKind != SVNNodeKind.DIR) {
			log.error("Failed to commit files to repository. Please check SVN URL gluuAppliance.properties");
			return false;
		}

		return true;
	}

	private void copyFilesToLocalRepository(File dir, List<SubversionFile> subversionFiles) throws IOException {
		log.debug("Copying files to temporary repository folder");
		String dirFn = dir.getAbsolutePath();
		for (SubversionFile subversionFile : subversionFiles) {
			String fn = getLocalRepositoryFilePath(dirFn, subversionFile);
			File f = new File(fn);
			try {
				FileUtils.copyFile(new File(subversionFile.getLocalFile()), f);
			} catch (IOException ex) {
				log.error("Failed to copy file {} into local repository folder", subversionFile.getLocalFile());
				throw ex;
			}
		}
	}

	private String getLocalRepositoryFilePath(String dirFn, SubversionFile subversionFile) {
		return FilenameUtils.concat(dirFn, subversionFile.getLocalFile().replaceFirst("^/", ""));
	}

	/*
	 * Initialize singleton instance during startup
	 */
	
	public void initSubversionService() {
		String svnConfigurationStoreRoot = null;
		if (appConfiguration.isPersistSVN()) { 
			svnConfigurationStoreRoot  = appConfiguration.getSvnConfigurationStoreRoot();
		}

		SVNAdminAreaFactory.setSelector(new ISVNAdminAreaFactorySelector() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Collection getEnabledFactories(File path, Collection factories, boolean writeAccess) throws SVNException {
				Collection enabledFactories = new TreeSet();
				for (Iterator factoriesIter = factories.iterator(); factoriesIter.hasNext();) {
					SVNAdminAreaFactory factory = (SVNAdminAreaFactory) factoriesIter.next();
					int version = factory.getSupportedVersion();
					if (version == SVNAdminAreaFactory.WC_FORMAT_16) {
						enabledFactories.add(factory);
					}
				}
				return enabledFactories;
			}
		});

		if (StringHelper.isEmpty(svnConfigurationStoreRoot)) {
			log.warn("The service which commit configuration files into SVN was disabled");
			return;
		}

		SvnHelper.setupLibrary(svnConfigurationStoreRoot);
	}

	public List<SubversionFile> getDifferentFiles(List<SubversionFile> files) throws IOException {
		String inumFN = StringHelper.removePunctuation(appConfiguration.getApplianceInum());
		String applianceSvnHomePath = String.format("%s/%s", baseSvnDir, inumFN);
		File dir = new File(applianceSvnHomePath);

		log.debug("Copying files to temporary repository folder");
		String dirFn = dir.getAbsolutePath();
		List<SubversionFile> differentFiles = new ArrayList<SubversionFile>();
		for (SubversionFile subversionFile : files) {
			String fn = getLocalRepositoryFilePath(dirFn, subversionFile);
			File f = new File(fn);
			File persistedFile = new File(subversionFile.getLocalFile());
			if ((!f.exists()) || FileUtils.checksumCRC32(persistedFile) != FileUtils.checksumCRC32(f)) {
				differentFiles.add(subversionFile);
			}
		}

		return differentFiles;
	}

}
