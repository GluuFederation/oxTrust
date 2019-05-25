package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.AttributeResolverConfiguration;
import org.gluu.config.oxtrust.LdapOxTrustConfiguration;
import org.gluu.config.oxtrust.NameIdConfig;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.model.GluuAttribute;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

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
	private AppConfiguration applicationConfiguration;

    @Inject
    private PersistenceEntryManager ldapEntryManager;
	
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
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Shibboleth v3 configuration");			
			}
			else {
				try {
					SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
					HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
					SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
					HttpClient client = HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
					HttpGet request = new HttpGet("https://localhost/idp/profile/admin/reload-service?id=shibboleth.NameIdentifierGenerationService");
					request.addHeader("User-Agent",  "Mozilla/5.0");
					HttpResponse response = client.execute(request);
					log.info(EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
				catch (Exception e) {
					e.printStackTrace();
					log.error("error refreshing nameid setting (kindly restart services manually)", e);
				}
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}
	
	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Saml NameId configuration not updated");
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
