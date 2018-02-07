
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.AttributeResolverConfiguration;
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
	
	@Inject
	private AppConfiguration applicationConfiguration;

    @Inject
    private LdapEntryManager ldapEntryManager;
	
	@Inject
	private TrustService trustService;
	
	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private ConfigurationFactory configurationFactory;
	
	private List<GluuAttribute> attributes;
	
	private String attributeBase;
	private String attributeName;
	private String nameIdType;
	private boolean enable;

	private boolean initialized;
	
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	public String getAttributeBase() {
		return attributeBase;
	}

	public void setAttributeBase(String attributeBase) {
		this.attributeBase = attributeBase;
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

	public List<GluuAttribute> getAttributes() {
		return attributes;
	}

	public String init() {
		if (initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.attributes = attributeService.getAllAttributes();

		final LdapOxTrustConfiguration conf = configurationFactory.loadConfigurationFromLdap("oxTrustConfAttributeResolver");
		AttributeResolverConfiguration attributeResolverConfiguration = conf.getAttributeResolverConfig();
		if (attributeResolverConfiguration != null) {
			this.attributeName = attributeResolverConfiguration.getAttributeName();
			this.nameIdType = attributeResolverConfiguration.getNameIdType();
			this.enable = attributeResolverConfiguration.isEnabled();
			
			String attributeBase = attributeResolverConfiguration.getAttributeBase();
			GluuAttribute foundAttribute = attributeService.getAttributeByName(attributeBase, this.attributes);
			if (foundAttribute != null) {
				this.attributeBase = foundAttribute.getName();
			}
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
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
		AttributeResolverConfiguration attributeResolverConfiguration = new AttributeResolverConfiguration();
		attributeResolverConfiguration.setAttributeBase(attributeBase);
		attributeResolverConfiguration.setAttributeName(attributeName);
		attributeResolverConfiguration.setNameIdType(nameIdType);
		attributeResolverConfiguration.setEnabled(enable);
		try {
			final LdapOxTrustConfiguration conf = configurationFactory.loadConfigurationFromLdap();
			conf.setAttributeResolverConfig(attributeResolverConfiguration);
			conf.setRevision(conf.getRevision() + 1);
			ldapEntryManager.merge(conf);
		} catch (Exception ex) {
			log.error("Failed to save Attribute Resolver configuration configuration", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (!enable) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		
		boolean updateShib3Configuration = applicationConfiguration.isConfigGeneration(); 
		if (updateShib3Configuration) {    
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();    
			if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
				log.error("Failed to update Shibboleth v3 configuration");
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Shibboleth v3 configuration");			}
		}

		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration updated successfully.");
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String cancel(){
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration not updated");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}
	
}
