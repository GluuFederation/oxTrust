package org.gluu.oxtrust.ldap.service.uma;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.hibernate.annotations.common.util.StringHelper;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.SimpleBranch;
import org.xdi.oxauth.model.uma.persistence.UmaPolicy;
import org.xdi.util.INumGenerator;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with UMA policies
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@Scope(ScopeType.STATELESS)
@Name("umaPolicyService")
@AutoCreate
public class UmaPolicyService implements Serializable {

	private static final long serialVersionUID = -1537567020929600777L;

	@In
	private LdapEntryManager ldapEntryManager;

	@Logger
	private Log log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("policies");
		branch.setDn(getDnForUmaPolicy(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(SimpleBranch.class, getDnForUmaPolicy(null));
	}

	/**
	 * Create UMA policy branch if needed
	 */
	public void prepareUmaPolicyBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Get UMA policy by DN
	 * 
	 * @param DN UMA policy DN
	 * @return UMA policy
	 */
	public UmaPolicy getUmaPolicyByDn(String dn) {
		return ldapEntryManager.find(UmaPolicy.class, dn);
	}

	/**
	 * Add new UMA policy entry
	 * 
	 * @param umaPolicy Uma policy
	 */
	public void addUmaPolicy(UmaPolicy umaPolicy) {
		ldapEntryManager.persist(umaPolicy);
	}

	/**
	 * Update UMA policy entry
	 * 
	 * @param umaPolicy Uma policy
	 */
	public void updateUmaPolicy(UmaPolicy umaPolicy) {
		ldapEntryManager.merge(umaPolicy);
	}

	/**
	 * Remove UMA policy entry
	 * 
	 * @param umaPolicy Uma policy
	 */
	public void removeUmaPolicy(UmaPolicy umaPolicy) {
		ldapEntryManager.remove(umaPolicy);
	}

	/**
	 * Check if LDAP server contains UMA policy with specified attributes
	 * 
	 * @return True if UMA policy with specified attributes exist
	 */
	public boolean containsUmaPolicy(UmaPolicy umaPolicy) {
		return ldapEntryManager.contains(umaPolicy);
	}

	/**
	 * Get all UMA policies
	 * 
	 * @return List of UMA policies
	 */
	public List<UmaPolicy> getAllUmaPolicies(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForUmaPolicy(null), UmaPolicy.class, ldapReturnAttributes, null);
	}

	/**
	 * Search UMA policies by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of UMA policies
	 */
	public List<UmaPolicy> findUmaPolicies(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<UmaPolicy> result = ldapEntryManager.findEntries(getDnForUmaPolicy(null), UmaPolicy.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Get UMA policies by example
	 * 
	 * @param umaPolicy UmaPolicy
	 * @return List of UmaPolicies which conform example
	 */
	public List<UmaPolicy> findUmaPolicies(UmaPolicy umaPolicy) {
		return ldapEntryManager.findEntries(umaPolicy);
	}

	/**
	 * Generate new inum for UMA policy
	 * 
	 * @return New inum for UMA policy
	 */
	public String generateInumForNewUmaPolicy() {
		UmaPolicy umaPolicy = new UmaPolicy();
		String newInum = null;
		do {
			newInum = generateInumForNewUmaPolicyImpl();
			String newDn = getDnForUmaPolicy(newInum);
			umaPolicy.setDn(newDn);
		} while (ldapEntryManager.contains(umaPolicy));

		return newInum;
	}

	/**
	 * Generate new inum for UMA policy
	 * 
	 * @return New inum for UMA policy
	 */
	private String generateInumForNewUmaPolicyImpl() {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Build DN string for UMA policy
	 */
	public String getDnForUmaPolicy(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=policies,ou=uma,%s", orgDn);
		}

		return String.format("inum=%s,ou=policies,ou=uma,%s", inum, orgDn);
	}

	/**
	 * Get UmaPolicyService instance
	 * 
	 * @return UmaPolicyService instance
	 */
	public static UmaPolicyService instance() {
		return (UmaPolicyService) Component.getInstance(UmaPolicyService.class);
	}

}
