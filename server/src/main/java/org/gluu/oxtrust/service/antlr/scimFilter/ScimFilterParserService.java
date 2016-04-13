/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter;

import com.unboundid.ldap.sdk.Filter;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterLexer;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.exception.ScimFilterErrorHandler;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import java.io.Serializable;

/**
 * @author Val Pecaoco
 */
@Scope(ScopeType.STATELESS)
@Name("scimFilterParserService")
@AutoCreate
public class ScimFilterParserService implements Serializable {

    @Logger
    private Log log;

    public Filter createFilter(String filterString) throws Exception {

        log.info(" createFilter() ");

        Filter filter = null;
        if (filterString != null && !filterString.isEmpty()) {

            // Visit tree
            String result = visitTree(filterString);

            filter = Filter.create(result);

        } else {
            throw new IllegalArgumentException("Filter string is null or empty");
        }

        return filter;
    }

    private String visitTree(String filter) throws Exception {

        log.info(" visitTree() ");

        ParseTree parseTree = getParser(filter).scimFilter();

        // Visit tree
        MainScimFilterVisitor mainScimFilterVisitor = new MainScimFilterVisitor();
        String result = mainScimFilterVisitor.visit(parseTree);

        return result;
    }

    private ScimFilterParser getParser(String filter) throws Exception {

        log.info(" getParser() ");

        // Get lexer
        ANTLRInputStream input = new ANTLRInputStream(filter);
        ScimFilterLexer lexer = new ScimFilterLexer(input);

        // Get list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass tokens to the parser
        ScimFilterParser parser = new ScimFilterParser(tokens);
        parser.setBuildParseTree(true);
        parser.setTrimParseTree(true);
        parser.setProfile(true);
        parser.removeErrorListeners();
        parser.setErrorHandler(new ScimFilterErrorHandler());

        return parser;
    }
}
