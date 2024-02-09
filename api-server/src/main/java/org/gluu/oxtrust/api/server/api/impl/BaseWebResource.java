package org.gluu.oxtrust.api.server.api.impl;

import org.slf4j.Logger;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.*;

@OpenAPIDefinition (
	info = @Info (
		title = "oxTrust API",
		version = "4.5.4",
		description = "This is an API for Gluu Server's oxTrust administrative interface. Go to https://gluu.org for more information",
		termsOfService = "https://gluu.org/gluu-terms-and-conditions/",
		contact = @Contact(url="https://support.gluu.org/",name="Gluu Support",email="support@gluu.org"),
		license = @License(name = "Gluu Support License", url = "https://gluu.org/")  
	)
)

@SecurityScheme(name = "oauth2", type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2, flows = @OAuthFlows(clientCredentials = @OAuthFlow(tokenUrl = "https://{op-hostname}/.../token", scopes = {
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.apiconfig.read", description = "View Api Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.apiconfig.write", description = "Manage Api Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.attribute.read", description = "View Attribute related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.attribute.write", description = "Manage Attribute related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.authenticationmethod.read", description = "View Authentication Method related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.authenticationmethod.write", description = "Manage Authentication Method related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.captchaconfig.read", description = "View Captcha Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.captchaconfig.write", description = "Manage Captcha Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.casprotocol.read", description = "View Cas Protocol related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.casprotocol.write", description = "Manage Cas Protocol related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.certificates.read", description = "View Certificates related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.certificates.write", description = "Manage Certificates related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.client.read", description = "View Client related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.client.write", description = "Manage Client related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.configuration.read", description = "View Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.configuration.write", description = "Manage Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.customscript.read", description = "View Custom Script related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.customscript.write", description = "Manage Custom Script related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.group.read", description = "View Group related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.group.write", description = "Manage Group related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.idpconfig.read", description = "View Idp Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.idpconfig.write", description = "Manage  Idp Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.ldapauthentication.read", description = "View Idap Authentication related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.ldapauthentication.write", description = "Manage Idap Authentication related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.metricconfig.read", description = "View Metric Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.metricconfig.write", description = "Manage Metric Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxauthconfiguration.read", description = "View Oxauth Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxauthconfiguration.write", description = "Manage Oxauth Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.OxauthjsonSetting.read", description = "View Oxauth Json Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxauthjsonSetting.write", description = "Manage Oxauth Json Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustconfiguration.read", description = "View Oxtrust Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustconfiguration.write", description = "Manage Oxtrust Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustjsonSetting.read", description = "View Oxtrust Json Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustjsonSetting.write", description = "Manage Oxtrust Json Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustsetting.read", description = "View Oxtrust Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.oxtrustsetting.write", description = "Manage Oxtrust Setting related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportbasicconfig.read", description = "View Passport Basic Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportbasicconfig.write", description = "Manage Passport Basic Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportconfig.read", description = "View Passport Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportconfig.write", description = "Manage Passport Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportprovider.read", description = "View Passport Provider related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.passportprovider.write", description = "Manage Passport Provider related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.people.read", description = "View People related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.people.write", description = "Manage People related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.rptConfig.read", description = "View Rpt Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.rptConfig.write", description = "Manage Rpt Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.scimconfig.read", description = "View Scim Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.scimconfig.write", description = "Manage Scim Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.scope.read", description = "View Scope related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.scope.write", description = "Manage Scope related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.sectoridentifier.read", description = "View Sector Identifier related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.sectoridentifier.write", description = "Manage Sector Identifier related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.serverstatus.read", description = "View Server Status related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.serverstatus.write", description = "Manage Server Status related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.smtpconfiguration.read", description = "View Smtp Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.smtpconfiguration.write", description = "Manage Smtp Configuration related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.systemconfig.read", description = "View System Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.systemconfig.write", description = "Manage System Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.trustedidp.read", description = "View Trusted Idp related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.trustedidp.write", description = "Manage Trusted Idp related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.saml.read", description = "View Saml related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.saml.write", description = "Manage Saml related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.umaresource.read", description = "View Uma Resource related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.umaresource.write", description = "Manage Uma Resource related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.umascope.read", description = "View Uma Scope related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.umascope.write", description = "Manage Uma Scope related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.gluuradiusclient.read", description = "View Gluu Radius Client related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.gluuradiusclient.write", description = "Manage Gluu Radius Client related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.gluuradiusconfig.read", description = "View Gluu Radius Config related information"),
		@OAuthScope(name = "https://gluu.org/auth/oxtrust.gluuradiusconfig.write", description = "Manage Gluu Radius Config related information")}
)))

public class BaseWebResource {

	public BaseWebResource() {
	}

	public void log(Logger logger, Exception e) {
		logger.debug("++++++++++API-ERROR", e);
	}

	public void log(Logger logger, String message) {
		logger.info(message);
	}

}
