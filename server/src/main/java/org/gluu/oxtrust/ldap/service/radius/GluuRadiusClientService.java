package org.gluu.oxtrust.ldap.service.radius;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.radius.model.RadiusClient;
import org.slf4j.Logger;

public class GluuRadiusClientService {

    @Inject
    private PersistenceEntryManager persistenceEntryManager;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private Logger logger;

    public List<RadiusClient> getAllClients() {

        String clientsBaseDn = getRadiusClientsBaseDn();
        return persistenceEntryManager.findEntries(clientsBaseDn,RadiusClient.class,null);
    }

    public RadiusClient getRadiusClientByInum(String inum) {

        RadiusClient client = null;
        try {
            client = persistenceEntryManager.find(RadiusClient.class,getRadiusClientDn(inum));
        }catch(Exception e) {
            logger.debug("Could not load radius client",e);
        }
        return client;
    }

    public void addRadiusClient(RadiusClient client) {
        persistenceEntryManager.persist(client);
    }

    public void updateRadiusClient(RadiusClient client)  {
        persistenceEntryManager.merge(client);
    }

    public void deleteRadiusClient(RadiusClient client) {
        persistenceEntryManager.remove(client);
    }

    public String getRadiusClientsBaseDn() {
        return String.format("ou=radius_clients,%s",organizationService.getDnForOrganization());
    }

    public String getRadiusClientDn(String inum) {
    
        if(inum == null)
            return null;
        String orgDn = organizationService.getDnForOrganization();
        return String.format("inum=%s,ou=radius_clients,%s",inum,orgDn);
    }

    public String generateInum() {

        String inum = null;
        String dn = null;
        do {
            inum = generateInumInternal();
            dn = getRadiusClientDn(inum);
        }while(persistenceEntryManager.contains(dn,RadiusClient.class));
        return inum;
    }

    public String generateInumInternal() {

        return UUID.randomUUID().toString();
    }
}