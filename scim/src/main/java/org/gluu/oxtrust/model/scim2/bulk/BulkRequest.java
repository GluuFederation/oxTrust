package org.gluu.oxtrust.model.scim2.bulk;

import java.util.Collections;

/**
 * @author Rahat Ali Date: 05.08.2015
 *
 * Updated by jgomer on 2017-11-21.
 */
public class BulkRequest extends BulkBase {

    private Integer failOnErrors;

    public BulkRequest(){
        //TODO: fix this;
        setSchemas(Collections.singletonList(null));
    }

    public Integer getFailOnErrors() {
        return failOnErrors;
    }

    public void setFailOnErrors(Integer failOnErrors) {
        this.failOnErrors = failOnErrors;
    }

}
