package org.gluu.oxtrust.model.scim2;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by jgomer on 2017-09-15.
 *
 * An enumeration of all possible (formatting) validations applicable to attributes of SCIM resources. It also contains
 * the implementation of validations themselves in static methods. These methods receive the validation "type" to apply
 * and the object upon which to perform the validation. They return a boolean (true/false) for success/failed validation.
 */
public enum Validations {EMAIL, PHONE, PHOTO, COUNTRY, X509, LOCALE, TIMEZONE;

    private static final Pattern EmailPattern = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
            "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    private static Set<String> validCountries;
    private static Set<String> validTimeZones;

    static {
        validCountries=new HashSet<String>(Arrays.asList(Locale.getISOCountries()));
        validTimeZones=new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    public static boolean apply(Validations validation, Object value){

        boolean pass=false;
        switch (validation){
            case EMAIL:
                pass=validateEmail(value);
                //From spec: The value SHOULD be specified according to [RFC5321].
                break;
            case PHONE:
                pass=true;
                //TODO: implement phone validation
                /*
                From spec: The value SHOULD be specified according to the format defined in [RFC3966], e.g.,
                'tel:+1-201-555-0123'.
                 */
                break;
            case PHOTO:
                pass=validateURI(value);
                //From spec: A URI that is a uniform resource locator (as defined in Section 1.1.3 of [RFC3986])
                break;
            case COUNTRY:
                pass=validateCountry(value);
                //From spec: When specified, the value MUST be in ISO 3166-1 "alpha-2" code format [ISO3166]
                break;
            case X509:
                pass=true;
                //TODO: implement X.509 validation
                //From spec: Each value contains exactly one DER-encoded X.509 certificate (see Section 4 of [RFC5280])
                break;
            case LOCALE:
                pass=true;
                //TODO: implement locale validation
                /*
                From spec: A valid value is a language tag as defined in [RFC5646].  Computer languages are explicitly
                excluded.
                 */
                break;
            case TIMEZONE:
                pass=validateTimezone(value);
                /*
                From spec:  The User's time zone, in IANA Time Zone database format [RFC6557], also known as the "Olson"
                time zone database format [Olson-TZ] (e.g., "America/Los_Angeles").
                 */
                break;
        }
        return pass;

    }

    public static boolean validateEmail(Object val){

        boolean valid=true;
        if (isString(val))
            valid=EmailPattern.matcher(val.toString()).matches();

        return valid;
    }

    public static boolean validateCountry(Object val){

        boolean valid=true;
        if (isString(val))
            valid=validCountries.contains(val.toString().toUpperCase());

        return valid;
    }

    public static boolean validateTimezone(Object val){

        boolean valid=true;
        if (isString(val))
            valid=validTimeZones.contains(val.toString());

        return valid;
    }

    public static boolean validateURI(Object val){

        boolean valid=true;
        try {
            if (isString(val))
                new URI(val.toString());
        }
        catch (Exception e){
            valid=false;
        }
        return valid;

    }

    private static boolean isString(Object val){
        return val instanceof String;
    }

}
