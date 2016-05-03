/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.Meta;

/**
 * This class represents a ServiceProviderConfig.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="https://tools.ietf.org/html/draft-ietf-scim-core-schema-20#section-5"
 * >SCIM core schema 20.0, section 5</a>
 * </p>
 */
public class ServiceProviderConfig implements Serializable {

	private String documentationUrl = "http://www.gluu.org/docs/";
	private PatchConfig patch = new PatchConfig(false);
	private FilterConfig filter = new FilterConfig(true, Constants.MAX_COUNT);
	private BulkConfig bulk = new BulkConfig(true, 10, 100);
	private SortConfig sort = new SortConfig(true);
	private ChangePasswordConfig changePassword = new ChangePasswordConfig(true);
	private ETagConfig etag = new ETagConfig(false); 
	private Collection<AuthenticationScheme> authenticationSchemes;

	private Meta meta;
	private Set<String> schemas = new HashSet<String>();

	public ServiceProviderConfig() {
    	Set<String> userSchemas = new HashSet<String>();
    	userSchemas.add(Constants.SERVICE_PROVIDER_CORE_SCHEMA_ID);
        setSchemas(userSchemas);
	}

	public String getDocumentationUrl() {
		return documentationUrl;
	}

	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	public PatchConfig getPatch() {
		return patch;
	}

	public void setPatch(PatchConfig patch) {
		this.patch = patch;
	}

	public FilterConfig getFilter() {
		return filter;
	}

	public void setFilter(FilterConfig filter) {
		this.filter = filter;
	}

	public BulkConfig getBulk() {
		return bulk;
	}

	public void setBulk(BulkConfig bulk) {
		this.bulk = bulk;
	}

	public SortConfig getSort() {
		return sort;
	}

	public void setSort(SortConfig sort) {
		this.sort = sort;
	}

	public ChangePasswordConfig getChangePassword() {
		return changePassword;
	}

	public void setChangePassword(ChangePasswordConfig changePassword) {
		this.changePassword = changePassword;
	}

	public ETagConfig getEtag() {
		return etag;
	}

	public void setEtag(ETagConfig etag) {
		this.etag = etag;
	}

	public void setAuthenticationSchemes(Collection<AuthenticationScheme> authenticationSchemes) {
		this.authenticationSchemes = authenticationSchemes;
	}

	public Collection<AuthenticationScheme> getAuthenticationSchemes() {
		return authenticationSchemes;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public Set<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(Set<String> schemas) {
		this.schemas = schemas;
	}
}
