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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.NotImplementedException;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.ldap.impl.LdifDataUtility;
import org.gluu.persist.ldap.operation.LdapOperationService;
import org.slf4j.Logger;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldif.LDIFReader;
import org.gluu.persist.model.AttributeData;
import org.gluu.persist.operation.PersistenceOperationService;

/**
 * Provides operations with LDIF files
 * 
 * @author Shekhar L Date: 02.28.2017
 * @author Yuriy Movchan Date: 03/06/2017
 */
@Stateless
@Named("ldifService")
public class LdifService implements Serializable {

	private static final String SEPERATOR = ":";
	private static final String CLOSE = "]";
	private static final String OPEN = "[";
	private static final String LINE_SEPARATOR = "line.separator";

	private static final long serialVersionUID = 6690460114767359078L;

	@Inject
	private Logger log;

	@Inject
	private DataSourceTypeService dataSourceTypeService;

	@Inject
	private PersistenceEntryManager persistenceManager;

	public ResultCode importLdifFileInLdap(InputStream is) throws LDAPException {
		if (dataSourceTypeService.isLDAP()) {
			ResultCode result = ResultCode.UNAVAILABLE;
			PersistenceOperationService persistenceOperationService = persistenceManager.getOperationService();
			LdapOperationService ldapOperationService = (LdapOperationService) persistenceOperationService;
			LDAPConnection connection = ldapOperationService.getConnection();
			try {
				LdifDataUtility ldifDataUtility = LdifDataUtility.instance();
				LDIFReader importLdifReader = new LDIFReader(is);
				result = ldifDataUtility.importLdifFile(connection, importLdifReader);
				importLdifReader.close();
			} catch (Exception ex) {
				log.error("Failed to import ldif file: ", ex);
			} finally {
				ldapOperationService.releaseConnection(connection);
			}
			return result;
		} else {
			performImport(is);
			return ResultCode.SUCCESS;
		}
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

	@SuppressWarnings("resource")
	public void performImport(InputStream inputStream) {
		final ArrayList<Entry> entryList = new ArrayList<Entry>();
		final LDIFReader reader = new LDIFReader(inputStream);
		while (true) {
			try {
				final Entry entry = reader.readEntry();
				if (entry == null) {
					break;
				} else {
					entryList.add(entry);
				}
			} catch (final Exception e) {
				log.error("", e);
			}
		}
		entryList.stream().forEach(e -> {
			Collection<Attribute> attributes = e.getAttributes();
			List<Attribute> values = new ArrayList<>();
			values.addAll(attributes);
			ArrayList<AttributeData> datas = new ArrayList<>();
			values.stream().forEach(value -> {
				datas.add(new AttributeData(value.getName(), value.getValues(),
						(value.getValues().length > 1) ? true : false));
			});
			try {
				persistenceManager.importEntry(e.getDN(), datas);
			} catch (Exception ex) {
				log.info("=========", ex);
			}

		});
	}

	public void exportLDIFFile(List<String> checkedItems, OutputStream output) throws LDAPException {
		try {
			StringBuilder builder = new StringBuilder();
			if (checkedItems != null && checkedItems.size() > 0) {
				checkedItems.stream().forEach(e -> {
					List<AttributeData> exportEntry = persistenceManager.exportEntry(e);
					if (exportEntry != null && exportEntry.size() >= 0) {
						exportEntry.forEach(v -> {
							String key = v.getName();
							for (Object value : v.getValues()) {
								builder.append(key + ": " + value + System.getProperty(LINE_SEPARATOR));
							}
						});
					}
					builder.append(System.getProperty(LINE_SEPARATOR));
				});
			}
			output.write(builder.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while exporting entries: ", e);
		}
	}

}
