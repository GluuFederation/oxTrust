/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

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
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.search.filter.Filter;
import org.gluu.util.StringHelper;
import org.python.jline.internal.Log;
import org.slf4j.Logger;

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
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private Logger logger;

	@Inject
	private EncryptionService encryptionService;

	@Inject
	private OrganizationService organizationService;

	public boolean contains(String clientDn) {
		return ldapEntryManager.contains(clientDn, OxAuthClient.class);
	}

	/**
	 * Add new client entry
	 *
	 * @param client
	 */
	public void addClient(OxAuthClient client) {
		ldapEntryManager.persist(client);
	}

	/**
	 * Remove client entry
	 *
	 * @param client
	 */
	public void removeClient(OxAuthClient client) {
		ldapEntryManager.removeRecursively(client.getDn());
	}

	/**
	 * Get client by inum
	 *
	 * @param inum
	 *            client Inum
	 * @return client
	 */
	public OxAuthClient getClientByInum(String inum, String... ldapReturnAttributes) {
		OxAuthClient result = null;
		try {
			result = ldapEntryManager.find(getDnForClient(inum), OxAuthClient.class, ldapReturnAttributes);
			String encodedClientSecret = result.getEncodedClientSecret();
			if (StringHelper.isNotEmpty(encodedClientSecret)) {
				String clientSecret = encryptionService.decrypt(encodedClientSecret);
				result.setOxAuthClientSecret(clientSecret);
			}
		} catch (Exception ex) {
			logger.debug("Failed to load client entry", ex);
		}
		return result;
	}

	/**
	 * Get custom client by inum
	 *
	 * @param inum
	 *            client Inum
	 * @return client
	 */
	public OxAuthCustomClient getClientByInumCustom(String inum) {
		return ldapEntryManager.find(OxAuthCustomClient.class, getDnForClient(inum));
	}

	/**
	 * Build DN string for client
	 *
	 * @param inum
	 *            client Inum
	 * @return DN string for specified Client or DN for clients branch if inum is
	 *         null
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
	 * @param client
	 *            Client
	 */
	public void updateClient(OxAuthClient client) {
		ldapEntryManager.merge(client);
	}

	/**
	 * Update Custom client entry
	 *
	 * @param client
	 *            Client
	 */
	public void updateCustomClient(OxAuthCustomClient client) {
		ldapEntryManager.merge(client);
	}

	/**
	 * Generate new inum for client
	 *
	 * @return New inum for client
	 */
	public String generateInumForNewClient() {
		String newInum = null;
		String newDn = null;
		do {
			newInum = generateInumForNewClientImpl();
			newDn = getDnForClient(newInum);
		} while (ldapEntryManager.contains(newDn, OxAuthClient.class));

		return newInum;
	}

	/**
	 * Generate new inum for client
	 *
	 * @return New inum for client
	 */
	private String generateInumForNewClientImpl() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Search clients by pattern
	 *
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of clients
	 */
	public List<OxAuthClient> searchClients(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
		Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
		Filter inumFilter = Filter.createSubstringFilter(OxTrustConstants.inum, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter, inumFilter);
		return ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class, searchFilter, sizeLimit);
	}

	public List<OxAuthClient> getAllClients(int sizeLimit) {
		return ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class, null, sizeLimit);
	}

	public List<OxAuthClient> getAllClients() {
		return ldapEntryManager.findEntries(getDnForClient(null), OxAuthClient.class, null);
	}

	/**
	 * returns oxAuthClient by Dn
	 *
	 * @return oxAuthClient
	 */

	public OxAuthClient getClientByDn(String Dn) {
		try {
			return ldapEntryManager.find(OxAuthClient.class, Dn);
		} catch (Exception e) {
			Log.warn("", e);
			return null;
		}

	}

	/**
	 * returns oxAuthClient by Dn
	 *
	 * @return oxAuthClient
	 */

	public OxAuthCustomClient getClientByDnCustom(String Dn) {
		return ldapEntryManager.find(OxAuthCustomClient.class, Dn);
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
			OxAuthCustomClient client = new OxAuthCustomClient();
			client.setBaseDn(getDnForClient(null));
			client.setAttribute(name, value);
			List<OxAuthCustomClient> clients = ldapEntryManager.findEntries(client);
			if ((clients != null) && (clients.size() > 0)) {
				return clients.get(0);
			}
			return null;
		} catch (Exception ex) {
			logger.info("", ex);
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
		return new SignatureAlgorithm[] { SignatureAlgorithm.HS256, SignatureAlgorithm.HS384, SignatureAlgorithm.HS512,
				SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512, SignatureAlgorithm.ES256,
				SignatureAlgorithm.ES384, SignatureAlgorithm.ES512, };
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

	public OxAuthClient getClientByInum(String inum) {
		OxAuthClient result = null;
		try {
			result = ldapEntryManager.find(OxAuthClient.class, getDnForClient(inum));
		} catch (Exception ex) {
			logger.error("Failed to load client entry", ex);
		}
		return result;
	}
}
