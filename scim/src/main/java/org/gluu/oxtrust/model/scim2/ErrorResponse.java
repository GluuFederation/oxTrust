/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * "HTTP Status and Error Response Handling" (see section 3.12 of RFC7644)
 *
 * @author Val Pecaoco
 * Updated by jgomer on 2017-09-14.
 */
public class ErrorResponse implements Serializable {

    private List<String> schemas;

    private String status;
    private ErrorScimType scimType;
    private String detail;

    public ErrorResponse() {
        schemas = new ArrayList<String>();
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScimType() {
        if (scimType != null) {
            return scimType.getValue();
        } else {
            return "";
        }
    }

    protected ErrorScimType getScimTypeEnum() {
        return scimType;
    }

    public void setScimType(ErrorScimType scimType) {
        this.scimType = scimType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
