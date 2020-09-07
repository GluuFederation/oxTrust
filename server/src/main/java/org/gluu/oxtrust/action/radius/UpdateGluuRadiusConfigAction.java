package org.gluu.oxtrust.action.radius;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.SelectableEntity;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.service.radius.GluuRadiusConfigService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.radius.model.ServerConfiguration;
import org.gluu.service.custom.CustomScriptService;
import org.gluu.service.security.Secure;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

@Named("updateGluuRadiusConfigAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('radius','access')}")
public class UpdateGluuRadiusConfigAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private Logger log;

    @Inject
    private GluuRadiusConfigService gluuRadiusConfigService;

    @Inject
    private CustomScriptService customScriptService;

    @Inject
    private ScopeService scopeService;

    @Inject
    private ClientService clientService;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    private ConversationService conversationService;

    private ServerConfiguration serverConfiguration;
    private List<SelectableEntity<Scope>> availableScopes;
    private List<Scope> scopes;

    public void init() {
        serverConfiguration = gluuRadiusConfigService.getServerConfiguration();
        initScopes();
    }

    public List<String> getRoPasswordScripts() {

        List<String> ret = new ArrayList<String>();
        List<CustomScriptType> types = Arrays.asList(CustomScriptType.RESOURCE_OWNER_PASSWORD_CREDENTIALS);
        List<CustomScript> scripts = customScriptService.findCustomScripts(types);
        for(CustomScript script : scripts) {
            if(script.isEnabled())
                ret.add(script.getName());
        }
        return ret;
    }

    public List<OxAuthClient> getOpenidClients() {

        List<OxAuthClient> ret = new ArrayList<OxAuthClient>();
        List<OxAuthClient> clients = clientService.getAllClients();
        for(OxAuthClient client : clients) {
            if(!client.isDisabled())
                ret.add(client);
        }
        return ret;
    }

    public ServerConfiguration getServerConfiguration() {

        return this.serverConfiguration;
    }

    public List<Scope> getScopes() {

        return this.scopes;
    }

    public void searchAvailableScopes() {

        if(this.availableScopes != null) {
            selectAddedScopes();
            return;
        }
        List<SelectableEntity<Scope>> tmpAvailableScopes = new ArrayList<SelectableEntity<Scope>>();
        List<Scope> scopes  = new ArrayList<Scope>();
        try {
            scopes = scopeService.getAllScopesList(100);
        }catch(Exception e) {
            log.warn(e.getMessage(),e);
        }
        for(Scope scope : scopes)
            tmpAvailableScopes.add(new SelectableEntity<Scope>(scope));
        this.availableScopes = tmpAvailableScopes;
        selectAddedScopes();
    }

    public List<SelectableEntity<Scope>> getAvailableScopes() {

        return this.availableScopes;
    }

    public void removeScope(String scopeDn) {

        if(scopeDn == null)
            return;
        for(Iterator<Scope> iterator = scopes.iterator();iterator.hasNext();) {
            Scope scope = iterator.next();
            if(scope.getDn().equalsIgnoreCase(scopeDn)) {
                iterator.remove();
                break;
            }
        }
    }

    public void acceptSelectedScopes() {

        for(SelectableEntity<Scope> availableScope : this.availableScopes) {
            Scope scopeobj  = availableScope.getEntity();
            if(availableScope.isSelected() && !containsScope(scopeobj,this.scopes)) {
                this.scopes.add(scopeobj);
                continue;
            }

            if(!availableScope.isSelected() && containsScope(scopeobj,this.scopes)) {
                removeScope(scopeobj.getDn());
            }
        }
    }

    public void cancelSelectedScopes() {

    }

    public String save() {

        List<String> scopednlist = new ArrayList<String>();
        for(Scope scope: this.scopes) {
            scopednlist.add(scope.getDn());
        }
        serverConfiguration.setScopes(scopednlist);
        try {
            gluuRadiusConfigService.updateServerConfiguration(serverConfiguration);
            facesMessages.add(FacesMessage.SEVERITY_INFO,"#{msgs['radius.config.save.success']}");
            conversationService.endConversation();
            return OxTrustConstants.RESULT_SUCCESS;
        }catch(Exception e) {
            log.error("Failed to save radius server configuration",e);
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"#{msgs['radius.config.save.error']}");
            return OxTrustConstants.RESULT_FAILURE;
        }
    }

    public String cancel() {

        conversationService.endConversation();
        return OxTrustConstants.RESULT_SUCCESS;
    }

    private void initScopes() {

        scopes = new ArrayList<Scope>();
        if(serverConfiguration.getScopes() != null && serverConfiguration.getScopes().size() > 0) {
            for(String scopedn : serverConfiguration.getScopes()) {
                try {
                    Scope scope = scopeService.getScopeByDn(scopedn);
                    if(scope != null)
                        scopes.add(scope);
                }catch(Exception e) {
                    log.warn("Radius config scope " + scopedn + " not found",e);
                }
            }
        }
    }

    private void selectAddedScopes() {

        for(SelectableEntity<Scope> availableScope : this.availableScopes) {
            availableScope.setSelected(containsScope(availableScope.getEntity(),this.scopes));
        }
    }

    private boolean containsScope(Scope scope,List<Scope> scopes) {

        boolean contains = false;
        for(Scope serverscope :  scopes) {
            if(serverscope.getDn().equalsIgnoreCase(scope.getDn())) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}