/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.AuthenticationMethod;
import org.gluu.oxtrust.model.BlockEncryptionAlgorithm;
import org.gluu.oxtrust.model.KeyEncryptionAlgorithm;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthCustomClient;
import org.gluu.oxtrust.model.OxAuthSubjectType;
import org.gluu.oxtrust.model.SignatureAlgorithm;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with clients
 *
 * @author Reda Zerrad Date: 06.08.2012
 * @author Javier Rojas Blum
 * @version July 19, 2016
 */

@Stateless
@Named
public class ClientService implements Serializable {

    private static final long serialVersionUID = 7912416439116338984L;

    @Inject
    private LdapEntryManager ldapEntryManager;

    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private EncryptionService encryptionService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
	private OxTrustAuditService oxTrustAuditService;

    public boolean contains(String clientDn) {
        return ldapEntryManager.contains(OxAuthClient.class, clientDn);
    }

    /**
     * Add new client entry
     *
     * @param client
     */
    public void addClient(OxAuthClient client) {
        ldapEntryManager.persist(client);
        oxTrustAuditService.audit("CLIENT "+client.getDisplayName()+ " SUCCESSFULLY ADDED");

    }

    /**
     * Remove client entry
     *
     * @param client
     */
    public void removeClient(OxAuthClient client) {
        ldapEntryManager.removeWithSubtree(client.getDn());
        oxTrustAuditService.audit("CLIENT "+client.getDisplayName()+ " SUCCESSFULLY REMOVED");
    }

    /**
     * Get client by inum
     *
     * @param inum client Inum
     * @return client
     */
    public OxAuthClient getClientByInum(String inum, String... ldapReturnAttributes) {
        OxAuthClient result = null;
        try {
            result = ldapEntryManager.find(OxAuthClient.class, getDnForClient(inum), ldapReturnAttributes);

            String encodedClientSecret = result.getEncodedClientSecret();
            if (StringHelper.isNotEmpty(encodedClientSecret)) {
                String clientSecret = encryptionService.decrypt(encodedClientSecret);
                result.setOxAuthClientSecret(clientSecret);
            }
        } catch (Exception ex) {
            log.debug("Failed to load client entry", ex);
        }
        return result;
    }

    /**
     * Get custom client by inum
     *
     * @param inum client Inum
     * @return client
     */
    public OxAuthCustomClient getClientByInumCustom(String inum) {

        OxAuthCustomClient result = ldapEntryManager.find(OxAuthCustomClient.class, getDnForClient(inum));

        return result;

    }

    /**
     * Build DN string for client
     *
     * @param inum client Inum
     * @return DN string for specified Client or DN for clients branch if inum
     * is null
     */
    public String getDnForClient(String inum) {
        String orgDn = organizationService.getDnForOrganization();
        if (StringHelper.isEmpty(inum)) {
            return String.format("ou=clients,%s", orgDn);
        }

        return String.format("inum=%s,ou=clients,%s", inum, orgDn);
    }

    /**
     * Update client entry
     *
     * @param client Client
     */
    public void updateClient(OxAuthClient client) {
        ldapEntryManager.merge(client);
        oxTrustAuditService.audit("CLIENT "+client.getDisplayName()+ "SUCCESSFULLY UPDATED");
    }

    /**
     * Update Custom client entry
     *
     * @param client Client
     */
    public void updateCustomClient(OxAuthCustomClient client) {
        ldapEntryManager.merge(client);
        oxTrustAuditService.audit("CLIENT "+client.getDisplayName()+ "SUCCESSFULLY UPDATED");
    }

    /**
     * Generate new inum for client
     *
     * @return New inum for client
     */
    public String generateInumForNewClient() {
        OxAuthClient client = new OxAuthClient();
        String newInum = null;
        do {
            newInum = generateInumForNewClientImpl();
            String newDn = getDnForClient(newInum);
            client.setDn(newDn);
        } while (ldapEntryManager.contains(client));

        return newInum;
    }

    /**
     * Generate new inum for client
     *
     * @return New inum for client
     */
    private String generateInumForNewClientImpl() {
        String orgInum = organizationService.getInumForOrganization();
        return orgInum + OxTrustConstants.inumDelimiter + "0008" + OxTrustConstants.inumDelimiter + INumGenerator.generate(4);

    }

    /**
     * Generate new iname for client
     *
     * @return New iname for client
     */
    public String generateInameForNewClient(String name) {
        return String.format("%s*clients*%s", appConfiguration.getOrgIname(), name);
    }

    /**
     * Search clients by pattern
     *
     * @param pattern   Pattern
     * @param sizeLimit Maximum count of results
     * @return List of clients
     */
    public List<OxAuthClient> searchClients(String pattern, int sizeLimit) {
        String[] targetArray = new String[]{pattern};
        Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
        Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
        Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
        Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
        Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter, inumFilter);

