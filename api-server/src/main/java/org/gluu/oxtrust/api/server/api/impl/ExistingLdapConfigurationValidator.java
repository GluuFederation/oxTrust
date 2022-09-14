package org.gluu.oxtrust.api.server.api.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.codec.binary.StringUtils;
import org.gluu.model.ldap.GluuLdapConfiguration;
import org.gluu.oxtrust.api.server.model.LdapConfigurationDTO;
import org.gluu.oxtrust.service.LdapConfigurationService;

import javax.inject.Inject;

import static org.gluu.oxtrust.util.CollectionsUtil.equalsUnordered;

public class ExistingLdapConfigurationValidator {

	@Inject
	private LdapConfigurationService ldapConfigurationService;

	public boolean isInvalid(LdapConfigurationDTO ldapConfiguration) {
		return FluentIterable.from(ldapConfigurationService.findLdapConfigurations())
				.anyMatch(havingSamePropertiesAs(ldapConfiguration));
	}

	private Predicate<GluuLdapConfiguration> havingSamePropertiesAs(final LdapConfigurationDTO ldapConfiguration) {
		return new Predicate<GluuLdapConfiguration>() {
			@Override
			public boolean apply(GluuLdapConfiguration gluuLdapConfiguration) {
				return StringUtils.equals(ldapConfiguration.getConfigId(), gluuLdapConfiguration.getConfigId())
						|| equalsUnordered(ldapConfiguration.getServers(),
								gluuLdapConfiguration.getServersStringsList());
			}
		};
	}

}
