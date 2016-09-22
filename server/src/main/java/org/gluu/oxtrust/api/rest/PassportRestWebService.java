package org.gluu.oxtrust.api.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.ldap.service.OxPassportService;
import org.gluu.oxtrust.model.passport.PassportConfigResponse;
import org.gluu.oxtrust.model.passport.PassportStrategy;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.oxauth.client.uma.RptStatusService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.wrapper.UmaClient;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;


/**
 * PassportConfigurationEndPoint Implementation
 * 
 * @author Shekhar L.
 */


@Name("PassportConfigurationEndPoint")
@Path("/passportconfig")
public class PassportRestWebService {
	@Logger
	private Log log;
	
	@In(create = true, value="passportService")
	private OxPassportService oxPassportService ;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "umaMetadataConfiguration")
	private UmaConfiguration metadataConfiguration;
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public Response getPassportConfig(@FormParam(OxTrustConstants.OXAUTH_ACCESS_TOKEN) final String rpt) throws Exception{
		PassportConfigResponse passportConfigResponse = null;
		try{
			RptStatusService rptStatusService = UmaClientFactory.instance().createRptStatusService(metadataConfiguration);			
			String umaPatClientId = applicationConfiguration.getOxAuthClientId();
			String umaPatClientSecret = applicationConfiguration.getOxAuthClientPassword();
			
			if (umaPatClientSecret != null) {
				try {
					umaPatClientSecret = StringEncrypter.defaultInstance().decrypt(umaPatClientSecret, cryptoConfigurationSalt);
				} catch (EncryptionException ex) {
					log.error("Failed to decrypt client password", ex);
				}
			}
			
			String tokenEndpoint = metadataConfiguration.getTokenEndpoint();			
			Token patToken = UmaClient.requestPat(tokenEndpoint, umaPatClientId, umaPatClientSecret);
			
			if((patToken != null) ){			
				RptIntrospectionResponse tokenStatusResponse = rptStatusService.requestRptStatus(
		                    "Bearer " + patToken.getAccessToken(),
		                    rpt, "");
				
				if((tokenStatusResponse != null) && (tokenStatusResponse.getActive())){
					passportConfigResponse = new PassportConfigResponse();
					LdapOxPassportConfiguration ldapOxPassportConfiguration = oxPassportService.loadConfigurationFromLdap();
					List<org.xdi.config.oxtrust.PassportConfiguration>  passportConfigurations  =ldapOxPassportConfiguration.getPassportConfigurations();
					Map  <String ,PassportStrategy> PassportConfigurationsMap = new HashMap<String, PassportStrategy>();
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
					return Response.status(Response.Status.OK).entity(passportConfigResponse).build();
					
				}else{
					log.info("Invalid GAT/RPT token. ");
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
				
			}else{
				log.info("Unable to get PAT token. ");	
				return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
			}
			
		}catch(Exception e){
			log.error("Exception Occured : {0} ", e.getMessage());
			e.printStackTrace();			
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}	
	}

}
