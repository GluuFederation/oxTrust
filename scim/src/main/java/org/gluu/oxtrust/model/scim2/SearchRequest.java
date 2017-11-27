package org.gluu.oxtrust.model.scim2;

import com.google.common.base.Joiner;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.SEARCH_REQUEST_SCHEMA_ID;

/**
 * @author Val Pecaoco
 * Updated by jgomer on 2017-10-08.
 *
 * See section 3.4.3 RFC 7644
 */
public class SearchRequest {

    private List<String> schemas;
    private List<String> attributes;
    private List<String> excludedAttributes;
    private String filter;
    private String sortBy;
    private String sortOrder;
    private Integer startIndex;
    private Integer count;

    @JsonIgnore
    private String attributesStr;

    @JsonIgnore
    private String excludedAttributesStr;

    public SearchRequest(){
        schemas=Collections.singletonList(SEARCH_REQUEST_SCHEMA_ID);
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    @JsonProperty
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public void setAttributes(String commaSeparatedString){
        setAttributes(commaSeparatedString==null ? null : Arrays.asList(commaSeparatedString.split(",")));
    }

    public List<String> getExcludedAttributes() {
        return excludedAttributes;
    }

    @JsonProperty
    public void setExcludedAttributes(List<String> excludedAttributes) {
        this.excludedAttributes = excludedAttributes;
    }

    public void setExcludedAttributes(String commaSeparatedString){
        setExcludedAttributes(commaSeparatedString==null ? null : Arrays.asList(commaSeparatedString.split(",")));
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

    public String getAttributesStr() {
        return attributes==null ? null : Joiner.on(",").join(attributes.toArray());
    }

    public String getExcludedAttributesStr() {
        return excludedAttributes==null ? null : Joiner.on(",").join(excludedAttributes.toArray());
    }

}
