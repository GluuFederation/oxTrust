/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.api.saml;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.gluu.oxtrust.action.TrustContactsAction;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.MetadataValidationTimer;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.SvnSyncTimer;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.uma.annotations.UmaSecure;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuStatus;
import org.xdi.model.TrustContact;
import org.xdi.util.StringHelper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;

/**
 * WS endpoint for TrustRelationship actions.
 * 
 * @author Dmitry Ognyannikov
 */
@Path("/api/saml/tr")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
@Api(value = "/api/saml/tr", description = "SAML UI API Endpoint", authorizations = {@Authorization(value = "Authorization", type = "uma")})
@UmaSecure(scope = "'api_saml', '/auth/oxtrust.allow-saml-config-all', '/auth/oxtrust.allow-saml-modify-all'")
public class TrustRelationshipWebService {
    
    @Inject
    private Logger logger;
    
    @Inject
    private TrustService trustService;

    @Inject
    private Identity identity;
	
    @Inject
    private ClientService clientService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private SvnSyncTimer svnSyncTimer;
	
    @Inject
    private MetadataValidationTimer metadataValidationTimer;

    @Inject
    private TrustContactsAction trustContactsAction;

    @Inject
    private Shibboleth3ConfService shibboleth3ConfService;
    
    ObjectMapper objectMapper;
    
