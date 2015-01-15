/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLFederationProposal;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.ProfileConfiguration;
import org.gluu.oxtrust.model.SubversionFile;
import org.gluu.oxtrust.util.EasyCASSLProtocolSocketFactory;
import org.gluu.oxtrust.util.EntityIDHandler;
import org.gluu.oxtrust.util.GluuErrorHandler;
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
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuUserRole;
import org.xdi.model.SchemaEntry;
import org.xdi.service.SchemaService;
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

/**
 * Provides operations with attributes
 * 
 * @author Yuriy Movchan Date: 11.15.2010
 */
@Scope(ScopeType.STATELESS)
@Name("shibboleth2ConfService")
@AutoCreate
public class Shibboleth2ConfService implements Serializable {

	private static final long serialVersionUID = -7645397652175481054L;

	private static final String SHIB2_IDP = "idp";
	private static final String SHIB2_SP = "sp";

	private static final String SHIB2_IDP_CONF_FOLDER = "conf";
	public static final String SHIB2_IDP_METADATA_FOLDER = "metadata";

	private static final String SHIB2_IDP_ATTRIBUTE_FILTER_FILE = "attribute-filter.xml";
	private static final String SHIB2_IDP_ATTRIBUTE_RESOLVER_FILE = "attribute-resolver.xml";
	private static final String SHIB2_IDP_RELYING_PARTY = "relying-party.xml";
	private static final String SHIB2_IDP_PROFILE_HADLER = "handler.xml";
	public static final String SHIB2_IDP_IDP_METADATA_FILE = "idp-metadata.xml";
	public static final String SHIB2_IDP_SP_METADATA_FILE = "sp-metadata.xml";
	public static final String SHIB2_SP_ATTRIBUTE_MAP = "attribute-map.xml";
	public static final String SHIB2_SP_SHIBBOLETH2 = "shibboleth2.xml";
	private static final String SHIB2_SP_READ_ME = "WEB-INF/resources/doc/README_SP.pdf";
	private static final String SHIB2_SP_READ_ME_WINDOWS = "WEB-INF/resources/doc/README_SP_windows.pdf";

	private static final String SHIB2_SP_METADATA_FILE_PATTERN = "%s-sp-metadata.xml";
	private static final String SHIB2_IDP_METADATA_FILE_PATTERN = "%s-idp-metadata.xml";

	public static final String PUBLIC_CERTIFICATE_START_LINE = "-----BEGIN CERTIFICATE-----";
	public static final String PUBLIC_CERTIFICATE_END_LINE = "-----END CERTIFICATE-----";
	
	public static final String PRIVATE_KEY_START_LINE = "-----BEGIN RSA PRIVATE KEY-----";
	public static final String PRIVATE_KEY_END_LINE = "-----END RSA PRIVATE KEY-----";
	
	
	private static final String SHIB2_IDP_LOGIN_CONFIG_FILE = "login.config";

	private static final String SHIB2_IDP_METADATA_CREDENTIALS_FOLDER = SHIB2_IDP_METADATA_FOLDER + File.separator + "credentials";

	private static final String SHIB2_METADATA_FILE_PATTERN = "%s-metadata.xml";

	public static final String SHIB2_IDP_TEMPMETADATA_FOLDER = "temp_metadata";

	public static final String SHIB2_IDP_SP_KEY_FILE = "spkey.key";

	public static final String SHIB2_IDP_SP_CERT_FILE = "spcert.crt";

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
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;

	/*
	 * Generate relying-party.xml, attribute-filter.xml, attribute-resolver.xml
	 */
	public boolean generateConfigurationFiles(List<GluuSAMLTrustRelationship> trustRelationships) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpConfFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_CONF_FOLDER + File.separator;
		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;

		// Prepare data for files
		initAttributes(trustRelationships);
		HashMap<String, Object> trustParams = initTrustParamMap(trustRelationships);
		HashMap<String, Object> attrParams = initAttributeParamMap(trustRelationships);

		boolean result = (trustParams != null) && (attrParams != null);
		if (!result) {
			return result;
		}

		VelocityContext context = prepareVelocityContext(trustParams, attrParams, idpMetadataFolder);

