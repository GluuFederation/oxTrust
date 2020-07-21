package org.gluu.oxtrust.action.radius;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.radius.GluuRadiusClientService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.radius.model.RadiusClient;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@Named("addRadiusClientAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('radius','access')}")
public class AddRadiusClientAction implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Integer DEFAULT_PRIORITY = 1;

    private RadiusClient client;

    @Inject
    private Logger log;

    @Inject
    private GluuRadiusClientService gluuRadiusClientService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private EncryptionService encryptionService;

    public RadiusClient getClient() {
        return this.client;
    }

    public void setClient(RadiusClient client) {
        this.client = client;
    }

    public void start() {

        client  = new RadiusClient();
        client.setPriority(DEFAULT_PRIORITY);
    }

    public String save() {

        try {
            String inum = gluuRadiusClientService.generateInum();
            client.setInum(inum);
            client.setDn(gluuRadiusClientService.getRadiusClientDn(inum));
            String encsecret = encryptionService.encrypt(client.getSecret());
            client.setSecret(encsecret);
            gluuRadiusClientService.addRadiusClient(client);
            facesMessages.add(FacesMessage.SEVERITY_INFO,"#{msgs['radius.client.add.success']}");
        }catch(Exception e) {
            log.error("Could not add new radius client",e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.client.add.error']}");
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String cancel() {

        return OxTrustConstants.RESULT_SUCCESS;
    }
}