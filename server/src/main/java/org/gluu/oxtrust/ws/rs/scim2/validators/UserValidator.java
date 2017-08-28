package org.gluu.oxtrust.ws.rs.scim2.validators;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.scim2.Address;
import org.gluu.oxtrust.model.scim2.Operation;
import org.gluu.oxtrust.model.scim2.ScimPatchUser;
import org.gluu.oxtrust.model.scim2.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jgomer on 2017-08-17.
 * This class contains methods to validate whether a org.gluu.oxtrust.model.scim2.User instance fulfills certain
 * characteristics - usually regarded to formatting - in order to adhere more closely to SCIM spec
 */
public class UserValidator {

    //@Inject
    //private static Logger log;

    private static Set<String> validCountries;  //See static block at the end of file

    public static boolean validate(User user) {
        //Other validations will go here soon
        return validateCountries(user.getAddresses());
    }

    public static boolean validate(ScimPatchUser user){

        List<Operation> list= user.getOperatons();
        boolean pass = list==null || list.size()==0;

        for (Operation op : list) {
            pass=validate(op.getValue());
            if (!pass) {
                break;
            }
        }
        return pass;

    }

    public static boolean validateCountries(List<Address> addresses){

        boolean pass =  addresses==null || addresses.size()==0;

        for (Address address : addresses){
            String country=address.getCountry();
            pass=StringUtils.isNotEmpty(country) && validCountries.contains(country.toUpperCase());
            if (!pass) {
                break;
            }
        }
        return pass;

    }

    //ISO 3166-1 alpha-2 country codes
    private static final String COUNTRY_CODES[]={
            "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ"
            , "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS", "BT", "BV", "BW", "BY", "BZ"
            , "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CW", "CX", "CY", "CZ"
            , "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET"
            , "FI", "FJ", "FK", "FM", "FO", "FR"
            , "GA", "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY"
            , "HK", "HM", "HN", "HR", "HT", "HU"
            , "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT"
            , "JE", "JM", "JO", "JP"
            , "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ"
            , "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY"
            , "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ"
            , "NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM"
            , "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY"
            , "QA", "RE", "RO", "RS", "RU", "RW"
            , "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV", "SX", "SY", "SZ"
            , "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ"
            , "UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU"
            , "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW"
    };

    static {
        validCountries=new HashSet<String>(Arrays.asList(COUNTRY_CODES));
    }

}
