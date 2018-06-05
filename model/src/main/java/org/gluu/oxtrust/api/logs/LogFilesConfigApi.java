package org.gluu.oxtrust.api.logs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gluulogconfig")
public class LogFilesConfigApi {

    private String oxTrustLocation;
    private String oxAuthLocation;
    private Map<String, String> allowedTemplates;

    public String getOxTrustLocation() {
        return oxTrustLocation;
    }

    public void setOxTrustLocation(String oxTrustLocation) {
        this.oxTrustLocation = oxTrustLocation;
    }

    public String getOxAuthLocation() {
        return oxAuthLocation;
    }

    public void setOxAuthLocation(String oxAuthLocation) {
        this.oxAuthLocation = oxAuthLocation;
    }

    public Map<String, String> getAllowedTemplates() {
        return allowedTemplates;
    }

    public void setAllowedTemplates(Map<String, String> allowedTemplates) {
        this.allowedTemplates = allowedTemplates;
    }
}