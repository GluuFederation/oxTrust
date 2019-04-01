/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.model.SimpleProperty;
import org.gluu.oxtrust.ldap.cache.service.CacheRefreshUpdateMethod;
import org.gluu.oxtrust.model.SimpleDoubleProperty;

/**
 * GluuCacheRefreshConfiguration
 * 
 * @author Yuriy Movchan Date: 07.29.2011
 */
public class GluuCacheRefreshConfiguration implements Serializable {

	private static final long serialVersionUID = -2540480457430436511L;

	private List<SimpleProperty> attrs;
	private List<SimpleProperty> objectClasses;

	private List<SimpleProperty> sourceAttributes;

	private String customLdapFilter;

	private String snapshotsFolder;
	private int snapshotsCount;
	private int cacheSizeLimit;

	private CacheRefreshUpdateMethod updateMethod;
	private boolean keepExternalPerson;
	private boolean loadSourceUsingSearchLimit;

	private List<SimpleDoubleProperty> attrsMapping;

	public GluuCacheRefreshConfiguration() {
		this.attrs = new ArrayList<SimpleProperty>();
		this.objectClasses = new ArrayList<SimpleProperty>();
		this.sourceAttributes = new ArrayList<SimpleProperty>();
		this.attrsMapping = new ArrayList<SimpleDoubleProperty>();
	}

	public GluuCacheRefreshConfiguration(List<SimpleProperty> attrs, List<SimpleProperty> objectClasses,
			List<SimpleProperty> sourceAttributes, String customLdapFilter, String snapshotsFolder, int snapshotsCount, int cacheSizeLimit,
			CacheRefreshUpdateMethod updateMethod, boolean keepExternalPerson, boolean loadSourceUsingSearchLimit, List<SimpleDoubleProperty> attrsMapping) {
		this.attrs = attrs;
		this.objectClasses = objectClasses;
		this.sourceAttributes = sourceAttributes;
		this.customLdapFilter = customLdapFilter;
		this.snapshotsFolder = snapshotsFolder;
		this.snapshotsCount = snapshotsCount;
		this.cacheSizeLimit = cacheSizeLimit;
		this.updateMethod = updateMethod;
		this.keepExternalPerson = keepExternalPerson;
		this.loadSourceUsingSearchLimit = loadSourceUsingSearchLimit;
		this.attrsMapping = attrsMapping;
	}

	public List<SimpleProperty> getAttrs() {
		return attrs;
	}

	public void setAttrs(List<SimpleProperty> attrs) {
		this.attrs = attrs;
	}

	public List<SimpleProperty> getObjectClasses() {
		return objectClasses;
	}

	public void setObjectClasses(List<SimpleProperty> objectClasses) {
		this.objectClasses = objectClasses;
	}

	public List<SimpleProperty> getSourceAttributes() {
		return sourceAttributes;
	}

	public void setSourceAttributes(List<SimpleProperty> sourceAttributes) {
		this.sourceAttributes = sourceAttributes;
	}

	public String getCustomLdapFilter() {
		return customLdapFilter;
	}

	public void setCustomLdapFilter(String customLdapFilter) {
		this.customLdapFilter = customLdapFilter;
	}

	public String getSnapshotsFolder() {
		return snapshotsFolder;
	}

	public void setSnapshotsFolder(String snapshotsFolder) {
		this.snapshotsFolder = snapshotsFolder;
	}

	public int getSnapshotsCount() {
		return snapshotsCount;
	}

	public void setSnapshotsCount(int snapshotsCount) {
		this.snapshotsCount = snapshotsCount;
	}

	public int getCacheSizeLimit() {
		return cacheSizeLimit;
	}

	public void setCacheSizeLimit(int cacheSizeLimit) {
		this.cacheSizeLimit = cacheSizeLimit;
	}

	public CacheRefreshUpdateMethod getUpdateMethod() {
		return updateMethod;
	}

	public void setUpdateMethod(CacheRefreshUpdateMethod updateMethod) {
		this.updateMethod = updateMethod;
	}

	public boolean isKeepExternalPerson() {
		return keepExternalPerson;
	}

	public void setKeepExternalPerson(boolean keepExternalPerson) {
		this.keepExternalPerson = keepExternalPerson;
	}

	public boolean isLoadSourceUsingSearchLimit() {
		return loadSourceUsingSearchLimit;
	}

	public void setLoadSourceUsingSearchLimit(boolean loadSourceUsingSearchLimit) {
		this.loadSourceUsingSearchLimit = loadSourceUsingSearchLimit;
	}

	public List<SimpleDoubleProperty> getAttrsMapping() {
		return attrsMapping;
	}

	public void setAttrsMapping(List<SimpleDoubleProperty> attrsMapping) {
		this.attrsMapping = attrsMapping;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GluuCacheRefreshConfiguration [attrs=").append(attrs).append(", objectClasses=").append(objectClasses)
				.append(", sourceAttributes=").append(sourceAttributes).append(", customLdapFilter=").append(customLdapFilter)
				.append(", snapshotsFolder=").append(snapshotsFolder).append(", snapshotsCount=").append(snapshotsCount)
				.append(", cacheSizeLimit=").append(cacheSizeLimit).append(", updateMethod=").append(updateMethod)
				.append(", keepExternalPerson=").append(keepExternalPerson).append(", loadSourceUsingSearchLimit=")
				.append(loadSourceUsingSearchLimit).append(", attrsMapping=").append(attrsMapping).append("]");
		return builder.toString();
	}

}
