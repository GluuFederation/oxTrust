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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A physical mailing address for a User
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0</a>
 * </p>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Address extends MultiValuedAttribute { // NOSONAR - Builder constructs instances of this class

    private String formatted;
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
    @JsonProperty
    private Type type;

    /**
     * Default constructor for Jackson
     */
    public Address() {
    }


    /**
     * Gets the full mailing address, formatted for display or use with a mailing label.
     * 
     * @return the formatted address
     */
    public String getFormatted() {
        return formatted;
    }

    /**
     * Gets the full street address, which may include house number, street name, etc.
     * 
     * @return the street address
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Gets the city or locality
     * 
     * @return the city or locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * Gets the state or region
     * 
     * @return region the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the postal code
     * 
     * @return postalCode the postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Gets the country name in ISO 3166-1 alpha 2 format, e.g. "DE" or "US".
     * 
     * @return the country
     */
    public String getCountry() {
        return country;
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

    @Override
    public String getOperation() {
        return super.getOperation();
    }

    @Override
    public boolean isPrimary() {
        return super.isPrimary();
    }

    public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((formatted == null) ? 0 : formatted.hashCode());
        result = prime * result + ((locality == null) ? 0 : locality.hashCode());
        result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
        result = prime * result + ((region == null) ? 0 : region.hashCode());
        result = prime * result + ((streetAddress == null) ? 0 : streetAddress.hashCode());
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
        Address other = (Address) obj;
        if (country == null) {
            if (other.country != null) {
                return false;
            }
        } else if (!country.equals(other.country)) {
            return false;
        }
        if (formatted == null) {
            if (other.formatted != null) {
                return false;
            }
        } else if (!formatted.equals(other.formatted)) {
            return false;
        }
        if (locality == null) {
            if (other.locality != null) {
                return false;
            }
        } else if (!locality.equals(other.locality)) {
            return false;
        }
        if (postalCode == null) {
            if (other.postalCode != null) {
                return false;
            }
        } else if (!postalCode.equals(other.postalCode)) {
            return false;
        }
        if (region == null) {
            if (other.region != null) {
                return false;
            }
        } else if (!region.equals(other.region)) {
            return false;
        }
        if (streetAddress == null) {
            if (other.streetAddress != null) {
                return false;
            }
        } else if (!streetAddress.equals(other.streetAddress)) {
            return false;
        }
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
        return "Address [formatted=" + formatted + ", streetAddress=" + streetAddress + ", locality=" + locality
                + ", region=" + region + ", postalCode=" + postalCode + ", country=" + country + ", type="
                + type + ", operation=" + getOperation() + ", primary=" + isPrimary() + "]";
    }

    

    /**
     * Represents an address type. Canonical values are available as static constants.
     */
    public static class Type extends MultiValuedAttributeType {

        public static final Type WORK = new Type("work");
        public static final Type HOME = new Type("home");
        public static final Type OTHER = new Type("other");

        private static Map<String, Type> namesMap = new HashMap<String, Type>(3);

        static {
            namesMap.put(WORK.getValue(), WORK);
            namesMap.put(HOME.getValue(), HOME);
            namesMap.put(OTHER.getValue(), OTHER);
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
