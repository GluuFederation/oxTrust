package org.gluu.oxtrust.model.scim2.bulk;

import java.util.Collections;

/**
 * @author Rahat Ali Date: 05.08.2015
 *
 * Updated by jgomer on 2017-11-21.
 */
public class BulkResponse extends BulkBase {

    public BulkResponse(){
        //TODO: fix this
        setSchemas(Collections.singletonList(null));
    }
}
