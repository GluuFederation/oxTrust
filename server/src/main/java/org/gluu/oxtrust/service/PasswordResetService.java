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

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.PasswordResetRequest;
import org.gluu.site.ldap.persistence.BatchOperation;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.slf4j.Logger;
import org.xdi.ldap.model.SearchScope;
import org.xdi.ldap.model.SimpleBranch;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

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
    private ApplianceService applianceService;

	@Inject
	private LdapEntryManager ldapEntryManager;

	@Inject
	private Logger log;

    public void addBranch() {
        SimpleBranch branch = new SimpleBranch();
        branch.setOrganizationalUnitName("resetPasswordRequests");
        branch.setDn(getDnForPasswordResetRequest(null));

        ldapEntryManager.persist(branch);
    }

    public boolean containsBranch() {
        return ldapEntryManager.contains(SimpleBranch.class, getDnForPasswordResetRequest(null));
    }

    public void prepareBranch() {
        // Create reset password requests branch if needed
        if (!containsBranch()) {
            addBranch();
        }
    }

    /**
     * Get password reset request by DN
     * 
     * @param DN password reset request DN
     * @return PasswordResetRequest Password reset request
     */
	public PasswordResetRequest findPasswordResetRequest(String guid) {
        String passwordResetRequestDn = getDnForPasswordResetRequest(guid);

        return ldapEntryManager.find(PasswordResetRequest.class, passwordResetRequestDn);

	}

	/**
	 * Add new password reset request
	 * 
	 * @param PasswordResetRequest Password reset request
	 */
	public void addPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		ldapEntryManager.persist(passwordResetRequest);
	}

	/**
     * Update new password reset request
	 * 
     * @param PasswordResetRequest Password reset request
	 */
	public void updatePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		ldapEntryManager.merge(passwordResetRequest);
	}

	/**
     * Remove new password reset request
	 * 
     * @param PasswordResetRequest Password reset request
	 */
	public void removePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		ldapEntryManager.remove(passwordResetRequest);
	}

	/**
	 * Check if there is password reset request with specified attributes
	 * 
	 * @return True if password reset request with specified attributes exist
	 */
	public boolean containsPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
		return ldapEntryManager.contains(passwordResetRequest);
	}

	/**
	 * Get password reset requests by example
	 * 
	 * @param PasswordResetRequest passwordResetRequest
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
		return ldapEntryManager.findEntries(getDnForPasswordResetRequest(null), PasswordResetRequest.class, ldapReturnAttributes, null);
	}

	/**
	 * Search latest user password reset request by person inum
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of password reset requests
	 */
	public PasswordResetRequest findActualPasswordResetRequest(String personInum) {
		Filter oxPersonInumFilter = Filter.createEqualityFilter("personInum", personInum);

		List<PasswordResetRequest> result = ldapEntryManager.findEntries(getDnForPasswordResetRequest(null), PasswordResetRequest.class, oxPersonInumFilter, 0, 0);
		
		if (result.size() == 0) {
		    return null;
		}

		return result.get(result.size() - 1);
	}

    public void cleanup(final Date now) {
    	try {
    		BatchOperation<PasswordResetRequest> rptBatchService = new BatchOperation<PasswordResetRequest>(ldapEntryManager) {
                @Override
                protected List<PasswordResetRequest> getChunkOrNull(int chunkSize) {
                    return ldapEntryManager.findEntries(getDnForPasswordResetRequest(null), PasswordResetRequest.class, getFilter(), SearchScope.SUB, null, this, 0, chunkSize, chunkSize);
                }

                @Override
                protected void performAction(List<PasswordResetRequest> entries) {
                    for (PasswordResetRequest p : entries) {
                        try {
                            ldapEntryManager.remove(p);
                        } catch (Exception e) {
                            log.error("Failed to remove entry", e);
                        }
                    }
                }

                private Filter getFilter() {
                    Filter expirationFilter = Filter.createLessOrEqualFilter("creationDate", ldapEntryManager.encodeGeneralizedTime(now));
                    return expirationFilter;
                }
            };
            rptBatchService.iterateAllByChunks(CleanerTimer.BATCH_SIZE);
    	}catch (EntryPersistenceException e) {
    		log.warn("Trying to clean expired password reset requests when the corresponding dn don't exist yet.");
		}        
    }

	/**
	 * Generate new guid for password reset request
	 * 
	 * @return new guid for password reset request
	 */
	public String generateGuidForNewPasswordResetRequest() {
		PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
		String newGuid = null;
		do {
			newGuid = generateGuidForNewPasswordResetRequestImpl();
			String newDn = getDnForPasswordResetRequest(newGuid);
			passwordResetRequest.setDn(newDn);
		} while (ldapEntryManager.contains(passwordResetRequest));

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
        String applianceDn = applianceService.getAppliance().getDn();

		if (StringHelper.isEmpty(guid)) {
			return String.format("ou=resetPasswordRequests,%s", applianceDn);
		}

		return String.format("oxGuid=%s,ou=resetPasswordRequests,%s", guid, applianceDn);
	}

}
