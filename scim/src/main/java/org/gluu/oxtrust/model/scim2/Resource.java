/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a SCIM Resource and is the base class for {@link User}s and {@link Group}s.
 */
public abstract class Resource implements Serializable {

    private String id;
    private String externalId;
    private Meta meta;

    @JsonProperty("schemas")
    private Set<String> schemas = new HashSet<String>();

    /**
     * Default constructor for Jackson
     */
    protected Resource() {
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

    public void setId(String id) {
		this.id = id;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public void setSchemas(Set<String> schemas) {
		this.schemas = schemas;
	}
    
    protected void addSchema(String schema){
        if(schemas == null){
            schemas = new HashSet<String>();
        }
        schemas.add(schema);
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
