package org.gluu.oxtrust.api.organization;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

class OrganizationImageDTO {
    @NotNull
    @Size(min = 1)
    private String logo;
    @NotNull
    @Size(min = 1)
    private String favicon;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }
}
