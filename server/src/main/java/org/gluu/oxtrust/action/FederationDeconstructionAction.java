/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.ldap.service.Shibboleth3ConfService;
import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.saml.metadata.SAMLMetadataParser;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;

@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class FederationDeconstructionAction implements Serializable {
	private static final long serialVersionUID = 1216276324815043884L;

	private List<String> bulkEntities;
	private List<String> managedEntities;
	private String filterString;
	private List<String> filteredEntities;

	@Inject
	private Logger log;
	
	@Inject
	private OrganizationService organizationService;
	
	private List<String> bulkFiltered;

	private List<String> managedFiltered;

	@Inject
	private TrustService trustService;

	private Set<String> selectedList = new HashSet<String>();

	private GluuSAMLTrustRelationship selectedTR;

	private boolean updateDescrInProgress;

	private boolean updateNameInProgress;

	private GluuSAMLTrustRelationship trustRelationship;

	@Inject
	private AppConfiguration appConfiguration;

	public String initFederationDeconstructions(GluuSAMLTrustRelationship trustRelationship) {
		this.trustRelationship = trustRelationship;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void setBulkEntities(List<String> bulkEntities) {

		this.bulkEntities.removeAll(bulkFiltered);
		this.bulkEntities.addAll(bulkEntities);
	}

	public List<String> getBulkEntities() {
		if (bulkEntities == null) {
			bulkEntities = new ArrayList<String>();
			if (trustService.getTrustContainerFederation(trustRelationship) != null) {
				trustRelationship = trustService.getTrustContainerFederation(trustRelationship) ;
			}

			bulkEntities.addAll(trustRelationship.getGluuEntityId());
			List<GluuSAMLTrustRelationship> currentDeconstruction = trustService.getDeconstructedTrustRelationships(trustRelationship);
			for (GluuSAMLTrustRelationship configuredTR : currentDeconstruction) {
				bulkEntities.remove(configuredTR.getEntityId());
			}

		}
		bulkFiltered = new ArrayList<String>();
		bulkFiltered.addAll(bulkEntities);

		if (filteredEntities != null) {
			bulkFiltered.retainAll(filteredEntities);

		}
		return bulkFiltered;
	}

	public void setManagedEntities(List<String> managedEntities) {
		this.managedEntities.removeAll(managedFiltered);
		this.managedEntities.addAll(managedEntities);
	}

	public List<String> getManagedEntities() {
		if (managedEntities == null) {
			List<GluuSAMLTrustRelationship> currentDeconstruction = trustService.getDeconstructedTrustRelationships(trustRelationship);
			managedEntities = new ArrayList<String>();
			for (GluuSAMLTrustRelationship configuredTR : currentDeconstruction) {
				managedEntities.add(configuredTR.getEntityId());
			}
		}

		managedFiltered = new ArrayList<String>();
		managedFiltered.addAll(managedEntities);
		if (filteredEntities != null) {
			managedFiltered.retainAll(filteredEntities);
		}
		return managedFiltered;

	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	public String getFilterString() {
		return filterString;
	}

	public String saveEntityList() {
		log.trace("Federation entity list is being saved");
		List<GluuSAMLTrustRelationship> currentDeconstruction = trustService.getDeconstructedTrustRelationships(trustRelationship);
		List<String> existingTRs = new ArrayList<String>();
		for (GluuSAMLTrustRelationship configuredTR : currentDeconstruction) {
			if (managedEntities.contains(configuredTR.getEntityId())) {
				// Filter not changed entities out.
				managedEntities.remove(configuredTR.getEntityId());
				existingTRs.add(configuredTR.getEntityId());
			} else {
				// Remove removed entities
				trustService.removeTrustRelationship(configuredTR);
			}
		}
		// Add new entities
		for (String entityName : managedEntities) {
			GluuSAMLTrustRelationship newTR = new GluuSAMLTrustRelationship();
			newTR.setInum(trustService.generateInumForNewTrustRelationship());
			String dn = trustService.getDnForTrustRelationShip(newTR.getInum());
			newTR.setDn(dn);
			newTR.setMaxRefreshDelay("PT8H");
			newTR.setOwner(organizationService.getOrganization().getDn());
			newTR.setSpMetaDataSourceType(GluuMetadataSourceType.FEDERATION);
			newTR.setContainerFederation(trustRelationship);
			newTR.setEntityId(entityName);
			newTR.setDisplayName(entityName);
			newTR.setDescription(entityName);
			newTR.setStatus(GluuStatus.ACTIVE);
			trustService.addTrustRelationship(newTR);
		}
		// Get final List
		managedEntities.addAll(existingTRs);
		if (selectedTR != null && managedEntities.contains(selectedTR.getEntityId())) {
			trustService.updateTrustRelationship(selectedTR);
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void filterEntities() {
		filteredEntities = null;
		if (StringHelper.isNotEmpty(filterString)) {
			filteredEntities = new ArrayList<String>();
			String idpMetadataFolder = appConfiguration.getShibboleth3IdpRootDir() + File.separator
					+ Shibboleth3ConfService.SHIB3_IDP_METADATA_FOLDER + File.separator;
			File metadataFile = new File(idpMetadataFolder + trustRelationship.getSpMetaDataFN());
			for (String entity : SAMLMetadataParser.getEntityIdFromMetadataFile(metadataFile)) {
				if (entity.toLowerCase().contains(filterString.toLowerCase())) {
					filteredEntities.add(entity);
				}
			}
		}
	}

	public boolean isFiltered(String entity) {
		return filteredEntities == null || filteredEntities.contains(entity);
	}

	public void setSelectedList(Set<String> selectedList) {
		if (selectedList.size() > 0) {
			for (GluuSAMLTrustRelationship trust : trustService.getDeconstructedTrustRelationships(trustRelationship)) {
				if (selectedList.toArray(new String[] {})[0].equals(trust.getEntityId())) {
					if (getSelectedTR() != null && !trust.equals(getSelectedTR())) {
						/*
						 * This flag is used to counter JSF behavior of
						 * automatic submit of a form on re-render. When text
						 * inputs are being re-rendered after new selection has
						 * been made - first of all they submit their current
						 * values, which leads to situation where values of
						 * previous object are assigned to the new object.
						 * 
						 * To counter this we pass first submit after changed
						 * selection.
						 */
						updateNameInProgress = true;
						updateDescrInProgress = true;
					}
					setSelectedTR(trust);
					break;
				}
			}
		} else {
			updateNameInProgress = true;
			updateDescrInProgress = true;
			setSelectedTR(null);
		}

	}

	public Set<String> getSelectedList() {
		return selectedList;
	}

	public GluuSAMLTrustRelationship getSelectedTR() {
		return selectedTR;
	}

	private void setSelectedTR(GluuSAMLTrustRelationship selectedTR) {
		this.selectedTR = selectedTR;
	}

	public String getSelectedTRDisplayName() {
		return selectedTR == null ? null : selectedTR.getDisplayName();
	}

	public void setSelectedTRDisplayName(String displayName) {
		if (!updateNameInProgress) {
			selectedTR.setDisplayName(displayName);
		} else {
			updateNameInProgress = false;
		}
	}

	public String getSelectedTRDescription() {
		return selectedTR == null ? null : selectedTR.getDescription();
	}

	public void setSelectedTRDescription(String description) {
		if (!updateDescrInProgress) {
			selectedTR.setDescription(description);
		} else {
			updateDescrInProgress = false;
		}
	}

}
