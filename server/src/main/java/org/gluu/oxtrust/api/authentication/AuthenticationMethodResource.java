package org.gluu.oxtrust.api.authentication;

import org.gluu.oxtrust.api.authentication.casprotocol.CasProtocolResource;
import org.gluu.oxtrust.api.authentication.defaultAuthenticationMethod.DefaultAuthenticationMethodResource;
import org.gluu.oxtrust.api.authentication.ldap.LdapResource;
import org.gluu.oxtrust.util.OxTrustApiConstants;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(OxTrustApiConstants.BASE_API_URL + "/configurations/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// TODO Uma
public class AuthenticationMethodResource {

    @Inject
    private LdapResource ldapResource;

    @Inject
    private CasProtocolResource casProtocolResource;

    @Inject
    private DefaultAuthenticationMethodResource defaultAuthenticationMethodResource;

    @Path("/ldap")
    public LdapResource ldapResource() {
        return ldapResource;
    }

    @Path("/cas-protocol")
    public CasProtocolResource casProtocolResource() {
        return casProtocolResource;
    }

    @Path("/auth-method")
    public DefaultAuthenticationMethodResource defaultAuthenticationMethodResource() {
        return defaultAuthenticationMethodResource;
    }

}