		// Generate attribute-resolver.xml
		String attributeResolver = templateService.generateConfFile(SHIB2_IDP_ATTRIBUTE_RESOLVER_FILE, context);
		// Generate attribute-filter.xml
		String attributeFilter = templateService.generateConfFile(SHIB2_IDP_ATTRIBUTE_FILTER_FILE, context);
		// Generate relying-party.xml
		String relyingParty = templateService.generateConfFile(SHIB2_IDP_RELYING_PARTY, context);
		// Generate shibboleth2.xml
		String shibConfig = templateService.generateConfFile(SHIB2_SP_SHIBBOLETH2, context);
		// Generate handler.xml
		String profileHandler = templateService.generateConfFile(SHIB2_IDP_PROFILE_HADLER, context);

		// Generate attribute-map.xml
		// String attributeMap =
		// templateService.generateConfFile(SHIB2_SP_ATTRIBUTE_MAP, context);

		result = (attributeFilter != null) && (attributeResolver != null) && (relyingParty != null) && (shibConfig != null)
				&& (profileHandler != null);

		// Write attribute-resolver.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB2_IDP_ATTRIBUTE_RESOLVER_FILE, attributeResolver);
		// Write attribute-filter.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB2_IDP_ATTRIBUTE_FILTER_FILE, attributeFilter);
		// Write relying-party.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB2_IDP_RELYING_PARTY, relyingParty);
		// Write shibboleth2.xml
		result &= templateService.writeConfFile(getSpShibboleth2FilePath(), shibConfig);
		// Write handler.xml
		result &= templateService.writeConfFile(idpConfFolder + SHIB2_IDP_PROFILE_HADLER, profileHandler);

		// Write attribute-map.xml
		// result &= templateService.writeConfFile(spConfFolder +
		// SHIB2_SP_ATTRIBUTE_MAP, attributeMap);

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
			trustRelationship.setReleasedCustomAttributes(attributeService.getCustomAttributesByAttributeDNs(releasedAttributes,
					attributesByDNs));

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
			boolean isPartOfFederation = !(trustRelationship.getSpMetaDataSourceType() == GluuMetadataSourceType.URI || trustRelationship
					.getSpMetaDataSourceType() == GluuMetadataSourceType.FILE);
			if (!isPartOfFederation) {
				// Set Id
				trustIds.put(trustRelationship.getInum(), String.valueOf(id++));

				// Set entityId
				String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER
						+ File.separator;
				File metadataFile = new File(idpMetadataFolder + trustRelationship.getSpMetaDataFN());
				List<String> entityIds = getEntityIdFromMetadataFile(metadataFile);
				// if for some reason metadata is corrupted or missing - mark
				// trust relationship INACTIVE
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
				}
				if (trustRelationship.getMetadataFilters().get("signatureValidation") != null) {
					Map<String, String> trustEngine = new HashMap<String, String>();
					trustEngine.put("id", "Trust" + StringHelper.removePunctuation(trustRelationship.getInum()));
					trustEngine.put("certPath", applicationConfiguration.getShibboleth2IdpRootDir() + File.separator
							+ SHIB2_IDP_METADATA_FOLDER + File.separator + "credentials" + File.separator
							+ trustRelationship.getMetadataFilters().get("signatureValidation").getFilterCertFileName());
					trustEngines.add(trustEngine);
				}

