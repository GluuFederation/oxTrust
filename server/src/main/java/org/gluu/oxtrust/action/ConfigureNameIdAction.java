package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.AttributeResolverConfiguration;
import org.gluu.config.oxtrust.NameIdConfig;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.model.GluuAttribute;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxtrust.service.Shibboleth3ConfService;
import org.gluu.oxtrust.service.TrustService;
import org.gluu.oxtrust.util.CloudEditionUtil;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

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
	private TrustService trustService;

	@Inject
	private Shibboleth3ConfService shibboleth3ConfService;

	@Inject
	private ConfigurationFactory configurationFactory;
	
	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private ArrayList<NameIdConfig> nameIdConfigs;
	private List<GluuAttribute> attributes;

	private Map<String, String> availableNamedIds = new HashMap<>();
	private Map<String, String> usedNamedIds = new HashMap<>();

	public List<GluuAttribute> getAttributes() {
		return attributes;
	}

	public String init() {
		loadNameIds();
		this.attributes = attributeService.getAllAttributes();
		this.nameIdConfigs = new ArrayList<NameIdConfig>();
		AttributeResolverConfiguration attributeResolverConfiguration = configurationFactory
				.getAttributeResolverConfiguration();
		if ((attributeResolverConfiguration != null) && (attributeResolverConfiguration.getNameIdConfigs() != null)) {
			this.usedNamedIds.clear();
			for (NameIdConfig nameIdConfig : attributeResolverConfiguration.getNameIdConfigs()) {
				this.nameIdConfigs.add(nameIdConfig);
				this.usedNamedIds.put(nameIdConfig.getNameIdType(), nameIdConfig.getNameIdType());
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void loadNameIds() {
		availableNamedIds.put("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent",
				"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		availableNamedIds.put("urn:oasis:names:tc:SAML:2.0:nameid-format:transient",
				"urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
		availableNamedIds.put("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
				"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		availableNamedIds.put("urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName",
				"urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");
		availableNamedIds.put("urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName",
				"urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName");
		availableNamedIds.put("urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos",
				"urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos");
		availableNamedIds.put("urn:oasis:names:tc:SAML:2.0:nameid-format:entity",
				"urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		availableNamedIds.put("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
				"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
	}

	public String save() {
		String outcome = saveImpl();
		if (OxTrustConstants.RESULT_SUCCESS.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "NameId configuration updated successfully");
		} else if (OxTrustConstants.RESULT_FAILURE.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update NameId configuration");
		}
		else if (OxTrustConstants.RESULT_RESTART_IDP.equals(outcome)) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Please kindly restart idp service to complete the NameID configuration");
			outcome = OxTrustConstants.RESULT_SUCCESS;
		}
		return outcome;
	}

	private String saveImpl() {
		AttributeResolverConfiguration attributeResolverConfiguration = new AttributeResolverConfiguration();
		attributeResolverConfiguration.setNameIdConfigs(this.nameIdConfigs);
		jsonConfigurationService.saveOxTrustAttributeResolverConfigurationConfiguration(attributeResolverConfiguration);
		boolean updateShib3Configuration = applicationConfiguration.isConfigGeneration();
		if (updateShib3Configuration) {
			List<GluuSAMLTrustRelationship> trustRelationships = trustService.getAllActiveTrustRelationships();
			if (!shibboleth3ConfService.generateConfigurationFiles(trustRelationships)) {
				log.error("Failed to update Shibboleth v3 configuration");
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update Shibboleth v3 configuration");
			} else {
				try {
					SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy())
							.build();
					HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
					SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
							allowAllHosts);
					HttpClient client = HttpClients.custom()
							.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
							.setSSLSocketFactory(connectionFactory).build();
					HttpGet request = new HttpGet(
							CloudEditionUtil.getIdpHost().orElse("https://localhost")+"/idp/profile/admin/reload-service?id=shibboleth.NameIdentifierGenerationService");
					request.addHeader("User-Agent", "Mozilla/5.0");
					HttpResponse response = client.execute(request);
					log.info(EntityUtils.toString(response.getEntity(), "UTF-8"));
				} catch (Exception e) {
					log.error("error refreshing nameid setting (kindly restart services manually)", e);
					return OxTrustConstants.RESULT_RESTART_IDP;
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

	public Map<String, String> getAvailableNamedIds(NameIdConfig config) {
		MapDifference<String, String> diff = Maps.difference(availableNamedIds, usedNamedIds);
		Map<String, String> value = diff.entriesOnlyOnLeft();
		Map<String, String> result = Maps.newHashMap(value);
		if (config.getNameIdType() != null) {
			result.put(config.getNameIdType(), config.getNameIdType());
		}
		return result;
	}

	public void setAvailableNamedIds(Map<String, String> availableNamedIds) {
		this.availableNamedIds = availableNamedIds;
	}

	public Map<String, String> getUsedNamedIds() {
		return usedNamedIds;
	}

	public void setUsedNamedIds(Map<String, String> usedNamedIds) {
		this.usedNamedIds = usedNamedIds;
	}

}
