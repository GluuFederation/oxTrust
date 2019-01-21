/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api;

import com.wordnik.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.gluu.oxtrust.util.X509CertificateShortInfo;

/**
 * Information about Gluu Server certificates (expiration date, etc).
 * 
 * @author Dmitry Ognyannikov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = "Information about Gluu Server certificates (expiration date, etc).")
public class Certificates implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2763806434704894667L;

	private List<X509CertificateShortInfo> asimbaCertificates;
    
    private List<X509CertificateShortInfo> trustStoreCertificates;
    
    private List<X509CertificateShortInfo> internalCertificates;

    /**
     * @return the asimbaCertificates
     */
    public List<X509CertificateShortInfo> getAsimbaCertificates() {
        return asimbaCertificates;
    }

    /**
     * @param asimbaCertificates the asimbaCertificates to set
     */
    public void setAsimbaCertificates(List<X509CertificateShortInfo> asimbaCertificates) {
        this.asimbaCertificates = asimbaCertificates;
    }

    /**
     * @return the trustStoreCertificates
     */
    public List<X509CertificateShortInfo> getTrustStoreCertificates() {
        return trustStoreCertificates;
    }

    /**
     * @param trustStoreCertificates the trustStoreCertificates to set
     */
    public void setTrustStoreCertificates(List<X509CertificateShortInfo> trustStoreCertificates) {
        this.trustStoreCertificates = trustStoreCertificates;
    }

    /**
     * @return the internalCertificates
     */
    public List<X509CertificateShortInfo> getInternalCertificates() {
        return internalCertificates;
    }

    /**
     * @param internalCertificates the internalCertificates to set
     */
    public void setInternalCertificates(List<X509CertificateShortInfo> internalCertificates) {
        this.internalCertificates = internalCertificates;
    }
    
}
