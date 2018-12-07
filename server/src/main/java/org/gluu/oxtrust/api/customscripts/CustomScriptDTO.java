/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.api.customscripts;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.xdi.model.AuthenticationScriptUsageType;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.model.ScriptLocationType;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.SimpleExtendedCustomProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.model.custom.script.model.ScriptError;
import org.xdi.model.custom.script.model.auth.AuthenticationCustomScript;
import org.xdi.util.StringHelper;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DTO for custom scripts
 *
 * @author Shoeb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomScriptDTO implements Serializable {

    private String dn;

    private String inum;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Name should contain only letters, digits and underscores"
    )
    @Size(
            min = 1,
            max = 30,
            message = "Length of the Name should be between 1 and 30"
    )
    private String name;

    private String description;

    private String script;

    @NotNull(message = "Script type is required")
    @ScriptType
    private String scriptType;

    @NotNull(message = "Script's programming language is required.")
    @ProgramLang
    private String programmingLanguage;

    private List<SimpleCustomProperty> moduleProperties;

    private List<SimpleExtendedCustomProperty> configurationProperties;

    private int level;

    private long revision;

    private boolean enabled;

    private ScriptError scriptError;

    @NotNull(message = "Location type is required.")
    @ScriptLocation
    private String locationType;

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public List<SimpleCustomProperty> getModuleProperties() {
        if (moduleProperties == null) {
            return moduleProperties = new ArrayList<SimpleCustomProperty>();
        }
        return moduleProperties;
    }

    public void setModuleProperties(List<SimpleCustomProperty> moduleProperties) {
        this.moduleProperties = moduleProperties;
    }

    public List<SimpleExtendedCustomProperty> getConfigurationProperties() {
        return configurationProperties;
    }

    public void setConfigurationProperties(List<SimpleExtendedCustomProperty> configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ScriptError getScriptError() {
        return scriptError;
    }

    public void setScriptError(ScriptError scriptError) {
        this.scriptError = scriptError;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
        if (locationType != null) {
            this.setModuleProperty("location_type", locationType);
        }

    }

    public String getLocationPath() {
        SimpleCustomProperty moduleProperty = this.getModuleProperty("location_path");
        return moduleProperty == null ? null : moduleProperty.getValue2();
    }

    public void setLocationPath(String locationPath) {
        this.setModuleProperty("location_path", locationPath);
    }

    protected SimpleCustomProperty getModuleProperty(String modulePropertyName) {
        SimpleCustomProperty result = null;
        List<SimpleCustomProperty> moduleProperties = this.getModuleProperties();
        if (moduleProperties == null) {
            return result;
        } else {
            Iterator var4 = this.getModuleProperties().iterator();

            while(var4.hasNext()) {
                SimpleCustomProperty moduleProperty = (SimpleCustomProperty)var4.next();
                if (StringHelper.equalsIgnoreCase(moduleProperty.getValue1(), modulePropertyName)) {
                    result = moduleProperty;
                    break;
                }
            }

            return result;
        }
    }

    protected void setModuleProperty(String name, String value) {
        SimpleCustomProperty moduleProperty = this.getModuleProperty(name);
        if (moduleProperty == null) {
            this.addModuleProperty(name, value);
        } else {
            moduleProperty.setValue2(value);
        }

    }
    
    public AuthenticationScriptUsageType getUsageType() {
        SimpleCustomProperty moduleProperty = this.getModuleProperty("usage_type");
        AuthenticationScriptUsageType usageType = null;
        if (moduleProperty == null) {
            return null;
        } else {
            final String propertyValue = moduleProperty.getValue2();
            if (propertyValue == null) return null;
            return AuthenticationScriptUsageType.getByValue(propertyValue.toLowerCase());
        }
    }

    public void setUsageType(AuthenticationScriptUsageType usageType) {
        if (usageType == null) return;
        this.setModuleProperty("usage_type", usageType.getValue());
    }

    public void addModuleProperty(String name, String value) {
        SimpleCustomProperty usageTypeModuleProperties = new SimpleCustomProperty(name, value);
        this.getModuleProperties().add(usageTypeModuleProperties);
    }

    public void removeModuleProperty(String modulePropertyName) {
        List<SimpleCustomProperty> moduleProperties = this.getModuleProperties();
        if (moduleProperties != null) {
            Iterator it = moduleProperties.iterator();

            while(it.hasNext()) {
                SimpleCustomProperty moduleProperty = (SimpleCustomProperty)it.next();
                if (StringHelper.equalsIgnoreCase(moduleProperty.getValue1(), modulePropertyName)) {
                    it.remove();
                    break;
                }
            }

        }
    }


    public String getBaseDn() {
        return this.dn;
    }

    public void setBaseDn(String dn) {
        this.dn = dn;
    }

    @JsonIgnore
    public static CustomScript fromDTO(CustomScriptDTO dto) {

        final CustomScript cs ;

        CustomScriptType dtoScriptType = CustomScriptType.getByValue(dto.scriptType.toLowerCase());

        if (dtoScriptType == CustomScriptType.PERSON_AUTHENTICATION) {
            AuthenticationCustomScript acs = new AuthenticationCustomScript();
            acs.setModuleProperties(new ArrayList<SimpleCustomProperty>());
            acs.setUsageType(dto.getUsageType());
            cs = acs;
        } else
        {
            cs = new CustomScript();
        }

        cs.setScriptType(dtoScriptType);
        cs.setScript(dto.getScript());

        ProgrammingLanguage dtoProgramLang = ProgrammingLanguage.getByValue(dto.getProgrammingLanguage().toLowerCase());
        cs.setProgrammingLanguage(dtoProgramLang);

        cs.setModuleProperties(dto.getModuleProperties());
        cs.setLocationPath(dto.getLocationPath());

        ScriptLocationType dtoScriptLocType = ScriptLocationType.getByValue(dto.getLocationType().toLowerCase());
        cs.setLocationType(dtoScriptLocType);

        cs.setName(dto.getName());
        cs.setConfigurationProperties(dto.getConfigurationProperties());
        cs.setInum(dto.getInum());
        cs.setRevision(dto.getRevision());
        cs.setDescription(dto.getDescription());
        cs.setEnabled(dto.isEnabled());
        cs.setLevel(dto.getLevel());
        cs.setBaseDn(dto.getBaseDn());
        cs.setDn(dto.getDn());
        cs.setScriptError(dto.scriptError);

        return cs;
    }
    
    @JsonIgnore
    public static CustomScriptDTO toDTO(final CustomScript cs) {
        
        final CustomScriptDTO dto = new CustomScriptDTO();

        final CustomScriptType csType = cs.getScriptType();
        dto.setScriptType(csType == null ? "" : csType.getValue());
        dto.setScript(cs.getScript());

        ProgrammingLanguage pl = cs.getProgrammingLanguage();
        dto.setProgrammingLanguage (pl == null ? "" : pl.getValue());

        dto.setModuleProperties(cs.getModuleProperties());
        dto.setLocationPath(cs.getLocationPath());

        final ScriptLocationType locType = cs.getLocationType();
        dto.setLocationType(locType == null ? "" : locType.getValue());
        dto.setName(cs.getName());
        dto.setConfigurationProperties(cs.getConfigurationProperties());
        dto.setInum(cs.getInum());
        dto.setRevision(cs.getRevision());
        dto.setDescription(cs.getDescription());
        dto.setEnabled(cs.isEnabled());
        dto.setLevel(cs.getLevel());
        dto.setBaseDn(cs.getBaseDn());
        dto.setDn(cs.getDn());
        dto.setScriptError(cs.getScriptError());

        return dto;
    } 
    
    
}
