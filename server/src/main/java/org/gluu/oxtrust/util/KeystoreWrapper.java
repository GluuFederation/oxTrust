/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Provides utility methods for JKS KeyStores.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
public class KeystoreWrapper {
    
    /** Every implementation of the Java platform is required to support the following standard KeyStore type: PKCS12 */
    private static final String KEYSTORE_PKCS12 = "PKCS12";
    /** Oracle JDK / OpenJDK specific */
    private static final String KEYSTORE_JKS = "JKS";
    
    private final String filepath;
    private final String password;
    private final KeyStore keystore;
    
    public KeystoreWrapper(String filepath, String password, String type) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.filepath = filepath;
        this.password = password;
        keystore = KeyStore.getInstance(type);
        keystore.load(new FileInputStream(filepath), password.toCharArray());
    }
    
    public void save() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(new FileOutputStream(filepath), password.toCharArray());
    }
    
    public List<X509CertificateShortInfo> listCertificates() throws KeyStoreException {
        List<X509CertificateShortInfo> certs = new ArrayList<X509CertificateShortInfo>();
        
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            
            Certificate cert = keystore.getCertificate(alias);
            
            if (cert instanceof X509Certificate) {
                X509Certificate certX509 = (X509Certificate)cert;

                X509CertificateShortInfo entry = new X509CertificateShortInfo(alias, certX509);

                certs.add(entry);
            }
        }
        
        return certs;
    }
    
    public void deleteCertificate(String alias) throws KeyStoreException {
        keystore.deleteEntry(alias);
    } 
    
    public void addCertificate(X509Certificate cert, String alias) throws KeyStoreException {
        // check alias
        if (keystore.containsAlias(alias)) {
            // nothing
        }
        
        keystore.setCertificateEntry(alias, cert);
    }
}
