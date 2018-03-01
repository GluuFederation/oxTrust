package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.List;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

/**
 * Sector Identifier
 *
 * @author Javier Rojas Blum
 * @version January 15, 2016
 */
@LdapEntry(sortBy = {"inum"})
@LdapObjectClass(values = {"top", "oxSectorIdentifier"})
public class OxAuthSectorIdentifier extends Entry implements Serializable {

    private static final long serialVersionUID = -2812480357430436514L;

    private transient boolean selected;

    @LdapAttribute(ignoreDuringUpdate = true)
    private String inum;

    @LdapAttribute(name = "oxAuthRedirectURI")
    private List<String> redirectUris;

    @LdapAttribute(name = "oxAuthClientId")
    private List<String> clientIds;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    @Override
    public String toString() {
        return String
                .format("OxAuthSectorIdentifier [inum=%s, toString()=%s]",
                        inum, super.toString());
    }
}
