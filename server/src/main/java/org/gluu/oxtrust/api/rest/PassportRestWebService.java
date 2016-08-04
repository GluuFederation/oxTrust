package org.gluu.oxtrust.api.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.ldap.service.OxPassportService;
import org.gluu.oxtrust.model.passport.PassportConfigResponse;
import org.gluu.oxtrust.model.passport.PassportStrategy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;


/**
 * PassportConfigurationEndPoint Implementation
 * 
 * @author Shekhar L.
 */


@Name("PassportConfigurationEndPoint")
@Path("/passportconfig")
public class PassportRestWebService {
	
	@In(create = true, value="passportService")
	private OxPassportService oxPassportService ;
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public PassportConfigResponse getPassportConfig(){
		
		PassportConfigResponse passportConfigResponse = new PassportConfigResponse();
		LdapOxPassportConfiguration ldapOxPassportConfiguration = oxPassportService.loadConfigurationFromLdap();
		List<org.xdi.config.oxtrust.PassportConfiguration>  passportConfigurations  =ldapOxPassportConfiguration.getPassportConfigurations();
		Map  <String ,PassportStrategy> PassportConfigurationsMap = new HashMap();
		for(org.xdi.config.oxtrust.PassportConfiguration passportConfiguration : passportConfigurations){			
			if(passportConfiguration.getProvider().equalsIgnoreCase("passport")){
				passportConfigResponse.setApplicationEndpoint(passportConfiguration.getApplicationEndpoint());				
			}else{
				PassportStrategy passportStrategy = new PassportStrategy();
				passportStrategy.setCallbackURL(passportConfiguration.getCallbackURL());
				passportStrategy.setClientID(passportConfiguration.getClientID());
				passportStrategy.setClientSecret(passportConfiguration.getClientSecret());
				passportStrategy.setProvider(passportConfiguration.getProvider());
				PassportConfigurationsMap.put(passportStrategy.getProvider(), passportStrategy);
			}
			
		}	
		passportConfigResponse.setPassportStrategies(PassportConfigurationsMap);	
		return passportConfigResponse;
		
	}

}
