package org.gluu.oxtrust.service.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.gluu.conf.service.ConfigurationFactory;
import org.gluu.oxauth.model.common.ScopeType;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.ResourceScope;
import org.gluu.oxtrust.model.RsResource;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ScopeService;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

@ApplicationScoped
public class ApiProtectionService {

	public static final String PROTECTION_CONFIGURATION_FILE_NAME = "config-api-rs-protect.json";

	@Inject
	Logger log;

	@Inject
	ScopeService scopeService;

	@Inject
	ClientService clientService;

	@Inject
	ConfigurationFactory<?, ?> configurationFactory;

	// ResourceAccess fullAccess;
	// ResourceAccess modifyAccess;
	// ResourceAccess readOnlyAccess;

	ResourceScope resourceScope;

	public void verifyResources(String apiProtectionType, String clientId) throws IOException {
		log.debug(
				"ApiProtectionService::verifyResources() - apiProtectionType:{}, clientId:{}, configurationFactory:{} ",
				apiProtectionType, clientId, configurationFactory);

		// Load the resource json
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = loader.getResourceAsStream(PROTECTION_CONFIGURATION_FILE_NAME);

		ResourceScope resourceScope = new ObjectMapper().readValue(inputStream, ResourceScope.class);
		log.debug("verifyResources() - fullAccess{} ", resourceScope);

		Preconditions.checkNotNull(resourceScope.getScopes(), "Config Api Resource list cannot be null !!!");

		createScopeIfNeeded(apiProtectionType);
		log.trace("ApiProtectionService:::verifyResources() - allScopes:{} ", resourceScope.getScopes());

		updateScopeForClientIfNeeded(clientId);

	}

	private void createScopeIfNeeded(String apiProtectionType) {
		log.debug("ApiProtectionService:::createScopeIfNeeded() - apiProtectionType:{}", apiProtectionType);

		List<Scope> scopeList = new ArrayList<>();
		for (String rsScope : resourceScope.getScopes()) {
			log.trace("ApiProtectionService:::createScopeIfNeeded() - resourceName:{}, rsScopes:{} ", rsScope);

			// If no scopes for the path then skip validation
			if (rsScope == null || rsScope.isEmpty()) {
				break;
			}

			log.debug("ApiProtectionService:::createScopeIfNeeded() - scopeName:{} ", rsScope);

			// validate scope
			try {
				validateScope(rsScope);
				log.debug("ApiProtectionService:::createScopeIfNeeded() - resourceName:{}, scopeList:{}", rsScope);
			} catch (Exception e) {
				log.error("Failed to load scope entry", e);
			}

		}
	}

	private void validateScope(String rsScope) throws Exception {
		// Check in DB
		log.debug("Verify Scope in DB - {} ", rsScope);
		Scope scope = scopeService.getScopeById(rsScope);
		log.debug("Scopes from DB - {}'", scope);

		ScopeType scopeType = ScopeType.OAUTH;

		if (scope == null) {
			log.debug("Scope - '{}' does not exist, hence creating it.", rsScope);
			// Scope does not exists hence create Scope
			scope = new Scope();
			String inum = UUID.randomUUID().toString();
			scope.setId(rsScope);
			scope.setDisplayName(rsScope);
			scope.setInum(inum);
			scope.setDn(scopeService.getDnForScope(inum));
			scope.setScopeType(scopeType);
			scopeService.addScope(scope);
		}
		if (scope != null) {
			// Update resource
			log.debug("Scope - '{}' already exists, hence updating it.", rsScope);
			scope.setId(rsScope);
			scope.setScopeType(scopeType);
			scopeService.updateScope(scope);
		}
	}

	private void updateScopeForClientIfNeeded(String clientId) {
		log.debug(" Internal clientId:{} ", clientId);

		if (StringUtils.isBlank(clientId)) {
			return;
		}

		try {
			OxAuthClient client = this.clientService.getClientByInum(clientId);
			log.debug("updateScopeForClientIfNeeded() - Verify client:{} ", client);
			List<String> scopes = resourceScope.getScopes();

			if (client != null) {
				// Assign scope
				// Prepare scope array
				
				log.trace("updateScopeForClientIfNeeded() - All scopes:{}", resourceScope.getScopes());

				if (client.getOxAuthScopes() != null) {
					List<String> existingScopes = client.getOxAuthScopes();
					log.trace("updateScopeForClientIfNeeded() - Clients existing scopes:{} ", existingScopes);
					if (scopes == null) {
						scopes = new ArrayList<>();
					}
					scopes.addAll(existingScopes);
				}

				// Distinct scopes
				List<String> distinctScopes = (scopes == null ? Collections.emptyList()
						: scopes.stream().distinct().collect(Collectors.toList()));
				log.debug("updateScopeForClientIfNeeded() - Distinct scopes to add:{} ", distinctScopes);

				// String[] scopeArray = this.getAllScopesArray(distinctScopes);
				log.debug("All Scope to assign to client:{}", distinctScopes);

				client.setOxAuthScopes(distinctScopes);
				this.clientService.updateClient(client);
			}
			client = this.clientService.getClientByInum(clientId);
			log.debug(" Verify scopes post assignment, clientId:{}, scopes:{}", clientId,
					Arrays.asList(client.getOxAuthScopes()));
			
			ArrayList<String> readScopes = new ArrayList<String>();
			ArrayList<String> writeScopes = new ArrayList<String>();
			for(String sc :scopes) {
				if(sc.contains("read")){
					readScopes.add(sc);
					break;
				}
				if(sc.contains("write")){
					writeScopes.add(sc);
				}
				
			}
			//create readonly client
			client.setInum(clientService.generateInumForNewClient());
			client.setOxAuthScopes(readScopes);
			client.setDisplayName("oxtrustAPIReadOnlyclient");
			client.setDescription("oxtrust API Read-Only client");
			clientService.addClient(client);
			
			
			//create write only client
			client.setInum(clientService.generateInumForNewClient());
			client.setDisplayName("oxtrustAPIWriteOnlyclient");
			client.setDescription("oxtrust API Write-Only client");
			client.setOxAuthScopes(writeScopes);
			clientService.addClient(client);
			
		} catch (Exception ex) {
			log.error("Error while searching internal client", ex);
		}

	}

}
