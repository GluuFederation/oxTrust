/*
 * oxTrust is available under the MIT License (2008). 
 * See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.helper.DateUtil;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.ldap.model.GluuStatus;

/**
 * Person
 *
 * @author Yuriy Movchan Date: 10.21.2010
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "gluuPerson"})
public class GluuCustomPerson extends User 
                                    implements Serializable {

    private static final long serialVersionUID = -1879582184398161112L;

    private transient boolean selected;

    private String sourceServerName;

    @LdapAttribute(name = "gluuWhitePagesListed")
    private String gluuAllowPublication;
    

    @LdapAttribute(name = "oxGuid")
    private String guid;

    @LdapAttribute(name = "gluuOptOuts")
    private List<String> gluuOptOuts;

    @LdapAttribute(name = "associatedClient")
    private List<String> associatedClient;
    
    @LdapAttribute(name = "oxPPID")
    private List<String> oxPPID;
    
    @LdapAttribute(name = "oxExternalUid")
    private List<String> oxExternalUid;

	@LdapAttribute(name = "oxCreationTimestamp")
    private Date creationDate;	

	@LdapAttribute
    private Date updatedAt;	

	// Value object holders
    private Set<String> schemas = new HashSet<String>();
    private Map<String, Extension> extensions = new HashMap<String, Extension>();

    public String getIname() {
        return getAttribute("iname");
    }

    public void setIname(String value) {
        setAttribute("iname", value);
    }

    public String getMail() {
        return getAttribute("mail");
    }

    public void setMail(String value) {
        setAttribute("mail", value);
    }
    
    public String getGluuIMAPData() {
        return getAttribute("gluuIMAPData");
    }

    public void setGluuIMAPData(String value) {
        setAttribute("gluuIMAPData", value);
    }

    public String getNetworkPoken() {
        return getAttribute("networkPoken");
    }

    public void setNetworkPoken(String value) {
        setAttribute("networkPoken", value);
    }

    public String getCommonName() {
        return getAttribute("cn");
    }

    public void setCommonName(String value) {
        setAttribute("cn", value);
    }

    public String getGivenName() {
        return getAttribute("givenName");
    }

    public void setGivenName(String value) {
        setAttribute("givenName", value);
    }

    public GluuStatus getStatus() {
        return GluuStatus.getByValue(getAttribute("gluuStatus"));
    }

    public void setStatus(GluuStatus value) {
        setAttribute("gluuStatus", value.getValue());
    }

    public String getUserPassword() {
        return getAttribute("userPassword");
    }

    public void setUserPassword(String value) {
        setAttribute("userPassword", value);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public GluuBoolean getSLAManager() {
        return GluuBoolean.getByValue(getAttribute("gluuSLAManager"));
    }

    public void setSLAManager(GluuBoolean value) {
        setAttribute("gluuSLAManager", value.getValue());
    }

    public List<String> getMemberOf() {
        String[] value = {};
        for (GluuCustomAttribute attribute : customAttributes) {
            if (attribute.getName().equalsIgnoreCase("memberOf")) {
                value = attribute.getValues();
                break;
            }
        }
        return Arrays.asList(value);
    }

    public void setMemberOf(List<String> value) {
        setAttribute("memberOf", value.toArray(new String[] {}));
    }

    public String getSurname() {
        return getAttribute("sn");
    }

    public void setSurname(String value) {
        setAttribute("sn", value);
    }

    public void setTimezone(String value) {
        setAttribute("zoneinfo", value);
    }

    public String getTimezone() {
        return getAttribute("zoneinfo");
    }

    public void setPreferredLanguage(String value) {
        setAttribute("preferredLanguage", value);
    }

    public String getPreferredLanguage() {
        return getAttribute("preferredLanguage");
    }

    public int getAttributeIndex(String attributeName) {
        int idx = 0;
        for (GluuCustomAttribute attribute : customAttributes) {
            if (attribute.getName().equalsIgnoreCase(attributeName)) {
                return idx;
            }
            idx++;
        }

        return idx;
    }

    public String getAttribute(String attributeName) {
        String value = null;
        for (GluuCustomAttribute attribute : customAttributes) {
            if (attribute.getName().equalsIgnoreCase(attributeName)) {
                value = attribute.getValue();
                break;
            }
        }
        return value;
    }

    public String[] getAttributeArray(String attributeName) {
        GluuCustomAttribute gluuCustomAttribute = 
                                getGluuCustomAttribute(attributeName);
        if (gluuCustomAttribute == null) {
            return null;
        } else {
            return gluuCustomAttribute.getValues();
        }
    }

    public GluuCustomAttribute getGluuCustomAttribute(String attributeName) {
        for (GluuCustomAttribute gluuCustomAttribute : customAttributes) {
            if (gluuCustomAttribute.getName().equalsIgnoreCase(attributeName)) {
                return gluuCustomAttribute;
            }
        }

        return null;
    }

    public void setAttribute(String attributeName, String attributeValue) {
        GluuCustomAttribute attribute = new GluuCustomAttribute(attributeName, 
                                                                attributeValue);
        customAttributes.remove(attribute);
        customAttributes.add(attribute);
    }

    public void setAttribute(String attributeName, String[] attributeValue) {
        GluuCustomAttribute attribute = new GluuCustomAttribute(attributeName, 
                                                                attributeValue);
        customAttributes.remove(attribute);
        customAttributes.add(attribute);
    }

    public void removeAttribute(String attributeName) {
        for (Iterator<GluuCustomAttribute> it = customAttributes.iterator(); 
                                                                it.hasNext();) {
            GluuCustomAttribute attribute = (GluuCustomAttribute) it.next();
            if (attribute.getName().equalsIgnoreCase(attributeName)) {
                it.remove();
                break;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setGluuAllowPublication(String allowPublication) {
        this.gluuAllowPublication = allowPublication;
    }

    public String getGluuAllowPublication() {
        return gluuAllowPublication;
    }

    public boolean isAllowPublication() {
        return Boolean.parseBoolean(gluuAllowPublication);
    }

    public void setAllowPublication(boolean allowPublication) {
        this.gluuAllowPublication = Boolean.toString(allowPublication);
    }

    public void setGluuOptOuts(List<String> optOuts) {
        this.gluuOptOuts = optOuts;
    }

    public List<String> getGluuOptOuts() {
        return gluuOptOuts;
    }

    public List<String> getAssociatedClient() {
        return this.associatedClient;
    }

    public void setAssociatedClient(List<String> associatedClientDNs) {
        this.associatedClient = associatedClientDNs;
    }

    public String getSourceServerName() {
        return sourceServerName;
    }

    public void setSourceServerName(String sourceServerName) {
        this.sourceServerName = sourceServerName;
    }

    public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<String> getSchemas() {
        return Collections.unmodifiableSet(schemas);
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    public Map<String, Extension> getExtensions() {
        return Collections.unmodifiableMap(extensions);
    }
    
    public Map<String, Extension> fetchExtensions() {
        return extensions;
    }
    
    public List<String> getOxExternalUid() {
		return oxExternalUid;
	}

	public void setOxExternalUid(List<String> oxExternalUid) {
		this.oxExternalUid = oxExternalUid;
	}

	public List<String> getOxPPID() {
		return oxPPID;
	}

	public void setOxPPID(List<String> oxPPID) {
		this.oxPPID = oxPPID;
	}

    public void setExtensions(Map<String, Extension> extensions) throws Exception {

        this.extensions = extensions;

        for (Map.Entry<String, Extension> extensionEntry : extensions.entrySet()) {

            Extension extension = extensionEntry.getValue();

            for (Map.Entry<String, Extension.Field> fieldEntry : extension.getFields().entrySet()) {
                String value=fieldEntry.getValue().getValue();

                if (value != null) {
                    if (fieldEntry.getValue().isMultiValued()) {

                        ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

                        String[] values = mapper.readValue(value, String[].class);
                        setAttribute(fieldEntry.getKey(), values);
                    }
                    else{
                        if (fieldEntry.getValue().getType().equals(ExtensionFieldType.DATE_TIME)){
                            //In the extension object, single valued dates are stored in ISO time, so convert to LDAP suitable format
                            value=DateUtil.ISOToGeneralizedStringDate(value);
                        }
                        setAttribute(fieldEntry.getKey(), value);
                    }
                }
            }
        }
    }

	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj != null && getInum()!=null && obj instanceof GluuCustomPerson){
            result = getInum().equals(((GluuCustomPerson) obj).getInum());
        }

        return result;
    }
    
    public GluuCustomPerson clone() throws CloneNotSupportedException{
    	return (GluuCustomPerson) super.clone();
    }

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
}
