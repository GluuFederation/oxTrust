/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gluu.oxtrust.ldap.service.ProfileConfigurationService;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.ProfileConfiguration;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.io.FileUploadWrapper;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ConversationScoped
@Named("relyingPartyAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class RelyingPartyAction implements Serializable {

	private static final long serialVersionUID = -5304171897858890801L;

	private List<String> profileConfigurations = null;

	private List<ProfileConfiguration> savedProfileConfigurations = null;

	private Set<ProfileConfiguration> selectedList = new HashSet<ProfileConfiguration>();

	private List<String> availableProfileConfigurations = new ArrayList<String>();

	private ProfileConfiguration profileConfigurationSelected;

	@Inject
	private Logger log;

	@Inject
	private ProfileConfigurationService profileConfigurationService;

	private GluuSAMLTrustRelationship trustRelationship;
	@Inject
	private UpdateTrustRelationshipAction updateTrustRelationshipAction;

	private Map<String, FileUploadWrapper> fileWrappers = new HashMap<String, FileUploadWrapper>();
	private Set<String> allAcrs = new HashSet<>();
	private Set<String> allNamedIds = new HashSet<>();

	@PostConstruct
	public void init() {
		initAcrs();
		initNameIds();
	}

	private void initNameIds() {
		allNamedIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		allNamedIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
		allNamedIds.add("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		allNamedIds.add("urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");
		allNamedIds.add("urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName");
		allNamedIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos");
		allNamedIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		allNamedIds.add("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
	}

	public List<String> getAllNamedIds() {
		return new ArrayList<String>(allNamedIds);
	}

	public void setAllNamedIds(Set<String> allNamedIds) {
		this.allNamedIds = allNamedIds;
	}

	public List<String> getAllAcrs() {
		return new ArrayList<String>(allAcrs);
	}

	public void setAllAcrs(Set<String> allAcrs) {
		this.allAcrs = allAcrs;
	}

	public String initProfileConfigurations() {
		if (profileConfigurations != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		trustRelationship = updateTrustRelationshipAction.getTrustRelationship();
		try {
			profileConfigurationService.parseProfileConfigurations(trustRelationship);
		} catch (Exception e) {
			log.error("Failed to parse stored metadataFilter configuration for trustRelationship "
					+ trustRelationship.getDn(), e);
		}

		availableProfileConfigurations = new ArrayList<String>();
		for (ProfileConfiguration profileConfiguration : profileConfigurationService
				.getAvailableProfileConfigurations()) {
			availableProfileConfigurations.add(profileConfiguration.getName());
			this.getFileWrappers().put(profileConfiguration.getName(), new FileUploadWrapper());
		}

		profileConfigurations = new ArrayList<String>();
		savedProfileConfigurations = new ArrayList<ProfileConfiguration>();

		for (ProfileConfiguration profileConfiguration : profileConfigurationService
				.getProfileConfigurationsList(trustRelationship)) {
			savedProfileConfigurations.add(profileConfiguration);
			profileConfigurations.add(profileConfiguration.getName());
		}

		// availableProfileConfigurations.removeAll(profileConfigurations);
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<String> getProfileConfigurations() {
		return this.profileConfigurations;
	}

	public void setProfileConfigurations(List<String> profileConfigurations) {
		this.profileConfigurations = profileConfigurations;
	}

	public boolean isProfileConfigurationSelected(String profileConfigurationName) {
		if (this.profileConfigurations == null) {
			return false;
		}

		for (String profileConfiguration : this.profileConfigurations) {
			if (profileConfiguration.equals(profileConfigurationName)) {
				return true;
			}
		}

		return false;
	}

	public ProfileConfiguration getProfileConfiguration(String profileConfigurationName) {

		for (ProfileConfiguration profileConfiguration : savedProfileConfigurations) {
			if (StringHelper.equalsIgnoreCase(profileConfiguration.getName(), profileConfigurationName)) {
				return profileConfiguration;
			}
		}

		for (ProfileConfiguration profileConfiguration : profileConfigurationService
				.getAvailableProfileConfigurations()) {
			if (StringHelper.equalsIgnoreCase(profileConfiguration.getName(), profileConfigurationName)) {
				savedProfileConfigurations.add(profileConfiguration);
				return profileConfiguration;
			}
		}
		return null;
	}

	public List<String> getAvailableProfileConfigurations() {

		return availableProfileConfigurations;
	}

	public void setAvailableProfileConfigurations(List<String> availableList) {

		this.availableProfileConfigurations = availableList;
	}

	public void setSelectedList(Set<ProfileConfiguration> selectedList) {
		this.selectedList = selectedList;

		if (selectedList.isEmpty()) {
			setProfileConfigurationSelected(null);
			return;
		}

		boolean selectionChanged = getProfileConfigurationSelected() == null
				|| !selectedList.toArray(new ProfileConfiguration[] {})[0].getName()
						.equals(getProfileConfigurationSelected().getName());
		if (selectionChanged) {
			boolean trustRelationshipAlreadyContainsThisFilter = trustRelationship.getProfileConfigurations()
					.get(selectedList.toArray(new ProfileConfiguration[] {})[0].getName()) != null;
			if (trustRelationshipAlreadyContainsThisFilter) {
				setProfileConfigurationSelected(trustRelationship.getProfileConfigurations()
						.get(selectedList.toArray(new ProfileConfiguration[] {})[0].getName()));
			} else {
				setProfileConfigurationSelected(selectedList.toArray(new ProfileConfiguration[] {})[0]);
			}
		}
	}

	public Set<ProfileConfiguration> getSelectedList() {
		return selectedList;
	}

	public String saveFilters() {
		updateProfileConfigurations();
		profileConfigurationService.saveProfileConfigurations(trustRelationship, fileWrappers);
		profileConfigurations = null;

		String resultInitProfileConfigurations = initProfileConfigurations();
		if (!StringHelper.equalsIgnoreCase(OxTrustConstants.RESULT_SUCCESS, resultInitProfileConfigurations)) {
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void initAcrs() {
		try {
			allAcrs.clear();
			File file = new File("/opt/shibboleth-idp/conf/authn/general-authn.xml");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			document.getDocumentElement().normalize();
			NodeList nodes = document.getElementsByTagName("util:list");
			NodeList childNodes = nodes.item(0).getChildNodes();
			Element element = null;
			for (int index = 0; index < childNodes.getLength(); index++) {
				Node node = childNodes.item(index);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					String id = e.getAttribute("id");
					if (id.equalsIgnoreCase("authn/oxAuth")) {
						element = e;
						break;
					}
				}
			}
			if (element != null) {
				NodeList items = element.getElementsByTagName("bean");
				for (int i = 0; i < items.getLength(); i++) {
					Element node = (Element) items.item(i);
					allAcrs.add(node.getAttribute("c:classRef"));
				}
			}
		} catch (Exception e) {
			log.info("", e);
		}
	}

	public ProfileConfiguration getProfileConfigurationSelected() {
		return profileConfigurationSelected;
	}

	private void setProfileConfigurationSelected(ProfileConfiguration profileConfigurationSelected) {
		this.profileConfigurationSelected = profileConfigurationSelected;
	}

	public String updateProfileConfigurations() {
		for (ProfileConfiguration profileConfiguration : savedProfileConfigurations) {
			if (!profileConfigurationService.isProfileConfigurationPresent(trustRelationship, profileConfiguration)) {
				this.getFileWrappers().put(profileConfiguration.getName(), new FileUploadWrapper());
				profileConfigurationService.updateProfileConfiguration(trustRelationship, profileConfiguration);
			}
		}

		for (ProfileConfiguration profileConfiguration : profileConfigurationService
				.getProfileConfigurationsList(trustRelationship)) {
			if (!profileConfigurations.contains(profileConfiguration.getName())) {
				this.getFileWrappers().remove(profileConfiguration.getName());
				ProfileConfiguration removedProfileConfiguration = null;
				for (ProfileConfiguration savedProfileConfiguration : savedProfileConfigurations) {
					if (savedProfileConfiguration.getName().equals(profileConfiguration.getName())) {
						removedProfileConfiguration = profileConfiguration;
					}
				}
				if (removedProfileConfiguration != null) {
					savedProfileConfigurations.remove(profileConfiguration);
				}

				profileConfigurationService.removeProfileConfiguration(trustRelationship, profileConfiguration);
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;

	}

	public boolean getIncludeAttributeStatement() {
		return getProfileConfigurationSelected().isIncludeAttributeStatement();
	}

	public void setIncludeAttributeStatement(boolean includeAttributeStatement) {
		getProfileConfigurationSelected().setIncludeAttributeStatement(includeAttributeStatement);
	}

	public String getSignResponses() {
		return getProfileConfigurationSelected().getSignResponses();
	}

	public void setSignResponses(String signResponses) {
		getProfileConfigurationSelected().setSignResponses(signResponses);
	}

	public String getSignAssertions() {
		return getProfileConfigurationSelected().getSignAssertions();
	}

	public void setSignAssertions(String signAssertions) {
		getProfileConfigurationSelected().setSignAssertions(signAssertions);
	}

	public String getSignRequests() {
		return getProfileConfigurationSelected().getSignRequests();
	}

	public void setSignRequests(String signRequests) {
		getProfileConfigurationSelected().setSignRequests(signRequests);
	}

	public int getAssertionLifetime() {
		return getProfileConfigurationSelected().getAssertionLifetime();
	}

	public void setAssertionLifetime(int assertionLifetime) {
		getProfileConfigurationSelected().setAssertionLifetime(assertionLifetime);
	}

	public String getEncryptNameIds() {
		return getProfileConfigurationSelected().getEncryptNameIds();
	}

	public void setEncryptNameIds(String encryptNameIds) {
		getProfileConfigurationSelected().setEncryptNameIds(encryptNameIds);
	}

	public String getEncryptAssertions() {
		return getProfileConfigurationSelected().getEncryptAssertions();
	}

	public void setEncryptAssertions(String encryptAssertions) {
		getProfileConfigurationSelected().setEncryptAssertions(encryptAssertions);
	}

	public int getAssertionProxyCount() {
		return getProfileConfigurationSelected().getAssertionProxyCount();
	}

	public void setAssertionProxyCount(int assertionProxyCount) {
		getProfileConfigurationSelected().setAssertionProxyCount(assertionProxyCount);
	}
	
	public String getDefaultAuthenticationMethod() {
		return getProfileConfigurationSelected().getDefaultAuthenticationMethod();
	}

	public void setDefaultAuthenticationMethod(String method) {
		getProfileConfigurationSelected().setDefaultAuthenticationMethod(method);
	}
	
	public String getDefaultNameIDFormat() {
		return getProfileConfigurationSelected().getDefaultNameIDFormat();
	}

	public void setDefaultNameIDFormat(String method) {
		getProfileConfigurationSelected().setDefaultNameIDFormat(method);
	}


	public void showFile() {

	}

	public Map<String, FileUploadWrapper> getFileWrappers() {
		return fileWrappers;
	}
}
