/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.io.FilenameUtils;
import org.gluu.oxtrust.ldap.cache.model.CacheCompoundKey;
import org.gluu.oxtrust.ldap.cache.model.GluuInumMap;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.InumService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.GluuCustomAttribute;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.service.external.ExternalCacheRefreshService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.LDAPConnectionProvider;
import org.gluu.site.ldap.OperationsFacade;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.xdi.config.CryptoConfigurationFile;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuDummyEntry;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.service.ObjectSerializationService;
import org.xdi.service.SchemaService;
import org.xdi.util.ArrayHelper;
import org.xdi.util.Pair;
import org.xdi.util.StringHelper;
import org.xdi.util.security.PropertiesDecrypter;

import com.unboundid.ldap.sdk.Filter;

/**
 * Check periodically if source servers contains updates and trigger target
 * server entry update if needed
 * 
 * @author Yuriy Movchan Date: 05.05.2011
 */
@Name("cacheRefreshTimer")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Startup(depends = { "appInitializer", "cacheRefreshSnapshotFileService" })
public class CacheRefreshTimer {

	@Logger
	Log log;

	private static final String LETTERS_FOR_SEARCH = "abcdefghijklmnopqrstuvwxyz1234567890.";
	private static final String[] TARGET_PERSON_RETURN_ATTRIBUTES = { OxTrustConstants.inum };

	@In
	protected AttributeService attributeService;

	@In
	private CacheRefreshConfiguration cacheRefreshConfiguration;

	@In
	private CacheRefreshService cacheRefreshService;

	@In
	private PersonService personService;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private ApplianceService applianceService;

	@In
	private CacheRefreshSnapshotFileService cacheRefreshSnapshotFileService;

	@In
	private ExternalCacheRefreshService externalCacheRefreshService;

	@In
	private SchemaService schemaService;

	@In
	private InumService inumService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;
	
	@In(value = "#{oxTrustConfiguration.cryptoConfiguration}")
	private CryptoConfigurationFile cryptoConfiguration;
	
	@In
	private ObjectSerializationService objectSerializationService;

	private AtomicBoolean isActive;
	private long lastFinishedTime;
	private String inumCachePath;

	@Observer("org.jboss.seam.postInitialization")
	public void init() {
		log.info("Initializing CacheRefreshTimer...");
		this.isActive = new AtomicBoolean(false);
		this.lastFinishedTime = System.currentTimeMillis();
		
		// Clean up previous Inum cache
		if (cacheRefreshConfiguration.isLoaded()) {
			String snapshotFolder = cacheRefreshConfiguration.getSnapshotFolder();
			if (StringHelper.isNotEmpty(snapshotFolder)) {
				this.inumCachePath = FilenameUtils.concat(cacheRefreshConfiguration.getSnapshotFolder(), "inum_cache.dat");
				objectSerializationService.cleanup(this.inumCachePath);
			}
		}

		// Schedule to start cache refresh every 1 minute
		Events.instance().raiseTimedEvent(OxTrustConstants.EVENT_CACHE_REFRESH_TIMER, new TimerSchedule(1 * 60 * 1000L, 1 * 60 * 1000L));
	}

	@Observer(OxTrustConstants.EVENT_CACHE_REFRESH_TIMER)
	@Asynchronous
	public void process() {
		if (this.isActive.get()) {
			log.debug("Another process is active");
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			log.debug("Failed to start process exclusively");
			return;
		}

		try {
			GluuAppliance currentAppliance = applianceService.getAppliance();
			if (!isStartCacheRefresh(currentAppliance)) {
				log.debug("Starting conditions aren't reached");
				return;
			}

			processImpl(currentAppliance);
			updateApplianceStatus(currentAppliance, System.currentTimeMillis());

			this.lastFinishedTime = System.currentTimeMillis();
		} catch (Throwable ex) {
			log.error("Exception happened while executing cache refresh synchronization", ex);
		} finally {
			log.debug("Allowing to run new process exclusively");
			this.isActive.set(false);
		}
	}

