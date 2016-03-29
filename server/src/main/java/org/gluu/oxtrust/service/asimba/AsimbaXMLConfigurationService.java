/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */

package org.gluu.oxtrust.service.asimba;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.richfaces.model.UploadedFile;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.gluu.oxtrust.ldap.service.SSLService;
import org.jboss.seam.annotations.In;
import org.w3c.dom.Document;

/**
 * Asimba XML configuration service.
 * 
 * @author Dmitry Ognyannikov, 2016
 */
@Name("asimbaXMLConfigurationService")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class AsimbaXMLConfigurationService implements Serializable {
    
    @Logger
    private Log log;
    
    @In
    private SSLService sslService;
    
    private static final String ASIMBA_XML_CONFIGURATION_PATH = "${webapp.root}/../asimba/WEB-INF/conf/asimba.xml";
    
    private String xmlFileConfigurationPath = ASIMBA_XML_CONFIGURATION_PATH;
    private String keystoreFilePath;
    private String keystoreType;
    private String keystorePassword;
    private String asimbaAias;
    private String asimbaAiasPassword;
    
    @Create
    public void init() {
        parse();
    }
    
    private void parse() {
        try {
            String realConfigurationPath = ASIMBA_XML_CONFIGURATION_PATH;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(realConfigurationPath));
            XPath xPath = XPathFactory.newInstance().newXPath();
            keystoreFilePath = xPath.evaluate("/asimba-server/crypto/signing/signingfactory/keystore/file", document);
            keystoreType = xPath.evaluate("/asimba-server/crypto/signing/signingfactory/keystore/type", document);
            keystorePassword = xPath.evaluate("/asimba-server/crypto/signing/signingfactory/keystore/keystore_password", document);
            if (keystoreType == null || "".equals(keystoreType))
                keystoreType = KeyStore.getDefaultType();
            asimbaAias = xPath.evaluate("/asimba-server/crypto/signing/signingfactory/keystore/alias", document);
            asimbaAiasPassword = xPath.evaluate("/asimba-server/crypto/signing/signingfactory/keystore/password", document);
        } catch (Exception e) {
            log.error("parse() exception", e);
            keystoreFilePath = null;
            keystoreType = null;
            asimbaAias = null;
            asimbaAiasPassword = null;
        }
    }
    
    /**
     * Add trust certificate file to Asimba's Keystore 
     * @param uploadedFile Certificate file 
     * @return path
     * @throws IOException 
     */
    public String addCertificateFile(UploadedFile uploadedFile, String alias) throws IOException {
        // make certificate from uploadedFile
        X509Certificate cert = sslService.getCertificate(uploadedFile.getInputStream());
        
        // load keystore
        try {
            KeyStore keystore = KeyStore.getInstance(keystoreType);
            keystore.load(new FileInputStream(keystoreFilePath), keystorePassword.toCharArray());
            
            // check alias
            if (keystore.containsAlias(alias)) {
                // nothing now
            }
            
            if (alias.equals(keystore.getCertificateAlias(cert)))
                return OxTrustConstants.RESULT_SUCCESS; // already exist
            
            keystore.setCertificateEntry(alias, cert);
            
            keystore.store(new FileOutputStream(keystoreFilePath), keystorePassword.toCharArray());
            
            return OxTrustConstants.RESULT_SUCCESS;
        } catch (Exception e) {
            log.error("Keystore load exception", e);
            return "Add Certificate exception : " + e.getMessage();
        }
    }
}