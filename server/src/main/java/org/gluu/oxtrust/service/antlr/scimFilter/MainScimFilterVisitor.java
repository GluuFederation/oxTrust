/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter;

import org.antlr.v4.runtime.tree.ParseTree;
import org.gluu.oxtrust.model.scim2.Name;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterBaseVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.ScimOperator;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts input filter to LDAP string filter format
 *
 * @author Val Pecaoco
 */
public class MainScimFilterVisitor extends ScimFilterBaseVisitor<String> {

    Logger logger = LoggerFactory.getLogger(MainScimFilterVisitor.class);

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public String visitScimFilter(ScimFilterParser.ScimFilterContext ctx) {

        logger.info(" visitScimFilter() ");

        StringBuilder result = new StringBuilder("");
        result.append("(");
        for (ScimFilterParser.ExpressionContext expressionContext : ctx.expression()) {
            result.append(visit(expressionContext));
        }
        result.append(")");

        return result.toString();
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
    public String visitLPAREN_EXPR_RPAREN(ScimFilterParser.LPAREN_EXPR_RPARENContext ctx) {
        // logger.info(" visitLPAREN_EXPR_RPAREN() ");
        return visit(ctx.expression());
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
    public String visitLBRAC_EXPR_RBRAC(ScimFilterParser.LBRAC_EXPR_RBRACContext ctx) {

        logger.info(" IN MainScimFilterVisitor.visitLBRAC_EXPR_RBRAC()... ");

        StringBuilder result = new StringBuilder("");
        // result.append("&");
        // result.append("(");
        // result.append(ctx.ATTRNAME());
        // result.append("=*");
        // result.append(")");
        // result.append("(");
        result.append(visit(ctx.expression()));  // Add check if child attributes belong to the parent
        // result.append(")");

        logger.info(" LEAVING MainScimFilterVisitor.visitLBRAC_EXPR_RBRAC()... ");

        return result.toString();
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
    public String visitNOT_EXPR(ScimFilterParser.NOT_EXPRContext ctx) {

        // logger.info(" visitNOT_EXPR() ");

        StringBuilder result = new StringBuilder("");
        result.append("!(");
        result.append(visit(ctx.expression()));
        result.append(")");

        return result.toString();
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
    public String visitEXPR_AND_EXPR(ScimFilterParser.EXPR_AND_EXPRContext ctx) {

        logger.info(" IN MainScimFilterVisitor.visitEXPR_AND_EXPR()... ");

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

            String leftText = FilterUtil.stripScimSchema(ctx.expression(0).getChild(0).getText());
            String rightText = FilterUtil.stripScimSchema(ctx.expression(1).getChild(0).getText());

            String[] leftTextTokens = leftText.split("\\.");
            String[] rightTextTokens = rightText.split("\\.");

            if (leftTextTokens[0].equalsIgnoreCase(rightTextTokens[0]) && !leftTextTokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {
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

        logger.info(" LEAVING MainScimFilterVisitor.visitEXPR_AND_EXPR()... ");

        return result.toString().replaceAll("\\*{2,}","\\*");
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
    public String visitEXPR_OR_EXPR(ScimFilterParser.EXPR_OR_EXPRContext ctx) {

        // logger.info(" visitEXPR_OR_EXPR() ");

        StringBuilder result = new StringBuilder("");
        result.append("|(");
        result.append(visit(ctx.expression(0)));
        result.append(")");
        result.append("(");
        result.append(visit(ctx.expression(1)));
        result.append(")");

        return result.toString();
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
    public String visitEXPR_OPER_EXPR(ScimFilterParser.EXPR_OPER_EXPRContext ctx) {
        // logger.info(" visitEXPR_OPER_EXPR() ");
        return ScimOperator.transform(visit(ctx.operator()), visit(ctx.expression(0)), visit(ctx.expression(1)));
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
        // logger.info(" visitATTR_OPER_CRITERIA() ");
        return ScimOperator.transform(visit(ctx.operator()), ctx.ATTRNAME().getText(), visit(ctx.criteria()));
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
        // logger.info(" visitATTR_OPER_EXPR() ");
        return ScimOperator.transform(visit(ctx.operator()), ctx.ATTRNAME().getText(), visit(ctx.expression()));
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

        // logger.info(" visitATTR_PR() ");

        StringBuilder result = new StringBuilder("");
        result.append(ctx.ATTRNAME().getText());
        result.append("=*");

        return result.toString();
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
    public String visitOperator(ScimFilterParser.OperatorContext ctx) {
        // logger.info(" visitOperator() ");
        return ctx.getText();
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
    public String visitCriteria(ScimFilterParser.CriteriaContext ctx) {

        // logger.info(" visitCriteria() ");
        String result = ctx.getText();
        result = result.replaceAll("^\"|\"$", "");  //...leaving this untouched (unsure of its usefulness)
        result = result.replace("\\", "\\5c").replace("*", "\\2a")
                .replace("(", "\\28").replace(")", "\\29")
                .replace("\0", "\\00");

        return result;
    }

    public String evaluateMultivaluedCriteria(String criteria, String operator, String[] tokens) {

        // This is already specific implementation. Currently only support up to second level.
        if (tokens.length == 2 && !tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {

            StringBuilder result = new StringBuilder();

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.ENDS_WITH) ||
                ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS)) {
                result.append("\"");
            } else {
                result.append("*\"");
            }

            result.append(tokens[1]);

            if (ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.EQUAL) ||
                ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.STARTS_WITH) ||
                ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.NOT_EQUAL)) {
                result.append("\":\"");
            } else {
                result.append("\":*");
            }

            result.append(criteria);

            if (!(ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.STARTS_WITH) ||
                ScimOperator.getByValue(operator.toLowerCase()).equals(ScimOperator.CONTAINS))) {
                result.append("\"*");
            }

            criteria = result.toString();
        }

        return criteria;
    }

    public String evaluateIsPresentCriteria(String ldapAttributeName, String[] tokens) {

        StringBuilder result = new StringBuilder("");

        // This is already specific implementation. Currently only support up to second level.
        if (tokens.length == 2 && !tokens[0].equalsIgnoreCase(Name.class.getSimpleName())) {

            result.append("&(");
            result.append(ldapAttributeName);
            result.append("=*");
            result.append(")(");

            result.append(ldapAttributeName);
            result.append("=*\"");
            result.append(tokens[1]);
            result.append("\":\"*");
            result.append(")");

        } else {

            result.append(ldapAttributeName);
            result.append("=*");
        }

        return result.toString();
    }

    public String transformToLdapAndFilter(String leftExp, String rightExp) {

        StringBuilder result = new StringBuilder();

        result.append("&(");
        result.append(leftExp);
        result.append(")");
        result.append("(");
        result.append(rightExp);
        result.append(")");

        return result.toString();
    }
}
