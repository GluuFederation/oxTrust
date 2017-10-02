package org.gluu.oxtrust.model.scim2.extensions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jgomer on 2017-09-29.
 */
public class Extension {

    private String urn;
    private Map<String, ExtensionField> fields=new HashMap<String, ExtensionField>();

    public Extension(String urn){
        this.urn=urn;
    }

    public String getUrn() {
        return urn;
    }

    public Map<String, ExtensionField> getFields() {
        return fields;
    }

    public void setFields(Map<String, ExtensionField> fields) {
        this.fields = fields;
    }

}
