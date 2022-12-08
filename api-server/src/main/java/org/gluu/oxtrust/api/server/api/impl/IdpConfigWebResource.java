package org.gluu.oxtrust.api.server.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.api.server.model.IdpConfig;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.api.server.util.Constants;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path(ApiConstants.CONFIGURATION + ApiConstants.IDP)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class IdpConfigWebResource extends BaseWebResource {
	@Inject
	private Logger logger;
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	@Inject
	private EncryptionService encryptionService;
	
	@GET
	@Operation(summary = "Retrieve idp configuration", description = "Retrieve idp configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = IdpConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_IDPCONFIG_READ })
	public Response retrieveIdpConfiguration() {
		try {
			log(logger, "Retrieving idp configuration");
			AppConfiguration oxTrustappConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			IdpConfig idpConfig = new IdpConfig();
			idpConfig.setIdp3EncryptionCert(oxTrustappConfiguration.getIdp3EncryptionCert());
			idpConfig.setIdp3SigningCert(oxTrustappConfiguration.getIdp3SigningCert());
			idpConfig.setIdpBindDn(oxTrustappConfiguration.getIdpBindDn());
			idpConfig.setIdpBindPassword(encryptionService.decrypt(oxTrustappConfiguration.getIdpBindPassword()));
			idpConfig.setIdpLdapProtocol(oxTrustappConfiguration.getIdpLdapProtocol());
			idpConfig.setIdpLdapServer(oxTrustappConfiguration.getIdpLdapServer());
			idpConfig.setIdpSecurityCert(oxTrustappConfiguration.getIdpSecurityCert());
			idpConfig.setIdpSecurityKey(oxTrustappConfiguration.getIdpSecurityKey());
			idpConfig.setIdpSecurityKeyPassword(
					encryptionService.decrypt(oxTrustappConfiguration.getIdpSecurityKeyPassword()));
			idpConfig.setIdpUrl(oxTrustappConfiguration.getIdpUrl());
			idpConfig.setIdpUserFields(oxTrustappConfiguration.getIdpUserFields());
			idpConfig.setShibboleth3IdpRootDir(oxTrustappConfiguration.getShibboleth3IdpRootDir());
			idpConfig.setShibboleth3FederationRootDir(oxTrustappConfiguration.getShibboleth3FederationRootDir());
			idpConfig.setShibboleth3SpConfDir(oxTrustappConfiguration.getShibboleth3SpConfDir());
			idpConfig.setShibbolethVersion(oxTrustappConfiguration.getShibbolethVersion());
			return Response.ok(idpConfig).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Operation(summary = "Update idp configuration", description = "Update idp configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = IdpConfig.class)), description = Constants.RESULT_SUCCESS),
			@ApiResponse(responseCode = "500", description = "Server error") })
	@ProtectedApi(scopes = { ApiScopeConstants.SCOPE_IDPCONFIG_WRITE })
	public Response updateIdpConfiguration(IdpConfig idpConfig) {
		try {
			log(logger, "Processing idp configuration update");
			Preconditions.checkNotNull(idpConfig, "Attempt to update null");
			AppConfiguration appConfiguration = jsonConfigurationService.getOxTrustappConfiguration();
			if (!Strings.isNullOrEmpty(idpConfig.getIdp3EncryptionCert())) {
				appConfiguration.setIdp3EncryptionCert(idpConfig.getIdp3EncryptionCert());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdp3SigningCert())) {
				appConfiguration.setIdp3SigningCert(idpConfig.getIdp3SigningCert());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpSecurityCert())) {
				appConfiguration.setIdpSecurityCert(idpConfig.getIdpSecurityCert());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpBindDn())) {
				appConfiguration.setIdpBindDn(idpConfig.getIdpBindDn());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpBindPassword())) {
				appConfiguration.setIdpBindPassword(encryptionService.encrypt(idpConfig.getIdpBindPassword()));
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpSecurityKeyPassword())) {
				appConfiguration
						.setIdpSecurityKeyPassword(encryptionService.encrypt(idpConfig.getIdpSecurityKeyPassword()));
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpLdapProtocol())) {
				appConfiguration.setIdpLdapProtocol(idpConfig.getIdpLdapProtocol());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpLdapServer())) {
				appConfiguration.setIdpLdapServer(idpConfig.getIdpLdapServer());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpSecurityKey())) {
				appConfiguration.setIdpSecurityKey(idpConfig.getIdpSecurityKey());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpUrl())) {
				appConfiguration.setIdpUrl(idpConfig.getIdpUrl());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getIdpUserFields())) {
				appConfiguration.setIdpUserFields(idpConfig.getIdpUserFields());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getShibboleth3IdpRootDir())) {
				appConfiguration.setShibboleth3IdpRootDir(idpConfig.getShibboleth3IdpRootDir());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getShibboleth3FederationRootDir())) {
				appConfiguration.setShibboleth3FederationRootDir(idpConfig.getShibboleth3FederationRootDir());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getShibboleth3SpConfDir())) {
				appConfiguration.setShibboleth3SpConfDir(idpConfig.getShibboleth3SpConfDir());
			}
			if (!Strings.isNullOrEmpty(idpConfig.getShibbolethVersion())) {
				appConfiguration.setShibbolethVersion(idpConfig.getShibbolethVersion());
			}
			jsonConfigurationService.saveOxTrustappConfiguration(appConfiguration);
			return Response.ok(Constants.RESULT_SUCCESS).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