				// If there is an intrusive filter - push it to the end of the
				// list.
				if (trustRelationship.getGluuSAMLMetaDataFilter() != null) {
					List<String> filtersList = new ArrayList<String>();
					String entityRoleWhiteList = null;
					for (String filterXML : trustRelationship.getGluuSAMLMetaDataFilter()) {
						Document xmlDocument = null;
						try {
							xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
									.parse(new java.io.ByteArrayInputStream(filterXML.getBytes()));
						} catch (Exception e) {
							log.error("GluuSAMLMetaDataFilter contains invalid value.", e);
							continue;
						}

						if (xmlDocument.getFirstChild().getAttributes().getNamedItem("xsi:type").getNodeValue()
								.equals(FilterService.ENTITY_ROLE_WHITE_LIST_TYPE)) {
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

		trustParams.put("idpCredentialsPath", applicationConfiguration.getShibboleth2IdpRootDir() + File.separator
				+ SHIB2_IDP_METADATA_FOLDER + File.separator + "credentials" + File.separator);

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
				boolean standard = metadata.isCustom() || StringHelper.isEmpty(metadata.getUrn())
						|| (!StringHelper.isEmpty(metadata.getUrn()) && metadata.getUrn().startsWith("urn:gluu:dir:attribute-def:"));

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

	public List<String> getEntityIdFromMetadataFile(File metadataFile) {
		if (!metadataFile.isFile()) {
			return null;
		}
		EntityIDHandler handler = parseMetadata(metadataFile);

		List<String> entityIds = handler.getEntityIDs();

		if (entityIds == null || entityIds.isEmpty()) {
			log.error("Failed to find entityId in metadata file '{0}'", metadataFile.getAbsolutePath());
		}

		return entityIds;
	}

	public List<String> getSpEntityIdFromMetadataFile(File metadataFile) {
		EntityIDHandler handler = parseMetadata(metadataFile);

		List<String> entityIds = handler.getSpEntityIDs();

		if (entityIds == null || entityIds.isEmpty()) {
			log.error("Failed to find entityId in metadata file '{0}'", metadataFile.getAbsolutePath());
		}

		return entityIds;
	}

	private EntityIDHandler parseMetadata(File metadataFile) {
		if (!metadataFile.exists()) {
			log.error("Failed to get entityId from metadata file '{0}'", metadataFile.getAbsolutePath());
			return null;
		}

		InputStream is = null;
		InputStreamReader isr = null;
		EntityIDHandler handler = null;
		try {
			is = FileUtils.openInputStream(metadataFile);
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();

			handler = new EntityIDHandler();
			is = FileUtils.openInputStream(metadataFile);
			saxParser.parse(is, handler);

		} catch (IOException ex) {
			log.error("Failed to read metadata file '{0}'", ex, metadataFile.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			log.error("Failed to confugure SAX parser for file '{0}'", e, metadataFile.getAbsolutePath());
		} catch (SAXException e) {
			log.error("Failed to parse file '{0}'", e, metadataFile.getAbsolutePath());
		} finally {
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(is);
		}

		return handler;
	}

	private VelocityContext prepareVelocityContext(HashMap<String, Object> trustParams, HashMap<String, Object> attrParams,
			String idpMetadataFolder) {
		VelocityContext context = new VelocityContext();
		context.put("trustParams", trustParams);
		context.put("attrParams", attrParams);
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

		context.put("ldapUrl", applicationConfiguration.getIdpLdapProtocol() + "://" + applicationConfiguration.getIdpLdapServer());
		context.put("bindDN", applicationConfiguration.getIdpBindDn());

		try {
			context.put("ldapPass", StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpBindPassword(), cryptoConfiguration.getEncodeSalt()));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt bindPassword", e);
		}

		context.put("securityKey", applicationConfiguration.getIdpSecurityKey());
		context.put("securityCert", applicationConfiguration.getIdpSecurityCert());
		try {
			context.put("securityKeyPassword",
					StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpSecurityKeyPassword(), cryptoConfiguration.getEncodeSalt()));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt idp.securityKeyPassword", e);
		}

		context.put("mysqlUrl", applicationConfiguration.getMysqlUrl());
		context.put("mysqlUser", applicationConfiguration.getMysqlUser());

		try {
			context.put("mysqlPass", StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getMysqlPassword(), cryptoConfiguration.getEncodeSalt()));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt mysqlPassword", e);
		}

		return context;
	}

	public String getIdpMetadataFilePath() {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to find IDP metadata file due to undefined IDP root folder");
		}

		String idpConfFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_CONF_FOLDER + File.separator;

		File relyingPartyFile = new File(idpConfFolder + SHIB2_IDP_RELYING_PARTY);
		if (!relyingPartyFile.exists()) {
			log.error("Failed to find IDP metadata file name because relaying party file '{0}' doesn't exist",
					relyingPartyFile.getAbsolutePath());
			return null;
		}

		InputStream is = null;
		InputStreamReader isr = null;
		Document xmlDocument = null;
		try {
			is = FileUtils.openInputStream(relyingPartyFile);
			isr = new InputStreamReader(is, "UTF-8");
			try {
				xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(isr));
			} catch (Exception ex) {
				log.error("Failed to parse relying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
			}
		} catch (IOException ex) {
			log.error("Failed to read relying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
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
			filePath = xPath.compile(
					"/RelyingPartyGroup/MetadataProvider[@id='ShibbolethMetadata']/MetadataProvider[@id='IdPMD']/MetadataResource/@file")
					.evaluate(xmlDocument);
		} catch (XPathExpressionException ex) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", ex, relyingPartyFile.getAbsolutePath());
		}

		if (filePath == null) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", relyingPartyFile.getAbsolutePath());
		}

