/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.InputStream;
import java.io.Serializable;

import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.LdifDataUtility;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldif.LDIFReader;

/**
 * Provides operations with LDIF files
 * 
 * @author Shekhar L Date: 02.28.2017
 */
@Scope(ScopeType.STATELESS)
@Name("ldifService")
@AutoCreate
public class LdifService implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6690460114767359078L;

	@Logger
	private Log log;
	
	@In
	private LdapEntryManager ldapEntryManager;
	
	LdifDataUtility ldifDataUtility = LdifDataUtility.instance();	

	
	public ResultCode importLdifFileInLdap(InputStream is) throws LDAPException{
		ResultCode result = ResultCode.UNAVAILABLE;
		
		LDAPConnectionPool ldapConnectionPool = ldapEntryManager.getLdapOperationService().getConnectionPool();
		LDAPConnection connection = null;
        try {
        	 connection = ldapConnectionPool.getConnection();

			LDIFReader importLdifReader = new LDIFReader(is);
			
			result = ldifDataUtility.importLdifFile(connection,importLdifReader);
			importLdifReader.close();
			
		} catch(Exception e ) {
			log.info("LDIFReader   --- : " + e.getMessage());
			e.printStackTrace();
			
		}finally {
			if (connection != null) {
				ldapConnectionPool.releaseConnection(connection);
			}
		}
		return result;
		
	}
	
	public ResultCode validateLdifFile(InputStream is, String dn) throws LDAPException{
		ResultCode result = ResultCode.UNAVAILABLE;
		try {
			LDIFReader validateLdifReader = new LDIFReader(is);
			result = ldifDataUtility.validateLDIF(validateLdifReader,dn);			
			log.info("LDIFReader successfully");
			validateLdifReader.close();
			
		} catch(Exception e ) {
			log.info("LDIFReader   --- : "+e.getMessage());
			e.printStackTrace();
			
		}
		return result;
		
	}
	

}
