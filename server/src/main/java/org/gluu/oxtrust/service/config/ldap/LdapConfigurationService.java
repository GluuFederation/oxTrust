package org.gluu.oxtrust.service.config.ldap;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.EncryptionService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.OxIDPAuthConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.ldap.GluuLdapConfiguration;
import org.xdi.util.security.StringEncrypter;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.gluu.oxtrust.util.CollectionsUtil.trimToEmpty;

public class LdapConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(LdapConfigurationService.class);
    private static final String AUTH = "auth";

    @Inject
    private ApplianceService applianceService;

    @Inject
    private EncryptionService encryptionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public List<GluuLdapConfiguration> findLdapConfigurations() {
        return FluentIterable.from(iDPAuthConfs())
                .transform(extractLdapConfiguration())
                .toList();
    }

    private Function<OxIDPAuthConf, GluuLdapConfiguration> extractLdapConfiguration() {
        return new Function<OxIDPAuthConf, GluuLdapConfiguration>() {
            @Override
            public GluuLdapConfiguration apply(OxIDPAuthConf oxIDPAuthConf) {
                try {
                    return objectMapper.readValue(oxIDPAuthConf.getConfig(), GluuLdapConfiguration.class);
                } catch (IOException e) {
                    LOG.error("Error while reading the GluuLdapConfiguration", e);
                    throw new LdapConfigurationException(e);
                }
            }
        };
    }

    private List<OxIDPAuthConf> iDPAuthConfs() {
        List<OxIDPAuthConf> authIdpConfs = new ArrayList<OxIDPAuthConf>();
        List<OxIDPAuthConf> idpConfs = trimToEmpty(applianceService.getAppliance().getOxIDPAuthentication());
        for (OxIDPAuthConf idpConf : idpConfs) {
            if (idpConf.getType().equalsIgnoreCase(AUTH)) {
                authIdpConfs.add(idpConf);
            }
        }
        return authIdpConfs;
    }

    public GluuLdapConfiguration findActiveLdapConfiguration() {
        GluuLdapConfiguration result = Iterables.getFirst(findLdapConfigurations(), null);
        if (result == null) {
            throw new LdapConfigurationNotFoundException();
        }
        return result;
    }

    public GluuLdapConfiguration findLdapConfigurationByName(final String name) {
        return new LdapConfigurationLookup(findLdapConfigurations()).findByName(name);
    }

    public void save(List<GluuLdapConfiguration> ldapConfigurations) {
        GluuAppliance appliance = applianceService.getAppliance();
        appliance.setOxIDPAuthentication(oxIDPAuthConfs(ldapConfigurations));
        applianceService.updateAppliance(appliance);
    }

    public void update(GluuLdapConfiguration ldapConfiguration) {
        List<GluuLdapConfiguration> ldapConfigurations = excludeFromConfigurations(new ArrayList<GluuLdapConfiguration>(findLdapConfigurations()),
                ldapConfiguration);
        ldapConfigurations.add(ldapConfiguration);

        save(ldapConfigurations);
    }

    public void save(GluuLdapConfiguration ldapConfiguration) {
        List<GluuLdapConfiguration> ldapConfigurations = new ArrayList<GluuLdapConfiguration>(findLdapConfigurations());
        ldapConfigurations.add(ldapConfiguration);

        save(ldapConfigurations);
    }

    private List<GluuLdapConfiguration> excludeFromConfigurations(List<GluuLdapConfiguration> ldapConfigurations, GluuLdapConfiguration ldapConfiguration) {
        boolean hadConfiguration = Iterables.removeIf(ldapConfigurations, new LdapConfigurationNamePredicate(ldapConfiguration));
        if (!hadConfiguration) {
            throw new LdapConfigurationNotFoundException(ldapConfiguration.getConfigId());
        }
        return ldapConfigurations;
    }

    private List<OxIDPAuthConf> oxIDPAuthConfs(List<GluuLdapConfiguration> ldapConfigurations) {
        final LdapConfigurationLookup ldapConfigurationLookup = new LdapConfigurationLookup(findLdapConfigurations());

        List<OxIDPAuthConf> idpConf = new ArrayList<OxIDPAuthConf>();
        for (GluuLdapConfiguration ldapConfig : ldapConfigurations) {

            if (ldapConfigurationLookup.shouldEncryptPassword(ldapConfig)) {
                ldapConfig.setBindPassword(encrypt(ldapConfig.getBindPassword()));
            }

            if (ldapConfig.isUseAnonymousBind()) {
                ldapConfig.setBindDN(null);
            }

            OxIDPAuthConf ldapConfigIdpAuthConf = new OxIDPAuthConf();
            ldapConfig.updateStringsLists();
            ldapConfigIdpAuthConf.setType(AUTH);
            ldapConfigIdpAuthConf.setVersion(ldapConfigIdpAuthConf.getVersion() + 1);
            ldapConfigIdpAuthConf.setName(ldapConfig.getConfigId());
            ldapConfigIdpAuthConf.setEnabled(ldapConfig.isEnabled());
            ldapConfigIdpAuthConf.setConfig(toJson(ldapConfig));

            idpConf.add(ldapConfigIdpAuthConf);
        }
        return idpConf;
    }

    private String encrypt(String data) {
        try {
            return encryptionService.encrypt(data);
        } catch (StringEncrypter.EncryptionException e) {
            throw new LdapConfigurationException(e);
        }
    }

    private String toJson(GluuLdapConfiguration ldapConfiguration) {
        try {
            return objectMapper.writeValueAsString(ldapConfiguration);
        } catch (IOException e) {
            LOG.error("Error while serializing the GluuLdapConfiguration", e);
            throw new LdapConfigurationException(e);
        }
    }

    public void remove(String name) {
        GluuLdapConfiguration toRemove = findLdapConfigurationByName(name);
        List<GluuLdapConfiguration> allConfiguration = new ArrayList<GluuLdapConfiguration>(findLdapConfigurations());
        List<GluuLdapConfiguration> newConfigurations = excludeFromConfigurations(allConfiguration, toRemove);

        save(newConfigurations);
    }
}