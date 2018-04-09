/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Short information about X509 Certificate in keystore.
 * 
 * @author Dmitry Ognyannikov, 2018
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class X509CertificateShortInfo implements Serializable {
    
    private String alias;
    private String issuer;
    private String subject;
    private String algorithm;
    private Date notBeforeDatetime;
    private Date notAfterDatetime;
    
    
    public X509CertificateShortInfo() {}
    
    public X509CertificateShortInfo(String alias, X509Certificate cert) {
        this.alias = alias;
        
        if (cert.getIssuerDN() != null)
            issuer = cert.getIssuerDN().getName();
        if (cert.getSubjectDN() != null)
            subject = cert.getSubjectDN().getName();
        algorithm = cert.getSigAlgName();
        notBeforeDatetime = cert.getNotBefore();
        notAfterDatetime = cert.getNotAfter();
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @return the notBeforeDatetime
     */
    public Date getNotBeforeDatetime() {
        return notBeforeDatetime;
    }

    /**
     * @param notBeforeDatetime the notBeforeDatetime to set
     */
    public void setNotBeforeDatetime(Date notBeforeDatetime) {
        this.notBeforeDatetime = notBeforeDatetime;
    }

    /**
     * @return the notAfterDatetime
     */
    public Date getNotAfterDatetime() {
        return notAfterDatetime;
    }

    /**
     * @param notAfterDatetime the notAfterDatetime to set
     */
    public void setNotAfterDatetime(Date notAfterDatetime) {
        this.notAfterDatetime = notAfterDatetime;
    }
}
