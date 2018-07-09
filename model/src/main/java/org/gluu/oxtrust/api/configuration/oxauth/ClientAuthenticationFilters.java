package org.gluu.oxtrust.api.configuration.oxauth;

public class ClientAuthenticationFilters {
    private String baseDn;

    private String bindPasswordAttribute;

    private String bind;

    private String filter;

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getBindPasswordAttribute() {
        return bindPasswordAttribute;
    }

    public void setBindPasswordAttribute(String bindPasswordAttribute) {
        this.bindPasswordAttribute = bindPasswordAttribute;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

}