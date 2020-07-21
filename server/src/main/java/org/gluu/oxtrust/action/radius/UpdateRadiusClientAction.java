package org.gluu.oxtrust.action.radius;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.service.EncryptionService;
import org.gluu.oxtrust.service.radius.GluuRadiusClientService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.radius.model.RadiusClient;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@Named("updateRadiusClientAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('radius','access')}")
public class UpdateRadiusClientAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inum;

    private RadiusClient client;

    @Inject
    private Logger log;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private GluuRadiusClientService gluuRadiusClientService;

    @Inject
    private EncryptionService encryptionService;

    public UpdateRadiusClientAction() {

        this.client = new RadiusClient();
    }

    public String start() {
        try {
            client = gluuRadiusClientService.getRadiusClientByInum(inum);
            if(client == null) {
                log.debug("Radius client " + inum + " not found.");
                facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.client.notfound']}");
                return OxTrustConstants.RESULT_FAILURE;
            }
        }catch(Exception e) {
            log.debug("An error occured while loading client " + inum,e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.client.update.loaderror']}");
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String save() {

        try {
            RadiusClient tmpclient = gluuRadiusClientService.getRadiusClientByInum(inum);
            if(client == null) {
                log.debug("Radius client " + inum + " not found.");
                return OxTrustConstants.RESULT_FAILURE;
            }
            if(client.getSecret()!=null && !client.getSecret().isEmpty()) {
                String encsecret = encryptionService.encrypt(client.getSecret());
                client.setSecret(encsecret);
            }else {
                client.setSecret(tmpclient.getSecret());
            }
            client.setDn(tmpclient.getDn());
            client.setInum(tmpclient.getInum());
            gluuRadiusClientService.updateRadiusClient(client);
            facesMessages.add(FacesMessage.SEVERITY_INFO,"#{msgs['radius.client.update.success']}");
        }catch(Exception e) {
            log.debug("Could not update client " + inum,e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.client.update.error']}");
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String cancel() {

        return OxTrustConstants.RESULT_SUCCESS;
    }

    public RadiusClient getClient() {

        return this.client;
    }

    public void setClient(RadiusClient client) {

        this.client = client;
    }

    public String getInum() {

        return this.inum;
    }

    public void setInum(String inum) {
        
        this.inum = inum;
    }
}