		return filePath;
	}

	public String getSpMetadataFilePath(String spMetaDataFN) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to return SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
		return idpMetadataFolder + spMetaDataFN;
	}

	public String getSpNewMetadataFileName(GluuSAMLTrustRelationship trustRel) {
		String relationshipInum = StringHelper.removePunctuation(trustRel.getInum());
		return String.format(SHIB2_SP_METADATA_FILE_PATTERN, relationshipInum);
	}

	public String saveSpMetadataFile(String spMetadataFileName, InputStream input) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			IOUtils.closeQuietly(input);
			throw new InvalidConfigurationException("Failed to save SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_TEMPMETADATA_FOLDER + File.separator;
		String tempFileName = getTempMetadataFilename(idpMetadataFolder, spMetadataFileName);
		File spMetadataFile = new File(idpMetadataFolder + tempFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(spMetadataFile);
			IOUtils.copy(input, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write SP meta-data file '{0}'", ex, spMetadataFile);
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

		VelocityContext context = prepareVelocityContext(null, attrParams, null);
		String spAttributeMap = templateService.generateConfFile(SHIB2_SP_ATTRIBUTE_MAP, context);

		return spAttributeMap;
	}

	public boolean generateSpMetadataFile(GluuSAMLTrustRelationship trustRelationship, String certificate) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to generate SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;

		VelocityContext context = new VelocityContext();
		context.put("certificate", certificate);
		context.put("trustRelationship", trustRelationship);
		context.put("entityId", Util.encodeString(StringHelper.removePunctuation(trustRelationship.getInum())));
		context.put("spHost", trustRelationship.getUrl().replaceFirst("/$", ""));

		// Generate sp-metadata.xml meta-data file
		String spMetadataFileContent = templateService.generateConfFile(SHIB2_IDP_SP_METADATA_FILE, context);
		if (StringHelper.isEmpty(spMetadataFileContent)) {
			return false;
		}

		return templateService.writeConfFile(idpMetadataFolder + trustRelationship.getSpMetaDataFN(), spMetadataFileContent);
	}

	public void removeSpMetadataFile(String spMetadataFileName) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to remove SP meta-data file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
		File spMetadataFile = new File(idpMetadataFolder + spMetadataFileName);

		if (spMetadataFile.exists()) {
			spMetadataFile.delete();
		}
	}

	public boolean isCorrectSpMetadataFile(String spMetadataFileName) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to check SP meta-data file due to undefined IDP root folder");
		}
		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER
				+ File.separator;
		File metadataFile = new File(idpMetadataFolder + spMetadataFileName);
		List<String> entityId = getSpEntityIdFromMetadataFile(metadataFile);
		return (entityId != null) && !entityId.isEmpty();
	}

	public String getSpAttributeMapFilePath() {
		String spConfFolder = applicationConfiguration.getShibboleth2SpConfDir() + File.separator;

		return spConfFolder + SHIB2_SP_ATTRIBUTE_MAP;
	}

	public String getSpShibboleth2FilePath() {
		String spConfFolder = applicationConfiguration.getShibboleth2SpConfDir() + File.separator;

		return spConfFolder + SHIB2_SP_SHIBBOLETH2;
	}

	/**
	 * Get shibboleth2ConfService instance
	 * 
	 * @return Shibboleth2ConfService instance
	 */
	public static Shibboleth2ConfService instance() {
		return (Shibboleth2ConfService) Component.getInstance(Shibboleth2ConfService.class);
	}

	public String getSpReadMeResourceName() {
		return SHIB2_SP_READ_ME;
	}

	public String getSpReadMeWindowsResourceName() {
		return SHIB2_SP_READ_ME_WINDOWS;
	}

	public String getPublicCertificate(FileUploadWrapper fileWrapper) {
		if (fileWrapper.getStream() == null) {
			return null;
		}

		List<String> lines = null;
		try {
			lines = (List<String>) IOUtils.readLines(new InputStreamReader(fileWrapper.getStream(), "US-ASCII"));
		} catch (IOException ex) {
			log.error("Failed to read public key file '{0}'", ex, fileWrapper.getFileName());
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
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to create SubversionFile file due to undefined IDP root folder");
		}

		String idpConfFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_CONF_FOLDER + File.separator;
		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
		String idpMetadataCredentialsFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_CREDENTIALS_FOLDER
				+ File.separator;
		String spConfFolder = applicationConfiguration.getShibboleth2SpConfDir() + File.separator;

		List<SubversionFile> subversionFiles = new ArrayList<SubversionFile>();
		subversionFiles.add(new SubversionFile(SHIB2_IDP, idpConfFolder + SHIB2_IDP_ATTRIBUTE_RESOLVER_FILE));
		subversionFiles.add(new SubversionFile(SHIB2_IDP, idpConfFolder + SHIB2_IDP_ATTRIBUTE_FILTER_FILE));
		subversionFiles.add(new SubversionFile(SHIB2_IDP, idpConfFolder + SHIB2_IDP_RELYING_PARTY));
		subversionFiles.add(new SubversionFile(SHIB2_SP, spConfFolder + SHIB2_SP_ATTRIBUTE_MAP));
		subversionFiles.add(new SubversionFile(SHIB2_SP, spConfFolder + SHIB2_SP_SHIBBOLETH2));

		for (GluuSAMLTrustRelationship trustRelationship : trustRelationships) {
			if (trustRelationship.getContainerFederation() == null) {
				subversionFiles.add(new SubversionFile(SHIB2_IDP + File.separator + SHIB2_IDP_METADATA_FOLDER, idpMetadataFolder
						+ trustRelationship.getSpMetaDataFN()));
			}
			if (trustRelationship.getMetadataFilters().containsKey("signatureValidation")) {
				subversionFiles.add(new SubversionFile(SHIB2_IDP + File.separator + SHIB2_IDP_METADATA_CREDENTIALS_FOLDER,
						idpMetadataCredentialsFolder + StringHelper.removePunctuation(trustRelationship.getInum())));
			}
		}

		return subversionFiles;
	}

	public SubversionFile getConfigurationFileForSubversion(GluuSAMLTrustRelationship trustRelationship) {
		if (trustRelationship.getSpMetaDataFN() == null) {
			return null;
		}

		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to create SubversionFile file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;

		return new SubversionFile(SHIB2_IDP + File.separator + SHIB2_IDP_METADATA_FOLDER, idpMetadataFolder
				+ trustRelationship.getSpMetaDataFN());
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
				xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(isr));
			} catch (Exception ex) {
				log.error("Failed to parse metadata file '{0}'", ex, spMetaDataFile.getAbsolutePath());
			}
		} catch (IOException ex) {
			log.error("Failed to read metadata file '{0}'", ex, spMetaDataFile.getAbsolutePath());
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
			federationTag = xPath.compile("count(/EntitiesDescriptor)").evaluate(xmlDocument);
		} catch (XPathExpressionException ex) {
			log.error("Failed to find IDP metadata file in relaying party file '{0}'", ex, spMetaDataFile.getAbsolutePath());
		}

		return Integer.parseInt(federationTag) > 0;
	}

	public String saveFilterCert(String filterCertFileName, InputStream input) {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			IOUtils.closeQuietly(input);
			throw new InvalidConfigurationException("Failed to save filter certificate file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator
				+ "credentials" + File.separator;
		File filterCertFile = new File(idpMetadataFolder + filterCertFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(filterCertFile);
			IOUtils.copy(input, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write  filter certificate file '{0}'", ex, filterCertFile);
			return null;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(input);
		}

		return filterCertFile.getAbsolutePath();
	}

	public boolean generateIdpConfigurationFiles() {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpConfFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_CONF_FOLDER + File.separator;

		// Prepare data for files
		VelocityContext context = new VelocityContext();
		String host = applicationConfiguration.getIdpLdapProtocol() + "://" + applicationConfiguration.getIdpLdapServer();
		String base = applicationConfiguration.getBaseDN();
		String serviceUser = applicationConfiguration.getIdpBindDn();
		String serviceCredential = "";
		try {
			serviceCredential = StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getIdpBindPassword(), cryptoConfiguration.getEncodeSalt());
		} catch (EncryptionException e) {
			log.error("Failed to decrypt bindPassword", e);
		}
		String userField = applicationConfiguration.getIdpUserFields();
		context.put("host", host);
		context.put("base", base);
		context.put("serviceUser", serviceUser);
		context.put("serviceCredential", serviceCredential);
		context.put("userField", userField);

		// Generate login.config
		String loginConfig = templateService.generateConfFile(SHIB2_IDP_LOGIN_CONFIG_FILE, context);

		boolean result = (loginConfig != null);

		// Write login.config
		result &= templateService.writeConfFile(idpConfFolder + SHIB2_IDP_LOGIN_CONFIG_FILE, loginConfig);

		return result;

	}

	public void removeUnusedMetadata() {
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}
		File metadataDir = new File(applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER);

		if (metadataDir.exists()) {
			ArrayList<SubversionFile> obsoleteMetadata = new ArrayList<SubversionFile>();

			for (File metadata : metadataDir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			})) {
				if (trustRelationExists(metadata.getName())) {
					continue;
				}
				obsoleteMetadata
						.add(new SubversionFile(SHIB2_IDP + File.separator + SHIB2_IDP_METADATA_FOLDER, metadata.getAbsolutePath()));
			}
			SubversionService.instance().commitShibboleth2ConfigurationFiles(OrganizationService.instance().getOrganization(),
					new ArrayList<SubversionFile>(), obsoleteMetadata, "Removed Metadata files that are no longer used");
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
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}
		File credentialsDir = new File(applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_CREDENTIALS_FOLDER);
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
				obsoleteMetadata.add(new SubversionFile(SHIB2_IDP + File.separator + SHIB2_IDP_METADATA_CREDENTIALS_FOLDER, credential
						.getAbsolutePath()));
			}
			SubversionService.instance().commitShibboleth2ConfigurationFiles(OrganizationService.instance().getOrganization(),
					new ArrayList<SubversionFile>(), obsoleteMetadata, "Removed Credentials files that are no longer used");
			for (SubversionFile file : obsoleteMetadata) {
				new File(file.getLocalFile()).delete();
			}
		}
	}

	private boolean profileCofigurationExists(String credentialName) {
		for (GluuSAMLTrustRelationship trust : TrustService.instance().getAllTrustRelationships()) {
			if (credentialName.contains(StringHelper.removePunctuation(trust.getInum()))
					&& !credentialName.equals(StringHelper.removePunctuation(trust.getInum()))) {
				try {
					ProfileConfigurationService.instance().parseProfileConfigurations(trust);
				} catch (Exception e) {
					return false;
				}

				ProfileConfiguration profileConfiguration = trust.getProfileConfigurations().get(
						credentialName.replace(StringHelper.removePunctuation(trust.getInum()), ""));
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
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			IOUtils.closeQuietly(stream);
			throw new InvalidConfigurationException("Failed to save Profile Configuration file due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator
				+ "credentials" + File.separator;
		File filterCertFile = new File(idpMetadataFolder + profileConfigurationCertFileName);

		FileOutputStream os = null;
		try {
			os = FileUtils.openOutputStream(filterCertFile);
			IOUtils.copy(stream, os);
			os.flush();
		} catch (IOException ex) {
			log.error("Failed to write  Profile Configuration  certificate file '{0}'", ex, filterCertFile);
			return null;
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(stream);
		}

		return filterCertFile.getAbsolutePath();

	}

	public boolean isCorrectMetadataFile(String spMetaDataFN) {
		if (applicationConfiguration.getShibboleth2FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to check meta-data file due to undefined federation root folder");
		}
		String metadataFolder = applicationConfiguration.getShibboleth2FederationRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER
				+ File.separator;
		File metadataFile = new File(metadataFolder + spMetaDataFN);
		List<String> entityId = getEntityIdFromMetadataFile(metadataFile);
		return (entityId != null) && !entityId.isEmpty();
	}

	public void removeMetadataFile(String spMetaDataFN) {
		if (applicationConfiguration.getShibboleth2FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to remove meta-data file due to undefined federation root folder");
		}

		String metadataFolder = applicationConfiguration.getShibboleth2FederationRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
		File spMetadataFile = new File(metadataFolder + spMetaDataFN);

		if (spMetadataFile.exists()) {
			spMetadataFile.delete();
		}
	}

	public String getMetadataFilePath(String metadataFileName) {
		if (applicationConfiguration.getShibboleth2FederationRootDir() == null) {
			throw new InvalidConfigurationException("Failed to return meta-data file due to undefined federation root folder");
		}

		String metadataFolderName = applicationConfiguration.getShibboleth2FederationRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
		File metadataFolder = new File(metadataFolderName);
		if (!metadataFolder.exists()) {
			metadataFolder.mkdirs();
		}
		return metadataFolderName + metadataFileName;
	}

	public String getNewMetadataFileName(GluuSAMLFederationProposal federationProposal,
			List<GluuSAMLFederationProposal> allFederationProposals) {
		String relationshipInum = StringHelper.removePunctuation(federationProposal.getInum());

		return String.format(SHIB2_METADATA_FILE_PATTERN, relationshipInum);
	}

	public boolean saveMetadataFile(String metadataFileName, InputStream stream) {
		if (applicationConfiguration.getShibboleth2FederationRootDir() == null) {
			IOUtils.closeQuietly(stream);
			throw new InvalidConfigurationException("Failed to save meta-data file due to undefined federation root folder");
		}

		String idpMetadataFolderName = applicationConfiguration.getShibboleth2FederationRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;
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
		if (applicationConfiguration.getShibboleth2IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpMetadataFolder = applicationConfiguration.getShibboleth2IdpRootDir() + File.separator + SHIB2_IDP_METADATA_FOLDER + File.separator;

		// Prepare data for files
		VelocityContext context = new VelocityContext();
		String idpHost = applicationConfiguration.getIdpUrl();

		context.put("idpHost", idpHost);
		String domain = idpHost.replaceAll(":[0-9]*$", "").replaceAll("^.*?//", "");
		context.put("domain", domain);
		try {
			String idpCertificate = FileUtils.readFileToString(new File(applicationConfiguration.getIdpSecurityCert())).replaceAll("-{5}.*?-{5}", "");
			context.put("idpCertificate", idpCertificate);

		} catch (IOException e) {
			log.error("Unable to get idp certificate from " + applicationConfiguration.getIdpSecurityCert(), e);
			return false;
		}

		try {
			String spCertificate = FileUtils.readFileToString(new File(applicationConfiguration.getGluuSpCert())).replaceAll("-{5}.*?-{5}",
					"");
			if(gluuSP.getUrl() == null || "".equals(gluuSP.getUrl())){
				gluuSP.setUrl(applicationConfiguration.getApplianceUrl());
			}
			generateSpMetadataFile(gluuSP, spCertificate);
		} catch (IOException e) {
			log.error("Unable to get sp certificate from " + applicationConfiguration.getGluuSpCert(), e);
			return false;
		}

		// Generate login.config
		String idpMetadata = templateService.generateConfFile(SHIB2_IDP_IDP_METADATA_FILE, context);

		boolean result = (idpMetadata != null);
		String idpMetadataName = String.format(SHIB2_IDP_METADATA_FILE_PATTERN,
				StringHelper.removePunctuation(OrganizationService.instance().getOrganizationInum()));
		// Write login.config
		result &= templateService.writeConfFile(idpMetadataFolder + File.separator + idpMetadataName, idpMetadata);

		return result;

	}

	/**
	 * @param stream
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public synchronized GluuErrorHandler validateMetadata(InputStream stream) throws ParserConfigurationException, SAXException,
			IOException {
		// boolean isValid = false;
		DocumentBuilderFactory newFactory = DocumentBuilderFactory.newInstance();
		newFactory.setCoalescing(false);
		newFactory.setExpandEntityReferences(true);
		newFactory.setIgnoringComments(false);

		newFactory.setIgnoringElementContentWhitespace(false);
		newFactory.setNamespaceAware(true);
		newFactory.setValidating(false);
		// try {
		DocumentBuilder xmlParser = newFactory.newDocumentBuilder();
		Document xmlDoc = xmlParser.parse(stream);
		String schemaDir = System.getProperty("catalina.home") + File.separator + "conf" + File.separator + "shibboleth2" + File.separator
				+ "idp" + File.separator + "schema" + File.separator;
		// System.out.println(schemaDir);
		Schema schema = SchemaBuilder.buildSchema(SchemaLanguage.XML, schemaDir);
		// System.out.println(schema);
		Validator validator = schema.newValidator();
		GluuErrorHandler handler = new GluuErrorHandler();
		validator.setErrorHandler(handler);
		validator.validate(new DOMSource(xmlDoc));
		// isValid = handler.isValid();
		// } catch (Exception e) {
		// isValid=false;
		// log.error("Validation of metadata failed", e);
		// }

		return handler;

	}

}
