package org.gluu.oxtrust.model.scim2.bulk;

import org.gluu.oxtrust.model.scim2.Constants;

import java.util.Collections;

/**
 * @author Rahat Ali Date: 05.08.2015
 *
 * Updated by jgomer on 2017-11-21.
 */
public class BulkRequest extends BulkBase {

    private Integer failOnErrors;

    public BulkRequest(){
        setSchemas(Collections.singletonList(Constants.BULK_REQUEST_SCHEMA_ID));
    }

    public Integer getFailOnErrors() {
        return failOnErrors;
    }

    public void setFailOnErrors(Integer failOnErrors) {
        this.failOnErrors = failOnErrors;
    }

}
