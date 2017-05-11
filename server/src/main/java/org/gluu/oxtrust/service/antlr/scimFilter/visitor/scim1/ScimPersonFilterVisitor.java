/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.gluu.oxtrust.model.scim.PersonMeta;
import org.gluu.oxtrust.model.scim.ScimName;
import org.gluu.oxtrust.model.scim.ScimPerson;
import org.gluu.oxtrust.service.antlr.scimFilter.MainScimFilterVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.ScimOperator;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Val Pecaoco
 */
public class ScimPersonFilterVisitor extends MainScimFilterVisitor {

    private Logger logger = LoggerFactory.getLogger(ScimPersonFilterVisitor.class);

    private static Class[] annotatedClasses = { PersonMeta.class, ScimPerson.class, ScimName.class };

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

        String ldapAttributeName = FilterUtil.stripScim1Schema(attrName);

        String[] tokens = ldapAttributeName.split("\\.");

        // This is already specific implementation. Currently only support up to second level.
        ldapAttributeName = tokens[0];
        if (tokens[0].equalsIgnoreCase("name")) {
            if (tokens.length == 1) {
                ldapAttributeName = "inum";
            } else if (tokens.length == 2) {
                ldapAttributeName = tokens[1];
            }
        } else if (tokens[0].equalsIgnoreCase("meta")) {
            if (tokens.length == 1) {
                ldapAttributeName = "meta";
            } else if (tokens.length == 2) {
                ldapAttributeName = tokens[1];
            }
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

    private String attrOperCriteriaResolver(String attrName, String operator, String criteria) {

        logger.info(" ScimPersonFilterVisitor.attrOperCriteriaResolver() ");

        attrName = FilterUtil.stripScim1Schema(attrName);

        String[] tokens = attrName.split("\\.");

        String ldapAttributeName = getUserLdapAttributeName(attrName);

        criteria = evaluateMultivaluedCriteria(criteria, operator, tokens);

        logger.info(" ##### attrName = " + attrName + ", ldapAttributeName = " + ldapAttributeName + ", criteria = " + criteria);

        String expr = ScimOperator.transform(operator, ldapAttributeName, criteria);
        logger.info(" ##### expr = " + expr);

        return expr;
    }

    /**
     * {@InjectheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitEXPR_AND_EXPR(ScimFilterParser.EXPR_AND_EXPRContext ctx) {

        logger.info(" IN ScimPersonFilterVisitor.visitEXPR_AND_EXPR()... ");

        boolean isMultivalued = false;

        ParseTree parent = ctx.getParent();
        while (parent != null) {

            if (parent.getClass().getSimpleName().equalsIgnoreCase(ScimFilterParser.LBRAC_EXPR_RBRACContext.class.getSimpleName())) {

                logger.info("********** PARENT = " + parent.getClass().getSimpleName());

                isMultivalued = true;
                break;

            } else {
                parent = parent.getParent();
            }
        }

        if (!isMultivalued) {

            String leftText = FilterUtil.stripScim1Schema(ctx.expression(0).getChild(0).getText());
            String rightText = FilterUtil.stripScim1Schema(ctx.expression(1).getChild(0).getText());

            String[] leftTextTokens = leftText.split("\\.");
            String[] rightTextTokens = rightText.split("\\.");

            if (leftTextTokens[0].equalsIgnoreCase(rightTextTokens[0]) && !leftTextTokens[0].equalsIgnoreCase("name")) {
                isMultivalued = true;
            }
        }

        String leftExp = visit(ctx.expression(0));
        String rightExp = visit(ctx.expression(1));

        StringBuilder result = new StringBuilder("");

        if (isMultivalued && !leftExp.startsWith("!(") && !rightExp.startsWith("!(")) {

            if (!leftExp.startsWith("&(") || !leftExp.startsWith("|(")) {

                String[] leftExpTokens = leftExp.split("\\=");
                String[] rightExpTokens = rightExp.split("\\=");

                if (leftExpTokens[0].equalsIgnoreCase(rightExpTokens[0])) {

                    result.append("&(|(");
                    result.append(leftExpTokens[0]);
                    result.append("=");
                    result.append(leftExpTokens[1]);
                    result.append("*");
                    result.append(rightExpTokens[1]);
                    result.append(")");
                    result.append("(");
                    result.append(leftExpTokens[0]);
                    result.append("=");
                    result.append(rightExpTokens[1]);
                    result.append("*");
                    result.append(leftExpTokens[1]);
                    result.append("))");

                } else {
                    result.append(transformToLdapAndFilter(leftExp, rightExp));
                }

            } else {
                result.append(transformToLdapAndFilter(leftExp, rightExp));
            }

        } else {
            result.append(transformToLdapAndFilter(leftExp, rightExp));
        }

        logger.info("##### parsed filter = " + result.toString().replaceAll("\\*{2,}","\\*"));

        logger.info(" LEAVING ScimPersonFilterVisitor.visitEXPR_AND_EXPR()... ");

        return result.toString().replaceAll("\\*{2,}","\\*");
    }

    /**
     * {@InjectheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_OPER_CRITERIA(ScimFilterParser.ATTR_OPER_CRITERIAContext ctx) {

        logger.info(" ScimPersonFilterVisitor.visitATTR_OPER_CRITERIA() ");

        ParseTree parent = ctx.getParent();
        while (parent != null) {

            if (parent.getClass().getSimpleName().equalsIgnoreCase(ScimFilterParser.LBRAC_EXPR_RBRACContext.class.getSimpleName())) {

                logger.info("********** PARENT = " + parent.getClass().getSimpleName());

                String attrName = ((ScimFilterParser.LBRAC_EXPR_RBRACContext)parent).ATTRNAME() + "." + ctx.ATTRNAME().getText();
                return attrOperCriteriaResolver(attrName, visit(ctx.operator()), visit(ctx.criteria()));

            } else {
                parent = parent.getParent();
            }
        }

        return attrOperCriteriaResolver(ctx.ATTRNAME().getText(), visit(ctx.operator()), visit(ctx.criteria()));
    }

    /**
     * {@InjectheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_OPER_EXPR(ScimFilterParser.ATTR_OPER_EXPRContext ctx) {

        logger.info(" ScimPersonFilterVisitor.visitATTR_OPER_EXPR() ");

        ParseTree parent = ctx.getParent();
        while (parent != null) {

            if (parent.getClass().getSimpleName().equalsIgnoreCase(ScimFilterParser.LBRAC_EXPR_RBRACContext.class.getSimpleName())) {

                logger.info("********** PARENT = " + parent.getClass().getSimpleName());

                String attrName = ((ScimFilterParser.LBRAC_EXPR_RBRACContext)parent).ATTRNAME() + "." + ctx.ATTRNAME().getText();
                return attrOperCriteriaResolver(attrName, visit(ctx.operator()), visit(ctx.expression()));

            } else {
                parent = parent.getParent();
            }
        }

        return attrOperCriteriaResolver(ctx.ATTRNAME().getText(), visit(ctx.operator()), visit(ctx.expression()));
    }

    /**
     * {@InjectheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitATTR_PR(ScimFilterParser.ATTR_PRContext ctx) {

        logger.info(" ScimPersonFilterVisitor.visitATTR_PR() ");

        String attrName = ctx.ATTRNAME().getText();

        ParseTree parent = ctx.getParent();
        while (parent != null) {

            if (parent.getClass().getSimpleName().equalsIgnoreCase(ScimFilterParser.LBRAC_EXPR_RBRACContext.class.getSimpleName())) {

                logger.info("********** PARENT = " + parent.getClass().getSimpleName());

                attrName = ((ScimFilterParser.LBRAC_EXPR_RBRACContext)parent).ATTRNAME() + "." + ctx.ATTRNAME().getText();
                break;

            } else {
                parent = parent.getParent();
            }
        }

        attrName = FilterUtil.stripScim1Schema(attrName);
        String[] tokens = attrName.split("\\.");

        String ldapAttributeName = getUserLdapAttributeName(attrName);

        logger.info(" ##### ATTRNAME = " + ctx.ATTRNAME().getText() + ", ldapAttributeName = " + ldapAttributeName);

        String expr = evaluateIsPresentCriteria(ldapAttributeName, tokens);
        logger.info(" ##### expr = " + expr);

        return expr;
    }
}
