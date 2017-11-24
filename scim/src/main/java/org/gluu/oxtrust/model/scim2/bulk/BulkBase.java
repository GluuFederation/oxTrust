package org.gluu.oxtrust.model.scim2.bulk;

import java.util.List;

/**
 * Created by jgomer on 2017-11-21.
 */
public class BulkBase {

    private List<String> schemas;
    private List<BulkOperation> Operations;

    public BulkBase(){}

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<BulkOperation> getOperations() {
        return Operations;
    }

    public void setOperations(List<BulkOperation> operations) {
        Operations = operations;
    }

}
