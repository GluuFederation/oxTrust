/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.model.scim2.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.regex.Pattern;

/**
 * Contains helper methods to convert between dates in ISO format and LDAP generalized time syntax.
 * <p>Examples of ISO-formated dates:</p>
 * <ul>
 *     <li>2011-12-03T10:15:30</li>
 *     <li>2011-12-03T10:15:30.4+01:00</li>
 * </ul>
 * <p>Equivalent dates in generalized time format:</p>
 * <ul>
 *     <li>20111203101530.000Z</li>
 *     <li>20111203111530.400Z</li>
 * </ul>
 */
/*
 * Created by jgomer on 2017-08-23.
 */
public class DateUtil {

    /**
     * Format used by LDAP generalized time syntax (see RFC 4517 section 3.3.13)
     */
    public static final String GENERALIZED_TIME_FORMAT="YYYYMMddHHmmss.SSSZ";

    private DateUtil() {}

    /**
     * Converts a string representation of a date (expected to follow the pattern of DateTime XML schema data type) to a
     * string representation of a date in LDAP generalized time syntax (see RFC 4517 section 3.3.13).
     * <p><code>xsd:dateTime</code> is equivalent to ISO-8601 format, namely, <code>yyyy-MM-dd'T'HH:mm:ss.SSSZZ</code></p>
     * @param strDate A string date in ISO format.
     * @return A String representing a date in generalized time syntax. If the date passed as parameter did not adhere to
     * xsd:dateTime, the returned value is null
     */
    public static String ISOToGeneralizedStringDate(String strDate) {

        String utcFormatted=null;
        try {
            //Check if date passed complies the xsd:dateTime definition
            Pattern p=Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
            if (p.matcher(strDate).find()) {
                //For ISO compliant dates, the new operator suffices to get a DateTime here
                DateTime dt = new DateTime(strDate);
                DateTimeFormatter fmt = DateTimeFormat.forPattern(GENERALIZED_TIME_FORMAT);
                utcFormatted = fmt.withZone(DateTimeZone.UTC).print(dt);
                //drop UTC timezone info (4 zeroes and sign)
                utcFormatted = utcFormatted.substring(0, utcFormatted.length() - 5) + "Z";
            }
        }
        catch (Exception e){
            utcFormatted=null;
        }
        return utcFormatted;

    }

    /**
     * Converts a string representing a date (in the LDAP generalized time syntax) to an ISO-8601 formatted string date.
     * @param strDate A string date in generalized time syntax (see RFC 4517 section 3.3.13)
     * @return A string representation of a date in ISO format. If the date passed as parameter did not adhere to generalized
     * time syntax, null is returned.
     */
    public static String generalizedToISOStringDate(String strDate){

        String isoFormatted;
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(GENERALIZED_TIME_FORMAT);
            DateTime dt = fmt.parseDateTime(strDate);
            isoFormatted = ISODateTimeFormat.dateTime().print(dt);
        }
        catch (Exception e){
            isoFormatted = null;
        }
        return isoFormatted;

    }

    /**
     * Returns a string representation of a date in ISO format based on a number of milliseconds elapsed from "the epoch",
     * namely January 1, 1970, 00:00:00 GMT.
     * @param millis Number of milliseconds
     * @return An ISO-formatted string date
     */
    public static String millisToISOString(long millis){
        //Useful for SCIM-client
        DateTime dt=new DateTime(millis);
        return ISODateTimeFormat.dateTime().print(dt);
    }

}
