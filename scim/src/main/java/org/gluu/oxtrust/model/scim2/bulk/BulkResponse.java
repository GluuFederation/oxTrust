package org.gluu.oxtrust.model.scim2.bulk;

import java.util.Collections;

import static org.gluu.oxtrust.model.scim2.Constants.BULK_RESPONSE_SCHEMA_ID;

/**
 * @author Rahat Ali Date: 05.08.2015
 *
 * Updated by jgomer on 2017-11-21.
 */
public class BulkResponse extends BulkBase {

    public BulkResponse(){
        setSchemas(Collections.singletonList(BULK_RESPONSE_SCHEMA_ID));
    }
}
