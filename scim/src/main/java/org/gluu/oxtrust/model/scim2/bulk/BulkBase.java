package org.gluu.oxtrust.model.scim2.bulk;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Created by jgomer on 2017-11-21.
 */
public class BulkBase {

    private List<String> schemas;

    @JsonProperty("Operations")
    private List<BulkOperation> operations;

    public BulkBase(){}

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<BulkOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<BulkOperation> operations) {
        this.operations = operations;
    }

}
