/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.server.api.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.gluu.oxtrust.action.TrustContactsAction;
import org.gluu.oxtrust.api.saml.SAMLTrustRelationshipShort;
import org.gluu.oxtrust.api.server.util.ApiConstants;
import org.gluu.oxtrust.api.server.util.ApiScopeConstants;
import org.gluu.oxtrust.service.TrustService;
import org.gluu.oxtrust.service.filter.ProtectedApi;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.service.MetadataValidationTimer;
import org.gluu.oxtrust.service.Shibboleth3ConfService;
import org.gluu.oxtrust.service.SvnSyncTimer;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.GluuAttribute;
import org.gluu.model.GluuStatus;
import org.gluu.model.TrustContact;
import org.gluu.orm.util.ArrayHelper;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.gluu.service.MailService;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import javax.validation.constraints.NotNull;

@Path(ApiConstants.BASE_API_URL + ApiConstants.SAMLTR)
@ApplicationScoped
public class TrustRelationshipWebService extends BaseWebResource {
    
    @Inject
    private Logger logger;
    
    @Inject
    private org.gluu.oxtrust.service.TrustService trustService;
	
    @Inject
    private ClientService clientService;
    
    private ConfigurationService configurationService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private SvnSyncTimer svnSyncTimer;
    
    @Inject
    private SAMLMetadataParser samlMetadataParser;
	
    @Inject
    private MetadataValidationTimer metadataValidationTimer;

    @Inject
    private TrustContactsAction trustContactsAction;

    @Inject
    private Shibboleth3ConfService shibboleth3ConfService;
    
    ObjectMapper objectMapper;
    
