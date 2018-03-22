
package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.xdi.config.oxtrust.NameIdConfig;
import org.xdi.model.GluuAttribute;
import org.xdi.service.security.Secure;

@ConversationScoped
@Named("configureNameIdAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class ConfigureNameIdAction implements Serializable {

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
	
    private ArrayList<NameIdConfig> nameIdConfigs;
	private List<GluuAttribute> attributes;

	private boolean initialized;


	public List<GluuAttribute> getAttributes() {
		return attributes;
	}

	public String init() {
		if (initialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.attributes = attributeService.getAllAttributes();

		final LdapOxTrustConfiguration conf = configurationFactory.loadConfigurationFromLdap("oxTrustConfAttributeResolver");
		if (conf == null) {
		    log.error("Failed to load oxTrust configuration");
            return OxTrustConstants.RESULT_FAILURE;
		}
		
		this.nameIdConfigs = new ArrayList<NameIdConfig>();

		AttributeResolverConfiguration attributeResolverConfiguration = conf.getAttributeResolverConfig();
		if ((attributeResolverConfiguration != null) && (attributeResolverConfiguration.getNameIdConfigs() != null)) {
		    for (NameIdConfig nameIdConfig : attributeResolverConfiguration.getNameIdConfigs()) {
		        this.nameIdConfigs.add(nameIdConfig);
		    }
		}

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() {
		String outcome = saveImpl();
		
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "NameId configuration updated successfully");
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update NameId configuration");
		}
		
		return outcome;
	}

	private String saveImpl() {
		AttributeResolverConfiguration attributeResolverConfiguration = new AttributeResolverConfiguration();
		attributeResolverConfiguration.setNameIdConfigs(this.nameIdConfigs);
		try {
			final LdapOxTrustConfiguration conf = configurationFactory.loadConfigurationFromLdap();
			conf.setAttributeResolverConfig(attributeResolverConfiguration);
			conf.setRevision(conf.getRevision() + 1);
			ldapEntryManager.merge(conf);
		} catch (Exception ex) {
			log.error("Failed to save Attribute Resolver configuration configuration", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		
 		boolean updateShib3Configuration = applicationConfiguration.isConfigGeneration(); 
		if (updateShib3Configuration) {    
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();    
			if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
				log.error("Failed to update Shibboleth v3 configuration");
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Shibboleth v3 configuration");			}
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration not updated");
//		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

    public ArrayList<NameIdConfig> getNameIdConfigs() {
        return nameIdConfigs;
    }
    
    public void addNameIdConfig() {
        NameIdConfig nameIdConfig = new NameIdConfig();
        this.nameIdConfigs.add(nameIdConfig);
    }
    
    public void removeNameIdConfig(NameIdConfig removenameIdConfig) {
        for (Iterator<NameIdConfig> iterator = this.nameIdConfigs.iterator(); iterator.hasNext();) {
            NameIdConfig nameIdConfig = iterator.next();
            if (System.identityHashCode(removenameIdConfig) == System.identityHashCode(nameIdConfig)) {
                iterator.remove();
                return;
            }
        }
    }

}
