/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a SCIM Resource and is the base class for {@link User}s and {@link Group}s.
 */
public abstract class Resource {

    private String id;
    private String externalId;
    private Meta meta;
    @JsonProperty(required = true)
    private Set<String> schemas;

    /**
     * Default constructor for Jackson
     */
    protected Resource() {
    }

    protected Resource(Builder builder) {
        this.id = builder.id;
        this.externalId = builder.externalId;
        this.meta = builder.meta;
        this.schemas = builder.schemas;
    }

    /**
     * Gets the Id of the resource.
     * 
     * @return the id of the resource
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the external Id of the resource.
     * 
     * <p>
     * For more information please look at <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-5.1">SCIM core schema 2.0, section
     * 5.1</a>
     * </p>
     * 
     * @return the externalId
     * 
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Gets the meta attribute
     * 
     * @return the meta
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * Gets the list of defined schemas
     * 
     * @return a the list of schemas as a {@link Set}
     */
    public Set<String> getSchemas() {
        return schemas;
    }

    /**
     * The Builder class is used to construct instances of the {@link Resource}
     */
    public abstract static class Builder {
        private String id;
        private Meta meta;
        private Set<String> schemas = new HashSet<String>(); // NOSONAR - fields are needed in child classes
        protected String externalId; // NOSONAR - fields are needed in child classes

        public Builder(Resource resource) {
            if (resource != null) {
                this.id = resource.id;
                this.externalId = resource.externalId;
                this.meta = resource.meta;
                this.schemas = resource.schemas;
            }
        }

        /**
         * sets the schemas of the Resource
         * 
         * @param schemas
         *            actual schemas
         * @return the builder itself
         */
        public Builder setSchemas(Set<String> schemas) {
            this.schemas = schemas;
            return this;
        }
        
        protected void addSchema(String schema){
            if(schemas == null){
                schemas = new HashSet<String>();
            }
            schemas.add(schema);
        }

        /**
         * Sets the id of the resource.
         * 
         * <p>
         * client info: The id of a User will be created and set by the OSIAM server. 
         * If a {@link User} or {@link Group} which is send to the OSIAM server has this value filled, 
         * the value will be ignored or the action will be rejected.
         * </p>
         * 
         * @param id
         *            if of the resource
         * @return the builder itself
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the external id (See {@link Resource#getExternalId()}).
         * 
         * @param externalId
         *            the external id
         * 
         * @return the builder itself
         */
        public Builder setExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        /**
         * Sets the meta data
         * 
         * <p>
         * client info: The meta information of a User will be created and set by the OSIAM server. 
         * If a {@link User} or {@link Group} which is send to the OSIAM server has this value filled, 
         * the value will be ignored or the action will be rejected. 
         * For an update(PATCH) the attribute value can be set by the client. In normal case this 
         * should be set by the {@link UpdateUser} or {@link UpdateGroup} and not by the client directly. 
         * </p>
         * 
         * @param meta
         *            the meta object
         * @return the builder itself
         */
        public Builder setMeta(Meta meta) {
            this.meta = meta;
            return this;
        }

        /**
         * Builds the Object of the Builder
         * 
         * @return a new main Object of the Builder
         */
        public abstract <T> T build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Resource other = (Resource) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