    @GET
    @Path("/read/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "read TrustRelationship", description = "Returns a GluuSAMLTrustRelationship by inum")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuSAMLTrustRelationship.class)), description = "Success"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(oauthScopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response read(@PathParam("inum") @NotNull String inum) {
        logger.info("Read Trust Relationship");
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            return Response.ok(trustRelationship).build();
        } catch (Exception e) {
            logger.error("read() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/create")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "create TrustRelationship", description = "Create new GluuSAMLTrustRelationship. Returns inum.")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response create(GluuSAMLTrustRelationship trustRelationship) {
        logger.info("Create Trust Relationship");
        try {
        	if(!StringHelper.isEmpty(trustRelationship.getSpMetaDataSourceType().name())
        			&& (GluuMetadataSourceType.contains(trustRelationship.getSpMetaDataSourceType().name()))) {
            
        		String result = saveTR(trustRelationship);
	            if(result.equalsIgnoreCase(OxTrustConstants.RESULT_SUCCESS)) {
	            	return Response.status(Response.Status.CREATED)
						.entity(trustRelationship.getInum()).build();
	        	}else {
	        		return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), result)
	        				.entity("{'status code' : 400,'status message' : '"+result+"'}").build();
	        	}
	            //return inum;
        	}else {
        		return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),"Source Type missing.")
    					.entity("{'status code' : 400,'status message' : 'Source Type missing.'}").build();
        	}
        } catch (Exception e) {
            logger.error("create() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PUT
    @Path("/update/{inum}")
    @Operation(summary = "update TrustRelationship", description = "Update GluuSAMLTrustRelationship.")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GluuSAMLTrustRelationship.class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response update(@PathParam("inum") @NotNull String inum, GluuSAMLTrustRelationship trustRelationship) {
        logger.info("Update Trust Relationship");
        try {
            //String dn = trustService.getDnForTrustRelationShip(inum);
            //trustRelationship.setDn(dn);
            //trustService.updateTrustRelationship(trustRelationship);
        	String result = saveTR(trustRelationship);
            if(result.equalsIgnoreCase(OxTrustConstants.RESULT_SUCCESS)) {
            	return Response.ok(trustService.getRelationshipByInum(inum)).build();
        	}else {
        		return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), result)
        				.entity("{'status code' : 400,'status message' : '"+result+"'}").build();
        	}
            
        } catch (Exception e) {
            logger.error("update() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DELETE
    @Path("/delete/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "delete TrustRelationship", description = "Delete GluuSAMLTrustRelationship.")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response delete(@PathParam("inum") @NotNull String inum) {
        logger.info("Delete Trust Relationship");
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            if(trustRelationship != null)
            	trustService.removeTrustRelationship(trustRelationship);
            else {
            	return Response.ok(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(Response.Status.OK).build();
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "list TrustRelationships", description = "List all GluuSAMLTrustRelationship.")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GluuSAMLTrustRelationship.class)), description = "SUCCESS"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response list() {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllTrustRelationships());
            //convert to JSON
            return Response.ok(trustRelationships).build();
            //return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("list() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } 
    }
    
    @GET
    @Path("/list_all_federations")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK",  content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response listAllFederations() {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllFederations());
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("listAllFederations() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }	
    }
    
    @GET
    @Path("/list_all_active_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response listAllActiveTrustRelationships() {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllActiveTrustRelationships());
            
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("listAllActiveTrustRelationships() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            
        }
    }
    
    @GET
    @Path("/list_all_other_federations/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response listAllOtherFederations(@PathParam("inum") String inum) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllOtherFederations(inum));
            
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("listAllOtherFederations() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/list_all_saml_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response listAllSAMLTrustRelationships(@QueryParam("size_limit") int sizeLimit) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllSAMLTrustRelationships(sizeLimit));
            
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("listAllSAMLTrustRelationships() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/list_deconstructed_trust_relationships/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK",  content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response listDeconstructedTrustRelationships(@PathParam("inum") String inum) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getDeconstructedTrustRelationships(trustRelationship));
           
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("listAllActiveTrustRelationships() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/search_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK",content = @Content(schema = @Schema(implementation = SAMLTrustRelationshipShort[].class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response searchTrustRelationships(@QueryParam("pattern") @NotNull String pattern, @QueryParam("size_limit") int sizeLimit) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.searchSAMLTrustRelationships(pattern, sizeLimit));
            
            return Response.ok(trustRelationships).build();
        } catch (Exception e) {
            logger.error("searchTrustRelationships() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/set_metadata/{inum}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response setMetadata(@PathParam("inum") String trustRelationshipInum, @NotNull String metadata) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            String metadataFileName = trustRelationship.getSpMetaDataFN();
            if (StringHelper.isEmpty(metadataFileName)) {
                // Generate new file name
            	metadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationshipInum);
            }
            shibboleth3ConfService.saveSpMetadataFile(metadataFileName, metadata.getBytes("UTF8"));
            
            trustRelationship.setSpMetaDataFN(metadataFileName);
            trustRelationship.setSpMetaDataSourceType(GluuMetadataSourceType.FILE);
            trustService.updateTrustRelationship(trustRelationship);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/set_metadata_url/{inum}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response setMetadataURL(@PathParam("inum") String trustRelationshipInum, @NotNull String url) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            String metadataFileName = trustRelationship.getSpMetaDataFN();
            if (StringHelper.isEmpty(metadataFileName)) {
                // Generate new file name
		metadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationshipInum);
            }
            
            shibboleth3ConfService.saveSpMetadataFile(url, metadataFileName);
            
            trustRelationship.setSpMetaDataFN(metadataFileName);
            trustRelationship.setSpMetaDataSourceType(GluuMetadataSourceType.FILE);
            trustService.updateTrustRelationship(trustRelationship);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/add_attribute/{inum}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE})
    public Response addAttribute(@PathParam("inum") String trustRelationshipInum, @NotNull String attribute) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            List<String> attributes = trustRelationship.getReleasedAttributes();
            for (String lAttr : attributes) {
                if (attribute.equals(lAttr)) 
                	return Response.status(Response.Status.CREATED).build(); // Nothing to add
            }
            
            attributes.add(attribute);
            updateReleasedAttributes(trustRelationship);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.error("addAttribute() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/generate_inum_for_new_trust_relationship")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK",  content = @Content(schema = @Schema(implementation =String.class))),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response generateInumForNewTrustRelationship() {
        try {
            String inum = trustService.generateInumForNewTrustRelationship();
            return Response.ok(inum).build();
        } catch (Exception e) {
            logger.error("generateInumForNewTrustRelationship() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/get_contacts/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
    		@ApiResponse(responseCode = "200", description = "OK",  content = @Content(schema = @Schema(implementation =TrustContact[].class))),
    		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_READ })
    public Response getContacts(@PathParam("inum") String trustRelationshipInum) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            List<TrustContact> list = trustService.getContacts(trustRelationship);
            return Response.ok(list).build();
        } catch (Exception e) {
            logger.error("getContacts() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/set_contacts/{inum}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "set contacts for TrustRelationship", description = "Find TrustRelationship by inum and set contacts. Contacts parameter is List<TrustContact>")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response setContacts(@PathParam("inum") String trustRelationshipInum, String contacts) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            List<TrustContact> contactsList = objectMapper.readValue(contacts, new TypeReference<List<TrustContact>>() {});

            trustService.saveContacts(trustRelationship, contactsList);
            return Response.ok(Response.Status.OK).build();
        } catch (Exception e) {
            logger.error("setContacts() Exception", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/set_certificate/{inum}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)    
    @Operation(summary = "set certificate for TrustRelationship", description = "Find TrustRelationship by inum and set certificate.")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response setCertificate(@PathParam("inum") String trustRelationshipInum, String certificate) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            if (StringHelper.isEmpty(certificate)) {
                logger.error("Failed to update TR certificate - certificate is empty");
                return Response.ok(Response.Status.BAD_REQUEST,"Certificate is empty.").build();
            }
            
            updateTRCertificate(trustRelationship, certificate);
            return Response.ok(Response.Status.OK).build();
        } catch (Exception e) {
            logger.error("Failed to update certificate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
        
    @DELETE
    @Path("/remove_attribute")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response removeAttribute(GluuAttribute attribute) {
        try {
            trustService.removeAttribute(attribute);
            return Response.ok(Response.Status.OK).build();
        } catch (Exception e) {
            logger.error("Failed to remove attribute", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }        
    }
    
    @PUT
    @Path("/generate_configuration_files")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "generate configuration files", description = "Generate configuration files for Shibboleth IDP")
    @ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "500", description = "Server error") })
    @ProtectedApi(scopes = { ApiScopeConstants.SCOPE_SAML_TR_WRITE })
    public Response generateConfigurationFiles() {
        try {
            List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
            if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
                logger.error("Failed to update Shibboleth v3 configuration by web API request");
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            } else {
                logger.info("Shibboleth v3 configuration updated successfully by web API request");
                return Response.ok(Response.Status.OK).build();
            }
        } catch (Exception e) {
            logger.error("Failed to generateConfigurationFiles", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } 
    }
    
    /**
     * Save SAML TrustRelationship.
     * 
     * @param trustRelationship
     * @param metadata - need for FILE type TR only
     * @param certificate - need for FILE type TR, optional for GENERATE type TR
     * @return 
     */
    private String saveTR(GluuSAMLTrustRelationship trustRelationship) {
        String inum;
        boolean update = false;
        synchronized (svnSyncTimer) {
            if (StringHelper.isEmpty(trustRelationship.getInum())) {
                inum = trustService.generateInumForNewTrustRelationship();
                trustRelationship.setInum(inum);
            } else {
                inum = trustRelationship.getInum();
                if(trustRelationship.getSpMetaDataFN() == null )
                update=true;
            }

            boolean updateShib3Configuration = appConfiguration.isConfigGeneration();
            switch (trustRelationship.getSpMetaDataSourceType()) {
            case MANUAL:
                try {
                    if (saveSpMetaDataFileSourceTypeManual(trustRelationship, trustRelationship.getMetadataStr())) {
                        //updateSpMetaDataCert(certWrapper);
                        if (!update) {
                            trustRelationship.setStatus(GluuStatus.ACTIVE);
                        }
                    } else {
                    	logger.error("Failed to save meta-data content in file");
                        return "Failed to save meta-data content.";
                    }
                } catch (IOException ex) {
                	logger.error("Failed to generate SP metadata", ex);
                    return "Manual : Failed to generate SP metadata";
                }

                break;
            case FILE:
                try {
                    if (saveSpMetaDataFileSourceTypeFile(trustRelationship, inum, trustRelationship.getMetadataStr())) {
                        
                        updateTRCertificate(trustRelationship, trustRelationship.getCertificate());
                        if(!update){
                            trustRelationship.setStatus(GluuStatus.ACTIVE);
                        }
                    } else {
                        logger.error("Failed to save SP metadata file {}", trustRelationship.getMetadataStr());
                        return OxTrustConstants.RESULT_FAILURE;
                    }
                } catch (IOException ex) {
                    logger.error("Failed to download SP metadata", ex);
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to download SP metadata");

                    return "File : Failed to save SP metadata File";
                }

                break;
            case URI:
                try {
                    boolean result = shibboleth3ConfService.existsResourceUri(trustRelationship.getSpMetaDataURL());
                    if(result){
                        saveSpMetaDataFileSourceTypeURI(trustRelationship);
                    }else{
                        logger.info("There is no resource found Uri : {}", trustRelationship.getSpMetaDataURL());
                    }
                    if(!update){
                        trustRelationship.setStatus(GluuStatus.ACTIVE);
                    }
                    
                } catch (Exception e) {
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Unable to download metadata");
                    return "URI : unable_download_metadata";
                }
                break;
            case FEDERATION:
                if(!update){
                    trustRelationship.setStatus(GluuStatus.ACTIVE);
                }
                if (trustRelationship.getEntityId() == null) {
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "EntityID must be set to a value");
                    return "FEDERATION : invalid_entity_id";
                }

                break;
            case MDQ:
            	try {
                    if (generateSpMetaDataFile(trustRelationship)) {
                    	if (!update) {
                            trustRelationship.setStatus(GluuStatus.ACTIVE);
                        }
                    } else {
                        logger.error("Failed to generate MDQ SP meta-data file");
                        return OxTrustConstants.RESULT_FAILURE;
                    }
                } catch (Exception ex) {
                	logger.error("Failed to generate MDQ  SP certificate", ex);

                    return "MDQ : Failed to generate MDQ SP meta-data file";
                }

                break;
            default:

                break;
            }

            updateReleasedAttributes(trustRelationship);

            // We call it from TR validation timer
            if (trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.MANUAL)
                            || (trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.FEDERATION))) {
                boolean federation = shibboleth3ConfService.isFederation(trustRelationship);
                trustRelationship.setFederation(federation);
            }

            //trustContactsAction.saveContacts();

            if (update) {
                try {
                    saveTR(trustRelationship, update);
                } catch (BasePersistenceException ex) {
                    logger.error("Failed to update trust relationship {}", inum, ex);
                    return "Failed to update trust relationship {}"+ inum;
                }
            } else {
                String dn = trustService.getDnForTrustRelationShip(inum);
                // Save trustRelationship
                trustRelationship.setDn(dn);
                try {
                        saveTR(trustRelationship, update);
                } catch (BasePersistenceException ex) {
                        logger.error("Failed to add new trust relationship {}", trustRelationship.getInum(), ex);
                        return "Failed to add new trust relationship.";
                }

                update = true;
            }

            if (updateShib3Configuration) {
                List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
                if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
                    logger.error("Failed to update Shibboleth v3 configuration");
                    return "Failed to update Shibboleth v3 configuration";
                } else {
                    logger.info("Shibboleth v3 configuration updated successfully");
                    
                }
            }
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
        
        
    private void saveTR(GluuSAMLTrustRelationship trustRelationship, boolean isUpdate) {
        logger.info("Saving Trust Relationship");
        if (isUpdate) {
            String oldLogoutRedirectUri = trustService.getRelationshipByDn(trustRelationship.getDn()).getSpLogoutURL();
            String newLogoutRedirectUri = trustRelationship.getSpLogoutURL();
            boolean oxClientUpdateNeeded = (oldLogoutRedirectUri != null) && (newLogoutRedirectUri != null) &&
                    !newLogoutRedirectUri.equals(oldLogoutRedirectUri);

            boolean parentInactive = trustRelationship.getStatus().equals(GluuStatus.INACTIVE);
//            if(! federatedSites.isEmpty()){
//                for (GluuSAMLTrustRelationship trust : federatedSites) {
//                    if (parentInactive) {
//                        trust.setStatus(GluuStatus.INACTIVE);
//                    }
//                    trustService.updateReleasedAttributes(trust);
//                    trustService.updateTrustRelationship(trust);
//                    svnSyncTimer.updateTrustRelationship(trust, identity.getCredentials().getUsername());
//                }
//            }
            trustService.updateTrustRelationship(trustRelationship);


            if(oxClientUpdateNeeded){
                OxAuthClient client = clientService.getClientByInum(appConfiguration.getOxAuthClientId());
                Set<String> updatedLogoutRedirectUris = new HashSet<String>();
                List<GluuSAMLTrustRelationship> trs = trustService.getAllTrustRelationships();
                if(trs != null && ! trs.isEmpty()){
                    for(GluuSAMLTrustRelationship tr: trs){
                        String logoutRedirectUri = tr.getSpLogoutURL();
                        if(logoutRedirectUri != null && ! logoutRedirectUri.isEmpty()){
                            updatedLogoutRedirectUris.add(logoutRedirectUri);
                        }
                    }
                }
                if(updatedLogoutRedirectUris.isEmpty()){
                    client.setPostLogoutRedirectUris(null);
                }else{
                    client.setPostLogoutRedirectUris(updatedLogoutRedirectUris.toArray(new String[0]));
                }
                clientService.updateClient(client);
            }        
        } else {
            trustService.addTrustRelationship(trustRelationship);
            
        }
    }
    
    /**
     * Sets entityId according to metadatafile. Works for all TR which have own
     * metadata file.
     * 
     * @author �Oleksiy Tataryn�
     */
    private void setEntityId(GluuSAMLTrustRelationship  trustRelationship) {
        String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator	+ Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
        File metadataFile = new File(idpMetadataFolder + trustRelationship.getSpMetaDataFN());

        List<String> entityIdList = samlMetadataParser.getEntityIdFromMetadataFile(metadataFile);
        Set<String> entityIdSet = new TreeSet<String>();

        if(entityIdList != null && ! entityIdList.isEmpty()){
            Set<String> duplicatesSet = new TreeSet<String>(); 
            for (String entityId : entityIdList) {
                if (!entityIdSet.add(entityId)) {
                    duplicatesSet.add(entityId);
                }
            }
        }

        trustRelationship.setUniqueGluuEntityId(entityIdSet);
    }
    
    public boolean saveSpMetaDataFileSourceTypeURI(GluuSAMLTrustRelationship  trustRelationship) throws IOException {
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();
        boolean emptySpMetadataFileName = StringHelper.isEmpty(spMetadataFileName);

        if (emptySpMetadataFileName) {
                // Generate new file name
                spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
                trustRelationship.setSpMetaDataFN(spMetadataFileName);
        }

        String result = shibboleth3ConfService.saveSpMetadataFile(trustRelationship.getSpMetaDataURL(), spMetadataFileName);
        if (StringHelper.isNotEmpty(result)) {
                metadataValidationTimer.queue(result);
        } else {
                logger.error("Failed to download metadata");
        }

        return StringHelper.isNotEmpty(result);
    }
    
    private boolean saveSpMetaDataFileSourceTypeFile(GluuSAMLTrustRelationship  trustRelationship, String inum, String metadata) throws IOException {
        logger.info("Saving metadata file source type: File");
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();
        boolean emptySpMetadataFileName = StringHelper.isEmpty(spMetadataFileName);

        if (StringHelper.isEmpty(metadata)) {
            if (emptySpMetadataFileName) {
                return false;
            }

            // Admin doesn't provide new file. Check if we already has this file
            String filePath = shibboleth3ConfService.getSpMetadataFilePath(spMetadataFileName);
            if (filePath == null) {
                return false;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }

            // File already exist
            return true;
        }

        if (emptySpMetadataFileName) {
            // Generate new file name
            spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
            trustRelationship.setSpMetaDataFN(spMetadataFileName);
            if (trustRelationship.getDn() == null) {
                String dn = trustService.getDnForTrustRelationShip(inum);
                trustRelationship.setDn(dn);
                trustService.addTrustRelationship(trustRelationship);
            } else {
                trustService.updateTrustRelationship(trustRelationship);
            }
        }
        String result = shibboleth3ConfService.saveSpMetadataFile(spMetadataFileName, new CharSequenceInputStream(metadata, StandardCharsets.UTF_8));
        if (StringHelper.isNotEmpty(result)) {
            metadataValidationTimer.queue(result);
        } else {
            //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to save SP meta-data file. Please check if you provide correct file");
        }

        return StringHelper.isNotEmpty(result);

    }
    
    
    /**
     * 
     * @return certificate for generated SP
     * @throws IOException 
     * @throws CertificateEncodingException
     */
    public String generateCertForGeneratedSP(GluuSAMLTrustRelationship trustRelationship) throws IOException {
        X509Certificate cert = null;

        //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Certificate were not provided, or was incorrect. Appliance will create a self-signed certificate.");
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC"); 
            keyPairGen.initialize(2048); 
            KeyPair pair = keyPairGen.generateKeyPair(); 
            StringWriter keyWriter = new StringWriter(); 
            PEMWriter pemFormatWriter = new PEMWriter(keyWriter); 
            pemFormatWriter.writeObject(pair.getPrivate()); 
            pemFormatWriter.close(); 

            String url = trustRelationship.getUrl().replaceFirst(".*//", "");

            X509v3CertificateBuilder v3CertGen = new JcaX509v3CertificateBuilder(new X500Name("CN=" + url + ", OU=None, O=None L=None, C=None"),
                BigInteger.valueOf(new SecureRandom().nextInt()),
                new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30),
                new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)),
                new X500Name("CN=" + url + ", OU=None, O=None L=None, C=None"),
                pair.getPublic());

            cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(v3CertGen.build(new JcaContentSignerBuilder("MD5withRSA").setProvider("BC").build(pair.getPrivate())));
            org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
            byte[] derCert = cert.getEncoded();
            String pemCertPre = new String(encoder.encode(derCert));
            logger.debug(Shibboleth3ConfService.PUBLIC_CERTIFICATE_START_LINE);
            logger.debug(pemCertPre);
            logger.debug(Shibboleth3ConfService.PUBLIC_CERTIFICATE_END_LINE);

            shibboleth3ConfService.saveCert(trustRelationship, pemCertPre);
            shibboleth3ConfService.saveKey(trustRelationship, keyWriter.toString());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to generate certificate", e);
        }

