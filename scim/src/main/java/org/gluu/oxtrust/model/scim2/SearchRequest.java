/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Val Pecaoco
 */
public class SearchRequest implements Serializable {

    private Set<String> schemas = new HashSet<String>();

    private String filter;
    private int startIndex;
    private int count;
    private String sortBy;
    private String sortOrder;
    private String attributesArray;

    public SearchRequest() {
        schemas.add(Constants.SEARCH_REQUEST_SCHEMA_ID);
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getAttributesArray() {
        return attributesArray;
    }

    public void setAttributesArray(String attributesArray) {
        this.attributesArray = attributesArray;
    }
}
