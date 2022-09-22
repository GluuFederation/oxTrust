package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxTrustedIdp;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;
import org.gluu.service.DataSourceTypeService;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Trusted IDP service
 * 
 * @author Shekhar L
 */

@ApplicationScoped
public class TrustedIDPService implements Serializable {
	
	private static final long serialVersionUID = 7622866899884574305L;

	@Inject
	private Logger log;
	
	@Inject
	private PersistenceEntryManager persistenceEntryManager;
	
	@Inject
	private OrganizationService organizationService;
	
	@Inject
	private IdGenService idGenService;
	
	@Inject
	private DataSourceTypeService dataSourceTypeService;

	
	public List<OxTrustedIdp> getAllTrustedIDP() {
		return persistenceEntryManager.findEntries(getDnForTrustedIDP(null), OxTrustedIdp.class, null);
	}
	
	public List<OxTrustedIdp> getAllTrustedIDP(int sizeLimit) {
		return persistenceEntryManager.findEntries(getDnForTrustedIDP(null), OxTrustedIdp.class, null, sizeLimit);
	}
	
	
	/**
	 * Get custom TrustedIDP by inum
	 *
	 * @param inum
	 *            OxTrustedIdp Inum
	 * @return OxTrustedIdp
	 */
	public OxTrustedIdp getTrustedIDPByInumCustom(String inum) {
		return persistenceEntryManager.find(OxTrustedIdp.class, getDnForTrustedIDP(inum));
	}
	
	/**
	 * Search OxTrustedIdp by pattern
	 *
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of OxTrustedIdp
	 */
	public List<OxTrustedIdp> searchOxTrustedIdp(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter remoteIdpNameFilter = Filter.createSubstringFilter(OxTrustConstants.remoteIdpName, null, targetArray, null);
		Filter remoteIdpHostFilter = Filter.createSubstringFilter(OxTrustConstants.remoteIdpHost, null, targetArray, null);
		Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(remoteIdpNameFilter, remoteIdpHostFilter, inumFilter);
		return persistenceEntryManager.findEntries(getDnForTrustedIDP(null), OxTrustedIdp.class, searchFilter, sizeLimit);
	}
	
	/**
	 * Build DN string for OxTrustedIdp
	 *
	 * @param inum
	 *            OxTrustedIdp Inum
	 * @return DN string for specified OxTrustedIdp or DN for OxTrustedIdp branch if inum is
	 *         null
	 */	
	public String getDnForTrustedIDP(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=trusted-idp,%s", orgDn);
		}
		return String.format("inum=%s,ou=trusted-idp,%s", inum, orgDn);
	}
	
	/**
	 * Add new OxTrustedIdp entry
	 *
	 * @param OxTrustedIdp
	 */
	public void addTrustedIDP(OxTrustedIdp oxTrustedIdp) {
		persistenceEntryManager.persist(oxTrustedIdp);
	}
	
	/**
	 * Remove OxTrustedIdp entry
	 *
	 * @param OxTrustedIdp
	 */
	public void removeTrustedIDP(OxTrustedIdp oxTrustedIdp) {
		persistenceEntryManager.removeRecursively(oxTrustedIdp.getDn(), OxTrustedIdp.class);
	}
	
	/**
	 * Get OxTrustedIdp by inum
	 *
	 * @param inum
	 *            OxTrustedIdp Inum
	 * @return OxTrustedIdp
	 */
	public OxTrustedIdp getTrustedIDPByInum(String inum, String... ldapReturnAttributes) {
		OxTrustedIdp result = null;
		try {
			result = persistenceEntryManager.find(getDnForTrustedIDP(inum), OxTrustedIdp.class, ldapReturnAttributes);
		} catch (Exception ex) {
			log.debug("Failed to load TrustedIDP entry", ex);
		}
		return result;
	}
	
	public void updateTrustedIDP(OxTrustedIdp oxTrustedIdp) {
		persistenceEntryManager.merge(oxTrustedIdp);
	}
	
	public String generateInumForTrustedIDP() {
		String newInum = null;
		String newDn = null;
		int trycount = 0;
		do {
			if(trycount < IdGenService.MAX_IDGEN_TRY_COUNT) {
				newInum = idGenService.generateId("TrustedIDP");
				trycount++;
			}else {
				newInum = idGenService.generateDefaultId();
			}
			newDn = getDnForTrustedIDP(newInum);
		} while (persistenceEntryManager.contains(newDn, OxTrustedIdp.class));
		return newInum;
	}
	
	public OxTrustedIdp getTrustedIDPByRemoteIdpHost(String remoteIdpHost, String... returnAttributes) {
		log.debug("Getting user information from DB: TrustedIDP = {}", remoteIdpHost);
		OxTrustedIdp oxTrustedIdp = null;
		if (StringHelper.isEmpty(remoteIdpHost)) {
			return null;
		}

		String gluuPassportConfigDn = getDnForTrustedIDP(null);
		Filter gluuPassportConfigIdpHostFilter;
		if (dataSourceTypeService.isSpanner(gluuPassportConfigDn)) {
			gluuPassportConfigIdpHostFilter = Filter.createEqualityFilter("remoteIdpHost",
					StringHelper.toLowerCase(remoteIdpHost));
		} else {
			gluuPassportConfigIdpHostFilter = Filter.createEqualityFilter(Filter.createLowercaseFilter("remoteIdpHost"),
					StringHelper.toLowerCase(remoteIdpHost));
		}

		List<OxTrustedIdp> entries = persistenceEntryManager.findEntries(gluuPassportConfigDn,
				OxTrustedIdp.class, gluuPassportConfigIdpHostFilter, returnAttributes);
		log.debug("Found {} entries for TrustedIDP = {}", entries.size(), remoteIdpHost);
		if(entries.size()>0) {
			oxTrustedIdp = entries.get(0);
		}
		return oxTrustedIdp;
	}

}