    public TrustRelationshipWebService() {
        // configure Jackson ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }
    
    @GET
    @Path("/read/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "read TrustRelationship", notes = "Returns a GluuSAMLTrustRelationship by inum", response = GluuSAMLTrustRelationship.class)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = GluuSAMLTrustRelationship.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String read(@PathParam("inum") @NotNull String inum, @Context HttpServletResponse response) {
        logger.trace("Read Trust Relationship");
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            // convert to JSON
            return objectMapper.writeValueAsString(trustRelationship);
        } catch (Exception e) {
            logger.error("read() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        }
    }
    
    @POST
    @Path("/create")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "create TrustRelationship", notes = "Create new GluuSAMLTrustRelationship. Returns inum.", response = String.class)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = String.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String create(GluuSAMLTrustRelationship trustRelationship, @Context HttpServletResponse response) {
        logger.trace("Create Trust Relationship");
        try {
            String inum = trustService.generateInumForNewTrustRelationship();
            trustRelationship.setInum(inum);
            String dn = trustService.getDnForTrustRelationShip(inum);
            // Save trustRelationship
            trustRelationship.setDn(dn);
            saveTR(trustRelationship, false);
            return inum;
        } catch (Exception e) {
            logger.error("create() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        }
    }
    
    @PUT
    @Path("/update/{inum}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "update TrustRelationship", notes = "Update GluuSAMLTrustRelationship.")
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void update(@PathParam("inum") @NotNull String inum, GluuSAMLTrustRelationship trustRelationship, @Context HttpServletResponse response) {
        logger.trace("Update Trust Relationship");
        try {
            String dn = trustService.getDnForTrustRelationShip(inum);
            trustRelationship.setDn(dn);
            trustService.updateTrustRelationship(trustRelationship);
        } catch (Exception e) {
            logger.error("update() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @DELETE
    @Path("/delete/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "delete TrustRelationship", notes = "Delete GluuSAMLTrustRelationship.")
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void delete(@PathParam("inum") @NotNull String inum, @Context HttpServletResponse response) {
        logger.trace("Delete Trust Relationship");
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            trustService.removeTrustRelationship(trustRelationship);
        } catch (Exception e) {
            logger.error("delete() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "list TrustRelationships", notes = "List all GluuSAMLTrustRelationship.", response = SAMLTrustRelationshipShort.class)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = GluuSAMLTrustRelationship.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String list(@Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllTrustRelationships());
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("list() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return null;
        } 
    }
    
    @GET
    @Path("/list_all_federations")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String listAllFederations(@Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllFederations());
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("listAllFederations() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list_all_active_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String listAllActiveTrustRelationships(@Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllActiveTrustRelationships());
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("listAllActiveTrustRelationships() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list_all_other_federations/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String listAllOtherFederations(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllOtherFederations(inum));
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("listAllOtherFederations() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list_all_saml_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String listAllSAMLTrustRelationships(@QueryParam("size_limit") int sizeLimit, @Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getAllSAMLTrustRelationships(sizeLimit));
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("listAllSAMLTrustRelationships() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/list_deconstructed_trust_relationships/{inum}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String listDeconstructedTrustRelationships(@PathParam("inum") String inum, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(inum);
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.getDeconstructedTrustRelationships(trustRelationship));
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("listAllActiveTrustRelationships() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/search_trust_relationships")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK", response = SAMLTrustRelationshipShort.class),
		@ApiResponse(code = 500, message = "Server error") })
    public String searchTrustRelationships(@QueryParam("pattern") @NotNull String pattern, @QueryParam("size_limit") int sizeLimit, @Context HttpServletResponse response) {
        try {
            List<SAMLTrustRelationshipShort> trustRelationships = convertTRtoTRShort(trustService.searchSAMLTrustRelationships(pattern, sizeLimit));
            //convert to JSON
            return objectMapper.writeValueAsString(trustRelationships);
        } catch (Exception e) {
            logger.error("searchTrustRelationships() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @POST
    @Path("/set_metadata/{inum}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void setMetadata(@PathParam("inum") String trustRelationshipInum, @NotNull String metadata, @Context HttpServletResponse response) {
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
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @POST
    @Path("/set_metadata_url/{inum}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void setMetadataURL(@PathParam("inum") String trustRelationshipInum, @NotNull String url, @Context HttpServletResponse response) {
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
        } catch (Exception e) {
            logger.error("addMetadata() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @POST
    @Path("/add_attribute/{inum}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void addAttribute(@PathParam("inum") String trustRelationshipInum, @NotNull String attribute, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            List<String> attributes = trustRelationship.getReleasedAttributes();
            for (String lAttr : attributes) {
                if (attribute.equals(lAttr)) 
                    return; // Nothing to add
            }
            
            attributes.add(attribute);
            trustService.updateReleasedAttributes(trustRelationship);
        } catch (Exception e) {
            logger.error("addAttribute() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @GET
    @Path("/generate_inum_for_new_trust_relationship")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public String generateInumForNewTrustRelationship(@Context HttpServletResponse response) {
        try {
            return trustService.generateInumForNewTrustRelationship();
        } catch (Exception e) {
            logger.error("generateInumForNewTrustRelationship() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @GET
    @Path("/get_contacts/{inum}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getContacts(@PathParam("inum") String trustRelationshipInum, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            List<TrustContact> list = trustService.getContacts(trustRelationship);
            //convert to JSON
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            logger.error("getContacts() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            return OxTrustConstants.RESULT_FAILURE;
        }
    }
    
    @POST
    @Path("/set_contacts/{inum}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "set contacts for TrustRelationship", notes = "Find TrustRelationship by inum and set contacts. Contacts parameter is List<TrustContact>")
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void setContacts(@PathParam("inum") String trustRelationshipInum, String contacts, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            List<TrustContact> contactsList = objectMapper.readValue(contacts, new TypeReference<List<TrustContact>>() {});

            trustService.saveContacts(trustRelationship, contactsList);
        } catch (Exception e) {
            logger.error("setContacts() Exception", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
    
    @POST
    @Path("/set_certificate/{inum}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)    
    @ApiOperation(value = "set certificate for TrustRelationship", notes = "Find TrustRelationship by inum and set certificate.")
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void setCertificate(@PathParam("inum") String trustRelationshipInum, String certificate, @Context HttpServletResponse response) {
        try {
            GluuSAMLTrustRelationship trustRelationship = trustService.getRelationshipByInum(trustRelationshipInum);
            
            if (StringHelper.isEmpty(certificate)) {
                logger.error("Failed to update TR certificate - certificate is empty");
                return;
            }
            
            updateTRCertificate(trustRelationship, certificate);
        } catch (Exception e) {
            logger.error("Failed to update certificate", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }
    }
        
    @DELETE
    @Path("/remove_attribute")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    public void removeAttribute(GluuAttribute attribute, @Context HttpServletResponse response) {
        try {
            trustService.removeAttribute(attribute);
        } catch (Exception e) {
            logger.error("Failed to remove attribute", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
        }        
    }
    
    @PUT
    @Path("/generate_configuration_files")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "generate configuration files", notes = "Generate configuration files for Shibboleth IDP")
    @ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 500, message = "Server error") })
    public void generateConfigurationFiles(@Context HttpServletResponse response) {
        try {
            List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
            if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
                logger.error("Failed to update Shibboleth v3 configuration by web API request");
                try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
            } else {
                logger.info("Shibboleth v3 configuration updated successfully by web API request");
            }
        } catch (Exception e) {
            logger.error("Failed to generateConfigurationFiles", e);
            try { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR"); } catch (Exception ex) {}
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
    private String saveTR(GluuSAMLTrustRelationship trustRelationship, String metadata, String certificate) {
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
            case GENERATE:
                try {
                    if (StringHelper.isEmpty(certificate))
                        certificate = generateCertForGeneratedSP(trustRelationship);
                    GluuStatus status = StringHelper.isNotEmpty(certificate) ? GluuStatus.ACTIVE : GluuStatus.INACTIVE;
                    trustRelationship.setStatus(status);
                    if (generateSpMetaDataFile(trustRelationship, certificate)) {
                        setEntityId(trustRelationship);
                    } else {
                        logger.error("Failed to generate SP meta-data file");
                        return OxTrustConstants.RESULT_FAILURE;
                    }
                } catch (IOException ex) {
                    logger.error("Failed to download SP certificate", ex);

                    return OxTrustConstants.RESULT_FAILURE;
                }

                break;
            case FILE:
                try {
                    if (saveSpMetaDataFileSourceTypeFile(trustRelationship, inum, metadata)) {
                        //update = true;
                        updateTRCertificate(trustRelationship, certificate);
//					setEntityId();
                        if(!update){
                            trustRelationship.setStatus(GluuStatus.ACTIVE);
                        }
                    } else {
                        logger.error("Failed to save SP metadata file {}", metadata);
                        return OxTrustConstants.RESULT_FAILURE;
                    }
                } catch (IOException ex) {
                    logger.error("Failed to download SP metadata", ex);
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to download SP metadata");

                    return OxTrustConstants.RESULT_FAILURE;
                }

                break;
            case URI:
                try {
                    //if (saveSpMetaDataFileSourceTypeURI()) {
//						setEntityId();
                    boolean result = shibboleth3ConfService.existsResourceUri(trustRelationship.getSpMetaDataURL());
                    if(result){
                        saveSpMetaDataFileSourceTypeURI(trustRelationship);
                    }else{
                        logger.info("There is no resource found Uri : {}", trustRelationship.getSpMetaDataURL());
                    }
                    if(!update){
                        trustRelationship.setStatus(GluuStatus.ACTIVE);
                    }
                    /*} else {
                            log.error("Failed to save SP meta-data file {}", fileWrapper);
                            return OxTrustConstants.RESULT_FAILURE;
                    }*/
                } catch (Exception e) {
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "Unable to download metadata");
                    return "unable_download_metadata";
                }
                break;
            case FEDERATION:
                if(!update){
                    trustRelationship.setStatus(GluuStatus.ACTIVE);
                }
                if (trustRelationship.getEntityId() == null) {
                    //facesMessages.add(FacesMessage.SEVERITY_ERROR, "EntityID must be set to a value");
                    return "invalid_entity_id";
                }

                break;
            default:

                break;
            }

            trustService.updateReleasedAttributes(trustRelationship);

            // We call it from TR validation timer
            if (trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.GENERATE)
                            || (trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.FEDERATION))) {
                boolean federation = shibboleth3ConfService.isFederation(trustRelationship);
                trustRelationship.setFederation(federation);
            }

            trustContactsAction.saveContacts();

            if (update) {
                try {
                    saveTR(trustRelationship, update);
                } catch (BasePersistenceException ex) {
                    logger.error("Failed to update trust relationship {}", inum, ex);
                    return OxTrustConstants.RESULT_FAILURE;
                }
            } else {
                String dn = trustService.getDnForTrustRelationShip(inum);
                // Save trustRelationship
                trustRelationship.setDn(dn);
                try {
                        saveTR(trustRelationship, update);
                } catch (BasePersistenceException ex) {
                        logger.error("Failed to add new trust relationship {}", trustRelationship.getInum(), ex);
                        return OxTrustConstants.RESULT_FAILURE;
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
                    return "Shibboleth v3 configuration updated successfully";
                }
            }
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }
        
        
    private void saveTR(GluuSAMLTrustRelationship trustRelationship, boolean isUpdate) {
        logger.trace("Saving Trust Relationship");
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

            svnSyncTimer.updateTrustRelationship(trustRelationship, identity.getCredentials().getUsername());
        } else {
            trustService.addTrustRelationship(trustRelationship);
            svnSyncTimer.addTrustRelationship(trustRelationship, identity.getCredentials().getUsername());
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

        List<String> entityIdList = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);
        Set<String> entityIdSet = new TreeSet<String>();

        if(entityIdList != null && ! entityIdList.isEmpty()){
            Set<String> duplicatesSet = new TreeSet<String>(); 
            for (String entityId : entityIdList) {
                if (!entityIdSet.add(entityId)) {
                    duplicatesSet.add(entityId);
                }
            }
        }

        trustRelationship.setGluuEntityId(entityIdSet);
    }
    
    public boolean saveSpMetaDataFileSourceTypeURI(GluuSAMLTrustRelationship  trustRelationship) throws IOException {
        String spMetadataFileName = trustRelationship.getSpMetaDataFN();
        boolean emptySpMetadataFileName = StringHelper.isEmpty(spMetadataFileName);

        if (emptySpMetadataFileName) {
                // Generate new file name
                spMetadataFileName = shibboleth3ConfService.getSpNewMetadataFileName(trustRelationship);
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
        logger.trace("Saving metadata file source type: File");
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
}
