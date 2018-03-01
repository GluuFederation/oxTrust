/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model;

import java.io.Serializable;

/**
 * Error response
 *
 * @author Yuriy Movchan Date: 02/13/2018
 */
public class ErrorResponse implements Serializable {

	private static final long serialVersionUID = 5142834177601225245L;

    private String status;
    private String detail;

    public ErrorResponse() {}

    /**
     * Retrieves the HTTP status code of the error. E.g. "500"
     * @return A string value
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieves a description of the error
     * @return A string value
     */
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

}
