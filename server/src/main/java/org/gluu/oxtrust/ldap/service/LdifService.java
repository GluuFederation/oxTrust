/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.InputStream;
import java.io.Serializable;

import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.LdifDataUtility;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldif.LDIFReader;

/**
 * Provides operations with persons
 * 
 * @author Yuriy Movchan Date: 10.13.2010
 */
@Scope(ScopeType.STATELESS)
@Name("ldifService")
@AutoCreate
public class LdifService implements Serializable{
	
	@Logger
	private Log log;
	
	@In
	private LdapEntryManager ldapEntryManager;
	
	LdifDataUtility ldifDataUtility = LdifDataUtility.instance();	

	@In
	private IGroupService groupService;

	private transient OperationsFacade ldapOperationService;
	
	public boolean destroy() {
		boolean destroyResult = this.ldapOperationService.destroy();
		
		return destroyResult;
	}
	
	public ResultCode importLdifFileInLdap(InputStream is) throws LDAPException{
		ResultCode result = ResultCode.UNAVAILABLE;
		
		LDAPConnection connection = null;
		try {
		connection = ldapOperationService.getConnection();
		//ResultCode result = LdifDataUtility.instance().importLdifFileContent(connection, ldifFileContent);

			LDIFReader importLdifReader = new LDIFReader(is);
			
			result = ldifDataUtility.importLdifFile(connection,importLdifReader);
			importLdifReader.close();
			
		} catch(Exception e ) {
			log.info("LDIFReader   --- : " + e.getMessage());
			
		}finally {
			if (connection != null) {
				ldapOperationService.releaseConnection(connection);
			}
		}
		return result;
		
	}
	
	public ResultCode validateLdifFile(InputStream is) throws LDAPException{
		ResultCode result = ResultCode.UNAVAILABLE;
		try {
			LDIFReader validateLdifReader = new LDIFReader(is);
			result = ldifDataUtility.validateLDIF(validateLdifReader);			
			log.info("LDIFReader successfully");
			validateLdifReader.close();
			
		} catch(Exception e ) {
			log.info("LDIFReader   --- : "+e.getMessage());
			
		}
		return result;
		
	}
	

}
