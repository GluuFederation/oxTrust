package org.gluu.oxtrust.model.helper;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.ExtensionFieldType;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.ExtensionFieldType.*;

/**
 * NOTE: this class is part of a temporary fix. This will not be needed for 3.2.0
 *
 * <p>
 * A helper class intended to be used by SCIM-Client mainly for correctly deserialization of custom attributes.
 * The Class ExtensionDeserializer cannot be used there as it's oxTrust server & weld dependant
 * Created by jgomer on 2017-11-16.
 * </p>
 */
public class ExtensionUtil {

    /**
     * Builds an extension based on parameters provided
     * @param urn URN of extension
     * @param map Name/Value pairs of attributes
     * @return Extension instance. Null if urn is null, empty or map is null or empty
     */
    public static Extension fromMap(String urn, Map<String, Object> map){

        //Here, we mimick somehow what ExtensionDeserializer does without using the attribute service managed bean...

        Extension ext=null;
        if (StringUtils.isNotEmpty(urn) && map!=null && map.size()>0){
            try {
                Extension.Builder builder = new Extension.Builder(urn);

                for (String attributeName : map.keySet()){
                    ExtensionFieldType<?> eft;
                    Object value=map.get(attributeName);

                    if (value!=null){
                        //Determine the "type". Here there is no way infer but by guessing using introspection

                        if (Collection.class.isAssignableFrom(value.getClass())){
                            Collection col=(Collection) value;

                            //Ignore empty collections
                            if (col.size()>0) {
                                //Grab the first and determine type
                                Object item = col.toArray()[0];
                                eft=getObjectType(item);

                                if (eft!=null) {
                                    if (eft.equals(STRING))
                                        builder.setFieldAsList(attributeName, new ArrayList<Object>(col));
                                    else
                                    if (eft.equals(DATE_TIME)) {
                                        //List<Date> dates = new ArrayList<Date>();
                                        List<String> dates = new ArrayList<String>();

                                        for (Object date : col) {
                                            String formatted = DateUtil.ISOToGeneralizedStringDate(date.toString());
                                            //Here we expect the string coming in ISO format, and we leave it that way
                                            //because data will be read on the client side
                                            if (formatted != null)
                                                //dates.add(new DateTime(date.toString()).toDate());
                                                dates.add(date.toString());
                                        }
                                        builder.setFieldAsList(attributeName, dates);
                                    }
                                    else
                                    if (eft.equals(DECIMAL)) {
                                        List<BigDecimal> numberList = new ArrayList<BigDecimal>();
                                        for (Object decimal : col) {
                                            numberList.add(new BigDecimal((Integer) decimal));
                                        }
                                        builder.setFieldAsList(attributeName, numberList);
                                    }
                                }
                                else
                                    System.out.println("======don't know what is " + attributeName);
                            }
                        }
                        else{
                            eft=getObjectType(value);
                            if (eft!=null){
                                if (eft.equals(STRING))
                                    builder.setField(attributeName, value.toString());
                                else
                                if (eft.equals(DATE_TIME)){
                                    String formatted=DateUtil.ISOToGeneralizedStringDate(value.toString());

                                    if (formatted != null)
                                        builder.setField(attributeName, new DateTime(value.toString()).toDate());
                                }
                                else
                                if (eft.equals(DECIMAL))
                                    builder.setField(attributeName, new BigDecimal((Integer)value));
                            }
                            else
                                System.out.println("======don't know what is " + attributeName);
                        }
                    }
                }
                ext= builder.build();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return ext;
    }

    private static ExtensionFieldType<?> getObjectType(Object value){

        if (value instanceof String) {
            String strValue = value.toString();
            String valAsDate = DateUtil.ISOToGeneralizedStringDate(strValue);
            if (valAsDate==null)    //Conversion failed
                return STRING;
            else
                return DATE_TIME;
        }
        else
        if (value instanceof Integer)
            return DECIMAL;
        else
            return null;
    }


}
