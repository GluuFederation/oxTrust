/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */

package org.gluu.oxtrust.service.asimba;

import java.io.IOException;
import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.richfaces.model.UploadedFile;

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
    
    @Create
    public void init() {
    }
    
    /**
     * Add trust certificate file to Asimba's Keystore 
     * @param uploadedFile Certificate file 
     * @return path
     * @throws IOException 
     */
    public String addCertificateFile(UploadedFile uploadedFile) throws IOException {
        //TODO: addCertificateFile
        return "";
    }
}
