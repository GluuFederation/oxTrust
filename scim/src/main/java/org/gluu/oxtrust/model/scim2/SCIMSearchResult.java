/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A class that holds all information from a search request
 *
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02">SCIM core schema 2.0</a>
 * </p>
 *
 * @param <T>
 *            {@link User} or {@link Group}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class SCIMSearchResult<T> {

    private long totalResults;
    private long itemsPerPage;
    private long startIndex;
    private Set<String> schemas;
    private List<T> resources = new ArrayList<T>();

    /**
     * Default constructor for Jackson
     */
    SCIMSearchResult() {
    }

    public SCIMSearchResult(List<T> resources, long totalResults, long itemsPerPage, long startIndex, String schema) {
        this.resources = resources;
        this.totalResults = totalResults;
        this.itemsPerPage = itemsPerPage;
        this.startIndex = startIndex;

        this.schemas = new HashSet<String>();
        this.schemas.add(schema);
    }

    public SCIMSearchResult(List<T> resources, long totalResults, long itemsPerPage, long startIndex,
            Set<String> schemas) {
        this.resources = resources;
        this.totalResults = totalResults;
        this.itemsPerPage = itemsPerPage;
        this.startIndex = startIndex;
        this.schemas = schemas;
    }

    /**
     * gets a list of found {@link User}s or {@link Group}s
     *
     * @return a list of found model
     */
    @JsonProperty("Resources")
    public List<T> getResources() {
        return resources;
    }

    /**
     * The total number of results returned by the list or query operation. This may not be equal to the number of
     * elements in the Resources attribute of the list response if pagination is requested.
     *
     * @return the total result
     */
    public long getTotalResults() {
        return totalResults;
    }

    /**
     * Gets the schemas of the search result
     *
     * @return the search result schemas
     */
    public Set<String> getSchemas() {
        return schemas;
    }

    /**
     * The number of Resources returned in a list response page.
     *
     * @return items per page
     */
    public long getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * The 1-based index of the first result in the current set of list results.
     *
     * @return the start index of the actual page
     */
    public long getStartIndex() {
        return startIndex;
    }
}