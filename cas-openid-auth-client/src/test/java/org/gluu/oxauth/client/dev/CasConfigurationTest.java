package org.gluu.oxauth.client.dev;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxauth.cas.auth.client.AuthClient;
import org.gluu.oxauth.cas.auth.conf.CasAppConfiguration;
import org.gluu.oxauth.cas.auth.conf.CasLdapAppConfiguration;
import org.gluu.oxauth.client.conf.Configuration;

public class CasConfigurationTest extends Configuration<CasAppConfiguration, CasLdapAppConfiguration> {

	@Override
	protected String getLdapConfigurationFileName() {
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
