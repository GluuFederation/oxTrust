package org.gluu.oxauth.client.dev;

import java.io.IOException;

import org.gluu.conf.service.ConfigurationFactory;
import org.gluu.oxauth.cas.auth.client.AuthClient;
import org.gluu.oxauth.cas.auth.conf.CasAppConfiguration;
import org.gluu.oxauth.cas.auth.conf.CasLdapAppConfiguration;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class CasConfigurationTest extends ConfigurationFactory<CasAppConfiguration, CasLdapAppConfiguration> {

	@Override
	protected String getDefaultConfigurationFileName() {
		return "oxcas-ldap.properties";
	}

	@Override
	protected Class<CasLdapAppConfiguration> getAppConfigurationType() {
		return CasLdapAppConfiguration.class;
	}

	@Override
	protected String getApplicationConfigurationPropertyName() {
		return "casConfigurationEntryDN";
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		AuthClient client = new AuthClient();
		client.init();
		
		System.out.println(client.isOpenIdDefaultAuthenticator());
		System.out.println(client.getAppConfiguration().getOpenIdClaimMapping());
		System.out.println(client.getOpenIdConfiguration());
	}

}
