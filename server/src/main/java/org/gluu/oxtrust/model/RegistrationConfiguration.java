/**
 * 
 */
package org.gluu.oxtrust.model;

import java.util.List;

import lombok.Data;

import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;

/**
 * @author "Oleksiy Tataryn"
 *
 */

public @Data class RegistrationConfiguration {
	
	@LdapJsonObject
	private List<RegistrationInterceptorScript> registrationInterceptorScripts;
	
	@LdapJsonObject
	private boolean registrationInterceptorsConfigured;
	
	@LdapJsonObject
	private boolean invitationCodesManagementEnabled;
	
	@LdapJsonObject
	private boolean uninvitedRegistrationAllowed;
	
	//unused
	@LdapJsonObject
	private boolean inboundSAMLRegistrationAllowed;
	
	@LdapJsonObject
	private boolean accountsTimeLimited;
	
	@LdapJsonObject
	private String accountsExpirationPeriod;

	@LdapJsonObject
	private String accountsExpirationServiceFrequency;
	
	@LdapJsonObject
	private String linksExpirationFrequency;
	
	@LdapJsonObject
	private List<String> additionalAttributes;
	
}
