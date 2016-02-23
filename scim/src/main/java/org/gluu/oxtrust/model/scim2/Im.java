/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.gluu.oxtrust.model.scim2.Email.Type;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a Im attribute.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2">SCIM core schema 2.0, section 3.2</a>
 * </p>
 */
public class Im extends MultiValuedAttribute {

    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public Im() {
    }


    @Override
    public String getOperation() {
        return super.getOperation();
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public String getDisplay() {
        return super.getDisplay();
    }

    @Override
    public boolean isPrimary() {
        return super.isPrimary();
    }

    /**
     * Gets the type of the attribute.
     * 
     * <p>
     * For more detailed information please look at the <a href=
     * "http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-3.2" >SCIM core schema 2.0, section 3.2</a>
     * </p>
     * 
     * @return
     * 
     * @return the actual type
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
		this.type = type;
	}
    
    
    public void setOperation(String operation) {
        super.setOperation(operation);
    }

    
    public void setDisplay(String display) {
        super.setDisplay(display);
    }

    
    public void setValue(String value) {
        super.setValue(value);
    }
    
    public void setPrimary(boolean primary) {
        super.setPrimary(primary);
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Im other = (Im) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return "Im [value=" + getValue() + ", type=" + type + ", primary=" + isPrimary() 
                + ", operation=" + getOperation() + "]";
    }

    

    /**
     * Represents an IM type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {
        public static final Type AIM = new Type("aim");
        public static final Type GTALK = new Type("gtalk");
        public static final Type ICQ = new Type("icq");
        public static final Type XMPP = new Type("xmpp");
        public static final Type MSN = new Type("msn");
        public static final Type SKYPE = new Type("skype");
        public static final Type QQ = new Type("qq");
        public static final Type YAHOO = new Type("yahoo");

        private static Map<String, Type> namesMap = new HashMap<String, Type>(3);

        static {
            namesMap.put(AIM.getValue(), AIM);
            namesMap.put(GTALK.getValue(), GTALK);
            namesMap.put(ICQ.getValue(), ICQ);
            namesMap.put(XMPP.getValue(), XMPP);
            namesMap.put(MSN.getValue(), MSN);
            namesMap.put(SKYPE.getValue(), SKYPE);
            namesMap.put(QQ.getValue(), QQ);
            namesMap.put(YAHOO.getValue(), YAHOO);
            
        }

        public Type(String value) {
            super(value);
        }

        @JsonCreator
        public static MultiValuedAttributeType forValue(String value) {
            return namesMap.get(StringUtils.lowerCase(value));
        }

        @JsonValue
        public String getValue() {
            return super.getValue();
        }
    }

}
