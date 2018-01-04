/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.enums;

/**
 * Created by jgomer on 2017-12-12.
 */
public enum LdapFilterTemplate {
    NULL_NOT_EQUALS("(%s=*)"),
    NULL_EQUALS_INNER("(%s=*\"%s\":null*)"),

    STRING_EQUALS("(%s=%s)"),
    STRING_EQUALS_INNER("(%s=*\"%s\":\"%s\"*)"),
    STRING_CONTAINS("(%s=*%s*)"),
    STRING_CONTAINS_INNER("(%s=*\"%s\":\"*%s*\"*)"),
    STRING_STARTSWITH("(%s=%s*)"),
    STRING_STARTSWITH_INNER("(%s=*\"%s\":\"%s*\"*)"),
    STRING_ENDSWITH("(%s=*%s)"),
    STRING_ENDSWITH_INNER("(%s=*\"%s\":\"*%s\"*)"),
    STRING_GREATER_THAN_OR_EQ("(%s>=%s)"),
    STRING_LESS_THAN_OR_EQ("(%s<=%s)"),

    NUMERIC_EQUALS("(%s=%s)"),
    NUMERIC_EQUALS_INNER("(%s=*\"%s\":%s*)"),
    NUMERIC_GREATER_THAN_OR_EQ("(%s>=%s)"),
    NUMERIC_LESS_THAN_OR_EQ("(%s<=%s)"),

    BOOLEAN_EQUALS("(%s=%s)"),
    BOOLEAN_EQUALS_INNER("(%s=*\"%s\":%s*)")
    ;

    private String template;

    LdapFilterTemplate(String template){
        this.template=template;
    }

    public String get(Object ...args){
        return String.format(template, args);
    }

}
