/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.visitor;

import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.service.antlr.scimFilter.MainScimFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.ScimOperator;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Val Pecaoco
 */
public class UserFilterVisitor extends MainScimFilterVisitor {

    Logger logger = LoggerFactory.getLogger(UserFilterVisitor.class);

    private static Class[] annotatedClasses = { User.class, Name.class };

    private static Map<String, String> declaredAnnotations = new HashMap<String, String>();

    static {

        for (Class clazz : annotatedClasses) {

            for (Field nameField : clazz.getDeclaredFields()) {

                for (Annotation annotation : nameField.getDeclaredAnnotations()) {

                    if (annotation instanceof LdapAttribute) {
                        LdapAttribute ldapAttribute = (LdapAttribute)annotation;
                        declaredAnnotations.put(nameField.getName(), ldapAttribute.name());
                    }
                }
            }
        }
    }

    public static String getUserLdapAttributeName(String attrName) {

        String[] tokens = attrName.split("\\.");

        // This is already specific implementation. Currently only support up to second level.
        String ldapAttributeName = tokens[0];
        if (tokens.length == 2 && tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {
            ldapAttributeName = tokens[1];
        }

        if (ldapAttributeName != null && !ldapAttributeName.isEmpty()) {

            for (Map.Entry<String, String> entry : declaredAnnotations.entrySet()) {

                if (ldapAttributeName.equalsIgnoreCase(entry.getKey())) {
                    ldapAttributeName = entry.getValue();
                    break;
                }
            }
        }

        return ldapAttributeName;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_OPER_CRITERIA(ScimFilterParser.ATTR_OPER_CRITERIAContext ctx) {

        // logger.info(" UserFilterVisitor.visitATTR_OPER_CRITERIA() ");

        String attrName = ctx.ATTRNAME().getText();
        String[] tokens = attrName.split("\\.");

        String ldapAttributeName = getUserLdapAttributeName(attrName);
        String operator = visit(ctx.operator());
        String criteria = visit(ctx.criteria());

        // This is already specific implementation. Currently only support up to second level.
        if (tokens.length == 2 && !tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {

            StringBuilder sb = new StringBuilder();

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.ENDS_WITH) ||
                    ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS)) {
                sb.append("\"");
            } else {
                sb.append("*\"");
            }

            sb.append(tokens[1]);
            sb.append("\":\"");
            sb.append(criteria);

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.STARTS_WITH) ||
                    ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS)) {
                sb.append("\"");
            } else {
                sb.append("\"*");
            }

            criteria = sb.toString();
        }

        logger.info(" ##### ATTRNAME = " + ctx.ATTRNAME().getText() + ", ldapAttributeName = " + ldapAttributeName + ", criteria = " + criteria);

        String expr = ScimOperator.transform(operator, ldapAttributeName, criteria);
        logger.info(" ##### expr = " + expr);

        return expr;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_OPER_EXPR(ScimFilterParser.ATTR_OPER_EXPRContext ctx) {

        // logger.info(" UserFilterVisitor.visitATTR_OPER_EXPR() ");

        String attrName = ctx.ATTRNAME().getText();
        String[] tokens = attrName.split("\\.");

        String ldapAttributeName = getUserLdapAttributeName(attrName);
        String operator = visit(ctx.operator());
        String expression = visit(ctx.expression());

        // This is already specific implementation. Currently only support up to second level.
        if (tokens.length == 2 && !tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {

            StringBuilder sb = new StringBuilder();

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.ENDS_WITH) ||
                    ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS)) {
                sb.append("\"");
            } else {
                sb.append("*\"");
            }

            sb.append(tokens[1]);
            sb.append("\":\"");
            sb.append(expression);

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.STARTS_WITH) ||
                    ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS)) {
                sb.append("\"");
            } else {
                sb.append("\"*");
            }

            expression = sb.toString();
        }

        logger.info(" ##### ATTRNAME = " + ctx.ATTRNAME().getText() + ", ldapAttributeName = " + ldapAttributeName + ", expression = " + expression);

        return ScimOperator.transform(operator, ldapAttributeName, expression);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_PR(ScimFilterParser.ATTR_PRContext ctx) {

        // logger.info(" UserFilterVisitor.visitATTR_PR() ");

        String attrName = ctx.ATTRNAME().getText();
        String[] tokens = attrName.split("\\.");

        String ldapAttributeName = getUserLdapAttributeName(attrName);
        String pr = "=*";

        // This is already specific implementation. Currently only support up to second level.
        if (tokens.length == 2 && !tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {

            StringBuilder sb = new StringBuilder();
            sb.append("=*\"");
            sb.append(tokens[1]);
            sb.append("\":*");
            pr = sb.toString();
        }

        StringBuilder result = new StringBuilder("");

        logger.info(" ##### ATTRNAME = " + ctx.ATTRNAME().getText() + ", ldapAttributeName = " + ldapAttributeName);

        result.append(ldapAttributeName);
        result.append(pr);

        String expr = result.toString();
        logger.info(" ##### expr = " + expr);

        return expr;
    }
}
