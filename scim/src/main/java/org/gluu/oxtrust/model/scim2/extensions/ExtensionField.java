package org.gluu.oxtrust.model.scim2.extensions;

import org.gluu.oxtrust.model.scim2.util.DateUtil;
import org.xdi.model.GluuAttributeDataType;

import java.util.regex.Pattern;

/**
 * Created by jgomer on 2017-09-29.
 */
public class ExtensionField {

    private static final String XSD_DATE_TIME_PATTERN="^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*$";

    private boolean multiValued;
    private GluuAttributeDataType type;

    public ExtensionField(GluuAttributeDataType type){
        new ExtensionField(type, false);
    }

    public ExtensionField(GluuAttributeDataType type, boolean multiValued){
        this.type=type;
        this.multiValued=multiValued;
    }

    /**
     * Tries parsing the value passed according to the data type associated to the field
     * @param field An ExtensionField instance that determines the data type expected to be received
     * @param val A non-null object that represents a (hopefully valid) value for this field
     * @return Null if the value is not consistent with the data type expected. Otherwise, the same value received is returned
     */
    public static Object valueOf(ExtensionField field, Object val){

        Object value=null;
        switch (field.getType()){
            case STRING:
                if (val instanceof String)
                    value = val;
                break;
            case DATE:
                //Dates are stored and read as strings indeed (no usage of Date-related classes take place)
                if (val instanceof String) {
                    Pattern p = Pattern.compile(XSD_DATE_TIME_PATTERN);
                    if (p.matcher(val.toString()).find())
                        value=val;
                }
                break;
            case NUMERIC:
                if (val instanceof Integer)
                    value = val;
                break;
        }
        return value;

    }

    /**
     * Does the same as valueOf, however, a String is supplied as value. Here no validations of data type consistence takes
     * place (it is expected value passed reflects the type of the field). If the field is a DATE, a previous conversion
     * to ISO format is done, nonetheless, DATE fields still remain being represented as Java strings
     * @param field An ExtensionField
     * @param val A non-null String value
     * @return A value
     */
    public static Object valueFromString(ExtensionField field, String val){

        Object value=null;  //In practice value will never end up being null
        switch (field.getType()){
            case STRING:
                value = val;
                break;
            case DATE:
                //Dates are stored and read as strings indeed (no handling of Date or DateTime objects)
                value=DateUtil.generalizedToISOStringDate(val);
                break;
            case NUMERIC:
                try{
                    value = new Integer(val);
                }
                catch (Exception e){
                    value=null;
                }
                break;
        }
        return value;

    }

    /**
     * Takes an object and a field, and returns a String. For a field data type NUMERIC or STRING, a string representation
     * is returned. When it's DATE, it is converted from ISO to generalized date time format.
     * @param field An instance of ExtensionField
     * @param val A value
     * @return String formated properly
     */
    public static String stringValueOf(ExtensionField field, Object val){

        String value=null;
        switch (field.getType()) {
            case NUMERIC:
            case STRING:
                value=val.toString();
                break;
            case DATE:
                value=DateUtil.ISOToGeneralizedStringDate(val.toString());
                break;
        }
        return value;

    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public GluuAttributeDataType getType() {
        return type;
    }

}