/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.enums;

import org.gluu.search.filter.Filter;

import java.util.stream.Stream;

import static org.gluu.oxtrust.service.antlr.scimFilter.enums.FilterTemplate.SubstringAssertion.SUB_ANY;
import static org.gluu.oxtrust.service.antlr.scimFilter.enums.FilterTemplate.SubstringAssertion.SUB_FINAL;
import static org.gluu.oxtrust.service.antlr.scimFilter.enums.FilterTemplate.SubstringAssertion.SUB_INITIAL;

/**
 * Created by jgomer on 2017-12-12.
 */
public enum FilterTemplate {
    NULL_NOT_EQUALS,    //%s=*
    NULL_EQUALS_INNER,  //%s=*"%s":null*
    STRING_EQUALS,      //%s=%s
    STRING_EQUALS_INNER,//%s=*"%s":"%s"*
    STRING_CONTAINS,    //%s=*%s*
    STRING_CONTAINS_INNER,   //%s=*"%s":"*%s*"*
    STRING_STARTSWITH,      //%s=%s*
    STRING_STARTSWITH_INNER, //%s=*"%s":"%s*"*
    STRING_ENDSWITH,     //%s=*%s
    STRING_ENDSWITH_INNER,  //%s=*"%s":"*%s"*
    STRING_GREATER_THAN_OR_EQ,   //%s>=%s
    STRING_LESS_THAN_OR_EQ,  //%s<=%s
    NUMERIC_EQUALS,  //%s=%s
    NUMERIC_EQUALS_INNER,    //%s=*"%s":%s*
    NUMERIC_GREATER_THAN_OR_EQ,  //%s>=%s,
    NUMERIC_LESS_THAN_OR_EQ, //%s<=%s
    BOOLEAN_EQUALS,  //%s=%s
    BOOLEAN_EQUALS_INNER    //%s=*"%s":%s*
    ;

    enum SubstringAssertion { SUB_INITIAL, SUB_ANY, SUB_FINAL }

    public Filter get(String attribute, String subAttribute, String value) {

        switch (this) {
            case NULL_NOT_EQUALS:
                return Filter.createPresenceFilter(attribute);
            case NULL_EQUALS_INNER:
                return getSubstringFilter(attribute, SUB_ANY, "\"%s\":null", subAttribute, null);
            case STRING_EQUALS:
            case NUMERIC_EQUALS:
            case BOOLEAN_EQUALS:
                return Filter.createEqualityFilter(attribute, value);
            case STRING_EQUALS_INNER:
                return getSubstringFilter(attribute, SUB_ANY, "\"%s\":\"%s\"", subAttribute, value);
            case STRING_CONTAINS:
                return getSubstringFilter(attribute, SUB_ANY, "%s", null, value);
            case STRING_CONTAINS_INNER:
                return getSubstringFilter(attribute, SUB_ANY,  "\"%s\":\"*%s*\"", subAttribute, value);
            case STRING_STARTSWITH:
                return getSubstringFilter(attribute, SUB_INITIAL, "%s", null, value);
            case STRING_STARTSWITH_INNER:
                return getSubstringFilter(attribute, SUB_ANY, "\"%s\":\"%s*\"", subAttribute, value);
            case STRING_ENDSWITH:
                return getSubstringFilter(attribute, SUB_FINAL, "%s", null, value);
            case STRING_ENDSWITH_INNER:
                return getSubstringFilter(attribute, SUB_ANY, "\"%s\":\"*%s\"", subAttribute, value);
            case STRING_GREATER_THAN_OR_EQ:
            case NUMERIC_GREATER_THAN_OR_EQ:
                return Filter.createGreaterOrEqualFilter(attribute, value);
            case STRING_LESS_THAN_OR_EQ:
            case NUMERIC_LESS_THAN_OR_EQ:
                return Filter.createLessOrEqualFilter(attribute, value);
            case NUMERIC_EQUALS_INNER:
            case BOOLEAN_EQUALS_INNER:
                return getSubstringFilter(attribute, SUB_ANY, "\"%s\":%s", subAttribute, value);
        }
        return null;

    }

    public Filter get(String attribute, String value) {
        return get(attribute, null, value);
    }

    public Filter get(String attribute) {
        return get(attribute, null);
    }

    private Filter getSubstringFilter(String attribute, SubstringAssertion ass, String pattern, String subAttribute, String value) {

        Object[] params = Stream.of(subAttribute, value).filter(p -> p != null).toArray();
        String sub = String.format(pattern, params);

        switch (ass) {
            case SUB_INITIAL:
                return Filter.createSubstringFilter(attribute, sub, null, null);
            case SUB_ANY:
                return Filter.createSubstringFilter(attribute, null, new String[]{sub}, null);
            case SUB_FINAL:
                return Filter.createSubstringFilter(attribute, null, null, sub);
        }
        return null;

    }

}
