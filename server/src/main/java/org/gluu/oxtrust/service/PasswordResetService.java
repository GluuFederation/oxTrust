/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.BatchOperation;
import org.gluu.persist.model.ProcessBatchOperation;
import org.gluu.persist.model.SearchScope;
import org.gluu.persist.model.base.SimpleBranch;
import org.gluu.search.filter.Filter;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Provides operations with password reset requests
 * 
 * @author Yuriy Movchan Date: 09/01/2018
 */
@Stateless
@Named("passwordResetService")
public class PasswordResetService implements Serializable {

	private static final long serialVersionUID = -4107684257624615866L;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private Logger log;
	@Inject
	private CleanUpLogger logger;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("resetPasswordRequests");
		branch.setDn(getDnForPasswordResetRequest(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(getDnForPasswordResetRequest(null), SimpleBranch.class);
	}

	public void prepareBranch() {
        String baseDn = getDnForPasswordResetRequest(null);
        if (!ldapEntryManager.hasBranchesSupport(baseDn)) {
        	return;
        }

		// Create reset password requests branch if needed
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Get password reset request by DN
	 * 
	 * @param DN
	 *            password reset request DN
	 * @return PasswordResetRequest Password reset request
	 */
	public PasswordResetRequest findPasswordResetRequest(String guid) {
		String passwordResetRequestDn = getDnForPasswordResetRequest(guid);

		return ldapEntryManager.find(PasswordResetRequest.class, passwordResetRequestDn);

	}

	/**
	 * Add new password reset request
	 * 
	 * @param PasswordResetRequest
	 *            Password reset request
	 */
	public void addPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
        ldapEntryManager.persist(passwordResetRequest);
	}

	/**
	 * Update new password reset request
	 * 
	 * @param PasswordResetRequest
	 *            Password reset request
	 */
	public void updatePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		ldapEntryManager.merge(passwordResetRequest);
	}

	/**
	 * Remove new password reset request
	 * 
	 * @param PasswordResetRequest
	 *            Password reset request
	 */
	public void removePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		ldapEntryManager.remove(passwordResetRequest);
	}

	/**
	 * Check if there is password reset request with specified attributes
	 * 
	 * @return True if password reset request with specified attributes exist
	 */
	public boolean containsPasswordResetRequest(String dn) {
		return ldapEntryManager.contains(dn, PasswordResetRequest.class);
	}

	/**
	 * Get password reset requests by example
	 * 
	 * @param PasswordResetRequest
	 *            passwordResetRequest
	 * @return List of PasswordResetRequests which conform example
	 */
	public List<PasswordResetRequest> findPasswordResetRequests(PasswordResetRequest passwordResetRequest) {
		return ldapEntryManager.findEntries(passwordResetRequest);
	}

	/**
	 * Get all password reset requests
	 * 
	 * @return List of password reset requests
	 */
	public List<PasswordResetRequest> getAllPasswordResetRequests(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForPasswordResetRequest(null), PasswordResetRequest.class, null,
				ldapReturnAttributes);
	}

	/**
	 * Search latest user password reset request by person inum
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of password reset requests
	 */
	public PasswordResetRequest findActualPasswordResetRequest(String personInum) {
		Filter oxPersonInumFilter = Filter.createEqualityFilter("personInum", personInum);

		List<PasswordResetRequest> result = ldapEntryManager.findEntries(getDnForPasswordResetRequest(null),
				PasswordResetRequest.class, oxPersonInumFilter);

		if (result.size() == 0) {
			return null;
		}

		return result.get(result.size() - 1);
	}

	public List<PasswordResetRequest> getExpiredPasswordResetRequests(BatchOperation<PasswordResetRequest> batchOperation, Date expirationDate, String[] returnAttributes,
			int sizeLimit, int chunkSize) {
		final String baseDn = getDnForPasswordResetRequest(null);
		Filter expirationFilter = Filter.createLessOrEqualFilter("creationDate",
				ldapEntryManager.encodeTime(baseDn, expirationDate));

		List<PasswordResetRequest> passwordResetRequests = ldapEntryManager.findEntries(baseDn,
				PasswordResetRequest.class, expirationFilter, SearchScope.SUB, returnAttributes, batchOperation, 0,
				sizeLimit, chunkSize);

		return passwordResetRequests;
	}

	public void cleanup(final Date expirationDate) {
		logger.addNewLogLine("Start actual password reset clean up with expiration date: " + expirationDate);
		BatchOperation<PasswordResetRequest> passwordResetRequestBatchService = new ProcessBatchOperation<PasswordResetRequest>() {
			@Override
			public void performAction(List<PasswordResetRequest> entries) {
				for (PasswordResetRequest passwordResetRequest : entries) {
					try {
						log.debug("Removing PasswordResetRequest: {}, Creation date: {}",
								passwordResetRequest.getOxGuid(), passwordResetRequest.getCreationDate());
						logger.addNewLogLine("Removing PasswordResetRequest" + passwordResetRequest.getOxGuid()
								+ " , create date :" + passwordResetRequest.getCreationDate());
						removePasswordResetRequest(passwordResetRequest);
					} catch (Exception ex) {
						log.error("Failed to remove entry", ex);
					}
				}
			}
		};

		getExpiredPasswordResetRequests(passwordResetRequestBatchService, expirationDate,
				new String[] { "oxGuid", "creationDate" }, 0, CleanerTimer.BATCH_SIZE);
		logger.addNewLogLine("Password reset clean up done at: " + new Date());
	}

	/**
	 * Generate new guid for password reset request
	 * 
	 * @return new guid for password reset request
	 */
	public String generateGuidForNewPasswordResetRequest() {
		String newDn = null;
		String newGuid = null;
		do {
			newGuid = generateGuidForNewPasswordResetRequestImpl();
			newDn = getDnForPasswordResetRequest(newGuid);
		} while (containsPasswordResetRequest(newDn));

		return newGuid;
	}

	/**
	 * Generate new guid for password reset request
	 * 
	 * @return new guid for password reset request
	 */
	private String generateGuidForNewPasswordResetRequestImpl() {
		return StringHelper.getRandomString(16);
	}

	/**
	 * Build DN string for password reset request
	 */
	public String getDnForPasswordResetRequest(String guid) {
		String configurationDn = organizationService.getBaseDn();

		if (StringHelper.isEmpty(guid)) {
			return String.format("ou=resetPasswordRequests,%s", configurationDn);
		}

		return String.format("oxGuid=%s,ou=resetPasswordRequests,%s", guid, configurationDn);
	}

}