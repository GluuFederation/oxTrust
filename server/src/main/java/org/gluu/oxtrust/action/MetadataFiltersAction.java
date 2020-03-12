/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;
import org.gluu.oxtrust.model.MetadataFilter;
import org.gluu.oxtrust.service.FilterService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.io.FileUploadWrapper;
import org.slf4j.Logger;

@ConversationScoped
@Named("metadataFiltersAction")
@Secure("#{permissionService.hasPermission('trust', 'access')}")
public class MetadataFiltersAction implements Serializable {

	private static final long serialVersionUID = -5304171897858890801L;

	private List<MetadataFilter> metadataFilters = null;

	private Set<MetadataFilter> selectedList = new HashSet<MetadataFilter>();

	private List<MetadataFilter> availableMetadataFilters = new ArrayList<MetadataFilter>();

	private MetadataFilter metadataFilterSelected;

	private FileUploadWrapper filterCertWrapper = new FileUploadWrapper();

	@Inject
	private FilterService filterService;

	private GluuSAMLTrustRelationship trustRelationship;
   
    @Inject
    private Logger log;
    
	public String initMetadataFilters(GluuSAMLTrustRelationship trustRelationship) {
		if (metadataFilters != null) {
			return OxTrustConstants.RESULT_SUCCESS;

		}
		this.trustRelationship = trustRelationship;
		try {
			filterService.parseFilters(trustRelationship);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		metadataFilters = filterService.getFiltersList(trustRelationship);
		availableMetadataFilters = filterService.getAvailableMetadataFilters();
		// availableMetadataFilters.removeAll(metadataFilters);

		this.metadataFilterSelected = new MetadataFilter();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public FileUploadWrapper getFilterCertWrapper() {
		return filterCertWrapper;
	}

	public void setFilterCertWrapper(FileUploadWrapper filterCertWrapper) {
		this.filterCertWrapper = filterCertWrapper;
	}

	public List<MetadataFilter> getMetadataFilters() {
		return this.metadataFilters;
	}

	public void setMetadataFilters(List<MetadataFilter> metadataFilters) {
		this.metadataFilters = metadataFilters;
	}

	public List<MetadataFilter> getAvailableMetadataFilters() {

		return availableMetadataFilters;
	}

	public void setAvailableMetadataFilters(List<MetadataFilter> availableList) {

		this.availableMetadataFilters = availableList;
	}

	public void setSelectedList(Set<MetadataFilter> selectedList) {
		this.selectedList = selectedList;

		if (selectedList.isEmpty()) {
			setMetadataFilterSelected(null);
			return;
		}

		boolean selectionChanged = getMetadataFilterSelected() == null
				|| !selectedList.toArray(new MetadataFilter[] {})[0].getName().equals(getMetadataFilterSelected().getName());
		if (selectionChanged) {
			boolean trustRelationshipAlreadyContainsThisFilter = trustRelationship.getMetadataFilters().get(
					selectedList.toArray(new MetadataFilter[] {})[0].getName()) != null;
			if (trustRelationshipAlreadyContainsThisFilter) {
				setMetadataFilterSelected(trustRelationship.getMetadataFilters().get(
						selectedList.toArray(new MetadataFilter[] {})[0].getName()));
			} else {
				setMetadataFilterSelected(selectedList.toArray(new MetadataFilter[] {})[0]);
			}
		}
	}

	public Set<MetadataFilter> getSelectedList() {
		return selectedList;
	}

	public String saveFilters() {
		updateMetadataFilters();
		filterService.saveFilters(trustRelationship, filterCertWrapper);
		metadataFilters = null;
		initMetadataFilters(this.trustRelationship);
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public boolean isMetadataFilterSelected(String metadataFilterName) {
		if (this.metadataFilters == null) {
			return false;
		}

		for (MetadataFilter metadataFilter : this.metadataFilters) {
			if (StringHelper.equalsIgnoreCase(metadataFilter.getName(), metadataFilterName)) {
				return true;
			}
		}

		return false;
	}

	public MetadataFilter getMetadataFilter(String metadataFilterName) {
		if (this.availableMetadataFilters == null) {
			return null;
		}

		for (MetadataFilter metadataFilter : this.availableMetadataFilters) {
			if (StringHelper.equalsIgnoreCase(metadataFilter.getName(), metadataFilterName)) {
				return metadataFilter;
			}
		}

		return null;
	}

	public MetadataFilter getMetadataFilterSelected() {
		return metadataFilterSelected;
	}

	private void setMetadataFilterSelected(MetadataFilter metadataFilterSelected) {
		this.metadataFilterSelected = metadataFilterSelected;
	}

	public void setExtensionSchemas(List<String> extensionSchemas) {
		getMetadataFilterSelected().setExtensionSchemas(extensionSchemas);
	}

	public List<String> getExtensionSchemas() {
		MetadataFilter metadataFilterSelected = getMetadataFilterSelected();
		if (metadataFilterSelected == null) {
			return null;
		} else {
			return metadataFilterSelected.getExtensionSchemas();
		}
	}

	public void setExtensionSchema(String extensionSchema) {
		if (StringUtils.isNotEmpty(extensionSchema)) {
			getMetadataFilterSelected().getExtensionSchemas().add(extensionSchema);
			getMetadataFilterSelected().setExtensionSchema("");
		}
	}

	public String getExtensionSchema() {
		MetadataFilter metadataFilterSelected = getMetadataFilterSelected();
		if (metadataFilterSelected == null) {
			return null;
		} else {
			return metadataFilterSelected.getExtensionSchema();
		}
	}

	public String updateMetadataFilters() {
		for (MetadataFilter filter : getMetadataFilters()) {
			if (!filterService.isMetadataFilterPresent(trustRelationship, filter)) {
				filterService.updateFilter(trustRelationship, filter);
			}
		}

		for (MetadataFilter filter : filterService.getFiltersList(trustRelationship)) {
			if (!getMetadataFilters().contains(filter)) {
				filterService.removeFilter(trustRelationship, filter);
			}
		}
		return OxTrustConstants.RESULT_SUCCESS;

	}

	public boolean getRemoveRolelessEntityDescriptors() {
		return getMetadataFilterSelected().getRemoveRolelessEntityDescriptors();
	}

	public void setRemoveRolelessEntityDescriptors(boolean removeRolelessEntityDescriptors) {
		getMetadataFilterSelected().setRemoveRolelessEntityDescriptors(removeRolelessEntityDescriptors);
	}

	public boolean getRemoveEmptyEntitiesDescriptors() {
		return getMetadataFilterSelected().getRemoveEmptyEntitiesDescriptors();
	}

	public void setRemoveEmptyEntitiesDescriptors(boolean removeEmptyEntitiesDescriptors) {
		getMetadataFilterSelected().setRemoveEmptyEntitiesDescriptors(removeEmptyEntitiesDescriptors);
	}

	public void setRetainedRoles(List<String> retainedRoles) {
		getMetadataFilterSelected().setRetainedRoles(retainedRoles);
	}

	public List<String> getRetainedRoles() {
		return getMetadataFilterSelected().getRetainedRoles();
	}

	public void setRetainedRole(String retainedRole) {
		if (StringUtils.isNotEmpty(retainedRole)) {
			getMetadataFilterSelected().getRetainedRoles().add(retainedRole);
			getMetadataFilterSelected().setRetainedRole("");
		}
	}

	public String getRetainedRole() {
		return getMetadataFilterSelected().getRetainedRole();
	}

	public int getMaxValidityInterval() {
		return getMetadataFilterSelected().getMaxValidityInterval();
	}

	public void setMaxValidityInterval(int maxValidityInterval) {
		getMetadataFilterSelected().setMaxValidityInterval(maxValidityInterval);
	}

	public String getFilterCertFileName() {
		return getMetadataFilterSelected().getFilterCertFileName();
	}

	public void setFilterCertFileName(String filterCertFileName) {
		getMetadataFilterSelected().setFilterCertFileName(filterCertFileName);
	}

	public boolean getRequireSignedMetadata() {
		return getMetadataFilterSelected().getRequireSignedMetadata();
	}

	public void setRequireSignedMetadata(boolean requireSignedMetadata) {
		getMetadataFilterSelected().setRequireSignedMetadata(requireSignedMetadata);
	}

	public void showFile() {
	    log.trace("\n");
	}
}
