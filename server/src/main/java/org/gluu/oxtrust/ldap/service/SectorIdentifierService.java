package org.gluu.oxtrust.ldap.service;

import com.unboundid.ldap.sdk.Filter;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import java.io.Serializable;
import java.util.List;

/**
 * Provides operations with Sector Identifiers
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@Scope(ScopeType.STATELESS)
@Name("sectorIdentifierService")
@AutoCreate
public class SectorIdentifierService implements Serializable {

    private static final long serialVersionUID = -9167587377957719153L;

    @In
    private LdapEntryManager ldapEntryManager;

    @Logger
    private Log log;

    @In(value = "#{oxTrustConfiguration.applicationConfiguration}")
    private ApplicationConfiguration applicationConfiguration;

    /**
     * Build DN string for sector identifier
     *
     * @param inum Sector Identifier Inum
     * @return DN string for specified sector identifier or DN for sector identifiers branch if inum is null
     * @throws Exception
     */
    public String getDnForSectorIdentifier(String inum) {
        String orgDn = OrganizationService.instance().getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=sector_identifiers,%s", orgDn);
        }

        return String.format("inum=%s,ou=sector_identifiers,%s", inum, orgDn);
    }

    /**
     * Search sector identifiers by pattern
     *
     * @param pattern   Pattern
     * @param sizeLimit Maximum count of results
     * @return List of sector identifiers
     */
    public List<OxAuthSectorIdentifier> searchSectorIdentifiers(String pattern, int sizeLimit) throws Exception {
        String[] targetArray = new String[]{pattern};
        Filter searchFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);

        List<OxAuthSectorIdentifier> result = ldapEntryManager.findEntries(getDnForSectorIdentifier(null), OxAuthSectorIdentifier.class, searchFilter, sizeLimit);

        return result;
    }

    /**
     * Get sector identifier by inum
     *
     * @param inum Sector identifier inum
     * @return Sector identifier
     */
    public OxAuthSectorIdentifier getSectorIdentifierByInum(String inum) {
        OxAuthSectorIdentifier result = null;
        try {
            result = ldapEntryManager.find(OxAuthSectorIdentifier.class, getDnForSectorIdentifier(inum));
        } catch (Exception e) {
            log.error("Failed to find sector identifier by Inum " + inum, e);
        }
        return result;
    }

    /**
     * Generate new inum for sector identifier
     *
     * @return New inum for sector identifier
     * @throws Exception
     */
    public String generateInumForNewSectorIdentifier() throws Exception {
        OxAuthSectorIdentifier sectorIdentifier = new OxAuthSectorIdentifier();
        String newInum = null;
        do {
            newInum = generateInumForNewSectorIdentifierImpl();
            String newDn = getDnForSectorIdentifier(newInum);
            sectorIdentifier.setDn(newDn);
        } while (ldapEntryManager.contains(sectorIdentifier));

        return newInum;
    }

    /**
     * Generate new inum for sector identifier
     *
     * @return New inum for sector identifier
     */
    private String generateInumForNewSectorIdentifierImpl() throws Exception {
        String orgInum = OrganizationService.instance().getInumForOrganization();
        return orgInum + OxTrustConstants.inumDelimiter + OxTrustConstants.INUM_SECTOR_IDENTIFIER_OBJECTTYPE + OxTrustConstants.inumDelimiter
                + INumGenerator.generate(2);
    }

    /**
     * Add new sector identifier entry
     *
     * @param sectorIdentifier Sector identifier
     */
    public void addSectorIdentifier(OxAuthSectorIdentifier sectorIdentifier) throws Exception {
        ldapEntryManager.persist(sectorIdentifier);
    }

    /**
     * Update sector identifier entry
     *
     * @param sectorIdentifier Sector identifier
     */
    public void updateSectorIdentifier(OxAuthSectorIdentifier sectorIdentifier) throws Exception {
        ldapEntryManager.merge(sectorIdentifier);
    }

    /**
     * Remove sector identifier entry
     *
     * @param sectorIdentifier Sector identifier
     */
    public void removeSectorIdentifier(OxAuthSectorIdentifier sectorIdentifier) {
        if (sectorIdentifier.getClientIds() != null) {
            List<String> clientDNs = sectorIdentifier.getClientIds();

            // clear references in Client entries
            for (String clientDN : clientDNs) {
                OxAuthClient client = ClientService.instance().getClientByDn(clientDN);
                client.setSectorIdentifierUri(null);
                ClientService.instance().updateClient(client);
            }
        }

        ldapEntryManager.remove(sectorIdentifier);
    }
}
