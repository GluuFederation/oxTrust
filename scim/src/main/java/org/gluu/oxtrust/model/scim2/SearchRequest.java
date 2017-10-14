package org.gluu.oxtrust.model.scim2;

import java.util.Collections;
import java.util.List;

/**
 * @author Val Pecaoco
 * Updated by jgomer on 2017-10-08.
 *
 * See section 3.4.3 RFC 7644
 */
public class SearchRequest {

    private List<String> schemas;
    private String attributes;
    private String excludedAttributes;
    private String filter;
    private String sortBy;
    private String sortOrder;
    private Integer startIndex;
    private Integer count;

    public SearchRequest(){}

    public SearchRequest(String schema){
        schemas=Collections.singletonList(schema);
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getExcludedAttributes() {
        return excludedAttributes;
    }

    public void setExcludedAttributes(String excludedAttributes) {
        this.excludedAttributes = excludedAttributes;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
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

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
