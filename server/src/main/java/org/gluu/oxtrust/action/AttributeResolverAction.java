
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.AppInitializer;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.CASService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.model.GluuAttribute;
import org.xdi.service.security.Secure;

@ConversationScoped
@Named("attributeResolverAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class AttributeResolverAction implements Serializable {

	private static final long serialVersionUID = -9125609238796284572L;
	
	@Inject
	private Logger log;

	@Inject
	private AttributeService attributeService;

	@Inject
	private FacesMessages facesMessages;
	
	@Inject
	private ConversationService conversationService;

	@Inject @Named("casService")
	private CASService casService;
	
	@Inject
	private AppConfiguration applicationConfiguration;
	
	@Inject
	@Named(AppInitializer.LDAP_ENTRY_MANAGER_NAME)
	private Instance<LdapEntryManager> ldapEntryManagerInstance;
	
	@Inject
	private TrustService trustService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private ConfigurationFactory configurationFactory;

	
	private List<GluuAttribute> attributes;
	
	private String attributeName;
	
	private String nameIdType;
	
	private boolean enable;
	private String base;
	
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}
	
	public List<GluuAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<GluuAttribute> attributes) {
		this.attributes = attributes;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getNameIdType() {
		return nameIdType;
	}
	public void setNameIdType(String nameIdType) {
		this.nameIdType = nameIdType;
	}
	public void init() {
		this.attributes = attributeService.getAllAttributes();
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
		LdapOxTrustConfiguration conf = loadConfigurationFromLdap();
		GluuAttribute attribute = attributeService.getAttributeByName(this.attributeName);
		
		boolean updateShib3Configuration = applicationConfiguration.isConfigGeneration(); 
		if (updateShib3Configuration) {    
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();    
			if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {     
				log.error("Failed to update Shibboleth v3 configuration");    
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Shibboleth v3 configuration");    
			}else{
				if(!shibboleth3ConfService.updateAttributeResolver(conf.getAttributeResolverConfig(), attribute)){
					log.error("Unable to update attribute-resolver.xml.vm");
				}
			}
		}
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration updated successfully.");
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String cancel(){
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration not updated");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	private LdapOxTrustConfiguration loadConfigurationFromLdap(String... returnAttributes) {
		final LdapEntryManager ldapEntryManager = ldapEntryManagerInstance.get();
		final String configurationDn = configurationFactory.getConfigurationDn();
		try {
			final LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn,
					returnAttributes);
			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP", ex);
		}
		return null;
	}
	
}
