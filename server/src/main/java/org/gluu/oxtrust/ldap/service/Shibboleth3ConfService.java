/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.ldap.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.ProfileConfiguration;
import org.gluu.oxtrust.model.SubversionFile;
import org.gluu.oxtrust.util.EasyCASSLProtocolSocketFactory;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.opensaml.xml.schema.SchemaBuilder;
import org.opensaml.xml.schema.SchemaBuilder.SchemaLanguage;
import org.w3c.dom.Document;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.model.SchemaEntry;
import org.xdi.service.SchemaService;
import org.xdi.service.XmlService;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;
import org.xdi.util.exception.InvalidConfigurationException;
import org.xdi.util.io.FileUploadWrapper;
import org.xdi.util.io.HTTPFileDownloader;
import org.xdi.util.security.StringEncrypter;
import org.xdi.util.security.StringEncrypter.EncryptionException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.unboundid.ldap.sdk.schema.AttributeTypeDefinition;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.xdi.xml.GluuErrorHandler;
import org.xdi.xml.XMLValidator;

/**
 * Provides operations with attributes
 * 
 * @author Dmitry Ognyannikov, 2016
 */
@Scope(ScopeType.STATELESS)
@Name("shibboleth3ConfService")
@AutoCreate
public class Shibboleth3ConfService implements Serializable {

	private static final long serialVersionUID = 6752452480800274694L;
    
	private static final String SHIB3_IDP = "shibboleth-idp";
	private static final String SHIB3_SP = "sp";

	private static final String SHIB3_IDP_CONF_FOLDER = "conf";
	public static final String SHIB3_IDP_METADATA_FOLDER = "metadata";

	private static final String SHIB3_IDP_METADATA_PROVIDERS_FILE = "metadata-providers.xml";
	private static final String SHIB3_IDP_ATTRIBUTE_FILTER_FILE = "attribute-filter.xml";
	private static final String SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE = "attribute-resolver.xml";
	private static final String SHIB3_IDP_RELYING_PARTY_FILE = "relying-party.xml";
	// private static final String SHIB3_IDP_PROFILE_HADLER = "handler.xml";
        private static final String SHIB3_IDP_CAS_PROTOCOL_FILE = "cas-protocol.xml";
	public static final String SHIB3_IDP_IDP_METADATA_FILE = "idp-metadata.xml";
	public static final String SHIB3_IDP_SP_METADATA_FILE = "sp-metadata.xml";
	public static final String SHIB3_SP_ATTRIBUTE_MAP_FILE = "attribute-map.xml";
	public static final String SHIB3_SP_SHIBBOLETH2_FILE = "shibboleth2.xml";
	private static final String SHIB3_SP_READ_ME = "/WEB-INF/resources/doc/README_SP.pdf";
	private static final String SHIB3_SP_READ_ME_WINDOWS = "/WEB-INF/resources/doc/README_SP_windows.pdf";

	private static final String SHIB3_SP_METADATA_FILE_PATTERN = "%s-sp-metadata.xml";
	// private static final String SHIB3_IDP_METADATA_FILE_PATTERN = "%s-idp-metadata.xml";

	public static final String PUBLIC_CERTIFICATE_START_LINE = "-----BEGIN CERTIFICATE-----";
	public static final String PUBLIC_CERTIFICATE_END_LINE = "-----END CERTIFICATE-----";
	
	// public static final String PRIVATE_KEY_START_LINE = "-----BEGIN RSA PRIVATE KEY-----";
	// public static final String PRIVATE_KEY_END_LINE = "-----END RSA PRIVATE KEY-----";
	
	public static final String SHIB3_IDP_PROPERTIES_FILE = "idp.properties";
	private static final String SHIB3_IDP_LOGIN_CONFIG_FILE = "login.config";

	private static final String SHIB3_IDP_METADATA_CREDENTIALS_FOLDER = SHIB3_IDP_METADATA_FOLDER + File.separator + "credentials";

	private static final String SHIB3_METADATA_FILE_PATTERN = "%s-metadata.xml";

	public static final String SHIB3_IDP_TEMPMETADATA_FOLDER = "temp_metadata";

	public static final String SHIB3_IDP_SP_KEY_FILE = "spkey.key";

	public static final String SHIB3_IDP_SP_CERT_FILE = "spcert.crt";

	@In
	private AttributeService attributeService;

	@In
	private TemplateService templateService;

	@Logger
	private Log log;

