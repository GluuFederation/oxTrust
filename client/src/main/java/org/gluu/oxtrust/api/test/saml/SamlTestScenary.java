/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test.saml;

import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.Client;
import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;
import org.gluu.oxtrust.api.saml.SAMLTrustRelationshipShort;
import org.gluu.oxtrust.api.test.APITestException;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;

/**
 * SAML-relater test requests.
 * 
 * @author Dmitry Ognyannikov
 */
public class SamlTestScenary {
    
    private static final Logger logger = LogManager.getLogger(SamlTestScenary.class);
    
    private final Client client;
    
    public SamlTestScenary(Client client) {
        this.client = client;
    }
    
    private final Random random = new Random();
    
    /**
     * Run tests.
     */
    public void run() throws APITestException {
        TrustRelationshipClient samlClient = client.getTrustRelationshipClient();
        
        GluuSAMLTrustRelationship trGenerated = generateRandomeSingleTrustRelationship();
        // test create()
        String inum = samlClient.create(trGenerated);
        // test read()
        GluuSAMLTrustRelationship trReaded = samlClient.read(inum);
        //TODO: compare
        trReaded.setDescription("description changed");
        // test read()
        samlClient.update(trReaded, inum);
        // test list()
        List<SAMLTrustRelationshipShort> trustRelationships = samlClient.list();
        if (!checkListForTrustRelationship(trustRelationships, inum))
            throw new APITestException("TrustRelationship really not saved");
        // test delete()
        samlClient.delete(inum);
        trustRelationships = samlClient.list();
        if (checkListForTrustRelationship(trustRelationships, inum))
            throw new APITestException("TrustRelationship really not deleted");
        
        //TODO: all API calls
    }
    
    private GluuSAMLTrustRelationship generateRandomeSingleTrustRelationship() {
        int randTestNumber = Math.abs(random.nextInt());
        
        GluuSAMLTrustRelationship tr = new GluuSAMLTrustRelationship();
        tr.setDisplayName("test_TrustRelationship_#" + randTestNumber);
        tr.setDescription("test TrustRelationship #" + randTestNumber);
        tr.setSpMetaDataSourceType(GluuMetadataSourceType.FILE);
        tr.setUrl("https://ce.gluu.info");
        tr.setSpMetaDataFN("38CBAF15F4E4708200029736F2AB0006BF5CFB85-sp-metadata.xml");
        return tr;
    }
    
    private boolean checkListForTrustRelationship(List<SAMLTrustRelationshipShort> trustRelationships, String inum) {
        for (SAMLTrustRelationshipShort tr : trustRelationships) {
            if (inum.equals(tr.getInum()))
                return true;
        }
        return false;
    }
}
