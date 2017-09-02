
package org.gluu.oxtrust.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.velocity.VelocityContext;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.ldap.service.FilterService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.ProfileConfigurationService;
import org.gluu.oxtrust.ldap.service.TemplateService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.ShibbolethCASProtocolConfiguration;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;
import org.xdi.model.GluuUserRole;
import org.xdi.model.OxMultivalued;
import org.xdi.model.SchemaEntry;
import org.xdi.service.SchemaService;
import org.xdi.service.XmlService;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;
import org.xdi.util.exception.InvalidConfigurationException;
import org.xdi.util.security.StringEncrypter.EncryptionException;

import com.unboundid.ldap.sdk.schema.AttributeTypeDefinition;

@ConversationScoped
@Named("attributeResolverAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class AttributeResolverAction implements Serializable {

	private static final long serialVersionUID = -9125609238796284572L;
	
	private static final String SHIB3_IDP = "shibboleth-idp";
	private static final String SHIB3_SP = "sp";

	private static final String SHIB3_IDP_CONF_FOLDER = "conf";
	public static final String SHIB3_IDP_METADATA_FOLDER = "metadata";
    private static final String SHIB3_IDP_METADATA_CREDENTIALS_FOLDER = SHIB3_IDP_METADATA_FOLDER + File.separator + "credentials";
    
	private static final String SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE = "attribute-resolver.xml";

	private static final String SHIBBOLETH3_ATTR_RESOLVER_VM_PATH = "/opt/gluu/jetty/identity/conf/shibboleth3/attribute-resolver.xml.vm";
	
	@Inject
	private Logger log;

	@Inject
	private AttributeService attributeService;

	@Inject
	private FacesMessages facesMessages;
	
	@Inject
	private ConversationService conversationService;

	@Inject
	private XmlService xmlService;
	
	@Inject
	private TemplateService templateService;
	
	@Inject
	private FilterService filterService;

	@Inject @Named("casService")
	private CASService casService;
	
	@Inject
	private SchemaService shemaService;
	
	@Inject
	private ProfileConfigurationService profileConfigurationService;
	@Inject
	private AppConfiguration applicationConfiguration;
	
	@Inject
	private ApplianceService applianceService;
	
	@Inject
	private OrganizationService organizationService;

	@Inject
	private EncryptionService encryptionService;
	
	@Inject
	private TrustService trustService;

	private GluuAttribute attribute = new GluuAttribute();
	private boolean enable;
	private String base;
	
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public GluuAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(GluuAttribute attribute) {
		this.attribute = attribute;
	}
	
	
	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String saveCustomAttributetoResolveImpl(){
		String outcome = saveCustomAttributetoResolve();
		
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "NameId configuration updated successfully");
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update NameId configuration");
		}
		
		return outcome;
	}

	public String saveCustomAttributetoResolve(){
		if(!enable){
			return OxTrustConstants.RESULT_FAILURE;
		}
		String attributeName = this.attribute.getName();
		if(attributeService.getAttributeByName(attributeName) != null){
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Attribute already exists.");
			return OxTrustConstants.RESULT_FAILURE;
		}
		String inum = attributeService.generateInumForNewAttribute();
		
		String orgInum = applicationConfiguration.getOrgInum();
		String dn = "inum="+inum+",ou=attributes,o="+orgInum+",o=gluu";
		this.attribute.setDataType(GluuAttributeDataType.STRING);
		GluuUserRole[] gluuEditRole = new GluuUserRole[]{GluuUserRole.ADMIN};
		this.attribute.setEditType(gluuEditRole);
		GluuUserRole[] gluuViewRole = new GluuUserRole[]{GluuUserRole.ADMIN, GluuUserRole.USER};
		this.attribute.setViewType(gluuViewRole);
		this.attribute.setOxMultivaluedAttribute(OxMultivalued.FALSE);
		this.attribute.setOrigin("gluuPerson");
		this.attribute.setStatus(GluuStatus.ACTIVE);
		this.attribute.setInum(inum);
		this.attribute.setDisplayName(attributeName);
		this.attribute.setDn(dn);
		try {
		attributeService.addAttribute(this.attribute);
		} catch (LdapMappingException ex) {
			log.error("Failed to add new attribute {0}", this.attribute.getInum(), ex);

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new attribute");
			return OxTrustConstants.RESULT_FAILURE;
		}
		

		if (applicationConfiguration.getShibboleth3IdpRootDir() == null) {
			throw new InvalidConfigurationException("Failed to update configuration due to undefined IDP root folder");
		}

		String idpConfFolder      = getIdpConfDir();
		String idpMetadataFolder  = getIdpMetadataDir();

		List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
		// Prepare data for files
		initAttributes(trustRelationships);
		HashMap<String, Object> trustParams = initTrustParamMap(trustRelationships);
		HashMap<String, Object> attrParams = initAttributeParamMap(trustRelationships);
        HashMap<String, Object> casParams = initCASParamMap();

		boolean result = (trustParams != null) && (attrParams != null);
		if (!result) {
			log.error("Not trusted to add new attribute {0}", this.attribute.getInum());

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Not trusted  to add new attribute");
			return OxTrustConstants.RESULT_FAILURE;
		}

		VelocityContext context = prepareVelocityContext(trustParams, attrParams, casParams, idpMetadataFolder);
		
		@SuppressWarnings("unchecked")
		HashMap<String, Object> aP = (HashMap<String, Object>)context.get("attrParams");
		@SuppressWarnings("unchecked")
		List<GluuAttribute> attributes = (List<GluuAttribute>)aP.get("attributes");
		attributes.add(this.attribute);
		
		String attributeResolver = templateService.generateConfFile(SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE, context);
		if(attributeResolver != null){
			templateService.writeConfFile(idpConfFolder + SHIB3_IDP_ATTRIBUTE_RESOLVER_FILE, attributeResolver);
		}else{
			log.error("Failed to load attribute-resolver.xml.vm");
		}
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration updated successfully.");
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String cancel(){
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration not updated");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	
	 public String getIdpConfDir() {
         return applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_CONF_FOLDER + File.separator;
 }
 
 public String getIdpMetadataDir() {
         return applicationConfiguration.getShibboleth3IdpRootDir() + File.separator + SHIB3_IDP_METADATA_FOLDER + File.separator;
 }
 
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
 
	private VelocityContext prepareVelocityContext(HashMap<String, Object> trustParams, HashMap<String, Object> attrParams, HashMap<String, Object> casParams, String idpMetadataFolder) {

		VelocityContext context = new VelocityContext();

		context.put("StringHelper", StringHelper.class);

		context.put("trustParams", trustParams);
		context.put("attrParams", attrParams);
		context.put("casParams", casParams);
		context.put("medataFolder", idpMetadataFolder);
		context.put("applianceInum", StringHelper.removePunctuation(applianceService.getApplianceInum()));
		context.put("orgInum", StringHelper.removePunctuation(organizationService.getOrganizationInum()));
		context.put("orgSupportEmail", applicationConfiguration.getOrgSupportEmail());

		String idpUrl = applicationConfiguration.getIdpUrl();
		context.put("idpUrl", idpUrl);

		String idpHost = idpUrl.replaceAll(":[0-9]*$", "");
		context.put("idpHost", idpHost);

		String spUrl = applicationConfiguration.getApplianceUrl();
		context.put("spUrl", spUrl);
		String spHost = spUrl.replaceAll(":[0-9]*$", "").replaceAll("^.*?//", "");
		context.put("spHost", spHost);
		String gluuSPInum = applianceService.getAppliance().getGluuSPTR();
		String gluuSPEntityId = trustService.getRelationshipByInum(gluuSPInum).getEntityId();
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
			context.put("ldapPass", encryptionService.decrypt(applicationConfiguration.getIdpBindPassword()));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt bindPassword", e);
			e.printStackTrace();
		}

		context.put("securityKey", applicationConfiguration.getIdpSecurityKey());
		context.put("securityCert", applicationConfiguration.getIdpSecurityCert());

		try {
			context.put("securityKeyPassword", encryptionService.decrypt(applicationConfiguration.getIdpSecurityKeyPassword()));
		} catch (EncryptionException e) {
			log.error("Failed to decrypt idp.securityKeyPassword", e);
			e.printStackTrace();
		}

		return context;
	}
	
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

			boolean isPartOfFederation = !(trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.URI) || trustRelationship.getSpMetaDataSourceType().equals(GluuMetadataSourceType.FILE));

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
					trustService.updateTrustRelationship(trustRelationship);
					continue;
				}

				trustEntityIds.put(trustRelationship.getInum(), entityIds);

				try {

					filterService.parseFilters(trustRelationship);
					profileConfigurationService.parseProfileConfigurations(trustRelationship);

				} catch (Exception e) {
					log.error("Failed to parse stored metadataFilter configuration for trustRelationship " + trustRelationship.getDn(), e);
					e.printStackTrace();
				}

				if (trustRelationship.getMetadataFilters().get("signatureValidation") != null) {

					Map<String, String> trustEngine = new HashMap<String, String>();

					trustEngine.put("id", "Trust" + StringHelper.removePunctuation(trustRelationship.getInum()));

					trustEngine.put("certPath", getIdpMetadataDir() + "credentials" + File.separator
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

		trustParams.put("idpCredentialsPath", getIdpMetadataDir() + "credentials" + File.separator);

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

		SchemaEntry schemaEntry = shemaService.getSchema();
		List<AttributeTypeDefinition> attributeTypes = shemaService.getAttributeTypeDefinitions(schemaEntry, attributeNames);

		Map<String, String> attributeSAML1Strings = new HashMap<String, String>();
		Map<String, String> attributeSAML2Strings = new HashMap<String, String>();

		for (GluuAttribute metadata : attributes) {

			String attributeName = metadata.getName();

			AttributeTypeDefinition attributeTypeDefinition = shemaService.getAttributeTypeDefinition(attributeTypes, attributeName);
			if (attributeTypeDefinition == null) {
				log.error("Failed to get OID for attribute name {}", attributeName);
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



}