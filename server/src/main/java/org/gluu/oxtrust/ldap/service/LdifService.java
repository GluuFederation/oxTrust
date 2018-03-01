/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.persist.exception.mapping.BaseMappingException;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.ldap.impl.LdifDataUtility;
import org.slf4j.Logger;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldif.LDIFReader;
import com.unboundid.ldif.LDIFWriter;

/**
 * Provides operations with LDIF files
 * 
 * @author Shekhar L Date: 02.28.2017
 * @author Yuriy Movchan Date: 03/06/2017
 */
@Stateless
@Named("ldifService")
public class LdifService implements Serializable {

	private static final long serialVersionUID = 6690460114767359078L;

	@Inject
	private Logger log;

	@Inject
	private LdapEntryManager ldapEntryManager;	
	@Inject
	private AttributeService attributeService;

	public ResultCode importLdifFileInLdap(InputStream is) throws LDAPException {
		ResultCode result = ResultCode.UNAVAILABLE;

		LDAPConnection connection = ldapEntryManager.getOperationService().getConnection();
		try {
			LdifDataUtility ldifDataUtility = LdifDataUtility.instance();
			LDIFReader importLdifReader = new LDIFReader(is);

			result = ldifDataUtility.importLdifFile(connection, importLdifReader);
			importLdifReader.close();
		} catch (Exception ex) {
			log.error("Failed to import ldif file: ", ex);
		} finally {
			ldapEntryManager.getOperationService().releaseConnection(connection);
		}

		return result;

	}

	public ResultCode validateLdifFile(InputStream is, String dn) throws LDAPException {
		ResultCode result = ResultCode.UNAVAILABLE;
		try {
			LdifDataUtility ldifDataUtility = LdifDataUtility.instance();
			LDIFReader validateLdifReader = new LDIFReader(is);

			result = ldifDataUtility.validateLDIF(validateLdifReader, dn);
			validateLdifReader.close();
		} catch (Exception ex) {
			log.error("Failed to validate ldif file: ", ex);
		}

		return result;

	}
	
	public void exportLDIFFile(List<String> checkedItems, OutputStream output)
			throws LDAPException {
		List<SearchResultEntry> result = null;
		LDAPConnection connection = ldapEntryManager.getOperationService().getConnection();
		try {
			LdifDataUtility ldifDataUtility = LdifDataUtility.instance();
			result = ldifDataUtility.getAttributeResultEntryLDIF(connection,checkedItems, attributeService.getDnForAttribute(null));
		} catch (Exception ex) {
			log.error("Failed to export ldif file: ", ex);
		} finally {
			ldapEntryManager.getOperationService().releaseConnection(connection);
		}

		if (result != null && result.size() > 0) {
			// Write all of the matching entries to LDIF.
			LDIFWriter ldifWriter;
			try {
				ldifWriter = new LDIFWriter(output);
				for (SearchResultEntry entry : result) {
					ldifWriter.writeEntry(entry);
				}

				ldifWriter.close();
			} catch (IOException e) {
				throw new BaseMappingException("Error writing to file, try again", e);
			}
		}
	}

}