        List<OxAuthClient> result = ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class, searchFilter, 0, sizeLimit);

        return result;
    }
    
    public List<OxAuthClient> getAllClients(int sizeLimit) {		
		return ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class, null, 0, sizeLimit);
    }

    /**
     * returns a list of all clients
     *
     * @return list of clients
     */

    public List<OxAuthClient> getAllClientsList() {

        List<OxAuthClient> result = ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class,
                Filter.createPresenceFilter("inum"), 0, 10);

        return result;

    }

    /**
     * returns oxAuthClient by Dn
     *
     * @return oxAuthClient
     */

    public OxAuthClient getClientByDn(String Dn) {
        OxAuthClient result = ldapEntryManager.find(OxAuthClient.class, Dn);

        return result;
    }

    /**
     * returns oxAuthClient by Dn
     *
     * @return oxAuthClient
     */

    public OxAuthCustomClient getClientByDnCustom(String Dn) {
        OxAuthCustomClient result = ldapEntryManager.find(OxAuthCustomClient.class, Dn);

        return result;
    }

    /**
     * returns oxAuthClient by Dn
     *
     * @return oxAuthClient
     */

    /**
     * Get Client by iname
     *
     * @param iname
     * @return Client
     */
    public OxAuthClient getClientByIname(String iname) {
        OxAuthClient client = new OxAuthClient();
        client.setBaseDn(getDnForClient(null));
        client.setIname(iname);

        List<OxAuthClient> clients = ldapEntryManager.findEntries(client);

        if ((clients != null) && (clients.size() > 0)) {
            return clients.get(0);
        }

        return null;
    }

    /**
     * Get client by DisplayName
     *
     * @param DisplayName
     * @return client
     * @throws Exception
     */
    public OxAuthClient getClientByDisplayName(String DisplayName) {
        OxAuthClient client = new OxAuthClient();
        client.setBaseDn(getDnForClient(null));
        client.setDisplayName(DisplayName);

        List<OxAuthClient> clients = ldapEntryManager.findEntries(client);

        if ((clients != null) && (clients.size() > 0)) {
            return clients.get(0);
        }

        return null;
    }

    /**
     * Get custom client by Attribute
     *
     * @param name
     * @param value
     * @return Custom client
     */
    public OxAuthCustomClient getClientByAttributeCustom(String name, String value) {
        try {
            if (name.equalsIgnoreCase("dn")) {
                return getClientByDnCustom(value);
            }
            if (name.equalsIgnoreCase("inum")) {
                return getClientByInumCustom(value);
            }

            log.info("creating a new instance of OxAuthCustomClient ");
            OxAuthCustomClient client = new OxAuthCustomClient();
            log.info("getting dn for client ");
            client.setBaseDn(getDnForClient(null));
            log.info("name ", name);
            log.info("value ", value);
            log.info("setting attribute value ");
            client.setAttribute(name, value);

            log.info("finding entries ");
            List<OxAuthCustomClient> clients = ldapEntryManager.findEntries(client);

            if ((clients != null) && (clients.size() > 0)) {
                log.info("entry found ");
                return clients.get(0);
            }
            log.info("no entry ");
            return null;
        } catch (Exception ex) {
            log.error("an error occured ", ex);

            return null;
        }

    }

    /**
     * Get all available Application types
     *
     * @return Array of Application types
     */
    public OxAuthApplicationType[] getApplicationType() {
        return OxAuthApplicationType.values();
    }

    /**
     * GetOxAuthTrustedClient
     *
     * @return Array of OxAuthTrustedClient
     */
    public GluuBoolean[] getOxAuthTrustedClient() {
        return new GluuBoolean[]{GluuBoolean.TRUE, GluuBoolean.FALSE};
    }

    /**
     * Get all available Subject types
     *
     * @return Array of Subject types
     */
    public OxAuthSubjectType[] getSubjectTypes() {
        return OxAuthSubjectType.values();
    }

    /**
     * Get all available Signature Algorithms
     *
     * @return Array of Signature Algorithms
     */
    public SignatureAlgorithm[] getSignatureAlgorithms() {
        return SignatureAlgorithm.values();
    }

    public SignatureAlgorithm[] getSignatureAlgorithmsWithoutNone() {
        return new SignatureAlgorithm[]{
                SignatureAlgorithm.HS256,
                SignatureAlgorithm.HS384,
                SignatureAlgorithm.HS512,
                SignatureAlgorithm.RS256,
                SignatureAlgorithm.RS384,
                SignatureAlgorithm.RS512,
                SignatureAlgorithm.ES256,
                SignatureAlgorithm.ES384,
                SignatureAlgorithm.ES512,
        };
    }

    /**
     * Get all available Key Encryption Algorithms
     *
     * @return Array of Key Encryption Algorithms
     */
    public KeyEncryptionAlgorithm[] getKeyEncryptionAlgorithms() {
        return KeyEncryptionAlgorithm.values();
    }

    /**
     * Get all available Block Encryption Algorithms
     *
     * @return Array of Block Encryption Algorithms
     */
    public BlockEncryptionAlgorithm[] getBlockEncryptionAlgorithms() {
        return BlockEncryptionAlgorithm.values();
    }

    /**
     * Get all available Authentication methods
     *
     * @return Array of Authentication methods
     */
    public AuthenticationMethod[] getAuthenticationMethods() {
        return AuthenticationMethod.values();
    }
}
