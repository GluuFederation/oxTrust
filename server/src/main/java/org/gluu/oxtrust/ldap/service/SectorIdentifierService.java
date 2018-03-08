package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.search.filter.Filter;
import org.slf4j.Logger;
import org.xdi.util.StringHelper;

/**
 * Provides operations with Sector Identifiers
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@Stateless
@Named
public class SectorIdentifierService implements Serializable {

    private static final long serialVersionUID = -9167587377957719153L;

    @Inject
    private Logger log;

    @Inject
    private LdapEntryManager ldapEntryManager;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private ClientService clientService;

    /**
     * Build DN string for sector identifier
     *
     * @param oxId Sector Identifier oxId
     * @return DN string for specified sector identifier or DN for sector identifiers branch if oxId is null
     * @throws Exception
     */
    public String getDnForSectorIdentifier(String oxId) {
        String orgDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(oxId)) {
            return String.format("ou=sector_identifiers,%s", orgDn);
        }

        return String.format("oxId=%s,ou=sector_identifiers,%s", oxId, orgDn);
    }

    /**
     * Search sector identifiers by pattern
     *
     * @param pattern   Pattern
     * @param sizeLimit Maximum count of results
     * @return List of sector identifiers
     */
    public List<OxAuthSectorIdentifier> searchSectorIdentifiers(String pattern, int sizeLimit) {
        String[] targetArray = new String[]{pattern};
        Filter searchFilter = Filter.createSubstringFilter(OxTrustConstants.oxId, null, targetArray, null);

        List<OxAuthSectorIdentifier> result = ldapEntryManager.findEntries(getDnForSectorIdentifier(null), OxAuthSectorIdentifier.class, searchFilter, sizeLimit);

        return result;
    }

    public List<OxAuthSectorIdentifier> getAllSectorIdentifiers() {
		return ldapEntryManager.findEntries(getDnForSectorIdentifier(null), OxAuthSectorIdentifier.class, null);
	}

    /**
     * Get sector identifier by oxId
     *
     * @param oxId Sector identifier oxId
     * @return Sector identifier
     */
    public OxAuthSectorIdentifier getSectorIdentifierById(String oxId) {
        OxAuthSectorIdentifier result = null;
        try {
            result = ldapEntryManager.find(OxAuthSectorIdentifier.class, getDnForSectorIdentifier(oxId));
        } catch (Exception e) {
            log.error("Failed to find sector identifier by oxId " + oxId, e);
        }
        return result;
    }

    /**
     * Generate new oxId for sector identifier
     *
     * @return New oxId for sector identifier
     * @throws Exception
     */
    public String generateIdForNewSectorIdentifier() {
        OxAuthSectorIdentifier sectorIdentifier = new OxAuthSectorIdentifier();
        String newId = null;
        do {
            newId = generateIdForNewSectorIdentifierImpl();
            String newDn = getDnForSectorIdentifier(newId);
            sectorIdentifier.setDn(newDn);
        } while (ldapEntryManager.contains(sectorIdentifier));

        return newId;
    }

    /**
     * Generate new oxId for sector identifier
     *
     * @return New oxId for sector identifier
     */
    private String generateIdForNewSectorIdentifierImpl(){
        return UUID.randomUUID().toString();
    }

    /**
     * Add new sector identifier entry
     *
     * @param sectorIdentifier Sector identifier
     */
    public void addSectorIdentifier(OxAuthSectorIdentifier sectorIdentifier) {
        ldapEntryManager.persist(sectorIdentifier);
    }

    /**
     * Update sector identifier entry
     *
     * @param sectorIdentifier Sector identifier
     */
    public void updateSectorIdentifier(OxAuthSectorIdentifier sectorIdentifier) {
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
                OxAuthClient client = clientService.getClientByDn(clientDN);
                client.setSectorIdentifierUri(null);
                clientService.updateClient(client);
            }
        }

        ldapEntryManager.remove(sectorIdentifier);
    }
    
    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }
}