	private boolean isStartCacheRefresh(GluuAppliance currentAppliance) {
		if (!GluuBoolean.ENABLED.equals(currentAppliance.getVdsCacheRefreshEnabled())) {
			return false;
		}

		long poolingInterval = StringHelper.toInteger(currentAppliance.getVdsCacheRefreshPollingInterval()) * 60 * 1000;
		if (poolingInterval < 0) {
			return false;
		}
//		currentAppliance.setCacheRefreshServerIpAddress("192.168.1.13")
		String cacheRefreshServerIpAddress = currentAppliance.getCacheRefreshServerIpAddress();
		if (StringHelper.isEmpty(cacheRefreshServerIpAddress)) {
			log.debug("There is no master Cache Refresh server");
			return false;
		}

		// Compare server IP address with cacheRefreshServerIp
		boolean cacheRefreshServer = false;
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	        for (NetworkInterface networkInterface : Collections.list(nets)) {
	            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	            	if (StringHelper.equals(cacheRefreshServerIpAddress, inetAddress.getHostAddress())) {
	            		cacheRefreshServer = true;
	            		break;
	            	}
	            }
	            
	            if (cacheRefreshServer) {
	            	break;
	            }
	        }
		} catch (SocketException ex) {
			log.error("Failed to enumerate server IP addresses", ex);
		}
        
        if (!cacheRefreshServer) {
			log.debug("This server isn't master Cache Refresh server");
			return false;
        }

		this.cacheRefreshConfiguration.reloadProperties();

		// Check if cache refresh specific configuration was loaded
		if (!this.cacheRefreshConfiguration.isLoaded()) {
			log.info("Failed to start cache refresh. Can't loading configuration from oxTrustCacheRefresh.properties");
			return false;
		}

		long timeDiffrence = System.currentTimeMillis() - this.lastFinishedTime;

		return timeDiffrence >= poolingInterval;
	}

	private void processImpl(GluuAppliance currentAppliance) {
		CacheRefreshUpdateMethod updateMethod = cacheRefreshConfiguration.getUpdateMethod();

		// Prepare and check connections to LDAP servers
		LdapServerConnection[] sourceServerConnections = prepareLdapServerConnections(cacheRefreshConfiguration.getSourceServerConfigs());
		LdapServerConnection inumDbServerConnection = prepareLdapServerConnection(cacheRefreshConfiguration.getInumDbServerConfig());

		boolean isVdsUpdate = CacheRefreshUpdateMethod.VDS.equals(updateMethod);
		LdapServerConnection targetServerConnection = null;
		if (isVdsUpdate) {
			targetServerConnection = prepareLdapServerConnection(cacheRefreshConfiguration.getDestinationServerConfig());
		}

		try {
			if ((sourceServerConnections == null) || (inumDbServerConnection == null) || (isVdsUpdate && (targetServerConnection == null))) {
				log.error("Skipping cache refresh due to invalid server configuration");
			} else {
				detectChangedEntries(currentAppliance, sourceServerConnections, inumDbServerConnection, targetServerConnection,
						updateMethod);
			}
		} finally {
			// Close connections to LDAP servers
			try {
				closeLdapServerConnection(sourceServerConnections);
			} catch (Exception e) {
				// Nothing can be done
			}
			try {
				closeLdapServerConnection(inumDbServerConnection);
			} catch (Exception e) {
				// Nothing can be done
			}
			try {
				if (isVdsUpdate) {
					closeLdapServerConnection(targetServerConnection);
				}
			} catch (Exception e) {
				// Nothing can be done
			}
		}

		return;
	}

	@SuppressWarnings("unchecked")
	private boolean detectChangedEntries(GluuAppliance currentAppliance, LdapServerConnection[] sourceServerConnections,
			LdapServerConnection inumDbServerConnection, LdapServerConnection targetServerConnection, CacheRefreshUpdateMethod updateMethod) {
		boolean isVDSMode = CacheRefreshUpdateMethod.VDS.equals(updateMethod);

		// Load all entries from Source servers
		log.info("Attempting to load entries from source server");
		List<GluuSimplePerson> sourcePersons;
		
		if (cacheRefreshConfiguration.isLoadSourceUsingSearchLimit()) {
			sourcePersons = loadSourceServerEntries(sourceServerConnections);
		} else {
			sourcePersons = loadSourceServerEntriesWithoutLimits(sourceServerConnections);
		}

		log.info("Found '{0}' entries in source server", sourcePersons.size());

		Map<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyMap = getSourcePersonCompoundKeyMap(sourcePersons);
		log.info("Found '{0}' unique entries in source server", sourcePersonCacheCompoundKeyMap.size());

		// Load all inum entries
		List<GluuInumMap> inumMaps = null;

		// Load all inum entries from local disk cache
		Object loadedObject = objectSerializationService.loadObject(this.inumCachePath);
		if (loadedObject != null) {
			try {
				inumMaps = (List<GluuInumMap>) loadedObject;
				log.debug("Found '{0}' entries in inum objects disk cache", inumMaps.size());
			} catch (Exception ex) {
				log.error("Failed to convert to GluuInumMap list", ex);
				objectSerializationService.cleanup(this.inumCachePath);
			}
		}

		if (inumMaps == null) {
			// Load all inum entries from LDAP
			inumMaps = loadInumServerEntries(inumDbServerConnection);
			log.info("Found '{0}' entries in inum server", inumMaps.size());
		}

		HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap = getPrimaryKeyAttrValueInumMap(inumMaps);

		// Go through Source entries and create new InumMap entries if needed
		HashMap<CacheCompoundKey, GluuInumMap> addedPrimaryKeyAttrValueInumMap = addNewInumServerEntries(inumDbServerConnection,
				sourcePersonCacheCompoundKeyMap, primaryKeyAttrValueInumMap);

		HashMap<CacheCompoundKey, GluuInumMap> allPrimaryKeyAttrValueInumMap = getAllInumServerEntries(primaryKeyAttrValueInumMap,
				addedPrimaryKeyAttrValueInumMap);
		log.debug("Count actual inum entries '{0}' after updating inum server", allPrimaryKeyAttrValueInumMap.size());

		HashMap<String, Integer> currInumWithEntryHashCodeMap = getSourcePersonsHashCodesMap(inumDbServerConnection,
				sourcePersonCacheCompoundKeyMap, allPrimaryKeyAttrValueInumMap);
		log.debug("Count actual source entries '{0}' after calculating hash code", currInumWithEntryHashCodeMap.size());

		// Create snapshots cache folder if needed
		boolean result = cacheRefreshSnapshotFileService.prepareSnapshotsFolder();
		if (!result) {
			return false;
		}

		// Load last snapshot into memory
		Map<String, Integer> prevInumWithEntryHashCodeMap = cacheRefreshSnapshotFileService.readLastSnapshot();

		// Compare 2 snapshot and invoke update if needed
		Set<String> changedInums = getChangedInums(currInumWithEntryHashCodeMap, prevInumWithEntryHashCodeMap, isVDSMode);
		log.info("Found '{0}' changed entries", changedInums.size());

		// Load problem list from disk and add to changedInums
		List<String> problemInums = cacheRefreshSnapshotFileService.readProblemList();
		if (problemInums != null) {
			log.info("Loaded '{0}' problem entries from problem file", problemInums.size());
			// Process inums from problem list too
			changedInums.addAll(problemInums);
		}

		List<String> updatedInums = null;
		if (isVDSMode) {
			// Update request to VDS to update entries on target server
			updatedInums = updateTargetEntriesViaVDS(targetServerConnection, changedInums);
		} else {
			updatedInums = updateTargetEntriesViaCopy(sourcePersonCacheCompoundKeyMap, allPrimaryKeyAttrValueInumMap, changedInums);
		}

		log.info("Updated '{0}' entries", updatedInums.size());
		changedInums.removeAll(updatedInums);
		log.info("Failed to update '{0}' entries", changedInums.size());

		// Persist snapshot to cache folder
		result = cacheRefreshSnapshotFileService.createSnapshot(currInumWithEntryHashCodeMap);
		if (!result) {
			return false;
		}

		// Retain only specified number of snapshots
		cacheRefreshSnapshotFileService.retainSnapshots(cacheRefreshConfiguration.getSnapshotMaxCount());

		// Save changedInums as problem list to disk
		currentAppliance.setVdsCacheRefreshProblemCount(String.valueOf(changedInums.size()));
		cacheRefreshSnapshotFileService.writeProblemList(changedInums);

		// Prepare list of persons for removal
		List<GluuSimplePerson> personsForRemoval = null;

		boolean keepExternalPerson = this.cacheRefreshConfiguration.isKeepExternalPerson();
		log.debug("Keep external persons: '{0}'", keepExternalPerson);
		if (keepExternalPerson) {
			// Determine entries which need to remove
			personsForRemoval = getRemovedPersons(currInumWithEntryHashCodeMap, prevInumWithEntryHashCodeMap);
		} else {
			// Process entries which don't exist in source server
	
			// Load all entries from Target server
			List<GluuSimplePerson> targetPersons = loadTargetServerEntries(ldapEntryManager);
			log.info("Found '{0}' entries in target server", targetPersons.size());
	
			// Detect entries which need to remove
			personsForRemoval = processTargetPersons(targetPersons, currInumWithEntryHashCodeMap);
		}
		log.debug("Count entries '{0}' for removal from target server", personsForRemoval.size());

		// Remove entries from target server
		HashMap<String, GluuInumMap> inumInumMap = getInumInumMap(inumMaps);
		Pair<List<String>, List<String>> removeTargetEntriesResult = removeTargetEntries(inumDbServerConnection, ldapEntryManager, personsForRemoval, inumInumMap);
		List<String> removedPersonInums = removeTargetEntriesResult.getFirst();
		List<String> removedGluuInumMaps = removeTargetEntriesResult.getSecond();
		log.info("Removed '{0}' persons from target server", removedPersonInums.size());

		// Prepare list of inum for serialization
		ArrayList<GluuInumMap> currentInumMaps = applyChangesToInumMap(inumInumMap, addedPrimaryKeyAttrValueInumMap, removedGluuInumMaps);

		// Strore all inum entries into local disk cache
		objectSerializationService.saveObject(this.inumCachePath, currentInumMaps);

		currentAppliance.setVdsCacheRefreshLastUpdateCount(String.valueOf(updatedInums.size() + removedPersonInums.size()));

		return true;
	}

	private ArrayList<GluuInumMap> applyChangesToInumMap(HashMap<String, GluuInumMap> inumInumMap,
			HashMap<CacheCompoundKey, GluuInumMap> addedPrimaryKeyAttrValueInumMap, List<String> removedGluuInumMaps) {
		log.info("There are '{0}' entries before updating inum list", inumInumMap.size());
		for (String removedGluuInumMap : removedGluuInumMaps) {
			inumInumMap.remove(removedGluuInumMap);
		}
		log.info("There are '{0}' entries after removal '{1}' entries", inumInumMap.size(), removedGluuInumMaps.size());
		
		ArrayList<GluuInumMap> currentInumMaps = new ArrayList<GluuInumMap>(inumInumMap.values());
		currentInumMaps.addAll(addedPrimaryKeyAttrValueInumMap.values());
		log.info("There are '{0}' entries after adding '{1}' entries", currentInumMaps.size(), addedPrimaryKeyAttrValueInumMap.size());

		return currentInumMaps;
	}

	private Set<String> getChangedInums(HashMap<String, Integer> currInumWithEntryHashCodeMap,
			Map<String, Integer> prevInumWithEntryHashCodeMap, boolean includeDeleted) {
		// Find chaged inums
		Set<String> changedInums = null;
		// First time run
		if (prevInumWithEntryHashCodeMap == null) {
			changedInums = new HashSet<String>(currInumWithEntryHashCodeMap.keySet());
		} else {
			changedInums = new HashSet<String>();

			// Add all inums which not exist in new snapshot
			if (includeDeleted) {
				for (String prevInumKey : prevInumWithEntryHashCodeMap.keySet()) {
					if (!currInumWithEntryHashCodeMap.containsKey(prevInumKey)) {
						changedInums.add(prevInumKey);
					}
				}
			}

			// Add all new inums and changed inums
			for (Entry<String, Integer> currEntry : currInumWithEntryHashCodeMap.entrySet()) {
				String currInumKey = currEntry.getKey();
				Integer prevHashCode = prevInumWithEntryHashCodeMap.get(currInumKey);
				if ((prevHashCode == null) || ((prevHashCode != null) && !(prevHashCode.equals(currEntry.getValue())))) {
					changedInums.add(currInumKey);
				}
			}
		}
		return changedInums;
	}

	private List<GluuSimplePerson> getRemovedPersons(HashMap<String, Integer> currInumWithEntryHashCodeMap,
			Map<String, Integer> prevInumWithEntryHashCodeMap) {
		// First time run
		if (prevInumWithEntryHashCodeMap == null) {
			return new ArrayList<GluuSimplePerson>(0);
		}
		
		// Add all inums which not exist in new snapshot
		Set<String> deletedInums = new HashSet<String>();
		for (String prevInumKey : prevInumWithEntryHashCodeMap.keySet()) {
			if (!currInumWithEntryHashCodeMap.containsKey(prevInumKey)) {
				deletedInums.add(prevInumKey);
			}
		}
		
		List<GluuSimplePerson> deletedPersons = new ArrayList<GluuSimplePerson>(deletedInums.size());
		for (String deletedInum : deletedInums) {
			GluuSimplePerson person = new GluuSimplePerson();
			String personDn = personService.getDnForPerson(deletedInum);
			person.setDn(personDn);

			List<GluuCustomAttribute> customAttributes = new ArrayList<GluuCustomAttribute>();
			customAttributes.add(new GluuCustomAttribute(OxTrustConstants.inum, deletedInum));
			person.setCustomAttributes(customAttributes);

			deletedPersons.add(person);
		}
		
		return deletedPersons;
	}

	private List<String> updateTargetEntriesViaVDS(LdapServerConnection targetServerConnection, Set<String> changedInums) {
		List<String> result = new ArrayList<String>();

		LdapEntryManager targetLdapEntryManager = targetServerConnection.getLdapEntryManager();
		Filter filter = cacheRefreshService.createObjectClassPresenceFilter();
		for (String changedInum : changedInums) {
			String baseDn = "action=synchronizecache," + personService.getDnForPerson(changedInum);
			try {
				targetLdapEntryManager.findEntries(baseDn, GluuDummyEntry.class, filter, null, cacheRefreshConfiguration.getSizeLimit());
				result.add(changedInum);
				log.debug("Updated entry with inum {0}", changedInum);
			} catch (LdapMappingException ex) {
				log.error("Failed to update entry with inum '{0}' using baseDN {1}", ex, changedInum, baseDn);
			}
		}

		return result;
	}

	private List<String> updateTargetEntriesViaCopy(Map<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyMap,
			HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap, Set<String> changedInums) {
		HashMap<String, CacheCompoundKey> inumCacheCompoundKeyMap = getInumCacheCompoundKeyMap(primaryKeyAttrValueInumMap);
		Map<String, String> targetServerAttributesMapping = cacheRefreshConfiguration.getTargetServerAttributesMapping();
		String[] customObjectClasses = applicationConfiguration.getPersonObjectClassTypes();

		List<String> result = new ArrayList<String>();

		if (!validateTargetServerSchema(targetServerAttributesMapping, customObjectClasses)) {
			return result;
		}

		for (String targetInum : changedInums) {
			CacheCompoundKey compoundKey = inumCacheCompoundKeyMap.get(targetInum);
			if (compoundKey == null) {
				continue;
			}

			GluuSimplePerson sourcePerson = sourcePersonCacheCompoundKeyMap.get(compoundKey);
			if (sourcePerson == null) {
				continue;
			}

			if (updateTargetEntryViaCopy(sourcePerson, targetInum, customObjectClasses, targetServerAttributesMapping)) {
				result.add(targetInum);
			}
		}

		return result;
	}

	private boolean validateTargetServerSchema(Map<String, String> targetServerAttributesMapping, String[] customObjectClasses) {
		// Get list of return attributes
		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		String[] sourceAttributes = cacheRefreshConfiguration.getSourceAttributes();
		String[] returnAttributes = ArrayHelper.arrayMerge(keyAttributesWithoutValues, sourceAttributes);

		GluuSimplePerson sourcePerson = new GluuSimplePerson();
		for (String returnAttribute : returnAttributes) {
			sourcePerson.setAttribute(returnAttribute, "Test");
		}

		String targetInum = inumService.generateInums(OxTrustConstants.INUM_TYPE_PEOPLE_SLUG, false);
		String targetPersonDn = personService.getDnForPerson(targetInum);

		GluuCustomPerson targetPerson = new GluuCustomPerson();
		targetPerson.setDn(targetPersonDn);
		targetPerson.setInum(targetInum);
		targetPerson.setStatus(GluuStatus.ACTIVE);
		targetPerson.setCustomObjectClasses(customObjectClasses);

		// Update list of return attributes according mapping
		cacheRefreshService.setTargetEntryAttributes(sourcePerson, targetServerAttributesMapping, targetPerson);

		// Execute interceptor script
		externalCacheRefreshService.executeExternalUpdateUserMethods(targetPerson);
		boolean executionResult = externalCacheRefreshService.executeExternalUpdateUserMethods(targetPerson);
		if (!executionResult) {
			log.error("Failed to execute Cache Refresh scripts for person '{0}'", targetInum);
			return false;
		}

		// Validate target server attributes
		List<GluuCustomAttribute> customAttributes = targetPerson.getCustomAttributes();

		List<String> targetAttributes = new ArrayList<String>(customAttributes.size());
		for (GluuCustomAttribute customAttribute : customAttributes) {
			targetAttributes.add(customAttribute.getName());
		}

		List<String> targetObjectClasses = Arrays.asList(ldapEntryManager.getObjectClasses(targetPerson, GluuCustomPerson.class));

		return validateTargetServerSchema(targetObjectClasses, targetAttributes);
	}

	private boolean validateTargetServerSchema(List<String> targetObjectClasses, List<String> targetAttributes) {
		Set<String> objectClassesAttributesSet = schemaService.getObjectClassesAttributes(schemaService.getSchema(),
				targetObjectClasses.toArray(new String[0]));

		Set<String> targetAttributesSet = new LinkedHashSet<String>();
		for (String attrbute : targetAttributes) {
			targetAttributesSet.add(StringHelper.toLowerCase(attrbute));
		}

		targetAttributesSet.removeAll(objectClassesAttributesSet);

		if (targetAttributesSet.size() == 0) {
			return true;
		}

		log.error("Skipping target entries update. Destination server shema doesn't has next attributes: '{0}'", targetAttributesSet);

		return false;
	}

	private boolean updateTargetEntryViaCopy(GluuSimplePerson sourcePerson, String targetInum, String[] targetCustomObjectClasses,
			Map<String, String> targetServerAttributesMapping) {
		String targetPersonDn = personService.getDnForPerson(targetInum);
		GluuCustomPerson targetPerson = null;
		boolean updatePerson;
		if (personService.contains(targetPersonDn)) {
			try {
				targetPerson = personService.findPersonByDn(targetPersonDn);
				log.debug("Found person by inum '{0}'", targetInum);
			} catch (EntryPersistenceException ex) {
				log.error("Failed to find person '{0}'", ex, targetInum);
				return false;
			}
			updatePerson = true;
		} else {
			targetPerson = new GluuCustomPerson();
			targetPerson.setDn(targetPersonDn);
			targetPerson.setInum(targetInum);
			targetPerson.setStatus(GluuStatus.ACTIVE);
			updatePerson = false;
		}
		targetPerson.setCustomObjectClasses(targetCustomObjectClasses);

		targetPerson.setSourceServerName(sourcePerson.getSourceServerName());

		cacheRefreshService.setTargetEntryAttributes(sourcePerson, targetServerAttributesMapping, targetPerson);

		// Execute interceptor script
		boolean executionResult = externalCacheRefreshService.executeExternalUpdateUserMethods(targetPerson);
		if (!executionResult) {
			log.error("Failed to execute Cache Refresh scripts for person '{0}'", targetInum);
			return false;
		}

		try {
			if (updatePerson) {
				personService.updatePerson(targetPerson);
				log.debug("Updated person '{0}'", targetInum);
			} else {
				personService.addPerson(targetPerson);
				log.debug("Added new person '{0}'", targetInum);
			}
		} catch (Exception ex) {
			log.error("Failed to '{0}' person '{1}'", ex, updatePerson ? "update" : "add", targetInum);
			return false;
		}

		return true;
	}

	private HashMap<String, CacheCompoundKey> getInumCacheCompoundKeyMap(HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap) {
		HashMap<String, CacheCompoundKey> result = new HashMap<String, CacheCompoundKey>();

		for (Entry<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMapEntry : primaryKeyAttrValueInumMap.entrySet()) {
			result.put(primaryKeyAttrValueInumMapEntry.getValue().getInum(), primaryKeyAttrValueInumMapEntry.getKey());
		}

		return result;
	}

	private Pair<List<String>, List<String>> removeTargetEntries(LdapServerConnection inumDbServerConnection, LdapEntryManager targetLdapEntryManager,
			List<GluuSimplePerson> removedPersons, HashMap<String, GluuInumMap> inumInumMap) {

		String runDate = ldapEntryManager.encodeGeneralizedTime(new Date(this.lastFinishedTime));

		LdapEntryManager inumDbLdapEntryManager = inumDbServerConnection.getLdapEntryManager();
		List<String> result1 = new ArrayList<String>();
		List<String> result2 = new ArrayList<String>();

		for (GluuSimplePerson removedPerson : removedPersons) {
			String inum = removedPerson.getAttribute(OxTrustConstants.inum);

			// Update GluuInumMap if it exist
			GluuInumMap currentInumMap = inumInumMap.get(inum);
			if (currentInumMap == null) {
				log.warn("Can't find inum entry of person with DN: {0}", removedPerson.getDn());
			} else {
				GluuInumMap removedInumMap = getMarkInumMapEntryAsRemoved(currentInumMap, runDate);
				try {
					inumDbLdapEntryManager.merge(removedInumMap);
					result2.add(removedInumMap.getInum());
				} catch (LdapMappingException ex) {
					log.error("Failed to update entry with inum '{0}' and DN: {1}", ex, currentInumMap.getInum(), currentInumMap.getDn());
					continue;
				}
			}

			// Remove person from target server
			try {
				targetLdapEntryManager.remove(removedPerson);
				result1.add(inum);
			} catch (LdapMappingException ex) {
				log.error("Failed to remove person entry with inum '{0}' and DN: {1}", ex, inum, removedPerson.getDn());
				continue;
			}

			log.debug("Person with DN: '{0}' removed from target server", removedPerson.getDn());
		}

		return new Pair<List<String>, List<String>>(result1, result2);
	}

	private GluuInumMap getMarkInumMapEntryAsRemoved(GluuInumMap currentInumMap, String date) {
		GluuInumMap clonedInumMap;
		try {
			clonedInumMap = (GluuInumMap) BeanUtilsBean2.getInstance().cloneBean(currentInumMap);
		} catch (Exception ex) {
			log.error("Failed to prepare GluuInumMap for removal", ex);
			return null;
		}

		String suffix = "-" + date;

		String[] primaryKeyValues = ArrayHelper.arrayClone(clonedInumMap.getPrimaryKeyValues());
		String[] secondaryKeyValues = ArrayHelper.arrayClone(clonedInumMap.getSecondaryKeyValues());
		String[] tertiaryKeyValues = ArrayHelper.arrayClone(clonedInumMap.getTertiaryKeyValues());

		if (ArrayHelper.isNotEmpty(primaryKeyValues)) {
			markInumMapEntryKeyValuesAsRemoved(primaryKeyValues, suffix);
		}

		if (ArrayHelper.isNotEmpty(secondaryKeyValues)) {
			markInumMapEntryKeyValuesAsRemoved(secondaryKeyValues, suffix);
		}

		if (ArrayHelper.isNotEmpty(tertiaryKeyValues)) {
			markInumMapEntryKeyValuesAsRemoved(tertiaryKeyValues, suffix);
		}

		clonedInumMap.setPrimaryKeyValues(primaryKeyValues);
		clonedInumMap.setSecondaryKeyValues(secondaryKeyValues);
		clonedInumMap.setTertiaryKeyValues(tertiaryKeyValues);

		clonedInumMap.setStatus(GluuStatus.INACTIVE);
		
		return clonedInumMap;
	}

	private void markInumMapEntryKeyValuesAsRemoved(String[] keyValues, String suffix) {
		for (int i = 0; i < keyValues.length; i++) {
			keyValues[i] = keyValues[i] + suffix;
		}
	}

	private List<GluuInumMap> loadInumServerEntries(LdapServerConnection inumDbServerConnection) {
		LdapEntryManager inumDbldapEntryManager = inumDbServerConnection.getLdapEntryManager();
		String inumbaseDn = inumDbServerConnection.getBaseDns()[0];

		Filter filterObjectClass = Filter.createEqualityFilter(OxTrustConstants.objectClass, OxTrustConstants.objectClassInumMap);
		Filter filterStatus = Filter.createNOTFilter(Filter.createEqualityFilter(OxTrustConstants.gluuStatus, GluuStatus.INACTIVE.getValue()));
		Filter filter = Filter.createANDFilter(filterObjectClass, filterStatus);

		return inumDbldapEntryManager.findEntries(inumbaseDn, GluuInumMap.class, filter, null, cacheRefreshConfiguration.getSizeLimit());
	}

	private List<GluuSimplePerson> loadSourceServerEntriesWithoutLimits(LdapServerConnection[] sourceServerConnections) {
		Filter customFilter = cacheRefreshService.createFilter(cacheRefreshConfiguration.getCustomLdapFilter());
		String[] keyAttributes = cacheRefreshConfiguration.getCompoundKeyAttributes();
		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		String[] keyObjectClasses = cacheRefreshConfiguration.getCompoundKeyObjectClasses();
		String[] sourceAttributes = cacheRefreshConfiguration.getSourceAttributes();

		String[] returnAttributes = ArrayHelper.arrayMerge(keyAttributesWithoutValues, sourceAttributes);

		Set<String> addedDns = new HashSet<String>();

		List<GluuSimplePerson> sourcePersons = new ArrayList<GluuSimplePerson>();
		for (LdapServerConnection sourceServerConnection : sourceServerConnections) {
			String sourceServerName = sourceServerConnection.getSourceServerName();

			LdapEntryManager sourceLdapEntryManager = sourceServerConnection.getLdapEntryManager();
			String[] baseDns = sourceServerConnection.getBaseDns();
			Filter filter = cacheRefreshService.createFilter(keyAttributes, keyObjectClasses, "", customFilter);
			if (log.isTraceEnabled()) {
				log.trace("Using next filter to load entris from source server: {0}", filter);
			}

			for (String baseDn : baseDns) {
				List<GluuSimplePerson> currentSourcePersons = sourceLdapEntryManager.findEntries(baseDn, GluuSimplePerson.class,
						filter, returnAttributes, cacheRefreshConfiguration.getSizeLimit());

				// Add to result and ignore root entry if needed
				for (GluuSimplePerson currentSourcePerson : currentSourcePersons) {
					currentSourcePerson.setSourceServerName(sourceServerName);
					// if (!StringHelper.equalsIgnoreCase(baseDn,
					// currentSourcePerson.getDn())) {
					String currentSourcePersonDn = currentSourcePerson.getDn().toLowerCase();
					if (!addedDns.contains(currentSourcePersonDn)) {
						sourcePersons.add(currentSourcePerson);
						addedDns.add(currentSourcePersonDn);
					}
					// }
				}
			}
		}

		return sourcePersons;
	}

	private List<GluuSimplePerson> loadSourceServerEntries(LdapServerConnection[] sourceServerConnections) {
		Filter customFilter = cacheRefreshService.createFilter(cacheRefreshConfiguration.getCustomLdapFilter());
		String[] keyAttributes = cacheRefreshConfiguration.getCompoundKeyAttributes();
		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		String[] keyObjectClasses = cacheRefreshConfiguration.getCompoundKeyObjectClasses();
		String[] sourceAttributes = cacheRefreshConfiguration.getSourceAttributes();

		String[] twoLettersArray = createTwoLettersArray();
		String[] returnAttributes = ArrayHelper.arrayMerge(keyAttributesWithoutValues, sourceAttributes);

		Set<String> addedDns = new HashSet<String>();

		List<GluuSimplePerson> sourcePersons = new ArrayList<GluuSimplePerson>();
		for (LdapServerConnection sourceServerConnection : sourceServerConnections) {
			String sourceServerName = sourceServerConnection.getSourceServerName();

			LdapEntryManager sourceLdapEntryManager = sourceServerConnection.getLdapEntryManager();
			String[] baseDns = sourceServerConnection.getBaseDns();
			for (String keyAttributeStart : twoLettersArray) {
				Filter filter = cacheRefreshService.createFilter(keyAttributes, keyObjectClasses, keyAttributeStart, customFilter);
				if (log.isDebugEnabled()) {
					log.trace("Using next filter to load entris from source server: {0}", filter);
				}

				for (String baseDn : baseDns) {
					List<GluuSimplePerson> currentSourcePersons = sourceLdapEntryManager.findEntries(baseDn, GluuSimplePerson.class,
							filter, returnAttributes, cacheRefreshConfiguration.getSizeLimit());

					// Add to result and ignore root entry if needed
					for (GluuSimplePerson currentSourcePerson : currentSourcePersons) {
						currentSourcePerson.setSourceServerName(sourceServerName);
						// if (!StringHelper.equalsIgnoreCase(baseDn,
						// currentSourcePerson.getDn())) {
						String currentSourcePersonDn = currentSourcePerson.getDn().toLowerCase();
						if (!addedDns.contains(currentSourcePersonDn)) {
							sourcePersons.add(currentSourcePerson);
							addedDns.add(currentSourcePersonDn);
						}
						// }
					}
				}
			}
		}

		return sourcePersons;
	}

	private List<GluuSimplePerson> loadTargetServerEntries(LdapEntryManager targetLdapEntryManager) {
		Filter filter = Filter.createEqualityFilter(OxTrustConstants.objectClass, OxTrustConstants.objectClassPerson);

		return targetLdapEntryManager.findEntries(personService.getDnForPerson(null), GluuSimplePerson.class, filter,
				TARGET_PERSON_RETURN_ATTRIBUTES, cacheRefreshConfiguration.getSizeLimit());
	}

	private GluuInumMap addGluuInumMap(String inumbBaseDn, LdapEntryManager inumDbLdapEntryManager, String[] primaryKeyAttrName,
			String[][] primaryKeyValues) {
		String inum = cacheRefreshService.generateInumForNewInumMap(inumbBaseDn, inumDbLdapEntryManager);
		String inumDn = cacheRefreshService.getDnForInum(inumbBaseDn, inum);

		GluuInumMap inumMap = new GluuInumMap();
		inumMap.setDn(inumDn);
		inumMap.setInum(inum);
		inumMap.setPrimaryKeyAttrName(primaryKeyAttrName[0]);
		inumMap.setPrimaryKeyValues(primaryKeyValues[0]);
		if (primaryKeyAttrName.length > 1) {
			inumMap.setSecondaryKeyAttrName(primaryKeyAttrName[1]);
			inumMap.setSecondaryKeyValues(primaryKeyValues[1]);
		}
		if (primaryKeyAttrName.length > 2) {
			inumMap.setTertiaryKeyAttrName(primaryKeyAttrName[2]);
			inumMap.setTertiaryKeyValues(primaryKeyValues[2]);
		}
		inumMap.setStatus(GluuStatus.ACTIVE);
		cacheRefreshService.addInumMap(inumDbLdapEntryManager, inumMap);

		return inumMap;
	}

	private HashMap<CacheCompoundKey, GluuInumMap> addNewInumServerEntries(LdapServerConnection inumDbServerConnection,
			Map<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyMap,
			HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap) {
		LdapEntryManager inumDbLdapEntryManager = inumDbServerConnection.getLdapEntryManager();
		String inumbaseDn = inumDbServerConnection.getBaseDns()[0];

		HashMap<CacheCompoundKey, GluuInumMap> result = new HashMap<CacheCompoundKey, GluuInumMap>();

		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		for (Entry<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyEntry : sourcePersonCacheCompoundKeyMap.entrySet()) {
			CacheCompoundKey cacheCompoundKey = sourcePersonCacheCompoundKeyEntry.getKey();
			GluuSimplePerson sourcePerson = sourcePersonCacheCompoundKeyEntry.getValue();

			if (log.isTraceEnabled()) {
				log.trace("Checking source entry with key: '{0}', and DN: {1}", cacheCompoundKey, sourcePerson.getDn());
			}

			GluuInumMap currentInumMap = primaryKeyAttrValueInumMap.get(cacheCompoundKey);
			if (currentInumMap == null) {
				String[][] keyAttributesValues = getKeyAttributesValues(keyAttributesWithoutValues, sourcePerson);
				currentInumMap = addGluuInumMap(inumbaseDn, inumDbLdapEntryManager, keyAttributesWithoutValues, keyAttributesValues);
				result.put(cacheCompoundKey, currentInumMap);
				log.debug("Added new inum entry for DN: {0}", sourcePerson.getDn());
			} else {
				log.trace("Inum entry for DN: '{0}' exist", sourcePerson.getDn());
			}
		}

		return result;
	}

	private HashMap<CacheCompoundKey, GluuInumMap> getAllInumServerEntries(
			HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap,
			HashMap<CacheCompoundKey, GluuInumMap> addedPrimaryKeyAttrValueInumMap) {
		HashMap<CacheCompoundKey, GluuInumMap> result = new HashMap<CacheCompoundKey, GluuInumMap>();

		result.putAll(primaryKeyAttrValueInumMap);
		result.putAll(addedPrimaryKeyAttrValueInumMap);

		return result;
	}

	private HashMap<String, Integer> getSourcePersonsHashCodesMap(LdapServerConnection inumDbServerConnection,
			Map<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyMap,
			HashMap<CacheCompoundKey, GluuInumMap> primaryKeyAttrValueInumMap) {
		LdapEntryManager inumDbLdapEntryManager = inumDbServerConnection.getLdapEntryManager();

		HashMap<String, Integer> result = new HashMap<String, Integer>();

		for (Entry<CacheCompoundKey, GluuSimplePerson> sourcePersonCacheCompoundKeyEntry : sourcePersonCacheCompoundKeyMap.entrySet()) {
			CacheCompoundKey cacheCompoundKey = sourcePersonCacheCompoundKeyEntry.getKey();
			GluuSimplePerson sourcePerson = sourcePersonCacheCompoundKeyEntry.getValue();

			GluuInumMap currentInumMap = primaryKeyAttrValueInumMap.get(cacheCompoundKey);

			result.put(currentInumMap.getInum(), inumDbLdapEntryManager.getHashCode(sourcePerson));
		}

		return result;
	}

	private List<GluuSimplePerson> processTargetPersons(List<GluuSimplePerson> targetPersons,
			HashMap<String, Integer> currInumWithEntryHashCodeMap) {
		List<GluuSimplePerson> result = new ArrayList<GluuSimplePerson>();

		for (GluuSimplePerson targetPerson : targetPersons) {
			String personInum = targetPerson.getAttribute(OxTrustConstants.inum);
			if (!currInumWithEntryHashCodeMap.containsKey(personInum)) {
				log.debug("Person with such DN: '{0}' isn't present on source server", targetPerson.getDn());
				result.add(targetPerson);
			}
		}

		return result;
	}

	private HashMap<CacheCompoundKey, GluuInumMap> getPrimaryKeyAttrValueInumMap(List<GluuInumMap> inumMaps) {
		HashMap<CacheCompoundKey, GluuInumMap> result = new HashMap<CacheCompoundKey, GluuInumMap>();

		for (GluuInumMap inumMap : inumMaps) {
			result.put(
					new CacheCompoundKey(inumMap.getPrimaryKeyValues(), inumMap.getSecondaryKeyValues(), inumMap.getTertiaryKeyValues()),
					inumMap);
		}

		return result;
	}

	private HashMap<String, GluuInumMap> getInumInumMap(List<GluuInumMap> inumMaps) {
		HashMap<String, GluuInumMap> result = new HashMap<String, GluuInumMap>();

		for (GluuInumMap inumMap : inumMaps) {
			result.put(inumMap.getInum(), inumMap);
		}

		return result;
	}

	private Map<CacheCompoundKey, GluuSimplePerson> getSourcePersonCompoundKeyMap(List<GluuSimplePerson> sourcePersons) {
		Map<CacheCompoundKey, GluuSimplePerson> result = new HashMap<CacheCompoundKey, GluuSimplePerson>();
		Set<CacheCompoundKey> duplicateKeys = new HashSet<CacheCompoundKey>();

		String[] keyAttributesWithoutValues = cacheRefreshConfiguration.getCompoundKeyAttributesWithoutValues();
		for (GluuSimplePerson sourcePerson : sourcePersons) {
			String[][] keyAttributesValues = getKeyAttributesValues(keyAttributesWithoutValues, sourcePerson);
			CacheCompoundKey cacheCompoundKey = new CacheCompoundKey(keyAttributesValues);

			if (result.containsKey(cacheCompoundKey)) {
				duplicateKeys.add(cacheCompoundKey);
			}

			result.put(cacheCompoundKey, sourcePerson);
		}

		for (CacheCompoundKey duplicateKey : duplicateKeys) {
			log.error("Non-deterministic primary key. Skipping user with key: {0}", duplicateKey);
			result.remove(duplicateKey);
		}

		return result;
	}

	private LdapServerConnection[] prepareLdapServerConnections(String... ldapConfigs) {
		LdapServerConnection[] ldapServerConnections = new LdapServerConnection[ldapConfigs.length];
		for (int i = 0; i < ldapConfigs.length; i++) {
			ldapServerConnections[i] = prepareLdapServerConnection(ldapConfigs[i]);
			if (ldapServerConnections[i] == null) {
				return null;
			}
		}

		return ldapServerConnections;
	}

	private LdapServerConnection prepareLdapServerConnection(String ldapConfig) {
		String prefix = String.format("ldap.conf.%s.", ldapConfig);
		Properties ldapProperties = cacheRefreshConfiguration.getPropertiesByPrefix(prefix);

		LDAPConnectionProvider ldapConnectionProvider = new LDAPConnectionProvider(PropertiesDecrypter.decryptProperties(ldapProperties, cryptoConfiguration.getEncodeSalt()));

		if (!ldapConnectionProvider.isConnected()) {
			log.error("Failed to connect to LDAP server using configuration {0}", ldapConfig);
			return null;
		}

		return new LdapServerConnection(ldapConfig, ldapConnectionProvider, cacheRefreshConfiguration.getStringArray(prefix + "baseDNs"));
	}

	private void closeLdapServerConnection(LdapServerConnection... ldapServerConnections) {
		for (LdapServerConnection ldapServerConnection : ldapServerConnections) {
			if ((ldapServerConnection != null) && (ldapServerConnection.getConnectionProvider() != null)) {
				ldapServerConnection.getConnectionProvider().closeConnectionPool();
			}
		}
	}

	private String[] createTwoLettersArray() {
		char[] characters = LETTERS_FOR_SEARCH.toCharArray();
		int lettersCount = characters.length;

		String[] result = new String[lettersCount * lettersCount];
		for (int i = 0; i < lettersCount; i++) {
			for (int j = 0; j < lettersCount; j++) {
				result[i * lettersCount + j] = "" + characters[i] + characters[j];
			}
		}

		return result;
	}

	private String[][] getKeyAttributesValues(String[] attrs, GluuSimplePerson person) {
		String[][] result = new String[attrs.length][];
		for (int i = 0; i < attrs.length; i++) {
			result[i] = person.getAttributes(attrs[i]);
		}

		return result;
	}

	private void updateApplianceStatus(GluuAppliance currentAppliance, long lastRun) {
		GluuAppliance appliance = applianceService.getAppliance();

		appliance.setVdsCacheRefreshLastUpdate(toIntString(lastRun / 1000));
		appliance.setVdsCacheRefreshLastUpdateCount(currentAppliance.getVdsCacheRefreshLastUpdateCount());
		appliance.setVdsCacheRefreshProblemCount(currentAppliance.getVdsCacheRefreshProblemCount());

		ApplianceService.instance().updateAppliance(appliance);
	}

	private String toIntString(Number number) {
		return (number == null) ? null : String.valueOf(number.intValue());
	}

	private class LdapServerConnection {
		private String sourceServerName;
		private LDAPConnectionProvider connectionProvider;
		private LdapEntryManager ldapEntryManager;
		private String[] baseDns;

		protected LdapServerConnection(String sourceServerName, LDAPConnectionProvider ldapConnectionProvider, String[] baseDns) {
			this.sourceServerName = sourceServerName;
			this.connectionProvider = ldapConnectionProvider;
			this.ldapEntryManager = new LdapEntryManager(new OperationsFacade(connectionProvider));
			this.baseDns = baseDns;
		}

		public final String getSourceServerName() {
			return sourceServerName;
		}

		public final LDAPConnectionProvider getConnectionProvider() {
			return connectionProvider;
		}

		public final LdapEntryManager getLdapEntryManager() {
			return ldapEntryManager;
		}

		public final String[] getBaseDns() {
			return baseDns;
		}

	}
	
	public static void main(String[] args) {
		String LETTERS_FOR_SEARCH = "abcdefghijklmnopqrstuvwxyz1";
		char[] characters = LETTERS_FOR_SEARCH.toCharArray();
		int lettersCount = characters.length;

		String[] result = new String[lettersCount * lettersCount];
		for (int i = 0; i < lettersCount; i++) {
			for (int j = 0; j < lettersCount; j++) {
				result[i * lettersCount + j] = "" + characters[i] + characters[j];
			}
		}
		System.out.println(result.length);
	}

}
