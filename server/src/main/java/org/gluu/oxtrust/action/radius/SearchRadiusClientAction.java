package org.gluu.oxtrust.action.radius;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SelectableEntity;
import org.gluu.oxtrust.service.radius.GluuRadiusClientService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.radius.model.RadiusClient;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

@Named("searchRadiusClientAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('radius','access')}")
public class SearchRadiusClientAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private GluuRadiusClientService gluuRadiusClientService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private Logger log;

    @Inject
    private ConversationService conversationService;

    @NotNull
    @Size(min=0,max=30,message="#{msgs['radius.clients.searchpattern.outofrange']}")
    private String searchPattern = "";

    private String oldSearchPattern;

    private List<SelectableEntity<RadiusClient>> results;

    public String getSearchPattern() {

        return this.searchPattern;
    }

    public void setSearchPattern(String searchPattern) {

        this.searchPattern = searchPattern;
    }

    public List<SelectableEntity<RadiusClient>> getResults() {

        return this.results;
    }

    public void setResults(List<SelectableEntity<RadiusClient>> results) {

        this.results = results;
    }

    public String start() {

        this.results = new ArrayList<SelectableEntity<RadiusClient>>();
        return search();
    }

    public String search() {
        
        if(this.oldSearchPattern!=null && StringUtils.equals(this.searchPattern,this.oldSearchPattern))
            return OxTrustConstants.RESULT_SUCCESS;
        
        try {
            List<RadiusClient> radiusclients = null;
            if(searchPattern == null || searchPattern.isEmpty()) {
                radiusclients = gluuRadiusClientService.getAllClients(100);
            }else {
                radiusclients = gluuRadiusClientService.searchClients(searchPattern,100);
            }

            radiusclients.sort(Comparator.comparing(RadiusClient::getName));
            this.results.clear();
            for(RadiusClient radiusclient : radiusclients) {
                this.results.add(new SelectableEntity<RadiusClient>(radiusclient));
            }

            this.oldSearchPattern = this.searchPattern;
            this.searchPattern = "";
        }catch(Exception e) {
            log.debug("Failed to find radius clients",e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.clients.search.error']}");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_FAILURE;
        }
        return OxTrustConstants.RESULT_SUCCESS;
    }

    public String deleteRadiusClients() {
        
        for(SelectableEntity<RadiusClient> radiusclient : results) {
            if(radiusclient.isSelected()) {
                gluuRadiusClientService.deleteRadiusClient(radiusclient.getEntity());
            }
        }
        this.oldSearchPattern = null;
        return search();
    }

}