/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
public enum ScimOperator {

    // eq | ne  | co   | sw  | ew  | gt           | lt           | ge | le
    // =  | !() | *{}* | {}* | *{} | (&(>=)(!{})) | (&(<=)(!{})) | >= | <=

    EQUAL ("eq"),
    NOT_EQUAL ("ne"),
    CONTAINS ("co"),
    STARTS_WITH ("sw"),
    ENDS_WITH ("ew"),
    GREATER_THAN ("gt"),
    LESS_THAN ("lt"),
    GREATER_THAN_OR_EQUAL ("ge"),
    LESS_THAN_OR_EQUAL ("le");

    private String value;

    private static Map<String, ScimOperator> mapByValues = new HashMap<String, ScimOperator>();

    static {
        for (ScimOperator enumType : values()) {
            mapByValues.put(enumType.getValue(), enumType);
        }
    }

    private ScimOperator(String value) {
        this.value = value;
    }

    public static String transform(String operator, String leftExpr, String rightExpr) {

        StringBuilder result = new StringBuilder("");

        switch (getByValue(operator.toLowerCase())) {

            case EQUAL:
                result.append(leftExpr);
                result.append("=");
                result.append(rightExpr);
                break;

            case NOT_EQUAL:
                result.append("!(");
                result.append(leftExpr);
                result.append("=");
                result.append(rightExpr);
                result.append(")");
                break;

            case CONTAINS:
                result.append(leftExpr);
                result.append("=*");
                result.append(rightExpr);
                result.append("*");
                break;

            case STARTS_WITH:
                result.append(leftExpr);
                result.append("=");
                result.append(rightExpr);
                result.append("*");
                break;

            case ENDS_WITH:
                result.append(leftExpr);
                result.append("=*");
                result.append(rightExpr);
                break;

            case GREATER_THAN:
                result.append("&(");
                result.append(leftExpr);
                result.append(">=");
                result.append(rightExpr);
                result.append(")");
                result.append("(!(");
                result.append(leftExpr);
                result.append("=");
                result.append(rightExpr);
                result.append("))");
                break;

            case LESS_THAN:
                result.append("&(");
                result.append(leftExpr);
                result.append("<=");
                result.append(rightExpr);
                result.append(")");
                result.append("(!(");
                result.append(leftExpr);
                result.append("=");
                result.append(rightExpr);
                result.append("))");
                break;

            case GREATER_THAN_OR_EQUAL:
                result.append(leftExpr);
                result.append(">=");
                result.append(rightExpr);
                break;

            case LESS_THAN_OR_EQUAL:
                result.append(leftExpr);
                result.append("<=");
                result.append(rightExpr);
                break;

            default:
                break;
        }

        return result.toString();
    }

    public static ScimOperator getByValue(String value) {
        return mapByValues.get(value);
    }

    public ScimOperator resolveByValue(String value) {
        return getByValue(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
