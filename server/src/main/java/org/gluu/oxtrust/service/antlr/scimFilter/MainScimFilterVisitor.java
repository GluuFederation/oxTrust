/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter;

import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterBaseVisitor;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.ScimOperator;
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

        // logger.info(" visitLBRAC_EXPR_RBRAC() ");

        StringBuilder result = new StringBuilder("");
        result.append("(&");
        result.append("(");
        result.append(ctx.ATTRNAME());
        result.append("=*");
        result.append(")");
        result.append("(");
        result.append(visit(ctx.expression()));  // Add check if child attributes belong to the parent
        result.append(")");
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

        // logger.info(" visitEXPR_AND_EXPR() ");

        StringBuilder result = new StringBuilder("");
        result.append("&(");
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
        result = result.replaceAll("^\"|\"$", "");
        result = result.replaceAll("\\\\", "\5c");
        result = result.replaceAll("\\*", "\2a");
        result = result.replaceAll("\\(", "\28");
        result = result.replaceAll("\\)", "\29");

        return result;
    }
}