//			String certName = appConfiguration.getCertDir() + File.separator + StringHelper.removePunctuation(appConfiguration.getOrgInum())
//					+ "-shib.crt";
//			File certFile = new File(certName);
//			if (certFile.exists()) {
//				cert = SSLService.instance().getPEMCertificate(certName);
//			}
            

        String certificate = null;

        if (cert != null) {

            try {
                certificate = new String(Base64.encode(cert.getEncoded()));

                logger.info("##### certificate = " + certificate);

            } catch (CertificateEncodingException e) {
                certificate = null;
                //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to encode provided certificate. Please notify Gluu support about this.");
                logger.error("Failed to encode certificate to DER", e);
            }

        } else {
            //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Certificate were not provided, or was incorrect. Appliance will create a self-signed certificate.");
        }

        return certificate;
    }
    
    private boolean generateSpMetaDataFile(GluuSAMLTrustRelationship trustRelationship, String certificate) {
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();

        if (StringHelper.isEmpty(spMetadataFileName)) {
            // Generate new file name
            spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
            trustRelationship.setSpMetaDataFN(spMetadataFileName);
        }

        return shibboleth3ConfService.generateSpMetadataFile(trustRelationship, certificate);
    }
    
    private void updateTRCertificate(GluuSAMLTrustRelationship trustRelationship, String certificate) throws IOException {
        if (StringHelper.isEmpty(certificate)) {
            logger.error("Failed to update TR certificate - certificate is empty");
            return;
        }
        // This regex defines certificate enclosed in X509Certificate tags
        // regardless of namespace(as long as it is not more then 9 characters)
        String certRegEx = "(?ms)(?<=<[^</>]{0,10}X509Certificate>).*(?=</[^</>]{0,10}?X509Certificate>)";

        shibboleth3ConfService.saveCert(trustRelationship, certificate);
        shibboleth3ConfService.saveKey(trustRelationship, null);

        String metadataFileName = trustRelationship.getSpMetaDataFN();
        File metadataFile = new File(shibboleth3ConfService.getSpMetadataFilePath(metadataFileName));
        String metadata = FileUtils.readFileToString(metadataFile);
        String updatedMetadata = metadata.replaceFirst(certRegEx, certificate);
        FileUtils.writeStringToFile(metadataFile, updatedMetadata);
        trustRelationship.setStatus(GluuStatus.ACTIVE);
    }
    
    private static List<SAMLTrustRelationshipShort> convertTRtoTRShort (List<GluuSAMLTrustRelationship> trustRelationships) {
        ArrayList<SAMLTrustRelationshipShort> trustRelationshipsShort = new ArrayList<SAMLTrustRelationshipShort>();
        trustRelationshipsShort.ensureCapacity(trustRelationships.size());
        
        for (GluuSAMLTrustRelationship tr : trustRelationships) {
            trustRelationshipsShort.add(new SAMLTrustRelationshipShort(tr));
        }
        return trustRelationshipsShort;
    }
    
    private void updateReleasedAttributes(GluuSAMLTrustRelationship trustRelationship) {
        List<String> releasedAttributes = new ArrayList<String>();
        String mailMsgPlain = "";
        String mailMsgHtml = "";
        for (GluuCustomAttribute customAttribute : trustRelationship.getReleasedCustomAttributes()) {
            if (customAttribute.isNew()) {
               /* rendererParameters.setParameter("attributeName", customAttribute.getName());
                rendererParameters.setParameter("attributeDisplayName", customAttribute.getMetadata().getDisplayName());
                rendererParameters.setParameter("attributeValue", customAttribute.getValue());

                mailMsgPlain += facesMessages.evalResourceAsString("#{msgs['mail.trust.released.attribute.plain']}");
                mailMsgHtml += facesMessages.evalResourceAsString("#{msgs['mail.trust.released.attribute.html']}");
                rendererParameters.reset();*/

                customAttribute.setNew(false);
            }
            releasedAttributes.add(customAttribute.getMetadata().getDn());
        }

        // send email notification
        if (!StringUtils.isEmpty(mailMsgPlain)) {
            try {
                GluuConfiguration configuration = configurationService.getConfiguration();
                if (ArrayHelper.isEmpty(configuration.getContactEmail()) || configuration.getContactEmail()[0].isEmpty())
                	logger.warn("Failed to send the 'Attributes released' notification email: unconfigured contact email");
                else if (configuration.getSmtpConfiguration() == null
                        || StringHelper.isEmpty(configuration.getSmtpConfiguration().getHost()))
                	logger.warn("Failed to send the 'Attributes released' notification email: unconfigured SMTP server");
                else {
                    /*String subj = facesMessages.evalResourceAsString("#{msgs['mail.trust.released.subject']}");
                    rendererParameters.setParameter("trustRelationshipName", trustRelationship.getDisplayName());
                    rendererParameters.setParameter("trustRelationshipInum", trustRelationship.getInum());
                    String preMsgPlain = facesMessages
                            .evalResourceAsString("#{msgs['mail.trust.released.name.plain']}");
                    String preMsgHtml = facesMessages.evalResourceAsString("#{msgs['mail.trust.released.name.html']}");
                    boolean result = mailService.sendMail(configuration.getContactEmail()[0], null, subj,
                            preMsgPlain + mailMsgPlain, preMsgHtml + mailMsgHtml);

                    if (!result) {
                    	logger.error("Failed to send the notification email");
                    }*/
                }
            } catch (Exception ex) {
            	logger.error("Failed to send the notification email: ", ex);
            }
        }

        if (!releasedAttributes.isEmpty()) {
            trustRelationship.setReleasedAttributes(releasedAttributes);
        } else {
            trustRelationship.setReleasedAttributes(null);
        }
    }
    
    private boolean generateSpMetaDataFile(GluuSAMLTrustRelationship trustRelationship) {
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();

        if (StringHelper.isEmpty(spMetadataFileName)) {
            // Generate new file name
            spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
            trustRelationship.setSpMetaDataFN(spMetadataFileName);
        }

        return shibboleth3ConfService.generateMDQMetadataFile(trustRelationship);
    }
    
    private boolean saveSpMetaDataFileSourceTypeManual(GluuSAMLTrustRelationship trustRelationship , String metadataStr) throws IOException {
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();
        InputStream is = new ByteArrayInputStream(metadataStr.getBytes());
        boolean emptySpMetadataFileName = StringHelper.isEmpty(spMetadataFileName);
        if ((metadataStr == null) || (is == null)) {
            if (emptySpMetadataFileName) {
                logger.debug("The trust relationship {} has an empty Metadata filename",trustRelationship.getInum());
                return false;
            }
            String filePath = shibboleth3ConfService.getSpMetadataFilePath(spMetadataFileName);
            if (filePath == null) {
                logger.debug("The trust relationship {} has an invalid Metadata file storage path", trustRelationship.getInum());
                return false;
            }

            if (shibboleth3ConfService.isLocalDocumentStoreType()) {
                
                File file = new File(filePath);
                if(!file.exists()) {
                    logger.debug("The trust relationship {} metadata used local storage but the SP metadata file `{}` was not found",
                    trustRelationship.getInum(),filePath);
                    return false;
                }
            }
            return true;
        }
        if (emptySpMetadataFileName) {
            spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
            trustRelationship.setSpMetaDataFN(spMetadataFileName);
            if (trustRelationship.getDn() == null) {
                String dn = trustService.getDnForTrustRelationShip(trustRelationship.getInum());
                trustRelationship.setDn(dn);
                trustService.addTrustRelationship(trustRelationship);
            } else {
                trustService.updateTrustRelationship(trustRelationship);
            }
        }
        String result = shibboleth3ConfService.saveSpMetadataFile(spMetadataFileName, is);
        if (StringHelper.isNotEmpty(result)) {
            metadataValidationTimer.queue(result);
        } else {
            //facesMessages.add(FacesMessage.SEVERITY_ERROR,
              //      "Failed to save SP meta-data file. Please check if you provide correct file");
        }
        return StringHelper.isNotEmpty(result);
    }
}