package org.gluu.oxtrust.service.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.gluu.conf.service.ConfigurationFactory;
import org.gluu.oxauth.model.common.ScopeType;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.model.ResourceScope;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ScopeService;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

@ApplicationScoped
public class ApiProtectionService {

	public static final String PROTECTION_CONFIGURATION_FILE_NAME = "api-protect.json";

	@Inject
	Logger log;

	@Inject
	ScopeService scopeService;

	@Inject
	ClientService clientService;
	
	ArrayList<String> scopeReadDns = new ArrayList<String>();
	ArrayList<String> scopeWriteDns = new ArrayList<String>();
	ArrayList<String> scopeAllDns = new ArrayList<String>();

	ResourceScope resourceScope;

	public void verifyResources(String apiProtectionType, String clientId) throws IOException {
		log.debug(
				"ApiProtectionService::verifyResources() - apiProtectionType:{}, clientId:{}, configurationFactory:{} ",
				apiProtectionType, clientId);

		// Load the resource json
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = loader.getResourceAsStream(PROTECTION_CONFIGURATION_FILE_NAME);

		resourceScope = new ObjectMapper().readValue(inputStream, ResourceScope.class);
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
		Scope updatedScope  = scopeService.getScopeByInum(scope.getInum());
		if(null != updatedScope.getId() && updatedScope.getId().contains("read") )
		{
			scopeReadDns.add(updatedScope.getDn());
		}
		if(null != updatedScope.getId() && updatedScope.getId().contains("write") )
		{
			scopeWriteDns.add(updatedScope.getDn());
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
			// List<String> scopes = resourceScope.getScopes();

			if (client != null) {
				// Assign scope
				// Prepare scope array

				log.trace("updateScopeForClientIfNeeded() - All scopes:{}", resourceScope.getScopes());

				List<String> existingClientScopes = client.getOxAuthScopes();
				List<String> existingScopes = new ArrayList();;
				for(String dn: existingClientScopes) {
					Scope scope = scopeService.getScopeByDn(dn);
					if(scope != null && !(scope.getId().contains("read") || scope.getId().contains("write"))){
						existingScopes.add(scope.getDn());
					}
				}
				log.trace("updateScopeForClientIfNeeded() - Clients existing scopes:{} ", existingScopes);
				if (existingScopes != null) {
					scopeAllDns.addAll(existingScopes);
					scopeAllDns.addAll(scopeReadDns);
					scopeAllDns.addAll(scopeWriteDns);
					scopeWriteDns.addAll(existingScopes);
					scopeReadDns.addAll(existingScopes);
				}

				// Distinct scopes
				List<String> distinctAllScopes = (scopeAllDns == null ? Collections.emptyList()
						: scopeAllDns.stream().distinct().collect(Collectors.toList()));
				List<String> distinctReadScopes = (scopeReadDns == null ? Collections.emptyList()
						: scopeReadDns.stream().distinct().collect(Collectors.toList()));
				List<String> distinctWriteScopes = (scopeWriteDns == null ? Collections.emptyList()
						: scopeWriteDns.stream().distinct().collect(Collectors.toList()));
				log.debug("updateScopeForClientIfNeeded() - Distinct scopes to add:{} ", distinctAllScopes);

				// String[] scopeArray = this.getAllScopesArray(distinctScopes);
				log.debug("All Scope to assign to client:{}", distinctAllScopes);

				client.setOxAuthScopes(distinctAllScopes);
				this.clientService.updateClient(client);

				// create readonly client
				try {
					OxAuthClient existingReadClient = clientService.getClientByDisplayName("oxtrustAPIReadOnlyclient");
					if (existingReadClient == null) {
						client.setInum(clientService.generateInumForNewClient());
						client.setOxAuthScopes(distinctReadScopes);
						client.setDn(clientService.getDnForClient(client.getInum()));
						client.setDisplayName("oxtrustAPIReadOnlyclient");
						client.setDescription("oxtrust API Read-Only client");
						clientService.addClient(client);
					}else {
						existingReadClient.setOxAuthScopes(distinctReadScopes);
						clientService.updateClient(existingReadClient);
					}
				} catch (Exception e) {
					log.info("Something went wrong on creating readonly client" + e.getMessage());
				}

				// create write only client
				try {
					OxAuthClient existingCWritelient = clientService.getClientByDisplayName("oxtrustAPIWriteOnlyclient");
					if (existingCWritelient == null) {
						client.setInum(clientService.generateInumForNewClient());
						client.setDisplayName("oxtrustAPIWriteOnlyclient");
						client.setDescription("oxtrust API Write-Only client");
						client.setDn(clientService.getDnForClient(client.getInum()));
						client.setOxAuthScopes(distinctWriteScopes);
						clientService.addClient(client);
					}else {
						existingCWritelient.setOxAuthScopes(distinctWriteScopes);
						clientService.updateClient(existingCWritelient);
					}
				} catch (Exception e) {
					log.info("Something went wrong on creating writeonly client" + e.getMessage());
				}
			}
			client = this.clientService.getClientByInum(clientId);
			log.debug(" Verify scopes post assignment, clientId:{}, scopes:{}", clientId,
					Arrays.asList(client.getOxAuthScopes()));

		} catch (Exception ex) {
			log.error("Error while searching internal client", ex);
		}

	}

}
