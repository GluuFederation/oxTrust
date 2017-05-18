/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */
package org.gluu.oxtrust.util;

import java.io.File;
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

import javax.crypto.SecretKey;

/**
 * Provides utility methods for JKS KeyStores.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
public class KeystoreWrapper {
    
    /** Every implementation of the Java platform is required to support the following standard KeyStore type: PKCS12 */
    public static final String KEYSTORE_PKCS12 = "PKCS12";
    /** Oracle JDK / OpenJDK specific */
    public static final String KEYSTORE_JKS = "JKS";
    /** Oracle JDK / OpenJDK specific */
    public static final String KEYSTORE_JCEKS = "JCEKS";
    
    private final String filepath;
    private final String password;
    private final KeyStore keystore;
    
    /**
     * Open existing keystore or create new if don't exist.
     * 
     * @param filepath
     * @param password
     * @param type
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    public KeystoreWrapper(String filepath, String password, String type) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        this.filepath = filepath;
        this.password = password;
        keystore = KeyStore.getInstance(type);
        File keystoreFile = new File(filepath);
        if (keystoreFile.exists()) {
            keystore.load(new FileInputStream(keystoreFile), password.toCharArray());
        } else {
            keystore.load(null, null);
        }
    }
    
    public void save() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(new FileOutputStream(filepath), password.toCharArray());
    }
    
    public void saveAs(String filepath, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
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
            // should be replaced if exist
            keystore.deleteEntry(alias);
        }
        
        keystore.setCertificateEntry(alias, cert);
    }
    
    /**
     * Add key.
     * 
     * Use JCEKS keystore type to add symmetric key.
     * 
     * @param key 
     * @param alias
     * @param password
     * @throws KeyStoreException 
     */
    public void addKey(SecretKey key, String alias, String password) throws KeyStoreException {
        // check alias
        if (keystore.containsAlias(alias)) {
            // should be replaced if exist
            keystore.deleteEntry(alias);
        }
        
        keystore.setKeyEntry(alias, key, password.toCharArray(), null);
    }
}
