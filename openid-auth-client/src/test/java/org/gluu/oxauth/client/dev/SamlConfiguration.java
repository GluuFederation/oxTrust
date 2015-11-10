package org.gluu.oxauth.client.dev;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxauth.client.OpenIdClient;
import org.gluu.oxauth.client.conf.AppConfiguration;
import org.gluu.oxauth.client.conf.Configuration;
import org.gluu.oxauth.client.conf.LdapAppConfiguration;

public class SamlConfiguration extends Configuration<AppConfiguration, LdapAppConfiguration> {

	@Override
	protected String getLdapConfigurationFileName() {
		return "oxidp-ldap.properties";
	}

	@Override
	protected Class<LdapAppConfiguration> getAppConfigurationType() {
		return LdapAppConfiguration.class;
	}

	@Override
	protected AppConfiguration initAppConfiguration(LdapAppConfiguration ldapAppConfiguration) {
		return ldapAppConfiguration.getApplication();
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		SamlConfiguration conf = new SamlConfiguration();
		OpenIdClient openIdClient = conf.getOpenIdClient();
		openIdClient.getRedirectionUrl(null);
	}

}