	@In
	private FilterService filterService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfigurationSalt}")
	private String cryptoConfigurationSalt;

	@In
	private XmlService xmlService;

	/*
	 * Generate relying-party.xml, attribute-filter.xml, attribute-resolver.xml
	 */
	public boolean generateConfigurationFiles(List<GluuSAMLTrustRelationship> trustRelationships) {

		log.info(">>>>>>>>>> IN Shibboleth3ConfService.generateConfigurationFiles()...");

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpConfFolder      = getIdpConfDir();
		String idpMetadataFolder  = getIdpMetadataDir();

		// Prepare data for files
		initAttributes(trustRelationships);
		HashMap<String, Object> trustParams = initTrustParamMap(trustRelationships);
		HashMap<String, Object> attrParams = initAttributeParamMap(trustRelationships);
                HashMap<String, Object> casParams = initCASParamMap();

		boolean result = (trustParams != null) && (attrParams != null);
		if (!result) {
			return result;
		}

		VelocityContext context = prepareVelocityContext(trustParams, attrParams, casParams, idpMetadataFolder);

		// Generate metadata-providers.xml
		String metadataProviders = templateService.generateConfFile(SHIB3_IDP_METADATA_PROVIDERS_FILE, context);
		// Generate attribute-resolver.xml
		String attributeResolver = templateService.generateConfFile(SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE, context);
		// Generate attribute-filter.xml
		String attributeFilter = templateService.generateConfFile(SHIB3_IDP_ATTRIBUTE_FILTER_FILE, context);
		// Generate relying-party.xml
		String relyingParty = templateService.generateConfFile(SHIB3_IDP_RELYING_PARTY_FILE, context);
                // Generate cas-protocol.xml
		String casProtocol = templateService.generateConfFile(SHIB3_IDP_CAS_PROTOCOL_FILE, context);
		// Generate shibboleth2.xml
		String shibConfig = templateService.generateConfFile(SHIB3_SP_SHIBBOLETH2_FILE, context);
		// Generate handler.xml
		// String profileHandler = templateService.generateConfFile(SHIB3_IDP_PROFILE_HADLER, context);

		// Generate attribute-map.xml
		// String attributeMap =
		// templateService.generateConfFile(SHIB2_SP_ATTRIBUTE_MAP, context);

		// result = (metadataProviders != null) && (attributeFilter != null) && (attributeResolver != null) && (relyingParty != null) && (shibConfig != null)	&& (profileHandler != null);
		result = (metadataProviders != null) && (attributeFilter != null) && (attributeResolver != null) && (relyingParty != null)  && (casProtocol != null) && (shibConfig != null);

		// Write metadata-providers.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_METADATA_PROVIDERS_FILE, metadataProviders);
		// Write attribute-resolver.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE, attributeResolver);
		// Write attribute-filter.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_ATTRIBUTE_FILTER_FILE, attributeFilter);
		// Write relying-party.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_RELYING_PARTY_FILE, relyingParty);
		// Write cas-protocol.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_CAS_PROTOCOL_FILE, casProtocol);
		// Write shibboleth2.xml
		result &= templateService.writeConfFile(getSpShibboleth3FilePath(), shibConfig);
		// Write handler.xml
		// result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_PROFILE_HADLER, profileHandler);

		// Write attribute-map.xml
		// result &= templateService.writeConfFile(spConfFolder +
		// SHIB2_SP_ATTRIBUTE_MAP, attributeMap);

		log.info(">>>>>>>>>> LEAVING Shibboleth3ConfService.generateConfigurationFiles()...");

		return result;
	}

	/*
	 * Init attributes
	 */
	private void initAttributes(List<GluuSAMLTrustRelationship> trustRelationships) {

		List<GluuAttribute> attributes = attributeService.getAllPersonAttributes(GluuUserRole.ADMIN);
		HashMap<String, GluuAttribute> attributesByDNs = attributeService.getAttributeMapByDNs(attributes);

		GluuAttribute uid = attributeService.getAttributeByName(OxTrustConstants.uid);

		// Load attributes definition
		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {

			// Add first attribute uid
			List<String> oldAttributes = trustRelationship.getReleasedAttributes();
			List<String> releasedAttributes = new ArrayList<String>();

			if (oldAttributes != null) {
				releasedAttributes.addAll(oldAttributes);
			}

			if (uid != null) {
				if (releasedAttributes.remove(uid.getDn())) {
					releasedAttributes.add(0, uid.getDn());
				}
			}

			// Resolve custom attributes by DNs
			trustRelationship.setReleasedCustomAttributes(attributeService.getCustomAttributesByAttributeDNs(releasedAttributes, attributesByDNs));

			// Set attribute meta-data
			attributeService.setAttributeMetadata(trustRelationship.getReleasedCustomAttributes(), attributes);
		}
	}

	/*
	 * Prepare trustRelationships to generate files
	 */
	private HashMap<String, Object> initTrustParamMap(List<GluuSAMLTrustRelationship> trustRelationships) {

		log.trace("Starting trust parameters map initialization.");

		HashMap<String, Object> trustParams = new HashMap<String, Object>();

		// Metadata signature verification engines
		// https://wiki.shibboleth.net/confluence/display/SHIB2/IdPTrustEngine
		List<Map<String, String>> trustEngines = new ArrayList<Map<String, String>>();

		// the map of {inum,number} for easy naming of relying parties.
		Map<String, String> trustIds = new HashMap<String, String>();

		// Trust relationships that are part of some federation
		List<GluuSAMLTrustRelationship> deconstructed = new ArrayList<GluuSAMLTrustRelationship>();

		// the map of {inum,number} for easy naming of federated relying
		// parties.
		Map<String, String> deconstructedIds = new HashMap<String, String>();

		// the map of {inum, {inum, inum, inum...}} describing the federations
		// and TRs defined from them.
		Map<String, List<String>> deconstructedMap = new HashMap<String, List<String>>();

		// entityIds defined in each TR.
		Map<String, List<String>> trustEntityIds = new HashMap<String, List<String>>();

		int id = 1;
		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {

			boolean isPartOfFederation = !(trustRelationship.getSpMetaDataSourceType() == GluuMetadataSourceType.URI || trustRelationship.getSpMetaDataSourceType() == GluuMetadataSourceType.FILE);

			if (!isPartOfFederation) {

				// Set Id
				trustIds.put(trustRelationship.getInum(), String.valueOf(id++));

				// Set entityId
				String idpMetadataFolder = getIdpMetadataDir();

				File metadataFile = new File(idpMetadataFolder + trustRelationship.getSpMetaDataFN());
				List<String> entityIds = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);

				// if for some reason metadata is corrupted or missing - mark trust relationship INACTIVE
				// user will be able to fix this in UI
				if (entityIds == null) {
					trustRelationship.setStatus(GluuStatus.INACTIVE);
					TrustService.instance().updateTrustRelationship(trustRelationship);
					continue;
				}

				trustEntityIds.put(trustRelationship.getInum(), entityIds);

				try {

					filterService.parseFilters(trustRelationship);
					ProfileConfigurationService.instance().parseProfileConfigurations(trustRelationship);

				} catch (Exception e) {
					log.error("Failed to parse stored metadataFilter configuration for trustRelationship " + trustRelationship.getDn(), e);
					e.printStackTrace();
				}

				if (trustRelationship.getMetadataFilters().get("signatureValidation") != null) {

					Map<String, String> trustEngine = new HashMap<String, String>();

					trustEngine.put("id", "Trust" + StringHelper.removePunctuation(trustRelationship.getInum()));

					trustEngine.put("certPath", applicationConfiguration.getShibboleth3IdpRootDir() + File.separator
							+ SHIB3_IDP_METADATA_FOLDER + File.separator + "credentials" + File.separator
							+ trustRelationship.getMetadataFilters().get("signatureValidation").getFilterCertFileName());

					trustEngines.add(trustEngine);
				}

				// If there is an intrusive filter - push it to the end of the list.
				if (trustRelationship.getGluuSAMLMetaDataFilter() != null) {

					List<String> filtersList = new ArrayList<String>();
					String entityRoleWhiteList = null;
					for (String filterXML : trustRelationship.getGluuSAMLMetaDataFilter()) {

						Document xmlDocument;

						try {

							xmlDocument = xmlService.getXmlDocument(filterXML.getBytes());

						} catch (Exception e) {
							log.error("GluuSAMLMetaDataFilter contains invalid value.", e);
							e.printStackTrace();
							continue;
						}

						if (xmlDocument.getFirstChild().getAttributes().getNamedItem("xsi:type").getNodeValue().equals(FilterService.ENTITY_ROLE_WHITE_LIST_TYPE)) {
							entityRoleWhiteList = filterXML;
							continue;
						}

						filtersList.add(filterXML);
					}

					if (entityRoleWhiteList != null) {
						filtersList.add(entityRoleWhiteList);
					}

					trustRelationship.setGluuSAMLMetaDataFilter(filtersList);
				}

			} else {

				String federationInum = trustRelationship.getContainerFederation().getInum();

				if (deconstructedMap.get(federationInum) == null) {
					deconstructedMap.put(federationInum, new ArrayList<String>());
				}

				deconstructedMap.get(federationInum).add(trustRelationship.getEntityId());
				deconstructed.add(trustRelationship);
				deconstructedIds.put(trustRelationship.getEntityId(), String.valueOf(id++));
			}
		}

		for (String trustRelationshipInum : trustEntityIds.keySet()) {
			List<String> federatedSites = deconstructedMap.get(trustRelationshipInum);
			if (federatedSites != null) {
				trustEntityIds.get(trustRelationshipInum).removeAll(federatedSites);
			}
		}

		trustParams.put("idpCredentialsPath", applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator + "credentials" + File.separator);

		trustParams.put("deconstructed", deconstructed);
		trustParams.put("deconstructedIds", deconstructedIds);

		trustParams.put("trustEngines", trustEngines);
		trustParams.put("trusts", trustRelationships);
		trustParams.put("trustIds", trustIds);
		trustParams.put("trustEntityIds", trustEntityIds);

		return trustParams;
	}

	private HashMap<String, Object> initAttributeParamMap(List<GluuSAMLTrustRelationship> trustRelationships) {

		HashMap<String, Object> attrParams = new HashMap<String, Object>();

		// Collect attributes
		List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
		List<String> attributeNames = new ArrayList<String>();

		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {

			for (GluuCustomAttribute customAttribute : trustRelationship.getReleasedCustomAttributes()) {

				GluuAttribute metadata = customAttribute.getMetadata();

				if (!attributes.contains(metadata)) {

					attributes.add(metadata);
					String attributeName = metadata.getName();
					attributeNames.add(attributeName);
				}
			}
		}

		SchemaService shemaService = SchemaService.instance();
		SchemaEntry schemaEntry = shemaService.getSchema();
		List<AttributeTypeDefinition> attributeTypes = shemaService.getAttributeTypeDefinitions(schemaEntry, attributeNames);

		Map<String, String> attributeSAML1Strings = new HashMap<String, String>();
		Map<String, String> attributeSAML2Strings = new HashMap<String, String>();

		for (GluuAttribute metadata : attributes) {

			String attributeName = metadata.getName();

			AttributeTypeDefinition attributeTypeDefinition = shemaService.getAttributeTypeDefinition(attributeTypes, attributeName);
			if (attributeTypeDefinition == null) {
				log.error("Failed to get OID for attribute name {0}", attributeName);
				return null;
			}

			//
			// urn::dir:attribute-def:$attribute.name
			// urn:oid:$attrParams.attributeOids.get($attribute.name)
			String saml1String = metadata.getSaml1Uri();
			if (StringHelper.isEmpty(saml1String)) {

				boolean standard = metadata.isCustom() || StringHelper.isEmpty(metadata.getUrn()) || (!StringHelper.isEmpty(metadata.getUrn()) && metadata.getUrn().startsWith("urn:gluu:dir:attribute-def:"));
				saml1String = String.format("urn:%s:dir:attribute-def:%s", standard ? "gluu" : "mace", attributeName);
			}

			attributeSAML1Strings.put(attributeName, saml1String);
			String saml2String = metadata.getSaml2Uri();

			if (StringHelper.isEmpty(saml2String)) {
				saml2String = String.format("urn:oid:%s", attributeTypeDefinition.getOID());
			}

			attributeSAML2Strings.put(attributeName, saml2String);
		}

		attrParams.put("attributes", attributes);
		attrParams.put("attributeSAML1Strings", attributeSAML1Strings);
		attrParams.put("attributeSAML2Strings", attributeSAML2Strings);

		return attrParams;
	}
        
        private HashMap<String, Object> initCASParamMap() {
		HashMap<String, Object> casParams = new HashMap<String, Object>();
                try {
                    CASService casService = CASService.instance();
                    
                    ShibbolethCASProtocolConfiguration configuration = casService.loadCASConfiguration();
                    if (configuration != null) {
                        log.info("add ShibbolethCASProtocolConfiguration parameters");
                        casParams.put("enabled", configuration.isEnabled());
                        casParams.put("extended", configuration.isExtended());
                        casParams.put("enableToProxyPatterns", configuration.isEnableToProxyPatterns());
                        casParams.put("authorizedToProxyPattern", configuration.getAuthorizedToProxyPattern());
                        casParams.put("unauthorizedToProxyPattern", configuration.getAuthorizedToProxyPattern());
                    }
                } catch (Exception e) {
                    log.error("initCASParamMap() exception", e);
                }
		return casParams;
        }

	private VelocityContext prepareVelocityContext(HashMap<String, Object> trustParams, HashMap<String, Object> attrParams, HashMap<String, Object> casParams, String idpMetadataFolder) {

		VelocityContext context = new VelocityContext();

		context.put("StringHelper", StringHelper.class);

		context.put("trustParams", trustParams);
		context.put("attrParams", attrParams);
		context.put("casParams", casParams);
		context.put("medataFolder", idpMetadataFolder);
		context.put("applianceInum", StringHelper.removePunctuation(ApplianceService.instance().getApplianceInum()));
		context.put("orgInum", StringHelper.removePunctuation(OrganizationService.instance().getOrganizationInum()));
		context.put("orgSupportEmail", applicationConfiguration.getOrgSupportEmail());

		String idpUrl = applicationConfiguration.getIdpUrl();
		context.put("idpUrl", idpUrl);

		String idpHost = idpUrl.replaceAll(":[0-9]*$", "");
		context.put("idpHost", idpHost);

		String spUrl = applicationConfiguration.getApplianceUrl();
		context.put("spUrl", spUrl);
		String spHost = spUrl.replaceAll(":[0-9]*$", "").replaceAll("^.*?//", "");
		context.put("spHost", spHost);
		String gluuSPInum = ApplianceService.instance().getAppliance().getGluuSPTR();
		String gluuSPEntityId = TrustService.instance().getRelationshipByInum(gluuSPInum).getEntityId();
		context.put("gluuSPEntityId", gluuSPEntityId);
		String regx = "\\s*(=>|,|\\s)\\s*";// white spaces or comma

		String ldapUrls[] =  applicationConfiguration.getIdpLdapServer().split(regx);
		String ldapUrl = "";
		if (ldapUrls != null) {

			for (String ldapServer : ldapUrls) {
				if(ldapUrl.length()>1)
					ldapUrl = ldapUrl+" ";
				ldapUrl = ldapUrl + applicationConfiguration.getIdpLdapProtocol() + "://" + ldapServer;
			}

		} else {
			ldapUrl = applicationConfiguration.getIdpLdapProtocol() + "://" + applicationConfiguration.getIdpLdapServer();
		}
		
		context.put("ldapUrl", ldapUrl);
		context.put("bindDN", applicationConfiguration.getIdpBindDn());

		try {
			context.put("ldapPass", StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpBindPassword(), cryptoConfigurationSalt));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt bindPassword", e);
			e.printStackTrace();
		}

		context.put("securityKey", applicationConfiguration.getIdpSecurityKey());
		context.put("securityCert", applicationConfiguration.getIdpSecurityCert());

		try {
			context.put("securityKeyPassword", StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpSecurityKeyPassword(), cryptoConfigurationSalt));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt idp.securityKeyPassword", e);
			e.printStackTrace();
		}

		return context;
	}

	public String getIdpMetadataFilePath() {

		String filePath = getIdpMetadataDir() + SHIB3_IDP_IDP_METADATA_FILE;

		return filePath;

		/*
		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to find IDP metadata file due to undefined IDP root folder");
		}

		String idpConfFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_CONF_FOLDER + File.separator;

		File relyingPartyFile = new File(idpConfFolder + SHIB3_IDP_RELYING_PARTY_FILE);
		if (!relyingPartyFile.exists()) {
			log.error("Failed to find IDP metadata file name because relaying party file '{0}' doesn't exist", relyingPartyFile.getAbsolutePath());
			return null;
		}

		InputStream is = null;
		InputStreamReader isr = null;
		Document xmlDocument = null;
		try {
			is = FileUtils.openInputStream(relyingPartyFile);
			isr = new InputStreamReader(is, "UTF-8");
			try {
				xmlDocument = xmlService.getXmlDocument(new InputSource(isr));
			} catch (Exception ex) {
				log.error("Failed to parse relying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
				ex.printStackTrace();
			}
		} catch (IOException ex) {
			log.error("Failed to read relying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(is);
		}

		if (xmlDocument == null) {
			return null;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();

		String filePath = null;
		try {
			filePath = xPath.compile("/RelyingPartyGroup/MetadataProvider[@id='ShibbolethMetadata']/MetadataProvider[@id='IdPMD']/MetadataResource/@file").evaluate(xmlDocument);
		} catch (XPathExpressionException ex) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
			ex.printStackTrace();
		}

		if (filePath == null) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", relyingPartyFile.getAbsolutePath());
		}

		return filePath;
		*/
	}
        
        public String getIdpConfDir() {
                return applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_CONF_FOLDER + File.separator;
        }
        
        public String getIdpMetadataDir() {
                return applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
        }

	public String getSpMetadataFilePath(String spMetaDataFN) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to return SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		return idpMetadataFolder + spMetaDataFN;
	}


	public String getSpNewMetadataFileName(GluuSAMLTrustRelationship trustRel) {
	    return getSpNewMetadataFileName(trustRel.getInum());
	}
	
    public String getSpNewMetadataFileName(String inum) {

        String relationshipInum = StringHelper.removePunctuation(inum);
        return String.format(SHIB3_SP_METADATA_FILE_PATTERN, relationshipInum);
    }

	public String saveSpMetadataFile(String spMetadataFileName, InputStream input) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {

			IOUtils.closeQuietly(input);
			String errorMessage = "Failed to save SP meta-data file due to undefined IDP root folder";
			log.error(errorMessage);
			throw new InvalidConfigurationException(errorMessage);
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_TEMPMETADATA_FOLDER + File.separator;
		String tempFileName = getTempMetadataFilename(idpMetadataFolder, spMetadataFileName);
		File spMetadataFile = new File(idpMetadataFolder + tempFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(spMetadataFile);
			IOUtils.copy(input, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write SP meta-data file '{0}'", ex, spMetadataFile);
			ex.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(input);
		}

		return tempFileName;
	}

	private String getTempMetadataFilename(String idpMetadataFolder, String fileName) {

		synchronized (getClass()) {
			File possibleTemp = new File(fileName);
			do {
				possibleTemp = new File(idpMetadataFolder + fileName + INumGenerator.generate(2));
			} while (possibleTemp.exists());
			return possibleTemp.getName();
		}
	}

	public String saveSpMetadataFile(String uri, String spMetadataFileName) {

		if (StringHelper.isEmpty(uri)) {
			return null;
		}

		HTTPFileDownloader.setEasyhttps(new Protocol("https", new EasyCASSLProtocolSocketFactory(), 443));
		String spMetadataFileContent = HTTPFileDownloader.getResource(uri, "application/xml, text/xml", null, null);

		if (StringHelper.isEmpty(spMetadataFileContent)) {
			return null;
		}

		// Save new file
		ByteArrayInputStream is;
		try {
			byte[] spMetadataFileContentBytes = spMetadataFileContent.getBytes("UTF-8");
			is = new ByteArrayInputStream(spMetadataFileContentBytes);
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return null;
		}

		FileUploadWrapper tmpfileWrapper = new FileUploadWrapper();
		tmpfileWrapper.setStream(is);

		return saveSpMetadataFile(spMetadataFileName, tmpfileWrapper.getStream());
	}

	public String generateSpAttributeMapFile(GluuSAMLTrustRelationship trustRelationship) {

		List<GluuSAMLTrustRelationship> trustRelationships = Arrays.asList(trustRelationship);
		initAttributes(trustRelationships);
		HashMap<String, Object> attrParams = initAttributeParamMap(trustRelationships);

		if (attrParams == null) {
			return null;
		}

		VelocityContext context = prepareVelocityContext(null, attrParams, null, null);
		String spAttributeMap = templateService.generateConfFile(SHIB3_SP_ATTRIBUTE_MAP_FILE, context);

		return spAttributeMap;
	}

	public boolean generateSpMetadataFile(GluuSAMLTrustRelationship trustRelationship, String certificate) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to generate SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;

		// Generate sp-metadata.xml meta-data file
		String spMetadataFileContent = generateSpMetadataFileContent( trustRelationship,  certificate);
		if (StringHelper.isEmpty(spMetadataFileContent)) {
			return false;
		}

		return templateService.writeConfFile(idpMetadataFolder + trustRelationship.getSpMetaDataFN(), spMetadataFileContent);
	}
	
	public String generateSpMetadataFileContent(GluuSAMLTrustRelationship trustRelationship, String certificate){

		VelocityContext context = new VelocityContext();
		context.put("certificate", certificate);
		context.put("trustRelationship", trustRelationship);
		context.put("entityId", Util.encodeString(StringHelper.removePunctuation(trustRelationship.getInum())));
		context.put("spHost", trustRelationship.getUrl().replaceFirst("/$", ""));

		// Generate sp-metadata.xml meta-data file
		String spMetadataFileContent = templateService.generateConfFile(SHIB3_IDP_SP_METADATA_FILE, context);
		return spMetadataFileContent;
	}

	public void removeSpMetadataFile(String spMetadataFileName) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to remove SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		File spMetadataFile = new File(idpMetadataFolder + spMetadataFileName);

		if (spMetadataFile.exists()) {
			spMetadataFile.delete();
		}
	}

	public boolean isCorrectSpMetadataFile(String spMetadataFileName) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to check SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER	+ File.separator;
		File metadataFile = new File(idpMetadataFolder + spMetadataFileName);
		List<String> entityId = SAMLMetadataParser.getSpEntityIdFromMetadataFile(metadataFile);

		return (entityId != null) && !entityId.isEmpty();
	}

	public String getSpAttributeMapFilePath() {

		String spConfFolder = applicationConfiguration.getShibboleth3SpConfDir() + File.separator;
		return spConfFolder + SHIB3_SP_ATTRIBUTE_MAP_FILE;
	}

	public String getSpShibboleth3FilePath() {

		String spConfFolder = applicationConfiguration.getShibboleth3SpConfDir() + File.separator;
		return spConfFolder + SHIB3_SP_SHIBBOLETH2_FILE;
	}

	/**
	 * Get shibboleth3ConfService instance
	 * 
	 * @return Shibboleth3ConfService instance
	 */
	public static Shibboleth3ConfService instance() {
		return (Shibboleth3ConfService) Component.getInstance(Shibboleth3ConfService.class);
	}

	public String getSpReadMeResourceName() {
		return SHIB3_SP_READ_ME;
	}

	public String getSpReadMeWindowsResourceName() {
		return SHIB3_SP_READ_ME_WINDOWS;
	}

	public String getPublicCertificate(FileUploadWrapper fileWrapper) {

		if (fileWrapper.getStream() == null) {
			return null;
		}

		List<String> lines = null;
		try {
			lines = IOUtils.readLines(new InputStreamReader(fileWrapper.getStream(), "US-ASCII"));
		} catch (IOException ex) {
			log.error("Failed to read public key file '{0}'", ex, fileWrapper.getFileName());
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileWrapper.getStream());
		}

		StringBuilder sb = new StringBuilder();

		boolean keyPart = false;
		for (String line : lines) {
			if (line.startsWith(PUBLIC_CERTIFICATE_END_LINE)) {
				break;
			}
			if (keyPart) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(line);
			}
			if (line.startsWith(PUBLIC_CERTIFICATE_START_LINE)) {
				keyPart = true;
			}
		}

		if (sb.length() == 0) {
			return null;
		}

		return sb.toString();
	}

	public List<SubversionFile> getConfigurationFilesForSubversion(List<GluuSAMLTrustRelationship> trustRelationships) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to create SubversionFile file due to undefined IDP root folder");
		}

		String idpConfFolder = getIdpConfDir();
		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		String idpMetadataCredentialsFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_CREDENTIALS_FOLDER	+ File.separator;
		String spConfFolder = applicationConfiguration.getShibboleth3SpConfDir() + File.separator;

		List<SubversionFile> subversionFiles = new ArrayList<SubversionFile>();
		subversionFiles.add(new SubversionFile(SHIB3_IDP, idpConfFolder + SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE));
		subversionFiles.add(new SubversionFile(SHIB3_IDP, idpConfFolder + SHIB3_IDP_ATTRIBUTE_FILTER_FILE));
		subversionFiles.add(new SubversionFile(SHIB3_IDP, idpConfFolder + SHIB3_IDP_RELYING_PARTY_FILE));
		subversionFiles.add(new SubversionFile(SHIB3_SP, spConfFolder + SHIB3_SP_ATTRIBUTE_MAP_FILE));
		subversionFiles.add(new SubversionFile(SHIB3_SP, spConfFolder + SHIB3_SP_SHIBBOLETH2_FILE));

		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			if (trustRelationship.getContainerFederation() == null) {
				subversionFiles.add(new SubversionFile(SHIB3_IDP + File.separator + SHIB3_IDP_METADATA_FOLDER, idpMetadataFolder
						+ trustRelationship.getSpMetaDataFN()));
			}
			if (trustRelationship.getMetadataFilters().containsKey("signatureValidation")) {
				subversionFiles.add(new SubversionFile(SHIB3_IDP + File.separator + SHIB3_IDP_METADATA_CREDENTIALS_FOLDER,
						idpMetadataCredentialsFolder + StringHelper.removePunctuation(trustRelationship.getInum())));
			}
		}

		return subversionFiles;
	}

	public SubversionFile getConfigurationFileForSubversion(GluuSAMLTrustRelationship trustRelationship) {

		if (trustRelationship.getSpMetaDataFN() == null) {
			return null;
		}

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to create SubversionFile file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;

		return new SubversionFile(SHIB3_IDP + File.separator + SHIB3_IDP_METADATA_FOLDER, idpMetadataFolder	+ trustRelationship.getSpMetaDataFN());
	}

	public boolean isFederationMetadata(String spMetaDataFN) {

		if (spMetaDataFN == null) {
			return false;
		}

		File spMetaDataFile = new File(getSpMetadataFilePath(spMetaDataFN));
		InputStream is = null;
		InputStreamReader isr = null;
		Document xmlDocument = null;

		try {
			is = FileUtils.openInputStream(spMetaDataFile);
			isr = new InputStreamReader(is, "UTF-8");
			try {
				xmlDocument = xmlService.getXmlDocument(new InputSource(isr));
			} catch (Exception ex) {
				log.error("Failed to parse metadata file '{0}'", ex, spMetaDataFile.getAbsolutePath());
				ex.printStackTrace();
			}
		} catch (IOException ex) {
			log.error("Failed to read metadata file '{0}'", ex, spMetaDataFile.getAbsolutePath());
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(is);
		}

		if (xmlDocument == null) {
			return false;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();

		String federationTag = null;
		try {
			federationTag = xPath.compile("count(/*[local-name() = 'EntitiesDescriptor'])").evaluate(xmlDocument);
		} catch (XPathExpressionException ex) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", ex, spMetaDataFile.getAbsolutePath());
			ex.printStackTrace();
		}

		return Integer.parseInt(federationTag) > 0;
	}

	public String saveFilterCert(String filterCertFileName, InputStream input) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			IOUtils.closeQuietly(input);
			throw new InvalidConfigurationException("Failed to save filter certificate file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator
				+ "credentials" + File.separator;
		File filterCertFile = new File(idpMetadataFolder + filterCertFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(filterCertFile);
			IOUtils.copy(input, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write  filter certificate file '{0}'", ex, filterCertFile);
			ex.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(input);
		}

		return filterCertFile.getAbsolutePath();
	}

	public boolean generateIdpConfigurationFiles() {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpConfFolder = getIdpConfDir();

		// Prepare data for files
		VelocityContext context = new VelocityContext();
		String regx = "\\s*(=>|,|\\s)\\s*";// white spaces or comma		
		String ldapUrls[] =  applicationConfiguration.getIdpLdapServer().split(regx);
		String ldapUrl = "";

		if(ldapUrls != null) {

			for (String ldapServer : ldapUrls) {
				if(ldapUrl.length()>1) {
					ldapUrl = ldapUrl + " ";
				}
				ldapUrl = ldapUrl + applicationConfiguration.getIdpLdapProtocol() + "://" + ldapServer;
			}

		} else {
			ldapUrl = applicationConfiguration.getIdpLdapProtocol() + "://" + applicationConfiguration.getIdpLdapServer();
		}

		String host = ldapUrl;
		String base = applicationConfiguration.getBaseDN();
		String serviceUser = applicationConfiguration.getIdpBindDn();
		String serviceCredential = "";
		try {
			serviceCredential = StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpBindPassword(), cryptoConfigurationSalt);
		} catch (EncryptionException e) {
			log.error("Failed to decrypt bindPassword", e);
			e.printStackTrace();
		}
		String userField = applicationConfiguration.getIdpUserFields();
		context.put("host", host);
		context.put("base", base);
		context.put("serviceUser", serviceUser);
		context.put("serviceCredential", serviceCredential);
		context.put("userField", userField);

		// Generate login.config
		String loginConfig = templateService.generateConfFile(SHIB3_IDP_LOGIN_CONFIG_FILE, context);

		boolean result = (loginConfig != null);

		// Write login.config
		result &= templateService.writeConfFile(idpConfFolder + SHIB3_IDP_LOGIN_CONFIG_FILE, loginConfig);

		return result;
	}

	public void removeUnusedMetadata() {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		File metadataDir = new File(applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER);

		if (metadataDir.exists()) {

			ArrayList<SubversionFile> obsoleteMetadata = new ArrayList<SubversionFile>();

			for (File metadata : metadataDir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			})) {

				if (metadata.getName().equals(SHIB3_IDP_IDP_METADATA_FILE) || trustRelationExists(metadata.getName())) {
					continue;
				}

				obsoleteMetadata.add(new SubversionFile(SHIB3_IDP + File.separator + SHIB3_IDP_METADATA_FOLDER, metadata.getAbsolutePath()));
			}

			// SubversionService.instance().commitShibboleth3ConfigurationFiles(OrganizationService.instance().getOrganization(), new ArrayList<SubversionFile>(), obsoleteMetadata, "Removed Metadata files that are no longer used");

			for (SubversionFile file : obsoleteMetadata) {
				new File(file.getLocalFile()).delete();
			}
		}
	}

	private boolean trustRelationExists(String metadataName) {

		if (metadataName.equals(StringHelper.removePunctuation(applicationConfiguration.getOrgInum()) + "-idp-metadata.xml")) {
			return true;
		}

		for (GluuSAMLTrustRelationship trust : TrustService.instance().getAllTrustRelationships()) {
			if (metadataName.equals(trust.getSpMetaDataFN())) {
				return true;
			}
		}

		return false;
	}

	public void removeUnusedCredentials() {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		File credentialsDir = new File(applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_CREDENTIALS_FOLDER);

		if (credentialsDir.exists()) {

			ArrayList<SubversionFile> obsoleteMetadata = new ArrayList<SubversionFile>();

			for (File credential : credentialsDir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			})) {

				if (filterExists(credential.getName()) || profileCofigurationExists(credential.getName())) {
					continue;
				}

				obsoleteMetadata.add(new SubversionFile(SHIB3_IDP + File.separator + SHIB3_IDP_METADATA_CREDENTIALS_FOLDER, credential.getAbsolutePath()));
			}

			// SubversionService.instance().commitShibboleth3ConfigurationFiles(OrganizationService.instance().getOrganization(), new ArrayList<SubversionFile>(), obsoleteMetadata, "Removed Credentials files that are no longer used");

			for (SubversionFile file : obsoleteMetadata) {
				new File(file.getLocalFile()).delete();
			}
		}
	}

	private boolean profileCofigurationExists(String credentialName) {

		for (GluuSAMLTrustRelationship trust : TrustService.instance().getAllTrustRelationships()) {

			if (credentialName.contains(StringHelper.removePunctuation(trust.getInum())) && !credentialName.equals(StringHelper.removePunctuation(trust.getInum()))) {

				try {
					ProfileConfigurationService.instance().parseProfileConfigurations(trust);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}

				ProfileConfiguration profileConfiguration = trust.getProfileConfigurations().get(credentialName.replace(StringHelper.removePunctuation(trust.getInum()), ""));

				if (profileConfiguration != null && credentialName.equals(profileConfiguration.getProfileConfigurationCertFileName())) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean filterExists(String credentialName) {

		for (GluuSAMLTrustRelationship trust : TrustService.instance().getAllTrustRelationships()) {
			if (credentialName.equals(StringHelper.removePunctuation(trust.getInum()))) {
				try {
					FilterService.instance().parseFilters(trust);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				if (trust.getMetadataFilters().get("signatureValidation") != null) {
					return true;
				}
			}
		}
		return false;
	}

	public String saveProfileConfigurationCert(String profileConfigurationCertFileName, InputStream stream) {

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			IOUtils.closeQuietly(stream);
			throw new InvalidConfigurationException("Failed to save Profile Configuration file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator + "credentials" + File.separator;
		File filterCertFile = new File(idpMetadataFolder + profileConfigurationCertFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(filterCertFile);
			IOUtils.copy(stream, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write  Profile Configuration  certificate file '{0}'", ex, filterCertFile);
			ex.printStackTrace();
			return null;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(stream);
		}

		return filterCertFile.getAbsolutePath();

	}

	public boolean isCorrectMetadataFile(String spMetaDataFN) {

		if (applicationConfiguration.getShibboleth3FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to check meta-data file due to undefined federation root folder");
		}

		String metadataFolder = applicationConfiguration.getShibboleth3FederationRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER	+ File.separator;
		File metadataFile = new File(metadataFolder + spMetaDataFN);
		List<String> entityId = SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile);
		return (entityId != null) && !entityId.isEmpty();
	}

	public void removeMetadataFile(String spMetaDataFN) {

		if (applicationConfiguration.getShibboleth3FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to remove meta-data file due to undefined federation root folder");
		}

		String metadataFolder = applicationConfiguration.getShibboleth3FederationRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		File spMetadataFile = new File(metadataFolder + spMetaDataFN);

		if (spMetadataFile.exists()) {
			spMetadataFile.delete();
		}
	}

	public String getMetadataFilePath(String metadataFileName) {

		if (applicationConfiguration.getShibboleth3FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to return meta-data file due to undefined federation root folder");
		}

		String metadataFolderName = applicationConfiguration.getShibboleth3FederationRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		File metadataFolder = new File(metadataFolderName);
		if (!metadataFolder.exists()) {
			metadataFolder.mkdirs();
		}

		return metadataFolderName + metadataFileName;
	}

	public String getNewMetadataFileName(GluuSAMLFederationProposal federationProposal, List<GluuSAMLFederationProposal> allFederationProposals) {

		String relationshipInum = StringHelper.removePunctuation(federationProposal.getInum());
		return String.format(SHIB3_METADATA_FILE_PATTERN, relationshipInum);
	}

	public boolean saveMetadataFile(String metadataFileName, InputStream stream) {

		if (applicationConfiguration.getShibboleth3FederationRootDir() == null) {
			IOUtils.closeQuietly(stream);
			throw new InvalidConfigurationException("Failed to save meta-data file due to undefined federation root folder");
		}

		String idpMetadataFolderName = applicationConfiguration.getShibboleth3FederationRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
		File idpMetadataFolder = new File(idpMetadataFolderName);
		if (!idpMetadataFolder.exists()) {
			idpMetadataFolder.mkdirs();
		}
		File spMetadataFile = new File(idpMetadataFolderName + metadataFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(spMetadataFile);
			IOUtils.copy(stream, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write meta-data file '{0}'", ex, spMetadataFile);
			ex.printStackTrace();
			return false;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(stream);
		}

		return true;
	}

	public boolean saveMetadataFile(String spMetaDataURL, String metadataFileName) {

		if (StringHelper.isEmpty(spMetaDataURL)) {
			return false;
		}

		String metadataFileContent = HTTPFileDownloader.getResource(spMetaDataURL, "application/xml, text/xml", null, null);

		if (StringHelper.isEmpty(metadataFileContent)) {
			return false;
		}

		// Save new file
		ByteArrayInputStream is;
		try {
			byte[] metadataFileContentBytes = metadataFileContent.getBytes("UTF-8");
			is = new ByteArrayInputStream(metadataFileContentBytes);
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return false;
		}

		FileUploadWrapper tmpfileWrapper = new FileUploadWrapper();
		tmpfileWrapper.setStream(is);

		return saveMetadataFile(metadataFileName, tmpfileWrapper.getStream());
	}

	/**
	 * Generate metadata files needed for appliance operations: gluuSP metadata
	 * and idp metadata.
	 */
	public boolean generateMetadataFiles(GluuSAMLTrustRelationship gluuSP) {

		log.info(">>>>>>>>>> IN Shibboleth3ConfService.generateMetadataFiles()...");

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;

		// Prepare data for files
		VelocityContext context = new VelocityContext();
		String idpHost = applicationConfiguration.getIdpUrl();

		context.put("idpHost", idpHost);
		String domain = idpHost.replaceAll(":[0-9]*$", "").replaceAll("^.*?//", "");
		context.put("domain", domain);

		context.put("orgName", applicationConfiguration.getOrganizationName());
		context.put("orgShortName", applicationConfiguration.getOrganizationName());

		try {

			String idpSigningCertificate = FileUtils.readFileToString(new File(applicationConfiguration.getIdp3SigningCert())).replaceAll("-{5}.*?-{5}", "");
			context.put("idpSigningCertificate", idpSigningCertificate);

		} catch (IOException e) {
			log.error("Unable to get IDP 3 signing certificate from " + applicationConfiguration.getIdp3SigningCert(), e);
			e.printStackTrace();
			return false;
		}

		try {

			String idpEncryptionCertificate = FileUtils.readFileToString(new File(applicationConfiguration.getIdp3EncryptionCert())).replaceAll("-{5}.*?-{5}", "");
			context.put("idpEncryptionCertificate", idpEncryptionCertificate);

		} catch (IOException e) {
			log.error("Unable to get IDP 3 encryption certificate from " + applicationConfiguration.getIdp3EncryptionCert(), e);
			e.printStackTrace();
			return false;
		}

		try {

			String spCertificate = FileUtils.readFileToString(new File(applicationConfiguration.getGluuSpCert())).replaceAll("-{5}.*?-{5}", "");

			if (gluuSP.getUrl() == null || "".equals(gluuSP.getUrl())) {
				gluuSP.setUrl(applicationConfiguration.getApplianceUrl());
			}

			generateSpMetadataFile(gluuSP, spCertificate);

		} catch (IOException e) {
			log.error("Unable to get SP certificate from " + applicationConfiguration.getGluuSpCert(), e);
			e.printStackTrace();
			return false;
		}

		// Generate idp-metadata.xml
		String idpMetadata = templateService.generateConfFile(SHIB3_IDP_IDP_METADATA_FILE, context);

		boolean result = (idpMetadata != null);
		// String idpMetadataName = String.format(SHIB3_IDP_METADATA_FILE_PATTERN, StringHelper.removePunctuation(OrganizationService.instance().getOrganizationInum()));

		// Write idp-metadata.xml
		result &= templateService.writeConfFile(idpMetadataFolder + SHIB3_IDP_IDP_METADATA_FILE, idpMetadata);

		log.info(">>>>>>>>>> LEAVING Shibboleth3ConfService.generateMetadataFiles()...");

		return result;
	}

	/**
	 * @param stream
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
         * @return GluuErrorHandler
	 */
	public static GluuErrorHandler validateMetadata(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
                Schema schema;
                try {
                    String idpTemplatesLocation = OxTrustConfiguration.instance().getIDPTemplatesLocation();
                    // String schemaDir = OxTrustConfiguration.DIR + "shibboleth3" + File.separator + "idp" + File.separator + "schema" + File.separator;
                    String schemaDir = idpTemplatesLocation + "shibboleth3" + File.separator + "idp" + File.separator + "schema" + File.separator;
                    schema = SchemaBuilder.buildSchema(SchemaLanguage.XML, schemaDir);
                } catch (Exception e) {
                    // Schema build error 
                    final List<String> validationLog = new ArrayList<String>();
                    validationLog.add(GluuErrorHandler.SCHEMA_CREATING_ERROR_MESSAGE);
                    validationLog.add(e.getMessage());
                    // return internal error
                    return new GluuErrorHandler(false, true, validationLog);
                }
		return XMLValidator.validateMetadata(stream, schema);
	}

	public  boolean existsResourceUri(String URLName) {

		try {

			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			//        HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con =	(HttpURLConnection) new URL(URLName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isIdpInstalled() {

		if (applicationConfiguration.getShibbolethVersion() != null && !applicationConfiguration.getShibbolethVersion().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
