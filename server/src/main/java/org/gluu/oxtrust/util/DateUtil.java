package org.gluu.oxtrust.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.regex.Pattern;

/**
 * Created by jgomer on 2017-08-23.
 * Contains methods to encompass data between expected dates of xsd:dateTime type and LDAP generalized time type
 */
public class DateUtil {

    public static final String GENERALIZED_TIME_FORMAT="YYYYMMddHHmmss.SSSZ";

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

    public static String generalizedToISOStringDate(String strDate){
        DateTimeFormatter fmt = DateTimeFormat.forPattern(GENERALIZED_TIME_FORMAT);
        DateTime dt=fmt.parseDateTime(strDate);
        return ISODateTimeFormat.dateTime().print(dt);
    }

}
