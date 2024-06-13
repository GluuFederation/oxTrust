/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.DisplayNameEntry;
import org.gluu.model.GluuAttribute;
import org.gluu.model.SelectableEntity;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxauth.model.common.BackchannelTokenDeliveryMode;
import org.gluu.oxauth.model.common.GrantType;
import org.gluu.oxauth.model.common.ResponseType;
import org.gluu.oxauth.model.crypto.signature.AsymmetricSignatureAlgorithm;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxauth.model.util.URLPatternList;
import org.gluu.oxtrust.model.AuthenticationMethod;
import org.gluu.oxtrust.model.BlockEncryptionAlgorithm;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.KeyEncryptionAlgorithm;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.OxAuthSubjectType;
import org.gluu.oxtrust.model.SignatureAlgorithm;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.service.uma.ResourceSetService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.LookupService;
import org.gluu.service.custom.script.AbstractCustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.SelectableEntityHelper;
import org.gluu.util.StringHelper;
import org.gluu.util.Util;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.oxauth.persistence.model.ClientAttributes;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.heading.Heading;

/**
 * Action class for viewing and updating clients.
 *
 * @author Reda Zerrad Date: 06.11.2012
 * @author Yuriy Movchan Date: 04/07/2014
 * @author Javier Rojas Blum
 * @version June 21, 2018
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('client', 'access')}")
public class UpdateClientAction implements Serializable {

    private String HTTPS = "https://";

    private static final long serialVersionUID = -5756470620039988876L;

    @Inject
    private Logger log;

    @Inject
    private ClientService clientService;

    @Inject
    private ScopeService scopeService;

    @Inject
    private AttributeService attributeService;

    @Inject
    private AbstractCustomScriptService customScriptService;

    @Inject
    private ResourceSetService resourceSetService;
    
    @Inject
    private LookupService lookupService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private ConversationService conversationService;

    @Inject
    private EncryptionService encryptionService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private OxTrustAuditService oxTrustAuditService;

    @Inject
    private Identity identity;

    private String inum;

    private String markDown = "";

    private boolean update;

    private OxAuthClient client;

    private List<String> loginUris = Lists.newArrayList();
    private List<String> logoutUris;
    private List<String> clientlogoutUris;
    private List<String> redirectLogoutUrl;
    private List<String> clientBackChannellogoutUris;
    private List<String> claimRedirectURIList;
    private List<String> additionalAudienceList;

    private List<Scope> scopes;
    private List<UmaResource> resources;
    private List<DisplayNameEntry> claims;
    private List<ResponseType> responseTypes;
    private List<CustomScript> customScripts;
    private List<CustomScript> postAuthnScripts = Lists.newArrayList();
    private List<CustomScript> rptClaimsScripts = Lists.newArrayList();
    private List<CustomScript> consentScripts = Lists.newArrayList();
    private List<CustomScript> spontaneousScopesScripts = Lists.newArrayList();
    private List<CustomScript> introspectionScripts = Lists.newArrayList();
    private List<GrantType> grantTypes;
    private List<String> contacts;
    private List<String> requestUris;
    private List<String> authorizedOrigins;

    private String searchAvailableClaimPattern;
    private String oldSearchAvailableClaimPattern;
    private String tlsSubjectDn;

    private String availableLoginUri = HTTPS;
    private String availableLogoutUri = HTTPS;
    private String availableClientlogoutUri = HTTPS;
    private String availableRedirectLogoutUrl = HTTPS;
    private String availableClientBacklogoutUri = HTTPS;
    private String availableContact = "";
    private String availableRequestUri = HTTPS;
    private String availableAuthorizedOrigin = HTTPS;
    private String availableClaimRedirectUri = HTTPS;
    private String availableAdditionalAudience = "";
    private String oxAttributesJson;
    private String backchannelLogoutUri;
    private String customScriptsforPostAuthn;
    private String customScriptsforConsentGather;
    private String spontaneousScopeCustomScript;
    private String introspectionCustomScript;
    private String rptClaimsScript;
    private String scopePattern;
    
    
    Pattern domainPattern = Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\\\.)+[A-Za-z]{2,6}");
    public String getAvailableAuthorizedOrigin() {
        return availableAuthorizedOrigin;
    }

    public void setAvailableAuthorizedOrigin(String availableAuthorizedOrigin) {
        this.availableAuthorizedOrigin = availableAuthorizedOrigin;
    }

    public String getAvailableClaimRedirectUri() {
        return availableClaimRedirectUri;
    }

    public void setAvailableClaimRedirectUri(String availableClaimRedirectUri) {
        this.availableClaimRedirectUri = availableClaimRedirectUri;
    }

    private List<GluuAttribute> availableClaims;
    private List<GluuGroup> availableGroups;
    private List<CustomScript> availableCustomScriptsAcr;
    private List<SelectableEntity<ResponseType>> availableResponseTypes;
    private List<SelectableEntity<CustomScript>> availableCustomScripts;
    private List<SelectableEntity<CustomScript>> availablePostAuthnScripts;
    private List<SelectableEntity<CustomScript>> availableRptClaimsScripts;
    private List<SelectableEntity<CustomScript>> availableConsentScripts;
    private List<SelectableEntity<CustomScript>> availableSpontaneousScripts;
    private List<SelectableEntity<CustomScript>> availableIntrospectionScripts;
    private List<SelectableEntity<GrantType>> availableGrantTypes;
    private List<SelectableEntity<Scope>> availableScopes;

    public String add() throws Exception {
        if (this.client != null) {
            this.client.setOxAuthAppType(OxAuthApplicationType.WEB);
            this.client.setSubjectType(OxAuthSubjectType.PAIRWISE);
            return OxTrustConstants.RESULT_SUCCESS;
        }
        this.update = false;
        this.oxAttributesJson = getClientAttributesJson();
        this.client = new OxAuthClient();
        this.client.setOxAuthAppType(OxAuthApplicationType.WEB);
        this.client.setSubjectType(OxAuthSubjectType.PAIRWISE);
        try {
            this.loginUris = getNonEmptyStringList(client.getOxAuthRedirectURIs());
            this.clientlogoutUris = getNonEmptyStringList(client.getLogoutUri());
            this.clientBackChannellogoutUris = getNonEmptyStringList(client.getAttributes().getBackchannelLogoutUri());
            this.scopes = getInitialEntries();
            this.claims = getInitialClaimDisplayNameEntries();
            this.responseTypes = getInitialResponseTypes();
            this.grantTypes = getInitialGrantTypes();
            this.contacts = getNonEmptyStringList(client.getContacts());
            this.requestUris = getNonEmptyStringList(client.getRequestUris());
            this.authorizedOrigins = getNonEmptyStringList(client.getAuthorizedOrigins());
            this.claimRedirectURIList = getNonEmptyStringList(client.getClaimRedirectURI());
            this.additionalAudienceList = getNonEmptyStringList(client.getAttributes().getAdditionalAudience());
            this.postAuthnScripts = Lists.newArrayList();
            this.rptClaimsScripts = Lists.newArrayList();
            this.consentScripts = Lists.newArrayList();
            this.spontaneousScopesScripts = Lists.newArrayList();
            this.backchannelLogoutUri = getStringFromList(client.getAttributes().getBackchannelLogoutUri());
            this.tlsSubjectDn = client.getAttributes().getTlsClientAuthSubjectDn();
            this.scopePattern = "";
            this.redirectLogoutUrl = getNonEmptyStringList(client.getOxAuthPostLogoutRedirectURIs());
            searchAvailableCustomScriptsforAcr();
        } catch (BasePersistenceException ex) {
            log.error("Failed to prepare lists", ex);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new client");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

	private List<String> getNonEmptyStringList(List<String> currentList) {
    	        if (currentList != null && currentList.size() > 0) {
    	            return new ArrayList<String>(currentList);
    	        } else {
    	            return new ArrayList<String>();
    	        }
	}

	private List<Scope> getInitialEntries() {
        List<Scope> existingScopes = new ArrayList<Scope>();
        if ((client.getOxAuthScopes() == null) || (client.getOxAuthScopes().size() == 0)) {
            return existingScopes;
        }
        for (String dn : client.getOxAuthScopes()) {
            try {
                Scope scope = scopeService.getScopeByDn(dn);
                if (scope != null) {
                    existingScopes.add(scope);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return existingScopes;
    }

    public String update() throws Exception {
        if (this.client != null) {
            return OxTrustConstants.RESULT_SUCCESS;
        }
        this.update = true;
        log.debug("this.update : " + this.update);
        try {
            log.debug("inum : " + inum);
            this.client = clientService.getClientByInum(inum);
            this.client.setOxAuthClientSecret(encryptionService.decrypt(this.client.getEncodedClientSecret()));
            log.trace("CLIENT SECRET UPDATE:" + this.client.getOxAuthClientSecret());
        } catch (BasePersistenceException ex) {
            log.error("Failed to find client {}", inum, ex);
        }
        if (this.client == null) {
            log.error("Failed to load client {}", inum);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find client");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_FAILURE;
        }
        try {
            this.loginUris = getNonEmptyStringList(client.getOxAuthRedirectURIs());

            this.clientlogoutUris = getNonEmptyStringList(client.getLogoutUri());
            this.redirectLogoutUrl= getNonEmptyStringList(client.getOxAuthPostLogoutRedirectURIs());
            this.clientBackChannellogoutUris = getNonEmptyStringList(client.getAttributes().getBackchannelLogoutUri());
            this.scopes = getInitialEntries();
            this.claims = getInitialClaimDisplayNameEntries();
            this.responseTypes = getInitialResponseTypes();
            this.grantTypes = getInitialGrantTypes();
            this.contacts = getNonEmptyStringList(client.getContacts());
            this.requestUris = getNonEmptyStringList(client.getRequestUris());
            this.authorizedOrigins = getNonEmptyStringList(client.getAuthorizedOrigins());
            this.claimRedirectURIList = getNonEmptyStringList(client.getClaimRedirectURI());
            this.additionalAudienceList = getNonEmptyStringList(client.getAttributes().getAdditionalAudience());
            this.tlsSubjectDn = client.getAttributes().getTlsClientAuthSubjectDn();
            this.scopePattern = "";
            
            this.postAuthnScripts = searchAvailablePostAuthnCustomScripts().stream()
                    .filter(entity -> client.getAttributes().getPostAuthnScripts().contains(entity.getEntity().getDn()))
                    .map(SelectableEntity::getEntity).collect(Collectors.toList());
            this.rptClaimsScripts = searchAvailableRptClaimsCustomScripts().stream()
                    .filter(entity -> client.getAttributes().getRptClaimsScripts().contains(entity.getEntity().getDn()))
                    .map(SelectableEntity::getEntity).collect(Collectors.toList());
            this.consentScripts = searchAvailableConsentCustomScripts().stream().filter(
                    entity -> client.getAttributes().getConsentGatheringScripts().contains(entity.getEntity().getDn()))
                    .map(SelectableEntity::getEntity).collect(Collectors.toList());
            this.spontaneousScopesScripts = searchAvailableSpontaneousScopeScripts().stream()
                    .filter(entity -> client.getAttributes().getSpontaneousScopeScriptDns()
                            .contains(entity.getEntity().getDn()))
                    .map(SelectableEntity::getEntity).collect(Collectors.toList());
            this.introspectionScripts = searchAvailableIntrospectionCustomScripts().stream().filter(
                    entity -> client.getAttributes().getIntrospectionScripts().contains(entity.getEntity().getDn()))
                    .map(SelectableEntity::getEntity).collect(Collectors.toList());
            this.oxAttributesJson = getClientAttributesJson();
            this.backchannelLogoutUri = getStringFromList(client.getAttributes().getBackchannelLogoutUri());
            this.customScriptsforPostAuthn = getStringFromList(client.getAttributes().getPostAuthnScripts());
            this.customScriptsforConsentGather = getStringFromList(client.getAttributes().getConsentGatheringScripts());
            this.spontaneousScopeCustomScript = getStringFromList(client.getAttributes().getSpontaneousScopeScriptDns());
            this.introspectionCustomScript = getStringFromList(client.getAttributes().getIntrospectionScripts());
            this.rptClaimsScript = getStringFromList(client.getAttributes().getRptClaimsScripts());
            this.resources = resourceSetService.findResourcesByClients(client.getDn());
            
            searchAvailableCustomScriptsforAcr();
        } catch (BasePersistenceException ex) {
            log.error("Failed to prepare lists", ex);
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load client");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_FAILURE;
        }

        return OxTrustConstants.RESULT_SUCCESS;
    }
    
    private String getStringFromList(List<String> currentList) {
        if (currentList != null && currentList.size() > 0) {
            return currentList.get(0);
        } else {
            return null;
        }
    }

    private List<String> getNonEmptyStringList(String[] currentList) {
        if (currentList != null && currentList.length > 0) {
            return new ArrayList<String>(Arrays.asList(currentList));
        } else {
            return new ArrayList<String>();
        }
    }

    public String cancel() {
        if (update) {
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    "Client '#{updateClientAction.client.displayName}' not updated");
        } else {
            facesMessages.add(FacesMessage.SEVERITY_INFO, "New client not added");
        }

        conversationService.endConversation();

        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String save() throws Exception {
        String sectorIdentifierUri = this.client.getSectorIdentifierUri();
        if (!StringUtils.isWhitespace(sectorIdentifierUri) && sectorIdentifierUri != null) {
            boolean check=loadSector(sectorIdentifierUri);
            if(!check){
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "The sector identifier uri provided is not valid!");
                return OxTrustConstants.RESULT_FAILURE;
            }
        }
        if (this.client.isDeletable() && this.client.getExp() == null) {
            this.client.setExp(oneDay());
        }
        if (!this.client.isDeletable()) {
            this.client.setExp(null);
        }
        updateLoginURIs();
        updateLogoutURIs();
        updateRedirectLogoutUrls();
        updateBackChannelLogoutURIs();
        updateScopes();
        updateClaims();
        updateResponseTypes();
        updateCustomScripts();
        updateGrantTypes();
        updateContacts();
        updateRequestUris();
        updateAuthorizedOrigins();
        updateClaimredirectUri();
        updateAdditionalAudience();
        trimUriProperties();
        client.getAttributes().setTlsClientAuthSubjectDn(tlsSubjectDn);
        this.client.setEncodedClientSecret(encryptionService.encrypt(this.client.getOxAuthClientSecret()));
        if (update) {
            try {
                clientService.updateClient(this.client);
                oxTrustAuditService.audit(
                        "OPENID CLIENT " + this.client.getInum() + " **" + this.client.getDisplayName() + "** UPDATED",
                        identity.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            } catch (BasePersistenceException ex) {
                log.error("Failed to update client {}", this.inum, ex);
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        "Failed to update client '#{updateClientAction.client.displayName}'");
                return OxTrustConstants.RESULT_FAILURE;
            }
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    "Client '#{updateClientAction.client.displayName}' updated successfully");
        } else {
            this.inum = clientService.generateInumForNewClient();
            if (StringHelper.isEmpty(this.client.getEncodedClientSecret())) {
                generatePassword();
            }
            this.client.setDn(clientService.getDnForClient(this.inum));
            this.client.setInum(this.inum);
            try {
                clientService.addClient(this.client);
                oxTrustAuditService.audit(
                        "OPENID CLIENT " + this.client.getInum() + " **" + this.client.getDisplayName() + "** ADDED ",
                        identity.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            } catch (BasePersistenceException ex) {
                log.error("Failed to add new client {}", this.inum, ex);
                facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new client");
                return OxTrustConstants.RESULT_FAILURE;
            }
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    "New client '#{updateClientAction.client.displayName}' added successfully");
            conversationService.endConversation();
            this.update = true;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    private void saveAttributesJson() {
        ClientAttributes clientAttributes = new ClientAttributes();
        try {
            clientAttributes = new ObjectMapper().readValue(this.oxAttributesJson, ClientAttributes.class);
        } catch (Exception e) {
            log.info("error parsing json:" + e);
        }
        clientAttributes
                .setPostAuthnScripts(postAuthnScripts.stream().map(CustomScript::getDn).collect(Collectors.toList()));
        clientAttributes
                .setRptClaimsScripts(rptClaimsScripts.stream().map(CustomScript::getDn).collect(Collectors.toList()));
        clientAttributes.setConsentGatheringScripts(
                consentScripts.stream().map(CustomScript::getDn).collect(Collectors.toList()));
        clientAttributes.setSpontaneousScopeScriptDns(
                spontaneousScopesScripts.stream().map(CustomScript::getDn).collect(Collectors.toList()));
        clientAttributes.setIntrospectionScripts(
                introspectionScripts.stream().map(CustomScript::getDn).collect(Collectors.toList()));
        this.client.setAttributes(clientAttributes);
    }

    private void trimUriProperties() {
        this.client.setClientUri(StringHelper.trimAll(this.client.getClientUri()));
        this.client.setJwksUri(StringHelper.trimAll(this.client.getJwksUri()));
        this.client.setLogoUri(StringHelper.trimAll(this.client.getLogoUri()));
        this.client.setPolicyUri(StringHelper.trimAll(this.client.getPolicyUri()));
        this.client.setSectorIdentifierUri(StringHelper.trimAll(this.client.getSectorIdentifierUri()));
        this.client.setTosUri(StringHelper.trimAll(this.client.getTosUri()));
        this.client.setInitiateLoginUri(StringHelper.trimAll(this.client.getInitiateLoginUri()));
    }

    public String delete() throws Exception {
        if (update) {
            try {
                clientService.removeClient(this.client);
                oxTrustAuditService.audit(
                        "OPENID CLIENT " + this.client.getInum() + " **" + this.client.getDisplayName() + "** DELETED ",
                        identity.getUser(),
                        (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
                facesMessages.add(FacesMessage.SEVERITY_INFO,
                        "Client '#{updateClientAction.client.displayName}' removed successfully");
                conversationService.endConversation();
                return OxTrustConstants.RESULT_SUCCESS;
            } catch (BasePersistenceException ex) {
                log.error("Failed to remove client {}", this.inum, ex);
            }
        }
        facesMessages.add(FacesMessage.SEVERITY_ERROR,
                "Failed to remove client '#{updateClientAction.client.displayName}'");
        return OxTrustConstants.RESULT_FAILURE;
    }

    public void removeLoginURI(String uri) {
        removeFromList(this.loginUris, uri);
    }

    public void removeLogoutURI(String uri) {
        removeFromList(this.logoutUris, uri);
    }

    public void removeClientLogoutURI(String uri) {
        removeFromList(this.clientlogoutUris, uri);
    }
    
    public void removeRedirectLogoutUrl(String uri) {
        removeFromList(this.redirectLogoutUrl, uri);
    }

    public void removeClientBackLogoutURI(String uri) {
        removeFromList(this.clientBackChannellogoutUris, uri);
    }

    public void removeClaimRedirectURI(String uri) {
        removeFromList(this.claimRedirectURIList, uri);
    }
    
    public void removeClientResource(String uri) {
        resourceSetService.removeResource(uri);
    }
    
    public void removeAdditionalAudience(String uri) {
        removeFromList(this.additionalAudienceList, uri);
    }

    public void removePostAuthnScript(CustomScript script) {
        postAuthnScripts.remove(script);
    }

    public void removeRptClaimsScript(CustomScript script) {
        rptClaimsScripts.remove(script);
    }

    public void removeConsentScript(CustomScript script) {
        consentScripts.remove(script);
    }

    public void removeSpontaneousScript(CustomScript script) {
        spontaneousScopesScripts.remove(script);
    }

    public void removeIntrospectionScript(CustomScript script) {
        introspectionScripts.remove(script);
    }

    public void removeContact(String contact) {
        if (StringUtils.isEmpty(contact)) {
            return;
        }
        for (Iterator<String> iterator = contacts.iterator(); iterator.hasNext();) {
            String tmpContact = iterator.next();
            if (contact.equals(tmpContact)) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeRequestUri(String requestUri) {
        if (StringUtils.isEmpty(requestUri)) {
            return;
        }
        for (Iterator<String> iterator = requestUris.iterator(); iterator.hasNext();) {
            String tmpRequestUri = iterator.next();
            if (requestUri.equals(tmpRequestUri)) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeAuthorizedOrigin(String authorizedOrigin) {
        if (StringUtils.isEmpty(authorizedOrigin)) {
            return;
        }
        for (Iterator<String> iterator = authorizedOrigins.iterator(); iterator.hasNext();) {
            String tmpAuthorizationOrigin = iterator.next();
            if (authorizedOrigin.equals(tmpAuthorizationOrigin)) {
                iterator.remove();
                break;
            }
        }
    }

    private void removeFromList(List<String> uriList, String uri) {
        if (StringUtils.isEmpty(uri)) {
            return;
        }
        for (Iterator<String> iterator = uriList.iterator(); iterator.hasNext();) {
            String tmpUri = iterator.next();
            if (uri.equals(tmpUri)) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeScope(String inum) {
        if (StringHelper.isEmpty(inum)) {
            return;
        }
        for (Scope scope : this.scopes) {
            if (scope.getInum().equalsIgnoreCase(inum)) {
                this.scopes.remove(scope);
                break;
            }
        }
    }

    private void addClaim(GluuAttribute claim) {
        DisplayNameEntry oneClaim = new DisplayNameEntry(claim.getDn(), claim.getInum(), claim.getDisplayName());
        this.claims.add(oneClaim);
    }

    public void removeClaim(String inum) throws Exception {
        if (StringHelper.isEmpty(inum)) {
            return;
        }
        String removeClaimDn = attributeService.getDnForAttribute(inum);
        for (Iterator<DisplayNameEntry> iterator = this.claims.iterator(); iterator.hasNext();) {
            DisplayNameEntry oneClaim = iterator.next();
            if (removeClaimDn.equals(oneClaim.getDn())) {
                iterator.remove();
                break;
            }
        }
    }

    public void acceptSelectLoginUri() {
        if (StringHelper.isEmpty(this.availableLoginUri)) {
            return;
        }
        if (availableLoginUri.contains("#")) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Fragment is not allowed in URI.");
            this.availableLoginUri = HTTPS;
            return;
        }
		if (!this.loginUris.contains(this.availableLoginUri) && checkWhiteListRedirectUris(availableLoginUri)
				&& checkBlackListRedirectUris(availableLoginUri)) {
			boolean acceptable = isAcceptable(this.availableLoginUri);
			if (acceptable) {
				this.loginUris.add(this.availableLoginUri);
			} else {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "A sector identifier must be defined first.");
			}
		} else {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "The URL is not valid or may be Blacklisted.",
					"The URL is not valid or may be Blacklisted.");
		}
        this.availableLoginUri = HTTPS;
    }
    
    private boolean isAcceptable(String availableLoginUri) {
        boolean result = false;
        try {
                if (getProtocol(availableLoginUri).equalsIgnoreCase("http") 
                		&& this.client.getOxAuthAppType().equals(OxAuthApplicationType.NATIVE) 
                		&& isImplicitFlow()) {
                    return true;
                }                
            	if(!availableLoginUri.contains("//") && domainPattern.matcher(availableLoginUri.split("/")[0]).matches()) {
            		return true;
            	}
            	if(availableLoginUri.startsWith("schema://")|| availableLoginUri.startsWith("appschema://")) {
            		return true;
            	}
                if (this.client.getSubjectType().equals(OxAuthSubjectType.PUBLIC)) {
                    return true;
                } else if (this.loginUris.size() < 1) {
                    result = true;
                } else if (this.loginUris.size() >= 1 && availableLoginUri.startsWith("https://") &&  hasSameHostname(this.availableLoginUri)) {
                    result = true;
                } else if (this.loginUris.size() >= 1 && availableLoginUri.startsWith("https://") && !hasSameHostname(this.availableLoginUri) && sectorExist()) {
                    result = true;
                }else if (this.loginUris.size() >= 1 && availableLoginUri.startsWith("http://") && !hasSameHostname(this.availableLoginUri) && sectorExist()) {
                    result = true;
                }else if (this.loginUris.size() >= 1 && !(availableLoginUri.startsWith("https://") || availableLoginUri.startsWith("http://")) && availableLoginUri.contains(":/")) {
                    result = true;
                }
      
        } catch (MalformedURLException e) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "The url is malformed", "The url is malformed");
            log.error(e.getMessage());
        }
        return result;
    }

    private boolean isImplicitFlow() {
        if (this.grantTypes.contains(GrantType.IMPLICIT)) {
            return true;
        }
        return false;
    }

    private boolean hasSameHostname(String url1) throws MalformedURLException {
        boolean result = true;
        URL uri1 = new URL(url1);
        for (String url : this.loginUris) {
        	if(url.startsWith(HTTPS)) {
	            URL uri = new URL(url);
	            if (!(uri1.getHost().equalsIgnoreCase(uri.getHost()))) {
	                result = false;
	                break;
	            }
        	}
        }
        return result;
    }

    private String getHostname(String url) {
        URL uri1;
        try {
            uri1 = new URL(url);
            return uri1.getHost();
        } catch (MalformedURLException e) {
            return null;
        }

    }

    private String getProtocol(String url) throws MalformedURLException {
        URL uri1 = new URL(url);
        return uri1.getProtocol();
    }

    private boolean sectorExist() {
        boolean result = false;
        String sectorUri = this.client.getSectorIdentifierUri();
        try {
            if (sectorUri != null && !sectorUri.isEmpty()) {
                JSONArray json = new JSONArray(IOUtils.toString(new URL(sectorUri), Charset.forName("UTF-8")));
                if (json != null) {
                    result = true;
                }
            }
        } catch (MalformedURLException e) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "The url of the sector assigned to this client is malformed",
                    "The url of the sector assigned to this client is malformed");
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public void acceptSelectClaims() {
        if (this.availableClaims == null) {
            return;
        }
        Set<String> addedClaimInums = new HashSet<String>();
        for (DisplayNameEntry claim : claims) {
            addedClaimInums.add(claim.getInum());
        }

        for (GluuAttribute aClaim : this.availableClaims) {
            if (aClaim.isSelected() && !addedClaimInums.contains(aClaim.getInum())) {
                addClaim(aClaim);
            }
        }
        this.searchAvailableClaimPattern = "";
    }

    public void acceptSelectLogoutUri() {
        if (StringHelper.isEmpty(this.availableLogoutUri)) {
            return;
        }
        if (this.availableLogoutUri.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.logoutUris.contains(this.availableLogoutUri) && checkWhiteListRedirectUris(availableLogoutUri)
                && checkBlackListRedirectUris(availableLogoutUri)) {
            this.logoutUris.add(this.availableLogoutUri);
        } else {
            facesMessages.add(FacesMessage.SEVERITY_ERROR, "The URL is not valid or may be Blacklisted.");
        }
        this.availableLogoutUri = HTTPS;
    }

    public void acceptSelectRedirectLogoutUrl() {
        if (StringHelper.isEmpty(this.availableRedirectLogoutUrl)) {
            return;
        }
        if (this.availableRedirectLogoutUrl.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.redirectLogoutUrl.contains(this.availableRedirectLogoutUrl)) {
            this.redirectLogoutUrl.add(this.availableRedirectLogoutUrl);
        }
        this.availableRedirectLogoutUrl = HTTPS;
    }
    
    public void acceptSelectClientLogoutUri() {
        if (StringHelper.isEmpty(this.availableClientlogoutUri)) {
            return;
        }
        if (this.availableClientlogoutUri.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.clientlogoutUris.contains(this.availableClientlogoutUri)) {
            this.clientlogoutUris.add(this.availableClientlogoutUri);
        }
        this.availableClientlogoutUri = HTTPS;
    }
    
    public void acceptSelectClientBackLogoutUri() {
        if (StringHelper.isEmpty(this.availableClientBacklogoutUri)) {
            return;
        }
        if (this.availableClientBacklogoutUri.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.clientBackChannellogoutUris.contains(this.availableClientBacklogoutUri)) {
            this.clientBackChannellogoutUris.add(this.availableClientBacklogoutUri);
        }
        this.availableClientBacklogoutUri = HTTPS;
    }

    public void acceptSelectClaimRedirectUri() {
        if (StringHelper.isEmpty(this.availableClaimRedirectUri)) {
            return;
        }
        if (this.availableClaimRedirectUri.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.claimRedirectURIList.contains(this.availableClaimRedirectUri)) {
            this.claimRedirectURIList.add(this.availableClaimRedirectUri);
        }
        this.availableClaimRedirectUri = HTTPS;
    }
    
    //method for audience
    public void acceptselectedAdditionalAudiences() {
        if (StringHelper.isEmpty(this.availableAdditionalAudience)) {
            return;
        }
        
        if (!this.additionalAudienceList.contains(this.availableAdditionalAudience)) {
            this.additionalAudienceList.add(this.availableAdditionalAudience);
        }
        this.availableAdditionalAudience = "";
    }

    public void acceptSelectContact() {
        if (StringHelper.isEmpty(this.availableContact)) {
            return;
        }
        if (!contacts.contains((availableContact))) {
            contacts.add(availableContact);
        }
        this.availableContact = "";
    }

    public void acceptSelectRequestUri() {
        if (StringHelper.isEmpty(this.availableRequestUri)) {
            return;
        }
        if (this.availableRequestUri.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.requestUris.contains(this.availableRequestUri)) {
            this.requestUris.add(this.availableRequestUri);
        }
        this.availableRequestUri = HTTPS;
    }

    public void acceptSelectAuthorizedOrigin() {
        if (StringHelper.isEmpty(this.availableAuthorizedOrigin)) {
            return;
        }
        if (this.availableAuthorizedOrigin.equalsIgnoreCase(HTTPS)) {
            return;
        }
        if (!this.authorizedOrigins.contains(this.availableAuthorizedOrigin)) {
            this.authorizedOrigins.add(this.availableAuthorizedOrigin);
        }
        this.availableAuthorizedOrigin = HTTPS;
    }

    public void cancelIntrospectionScripts() {
    }

    public void cancelPostAuthnScripts() {
    }

    public void cancelRptClaimsScripts() {
    }

    public void cancelConsentScripts() {
    }

    public void cancelSpontaneousScopesScripts() {
    }

    public void cancelSelectClaims() {
    }

    public void cancelSelectGroups() {
    }

    public void cancelSelectLoginUri() {
        this.availableLoginUri = HTTPS;
    }

    public void cancelSelectLogoutUri() {
        this.availableLogoutUri = HTTPS;
    }

    public void cancelClientLogoutUri() {
        this.availableClientlogoutUri = HTTPS;
    }
    
    public void cancelRedirectLogoutUrl() {
        this.availableRedirectLogoutUrl = HTTPS;
    }
    
    public void cancelClientBackLogoutUri() {
        this.availableClientBacklogoutUri = HTTPS;
    }

    public void cancelClaimRedirectUri() {
        this.availableClaimRedirectUri = HTTPS;
    }
    
    public void cancelAdditionalAudience() {
        this.availableAdditionalAudience = "";
    }

    public void cancelSelectContact() {
        this.availableContact = "";
    }

    public void cancelSelectDefaultAcrValue() {
    }

    public void cancelSelectRequestUri() {
    }

    public void cancelSelectAuthorizedOrigin() {
    }

    private void updateLoginURIs() {
        if (this.loginUris == null || this.loginUris.size() == 0) {
            this.client.setOxAuthRedirectURIs(null);
            return;
        }
        List<String> tmpUris = new ArrayList<String>();
        for (String uri : this.loginUris) {
            tmpUris.add(StringHelper.trimAll(uri));
        }
        this.client.setOxAuthRedirectURIs(tmpUris);
    }
    
    private void updateLogoutURIs() {
        if (this.clientlogoutUris == null || this.clientlogoutUris.size() == 0) {
            this.client.setLogoutUri(null);
            return;
        }
        List<String> tmpUris = new ArrayList<String>();
        for (String uri : this.clientlogoutUris) {
            tmpUris.add(StringHelper.trimAll(uri));
        }
        this.client.setLogoutUri(tmpUris);
    }
    
    private void updateRedirectLogoutUrls() {
        if (this.redirectLogoutUrl == null || this.redirectLogoutUrl.size() == 0) {
            this.client.setOxAuthPostLogoutRedirectURIs(null);
            return;
        }
        List<String> tmpUris = new ArrayList<String>();
        for (String uri : this.redirectLogoutUrl) {
            tmpUris.add(StringHelper.trimAll(uri));
        }
        this.client.setOxAuthPostLogoutRedirectURIs(tmpUris);
    }
    
    private void updateBackChannelLogoutURIs() {
        if (this.clientBackChannellogoutUris == null || this.clientBackChannellogoutUris.size() == 0) {
        	client.getAttributes().setBackchannelLogoutUri(new ArrayList<String>());
            return;
        }
        List<String> tmpUris = new ArrayList<String>();
        for (String uri : this.clientBackChannellogoutUris) {
            tmpUris.add(StringHelper.trimAll(uri));
        }
        
        client.getAttributes().getBackchannelLogoutUri().clear();
        client.getAttributes().getBackchannelLogoutUri().addAll(tmpUris);
    }
    
    private void updateBackchannelLogoutUri() {
    	if(client.getAttributes().getBackchannelLogoutUri() == null) {
    		client.getAttributes().setBackchannelLogoutUri(new ArrayList<String>());
    	}
    	
    	if(!client.getAttributes().getBackchannelLogoutUri().contains(backchannelLogoutUri.trim())) {    	
        client.getAttributes().getBackchannelLogoutUri().add(backchannelLogoutUri);
    	}
        
    }

    private void updateContacts() {
        validateContacts();
        if (contacts == null || contacts.size() == 0) {
            client.setContacts(null);
            return;
        }
        List<String> tmpContacts = new ArrayList<String>();
        for (String contact : contacts) {
            tmpContacts.add(contact);
        }
        client.setContacts(tmpContacts);
    }

    private void updateRequestUris() {
        if (requestUris == null || requestUris.size() == 0) {
            client.setRequestUris(null);
            return;
        }
        List<String> tmpRequestUris = new ArrayList<String>();
        for (String requestUri : requestUris) {
            tmpRequestUris.add(StringHelper.trimAll(requestUri));
        }
        client.setRequestUris(tmpRequestUris.toArray(new String[tmpRequestUris.size()]));
    }

    private void updateAuthorizedOrigins() {
        if (authorizedOrigins == null || authorizedOrigins.size() == 0) {
            client.setAuthorizedOrigins(null);
            return;
        }
        List<String> tmpAuthorizedOrigins = new ArrayList<String>();
        for (String authorizedOrigin : authorizedOrigins) {
            tmpAuthorizedOrigins.add(StringHelper.trimAll(authorizedOrigin));
        }
        client.setAuthorizedOrigins(tmpAuthorizedOrigins.toArray(new String[tmpAuthorizedOrigins.size()]));
    }

    private void updateClaimredirectUri() {
        if (claimRedirectURIList == null || claimRedirectURIList.size() == 0) {
            client.setClaimRedirectURI(null);
            return;
        }
        List<String> tmpClaimRedirectURI = new ArrayList<String>();
        for (String claimRedirectURI : claimRedirectURIList) {
            tmpClaimRedirectURI.add(StringHelper.trimAll(claimRedirectURI));
        }
        client.setClaimRedirectURI(tmpClaimRedirectURI.toArray(new String[tmpClaimRedirectURI.size()]));
    }
    
    private void updateAdditionalAudience() {
        if (additionalAudienceList == null || additionalAudienceList.size() == 0) {
            client.getAttributes().setAdditionalAudience(null);
            return;
        }
        List<String> tmpAdditionalAudience = new ArrayList<String>();
        for (String additionalAudience : additionalAudienceList) {
        	tmpAdditionalAudience.add(StringHelper.trimAll(additionalAudience));
        }
        client.getAttributes().setAdditionalAudience(tmpAdditionalAudience);
        
    }

    private void updateClaims() {
        if (this.claims == null || this.claims.size() == 0) {
            this.client.setOxAuthClaims(null);
            return;
        }
        List<String> tmpClaims = new ArrayList<String>();
        for (DisplayNameEntry claim : this.claims) {
            tmpClaims.add(claim.getDn());
        }
        this.client.setOxAuthClaims(tmpClaims);
    }

    private void updateResponseTypes() {
        List<ResponseType> currentResponseTypes = this.responseTypes;
        if (currentResponseTypes == null || currentResponseTypes.size() == 0) {
            this.client.setResponseTypes(null);
            return;
        }
        this.client.setResponseTypes(currentResponseTypes.toArray(new ResponseType[currentResponseTypes.size()]));
    }

    private void updateScopes() {
        List<Scope> currentResponseTypes = this.scopes;
        if (currentResponseTypes == null || currentResponseTypes.size() == 0) {
            this.client.setOxAuthScopes(null);
            return;
        }
        List<String> scopes = new ArrayList<String>();
        for (Scope scope : this.scopes) {
            scopes.add(scope.getDn());
        }
        this.client.setOxAuthScopes(scopes);
    }

    private void updateGrantTypes() {
        List<GrantType> currentGrantTypes = this.grantTypes;
        if (currentGrantTypes == null || currentGrantTypes.size() == 0) {
            this.client.setGrantTypes(null);
            return;
        }
        this.client.setGrantTypes(currentGrantTypes.toArray(new GrantType[currentGrantTypes.size()]));
    }

    private void updateCustomScripts() {
    	if(!this.client.getAttributes().getPostAuthnScripts().contains(customScriptsforPostAuthn)
    			&&  !customScriptsforPostAuthn.isEmpty()) {
    		this.client.getAttributes().getPostAuthnScripts().add(customScriptsforPostAuthn);
    	}
    	if(!this.client.getAttributes().getRptClaimsScripts().contains(rptClaimsScript)
    			&&  !rptClaimsScript.isEmpty()) {
    		this.client.getAttributes().getRptClaimsScripts().add(rptClaimsScript);
    	}
    	if(!this.client.getAttributes().getConsentGatheringScripts().contains(customScriptsforConsentGather)
    			&&  !customScriptsforConsentGather.isEmpty()) {
    		this.client.getAttributes().getConsentGatheringScripts().add(customScriptsforConsentGather);
    	}
    	if(!this.client.getAttributes().getIntrospectionScripts().contains(introspectionCustomScript)
    			&&  !introspectionCustomScript.isEmpty()) {
            this.client.getAttributes().getIntrospectionScripts().clear();
    		this.client.getAttributes().getIntrospectionScripts().add(introspectionCustomScript);
    	}
    	if(!this.client.getAttributes().getSpontaneousScopeScriptDns().contains(spontaneousScopeCustomScript)
    			&&  !spontaneousScopeCustomScript.isEmpty()) {
    		this.client.getAttributes().getSpontaneousScopeScriptDns().add(spontaneousScopeCustomScript);
    	}
    	
    }

    public void selectAddedClaims() {
        if (this.availableClaims == null) {
            return;
        }
        Set<String> addedClaimInums = new HashSet<String>();
        for (DisplayNameEntry claim : this.claims) {
            addedClaimInums.add(claim.getInum());
        }
        for (GluuAttribute aClaim : this.availableClaims) {
            aClaim.setSelected(addedClaimInums.contains(aClaim.getInum()));
        }
    }

    public void searchAvailableClaims() {
        if (Util.equals(this.oldSearchAvailableClaimPattern, this.searchAvailableClaimPattern)) {
            return;
        }
        try {
            this.availableClaims = attributeService.searchAttributes(this.searchAvailableClaimPattern,
                    OxTrustConstants.searchClientsSizeLimit);
            this.oldSearchAvailableClaimPattern = this.searchAvailableClaimPattern;
            selectAddedClaims();
        } catch (Exception ex) {
            log.error("Failed to find attributes", ex);
        }
    }

    private List<DisplayNameEntry> getInitialClaimDisplayNameEntries() throws Exception {
        List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
        if ((client.getOxAuthClaims() == null) || (client.getOxAuthClaims().size() == 0)) {
            return result;
        }
        List<AttributeDisplayNameEntry> tmp = lookupService.getDisplayNameEntries(attributeService.getDnForAttribute(null),
        		AttributeDisplayNameEntry.class, this.client.getOxAuthClaims());
        if (tmp != null) {
            result.addAll(tmp);
        }
        return result;
    }

    private List<ResponseType> getInitialResponseTypes() {
        List<ResponseType> result = new ArrayList<ResponseType>();
        ResponseType[] currentResponseTypes = this.client.getResponseTypes();
        if ((currentResponseTypes == null) || (currentResponseTypes.length == 0)) {
            return result;
        }
        result.addAll(Arrays.asList(currentResponseTypes));
        return result;
    }

    private List<GrantType> getInitialGrantTypes() {
        List<GrantType> result = new ArrayList<GrantType>();
        GrantType[] currentGrantTypes = this.client.getGrantTypes();
        if (currentGrantTypes == null || currentGrantTypes.length == 0) {
            return result;
        }
        result.addAll(Arrays.asList(currentGrantTypes));
        return result;
    }

    public void acceptSelectResponseTypes() {
        List<ResponseType> addedResponseTypes = getResponseTypes();
        for (SelectableEntity<ResponseType> availableResponseType : this.availableResponseTypes) {
            ResponseType responseType = availableResponseType.getEntity();
            if (availableResponseType.isSelected() && !addedResponseTypes.contains(responseType)) {
                addResponseType(responseType.getValue());
            }
            if (!availableResponseType.isSelected() && addedResponseTypes.contains(responseType)) {
                removeResponseType(responseType.getValue());
            }
        }
    }

    public void cancelSelectSectors() {

    }

    public void acceptSelectCustomScripts() {
        List<CustomScript> addedCustomScripts = getCustomScripts();
        for (SelectableEntity<CustomScript> availableCustomScript : this.availableCustomScripts) {
            CustomScript customScript = availableCustomScript.getEntity();
            if (availableCustomScript.isSelected() && !addedCustomScripts.contains(customScript)) {
                addCustomScript(customScript.getName());
            }
            if (!availableCustomScript.isSelected() && addedCustomScripts.contains(customScript)) {
                removeCustomScript(customScript.getName());
            }
        }
    }

    public void acceptIntrospectionScripts() {
        introspectionScripts.clear();
        introspectionScripts.addAll(availableIntrospectionScripts.stream().filter(SelectableEntity::isSelected)
                .map(SelectableEntity::getEntity).collect(Collectors.toList()));
    }

    public void acceptPostAuthnScripts() {
        postAuthnScripts.clear();
        postAuthnScripts.addAll(availablePostAuthnScripts.stream().filter(SelectableEntity::isSelected)
                .map(SelectableEntity::getEntity).collect(Collectors.toList()));
    }

    public void acceptRptClaimsScripts() {
        rptClaimsScripts.clear();
        rptClaimsScripts.addAll(availableRptClaimsScripts.stream().filter(SelectableEntity::isSelected)
                .map(SelectableEntity::getEntity).collect(Collectors.toList()));
    }

    public void acceptConsentScripts() {
        consentScripts.clear();
        consentScripts.addAll(availableConsentScripts.stream().filter(SelectableEntity::isSelected)
                .map(SelectableEntity::getEntity).collect(Collectors.toList()));
    }

    public void acceptSpontaneousScopesScripts() {
        spontaneousScopesScripts.clear();
        spontaneousScopesScripts.addAll(availableSpontaneousScripts.stream().filter(SelectableEntity::isSelected)
                .map(SelectableEntity::getEntity).collect(Collectors.toList()));
    }

    public void acceptSelectScopes() {
        List<Scope> addedScopes = getScopes();
        for (SelectableEntity<Scope> availableScope : this.availableScopes) {
            Scope scope = availableScope.getEntity();
            if (availableScope.isSelected() && !contain(addedScopes, scope)) {
                addScope(scope.getInum());
            }
            if (!availableScope.isSelected() && addedScopes.contains(scope)) {
                removeScope(scope.getInum());
            }
        }
    }

    private boolean contain(List<Scope> scopes, Scope element) {
        boolean found = false;
        for (Scope scope : scopes) {
            if (scope.getInum().equalsIgnoreCase(element.getInum())) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void addScope(String inum) {
        if (StringHelper.isEmpty(inum)) {
            return;
        }
        Scope addScope = new Scope();
        try {
            addScope = scopeService.getScopeByInum(inum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (addScope != null) {
            this.scopes.add(addScope);
        }
    }

    private void addCustomScript(String name) {
        if (StringHelper.isEmpty(name)) {
            return;
        }
        CustomScript addCustomScript = new CustomScript();
        addCustomScript.setName(name);
        if (addCustomScript != null) {
            this.customScripts.add(addCustomScript);
        }
    }

    public void removeCustomScript(String value) {
        if (StringHelper.isEmpty(value)) {
            return;
        }
        for (CustomScript customScript : customScripts) {
            if (customScript.getName().equalsIgnoreCase(value)) {
                this.customScripts.remove(customScript);
                break;
            }
        }
    }

    public void acceptSelectGrantTypes() {
        List<GrantType> addedGrantTypes = getGrantTypes();
        for (SelectableEntity<GrantType> availableGrantType : this.availableGrantTypes) {
            GrantType grantType = availableGrantType.getEntity();
            if (availableGrantType.isSelected() && !addedGrantTypes.contains(grantType)) {
                addGrantType(grantType.toString());
            }
            if (!availableGrantType.isSelected() && addedGrantTypes.contains(grantType)) {
                removeGrantType(grantType.toString());
            }
        }
    }

    public void cancelSelectCustomScripts() {
    }

    public void cancelSelectScopes() {
    }

    public void cancelSelectResponseTypes() {
    }

    public void cancelSelectGrantTypes() {
    }

    public void addResponseType(String value) {
        if (StringHelper.isEmpty(value)) {
            return;
        }
        ResponseType addResponseType = ResponseType.getByValue(value);
        if (addResponseType != null) {
            this.responseTypes.add(addResponseType);
        }
    }

    public void addGrantType(String value) {
        if (StringHelper.isEmpty(value)) {
            return;
        }
        GrantType addGrantType = GrantType.fromString(value);
        if (addGrantType != null) {
            this.grantTypes.add(addGrantType);
        }
    }

    public void removeResponseType(String value) {
        if (StringHelper.isEmpty(value)) {
            return;
        }
        ResponseType removeResponseType = ResponseType.getByValue(value);
        if (removeResponseType != null) {
            this.responseTypes.remove(removeResponseType);
        }
    }

    public void removeGrantType(String value) {
        if (StringHelper.isEmpty(value)) {
            return;
        }
        GrantType removeGrantType = GrantType.fromString(value);
        if (removeGrantType != null) {
            this.grantTypes.remove(removeGrantType);
        }
    }

    public void searchAvailableResponseTypes() {
        if (this.availableResponseTypes != null) {
            selectAddedResponseTypes();
            return;
        }

        List<SelectableEntity<ResponseType>> tmpAvailableResponseTypes = new ArrayList<SelectableEntity<ResponseType>>();

        for (ResponseType responseType : ResponseType.values()) {
            tmpAvailableResponseTypes.add(new SelectableEntity<ResponseType>(responseType));
        }

        this.availableResponseTypes = tmpAvailableResponseTypes;
        selectAddedResponseTypes();
    }

    public void searchAvailableCustomScripts() {
        if (this.availableCustomScripts != null) {
            selectAddedCustomScripts();
            return;
        }
        this.availableCustomScripts = getSelectableScripts(CustomScriptType.PERSON_AUTHENTICATION);
    }
    
    public void searchAvailableCustomScriptsforAcr() {        
        this.availableCustomScriptsAcr = getScripts(CustomScriptType.PERSON_AUTHENTICATION);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforPostAuthn() {        
        return getScripts(CustomScriptType.POST_AUTHN);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforConsentGath() {        
        return getScripts(CustomScriptType.CONSENT_GATHERING);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforSpontaneousScope() {        
        return getScripts(CustomScriptType.SPONTANEOUS_SCOPE);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforintrospection() {        
        return getScripts(CustomScriptType.INTROSPECTION);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforrptClaimsScripts() {        
        return getScripts(CustomScriptType.UMA_RPT_CLAIMS);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforUpdateTokens() {        
        return getScripts(CustomScriptType.UPDATE_TOKEN);
    }
    
    public List<CustomScript> searchAvailableCustomScriptsforRptModifications() {        
        return getScripts(CustomScriptType.UMA_RPT_POLICY);
    }

    public List<SelectableEntity<CustomScript>> searchAvailableIntrospectionCustomScripts() {
        if (availableIntrospectionScripts != null) {
            SelectableEntityHelper.select(availableIntrospectionScripts, introspectionScripts);
            return availableIntrospectionScripts;
        }
        availableIntrospectionScripts = getSelectableScripts(CustomScriptType.INTROSPECTION);
        SelectableEntityHelper.select(availableIntrospectionScripts, introspectionScripts);
        return availableIntrospectionScripts;
    }

    public List<SelectableEntity<CustomScript>> searchAvailablePostAuthnCustomScripts() {
        if (availablePostAuthnScripts != null) {
            SelectableEntityHelper.select(availablePostAuthnScripts, postAuthnScripts);
            return availablePostAuthnScripts;
        }
        availablePostAuthnScripts = getSelectableScripts(CustomScriptType.POST_AUTHN);
        SelectableEntityHelper.select(availablePostAuthnScripts, postAuthnScripts);
        return availablePostAuthnScripts;
    }

    public List<SelectableEntity<CustomScript>> searchAvailableRptClaimsCustomScripts() {
        if (availableRptClaimsScripts != null) {
            SelectableEntityHelper.select(availableRptClaimsScripts, rptClaimsScripts);
            return availableRptClaimsScripts;
        }
        availableRptClaimsScripts = getSelectableScripts(CustomScriptType.UMA_RPT_CLAIMS);
        SelectableEntityHelper.select(availableRptClaimsScripts, rptClaimsScripts);
        return availableRptClaimsScripts;
    }

    public List<SelectableEntity<CustomScript>> searchAvailableConsentCustomScripts() {
        if (availableConsentScripts != null) {
            SelectableEntityHelper.select(availableConsentScripts, consentScripts);
            return availableConsentScripts;
        }
        availableConsentScripts = getSelectableScripts(CustomScriptType.CONSENT_GATHERING);
        SelectableEntityHelper.select(availableConsentScripts, consentScripts);
        return availableConsentScripts;
    }

    public List<SelectableEntity<CustomScript>> searchAvailableSpontaneousScopeScripts() {
        if (availableSpontaneousScripts != null) {
            SelectableEntityHelper.select(availableSpontaneousScripts, spontaneousScopesScripts);
            return availableSpontaneousScripts;
        }
        availableSpontaneousScripts = getSelectableScripts(CustomScriptType.SPONTANEOUS_SCOPE);
        SelectableEntityHelper.select(availableSpontaneousScripts, spontaneousScopesScripts);
        return availableSpontaneousScripts;
    }

    public List<SelectableEntity<CustomScript>> getSelectableScripts(CustomScriptType type) {
        return getScripts(type).stream().map(SelectableEntity::new).collect(Collectors.toList());
    }

    public List<CustomScript> getScripts(CustomScriptType type) {
        return customScriptService.findCustomScripts(Lists.newArrayList(type));
    }

    public void searchAvailableScopes() {
		/*
		 * if (this.availableScopes != null) { selectAddedScopes(); return; }
		 */
        List<SelectableEntity<Scope>> tmpAvailableScopes = new ArrayList<SelectableEntity<Scope>>();
        List<Scope> scopes = new ArrayList<Scope>();
        try {
        	if(scopePattern !=  null && !scopePattern.isEmpty()) {
        		scopes = scopeService.searchScopes(scopePattern, 0);
        	}else {
        		scopes = scopeService.getAllScopesList(1000);
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Scope scope : scopes) {
            tmpAvailableScopes.add(new SelectableEntity<Scope>(scope));
        }
        this.availableScopes = tmpAvailableScopes;
        selectAddedScopes();
    }

    public void searchAvailableGrantTypes() {
        if (this.availableGrantTypes != null) {
            selectAddedGrantTypes();
            return;
        }
        List<SelectableEntity<GrantType>> tmpAvailableGrantTypes = new ArrayList<SelectableEntity<GrantType>>();
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.AUTHORIZATION_CODE));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.IMPLICIT));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.REFRESH_TOKEN));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.CLIENT_CREDENTIALS));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.OXAUTH_UMA_TICKET));
        tmpAvailableGrantTypes.add(new SelectableEntity<GrantType>(GrantType.DEVICE_CODE));
        this.availableGrantTypes = tmpAvailableGrantTypes;
        selectAddedGrantTypes();
    }

    private void selectAddedResponseTypes() {
        List<ResponseType> addedResponseTypes = getResponseTypes();
        for (SelectableEntity<ResponseType> availableResponseType : this.availableResponseTypes) {
            availableResponseType.setSelected(addedResponseTypes.contains(availableResponseType.getEntity()));
        }
    }

    public void selectAddedScopes() {
        List<String> ids = getScopes().stream().map(item -> item.getId()).collect(Collectors.toList());
        availableScopes.stream().forEach(item -> {
            item.setSelected(ids.contains(item.getEntity().getId()));
        });
    }

    private void selectAddedCustomScripts() {
        List<CustomScript> addedCustomScripts = getCustomScripts();
        for (SelectableEntity<CustomScript> availableCustomScript : this.availableCustomScripts) {
            availableCustomScript.setSelected(addedCustomScripts.contains(availableCustomScript.getEntity()));
        }
    }

    private void selectAddedGrantTypes() {
        List<GrantType> addedGrantTypes = getGrantTypes();
        for (SelectableEntity<GrantType> availableGrantType : this.availableGrantTypes) {
            availableGrantType.setSelected(addedGrantTypes.contains(availableGrantType.getEntity()));
        }
    }

    public List<String> getClaimRedirectURIList() {
        return claimRedirectURIList;
    }

    public void setClaimRedirectURIList(List<String> claimRedirectURIList) {
        this.claimRedirectURIList = claimRedirectURIList;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public OxAuthClient getClient() {
        return client;
    }

    public boolean isUpdate() {
        return update;
    }

    public String getAvailableLoginUri() {
        return availableLoginUri;
    }

    public void setAvailableLoginUri(String availableLoginUri) {
        this.availableLoginUri = availableLoginUri;
    }

    public String getAvailableLogoutUri() {
        return availableLogoutUri;
    }

    public void setAvailableLogoutUri(String availableLogoutUri) {
        this.availableLogoutUri = availableLogoutUri;
    }

    public String getAvailableContact() {
        return availableContact;
    }

    public void setAvailableContact(String availableContact) {
        this.availableContact = availableContact;
    }

    public String getAvailableRequestUri() {
        return availableRequestUri;
    }

    public String availableAuthorizedOrigin() {
        return availableAuthorizedOrigin;
    }

    public void setAvailableRequestUri(String availableRequestUri) {
        this.availableRequestUri = availableRequestUri;
    }

    public List<SelectableEntity<Scope>> getAvailableScopes() {
        return this.availableScopes;
    }

    public List<GluuAttribute> getAvailableClaims() {
        return this.availableClaims;
    }

    public List<GluuGroup> getAvailableGroups() {
        return this.availableGroups;
    }

    public List<SelectableEntity<ResponseType>> getAvailableResponseTypes() {
        return this.availableResponseTypes;
    }

    public List<SelectableEntity<CustomScript>> getAvailableCustomScripts() {
        return this.availableCustomScripts;
    }

    public List<CustomScript> getPostAuthnScripts() {
        return postAuthnScripts;
    }

    public List<CustomScript> getRptClaimsScripts() {
        return rptClaimsScripts;
    }

    public List<CustomScript> getConsentScripts() {
        return consentScripts;
    }

    public List<CustomScript> getSpontaneousScopesScripts() {
        return spontaneousScopesScripts;
    }

    public List<CustomScript> getIntrospectionScripts() {
        return introspectionScripts;
    }

    public List<SelectableEntity<CustomScript>> getAvailablePostAuthnScripts() {
        return availablePostAuthnScripts;
    }

    public List<SelectableEntity<CustomScript>> getAvailableRptClaimsScripts() {
        return availableRptClaimsScripts;
    }

    public List<SelectableEntity<CustomScript>> getAvailableConsentScripts() {
        return availableConsentScripts;
    }

    public List<SelectableEntity<CustomScript>> getAvailableSpontaneousScopesScripts() {
        return availableSpontaneousScripts;
    }

    public List<SelectableEntity<CustomScript>> getAvailableIntrospectionScripts() {
        return availableIntrospectionScripts;
    }

    public List<SelectableEntity<GrantType>> getAvailableGrantTypes() {
        return this.availableGrantTypes;
    }

    public List<String> getLoginUris() {
        return loginUris;
    }

    public void setLoginUris(List<String> values) {
        this.loginUris = values;
    }

    public List<String> getLogoutUris() {
        return logoutUris;
    }

    public List<Scope> getScopes() {
        return this.scopes;
    }

    public List<DisplayNameEntry> getClaims() {
        return this.claims;
    }

    public List<ResponseType> getResponseTypes() {
        return responseTypes;
    }

    public List<CustomScript> getCustomScripts() {
        return customScripts;
    }

    public List<GrantType> getGrantTypes() {
        return grantTypes;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public List<String> getRequestUris() {
        return requestUris;
    }

    public List<String> getAuthorizedOrigins() {
        return authorizedOrigins;
    }

    public String getSearchAvailableClaimPattern() {
        return searchAvailableClaimPattern;
    }

    public void setSearchAvailableClaimPattern(String searchAvailableClaimPattern) {
        this.searchAvailableClaimPattern = searchAvailableClaimPattern;
    }

    public String getAvailableClientlogoutUri() {
        return availableClientlogoutUri;
    }

    public void setAvailableClientlogoutUri(String availableClientlogoutUri) {
        this.availableClientlogoutUri = availableClientlogoutUri;
    }

    public List<String> getClientlogoutUris() {
        return clientlogoutUris;
    }

    public void setClientlogoutUris(List<String> clientlogoutUris) {
        this.clientlogoutUris = clientlogoutUris;
    }

    private boolean checkWhiteListRedirectUris(String redirectUri) {
        try {
            boolean valid = true;
            List<String> whiteList = appConfiguration.getClientWhiteList();
            URLPatternList urlPatternList = new URLPatternList(whiteList);
            valid &= urlPatternList.isUrlListed(redirectUri);
            return valid;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkBlackListRedirectUris(String redirectUri) {
        try {
            boolean valid = true;
            List<String> blackList = appConfiguration.getClientBlackList();
            URLPatternList urlPatternList = new URLPatternList(blackList);
            valid &= !urlPatternList.isUrlListed(redirectUri);
            return valid;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean checkClientSecretRequired() {
        for (ResponseType responseType : this.responseTypes) {
            if (responseType.getValue().equalsIgnoreCase("token")
                    || responseType.getValue().equalsIgnoreCase("id_token")) {
                return false;
            }
        }
        for (GrantType grantType : this.grantTypes) {
            if (grantType.getValue().equalsIgnoreCase("implicit")) {
                return false;
            }
        }
        return true;
    }

    private void validateContacts() {
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        List<String> tmpContactsList = new ArrayList<String>();
        boolean shouldShowWarning = false;
        for (String contact : contacts) {
            if (pattern.matcher(contact).matches()) {
                tmpContactsList.add(contact);
            } else {
                shouldShowWarning = true;
            }
        }
        contacts.clear();
        contacts.addAll(tmpContactsList);
        if (shouldShowWarning) {
            facesMessages.add(FacesMessage.SEVERITY_WARN, "Invalid contacts have been removed from contacts list");
        }
    }

    public void generatePassword() throws EncryptionException {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String pwd = RandomStringUtils.random(40, characters);
        this.client.setOxAuthClientSecret(pwd);
        this.client.setEncodedClientSecret(encryptionService.encrypt(pwd));
    }

    public void setSecret(String pwd) throws EncryptionException {
        this.client.setOxAuthClientSecret(pwd);
        this.client.setEncodedClientSecret(encryptionService.encrypt(pwd));
    }

    public String getMarkDown() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(new Heading("OPENID CONNECT CLIENTS DETAILS", 2)).append("\n");
            List<Object> items = new ArrayList<Object>();
            if (client.getDisplayName() != null && !client.getDisplayName().isEmpty()) {
                items.add("**Name:** " + client.getDisplayName());
            }
            if (client.getDescription() != null && !client.getDescription().isEmpty()) {
                items.add("**Description:** " + client.getDescription());
            }

            if (client.getInum() != null && !client.getInum().isEmpty()) {
                items.add("**Client ID:** " + client.getInum());
            }

            if (client.getSubjectType() != null && !client.getSubjectType().name().isEmpty()) {
                items.add("**Subject Type:** " + client.getSubjectType());
            }

            if (client.getExp() != null && !client.getExp().toString().isEmpty()) {
                items.add("**Expirattion date:** " + client.getExp());
            }
            if (client.getOxAuthClientSecret() != null && !client.getOxAuthClientSecret().toString().isEmpty()) {
                items.add("**ClientSecret:** XXXXXXXXXXX");
            }

            if (client.getClientUri() != null && !client.getClientUri().toString().isEmpty()) {
                items.add("**Client Uri:** " + client.getClientUri());
            }
            if (client.getIdTokenTokenBindingCnf() != null
                    && !client.getIdTokenTokenBindingCnf().toString().isEmpty()) {
                items.add("**TokenTokenBindingCnf:** " + client.getIdTokenTokenBindingCnf());
            }
            if (client.getOxAuthAppType() != null) {
                items.add("**Application Type:** " + client.getOxAuthAppType().getValue());
            }
            items.add("**Persist Client Authorizations:** " + client.getOxAuthPersistClientAuthorizations());
            items.add("**Pre-Authorization:** " + client.getOxAuthTrustedClient());
            items.add("**Authentication method for the Token Endpoint:** " + client.getTokenEndpointAuthMethod());
            items.add("**Logout Session Required:** " + client.getLogoutSessionRequired());
            items.add("**Include Claims In Id Token:** " + client.getOxIncludeClaimsInIdToken());
            items.add("**Disabled:** " + client.isDisabled());
            if (client.getLogoutUri() != null && !client.getLogoutUri().isEmpty()) {
                items.add("**Logout Uri:** " + client.getLogoutUri().toString());
            }
            if (client.getOxAuthPostLogoutRedirectURIs() != null
                    && !client.getOxAuthPostLogoutRedirectURIs().isEmpty()) {
                items.add("**Logout Redirect URIs:** " + client.getOxAuthPostLogoutRedirectURIs().toString());
            }
            if (client.getOxAuthRedirectURIs() != null && !client.getOxAuthRedirectURIs().isEmpty()) {
                items.add("**Login Redirect URIs:** " + client.getOxAuthRedirectURIs().toString());
            }
            if (client.getOxAuthClaims() != null && !client.getOxAuthClaims().isEmpty()) {
                items.add("**Claims:** " + client.getOxAuthClaims().toString());
            }
            if (client.getAccessTokenSigningAlg() != null && !client.getAccessTokenSigningAlg().name().isEmpty()) {
                items.add("**AccessTokenSigningAlg:** " + client.getAccessTokenSigningAlg().name().toString());
            }
            if (client.getOxAuthScopes() != null && !client.getOxAuthScopes().isEmpty()) {
                List<String> scopes = new ArrayList<String>();
                for (Scope scope : this.scopes) {
                    scopes.add(scope.getId());
                }
                items.add("**Scopes:** " + scopes.toString());
            }
            if (client.getGrantTypes() != null && client.getGrantTypes().length > 0) {
                items.add("**Grant types:** " + this.grantTypes.toString());
            }

            if (client.getResponseTypes() != null && client.getResponseTypes().length > 0) {
                items.add("**Response types:** " + this.responseTypes.toString());
            }
            if (client.getContacts() != null && !client.getContacts().toString().isEmpty()) {
                items.add("**Contacts:** " + this.contacts.toString());
            }
            
            sb.append(new UnorderedList<Object>(items)).append("\n");
            markDown = sb.toString();
        } catch (Exception e) {
            log.error("Error computing markdown", e);
        }
        return markDown;
    }

    public void setMarkDown(String markDown) {
        this.markDown = markDown;
    }

    public String getOxAttributesJson() {
        return oxAttributesJson;
    }

    public void setOxAttributesJson(String oxAttributesJson) {
        this.oxAttributesJson = oxAttributesJson;
    }

    private String getClientAttributesJson() {
        if (client != null) {
            try {
                return new ObjectMapper().writeValueAsString(this.client.getAttributes());
            } catch (Exception e) {
                return "{}";
            }
        } else {
            return "{}";
        }
    }

    public void subjectTypeChanged() {
        if (this.client.getSubjectType().equals(OxAuthSubjectType.PAIRWISE)) {
            if (!sectorExist() && hasDifferentHostname()) {
                this.loginUris.clear();
            }
        }
        this.client.getSubjectType();
    }

    private boolean hasDifferentHostname() {
        long size = loginUris.stream().map(e -> getHostname(e)).distinct().count();
        if (size > 1) {
            return true;
        }
        return false;
    }

    private Date oneDay() {
        LocalDate nextCentury = LocalDate.now().plusDays(1);
        return Date.from(nextCentury.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void appTypeChanged() {

    }

    public OxAuthApplicationType[] getApplicationType() {
        return clientService.getApplicationType();
    }

    public OxAuthSubjectType[] getSubjectTypes() {
        return clientService.getSubjectTypes();
    }

    public SignatureAlgorithm[] getSignatureAlgorithmsWithoutNone() {
        return clientService.getSignatureAlgorithmsWithoutNone();
    }

    public SignatureAlgorithm[] getSignatureAlgorithms() {
        return clientService.getSignatureAlgorithms();
    }

    public KeyEncryptionAlgorithm[] getKeyEncryptionAlgorithms() {
        return clientService.getKeyEncryptionAlgorithms();
    }

    public BlockEncryptionAlgorithm[] getBlockEncryptionAlgorithms() {
        return clientService.getBlockEncryptionAlgorithms();
    }

    public AuthenticationMethod[] getAuthenticationMethods() {
        return clientService.getAuthenticationMethods();
    }

    public AsymmetricSignatureAlgorithm[] getAsymmetricSignatureAlgorithms() {
        return AsymmetricSignatureAlgorithm.values();
    }

    public List<String> getCibaTokenDeliveryModes() {
        List<String> modes = new ArrayList<>();
        for (BackchannelTokenDeliveryMode deliveryMode : BackchannelTokenDeliveryMode.values()) {
            modes.add(deliveryMode.getValue());
        }
        return modes;
    }

    public List<SelectableEntity<CustomScript>> getAvailableSpontaneousScripts() {
        return availableSpontaneousScripts;
    }

    private boolean loadSector(String sectorIdentifierUri) throws ClientProtocolException, IOException {
    	try {
    	String sectoruriContent = downloadSectorIdentifierUri(sectorIdentifierUri);
    	
    	if (sectoruriContent == null) {
    		return false;
    	}

    	
            JSONArray uris = new JSONArray(sectoruriContent);
            this.loginUris.clear();
            for (int i = 0; i < uris.length(); i++) {
                if (!uriIsInValid(uris.getString(i))) {
                    this.loginUris.add(uris.getString(i));
                }
            }
            return true;
        } catch (Exception e) {
            log.error("exception in parsing the uri", e.getMessage());
            return false;
        }
    }

    private String getResponseContent(HttpResponse httpResponse) throws IOException {
        if ((httpResponse == null) || (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)) {
        	return null;
        }

        HttpEntity entity = httpResponse.getEntity();
		byte[] responseBytes = new byte[0];
		if (entity != null) {
			responseBytes = EntityUtils.toByteArray(entity);
		}

    	// Consume response content
		if (entity != null) {
			EntityUtils.consume(entity);
		}

    	return new String(responseBytes, StandardCharsets.UTF_8);
	}

    private String downloadSectorIdentifierUri(String sectorIdentifierUri) throws IOException, ClientProtocolException, URISyntaxException {
		
    	HttpGet httpGet = new HttpGet();
		httpGet.setURI(new URI(sectorIdentifierUri));
    	httpGet.setHeader("Accept", "application/json");

    	String fileContent = null;
    	try ( CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.build() ) {
    		HttpResponse httpResponse = httpClient.execute(httpGet);
    		fileContent = getResponseContent(httpResponse);
		}catch(Exception e) {
			log.error("url is invalid please check ", e.getMessage());
            return null;			
		}

        if (fileContent == null) {
            return null;
        }

        return fileContent;
	}

    private boolean uriIsInValid(String uri) {
        URL url;
        try {
        	if(uri.startsWith("schema://")|| uri.startsWith("appschema://")) {
        		return false;
        	}
        	if(!uri.contains("//") && !domainPattern.matcher(uri.split("/")[0]).matches()) {
        		return false;
        	}
            url = new URL(uri);
            return url.getProtocol().equalsIgnoreCase("http") && !url.getHost().equalsIgnoreCase("localhost");
        } catch (MalformedURLException e) {
            return true;
        }

    }
	
    public List<String> getAdditionalAudienceList() {
		return additionalAudienceList;
	}

	public void setAdditionalAudienceList(List<String> additionalAudienceList) {
		this.additionalAudienceList = additionalAudienceList;
	}

	public String getAvailableAdditionalAudience() {
		return availableAdditionalAudience;
	}

	public void setAvailableAdditionalAudience(String availableAdditionalAudience) {
		this.availableAdditionalAudience = availableAdditionalAudience;
	}

	public String getBackchannelLogoutUri() {
		return backchannelLogoutUri;
	}

	public void setBackchannelLogoutUri(String backchannelLogoutUri) {
		this.backchannelLogoutUri = backchannelLogoutUri;
	}

	@ObjectClass(value = "gluuAttribute")
	class AttributeDisplayNameEntry extends DisplayNameEntry {
	    public AttributeDisplayNameEntry() {}
	}

	public String getTlsSubjectDn() {
		return tlsSubjectDn;
	}

	public void setTlsSubjectDn(String tlsSubjectDn) {
		this.tlsSubjectDn = tlsSubjectDn;
	}

	public List<CustomScript> getAvailableCustomScriptsAcr() {
		return availableCustomScriptsAcr;
	}

	public void setAvailableCustomScriptsAcr(List<CustomScript> availableCustomScriptsAcr) {
		this.availableCustomScriptsAcr = availableCustomScriptsAcr;
	}

	public String getCustomScriptsforPostAuthn() {
		return customScriptsforPostAuthn;
	}

	public void setCustomScriptsforPostAuthn(String customScriptsforPostAuthn) {
		this.customScriptsforPostAuthn = customScriptsforPostAuthn;
	}

	public String getCustomScriptsforConsentGather() {
		return customScriptsforConsentGather;
	}

	public void setCustomScriptsforConsentGather(String customScriptsforConsentGather) {
		this.customScriptsforConsentGather = customScriptsforConsentGather;
	}

	public String getSpontaneousScopeCustomScript() {
		return spontaneousScopeCustomScript;
	}

	public void setSpontaneousScopeCustomScript(String spontaneousScopeCustomScript) {
		this.spontaneousScopeCustomScript = spontaneousScopeCustomScript;
	}

	public String getIntrospectionCustomScript() {
		return introspectionCustomScript;
	}

	public void setIntrospectionCustomScript(String introspectionCustomScript) {
		this.introspectionCustomScript = introspectionCustomScript;
	}

	public String getRptClaimsScript() {
		return rptClaimsScript;
	}

	public void setRptClaimsScript(String rptClaimsScript) {
		this.rptClaimsScript = rptClaimsScript;
	}

	public List<UmaResource> getResources() {
		return resources;
	}

	public void setResources(List<UmaResource> resources) {
		this.resources = resources;
	}

	public List<String> getClientBackChannellogoutUris() {
		return clientBackChannellogoutUris;
	}

	public void setClientBackChannellogoutUris(List<String> clientBackChannellogoutUris) {
		this.clientBackChannellogoutUris = clientBackChannellogoutUris;
	}

	public String getAvailableClientBacklogoutUri() {
		return availableClientBacklogoutUri;
	}

	public void setAvailableClientBacklogoutUri(String availableClientBacklogoutUri) {
		this.availableClientBacklogoutUri = availableClientBacklogoutUri;
	}

	public List<String> getRedirectLogoutUrl() {
		return redirectLogoutUrl;
	}

	public void setRedirectLogoutUrl(List<String> redirectLogoutUrl) {
		this.redirectLogoutUrl = redirectLogoutUrl;
	}

	public String getAvailableRedirectLogoutUrl() {
		return availableRedirectLogoutUrl;
	}

	public void setAvailableRedirectLogoutUrl(String availableRedirectLogoutUrl) {
		this.availableRedirectLogoutUrl = availableRedirectLogoutUrl;
	}
	
	public String getScopePattern() {
		return scopePattern;
	}

	public void setScopePattern(String scopePattern) {
		this.scopePattern = scopePattern;
	}
}
