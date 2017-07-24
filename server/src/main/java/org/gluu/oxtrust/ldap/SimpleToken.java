package org.gluu.oxtrust.ldap;

import org.gluu.site.ldap.persistence.annotation.LdapDN;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

/**
 * Created by jgomer on 2017-07-15.
 */

@LdapEntry
@LdapObjectClass(values = {"top", "oxAuthToken"})
public class SimpleToken {

    @LdapDN
    private String dn;

    public SimpleToken(){

    }

    public void setDn(String dn) {
        this.dn = dn;
    }

}