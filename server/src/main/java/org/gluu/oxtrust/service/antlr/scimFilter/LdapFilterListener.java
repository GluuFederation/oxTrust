/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.service.antlr.scimFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.model.scim2.AttributeDefinition.Type;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;
import org.gluu.oxtrust.model.scim2.util.DateUtil;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterBaseListener;
import org.gluu.oxtrust.service.antlr.scimFilter.antlr4.ScimFilterParser;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.CompValueType;
import org.gluu.oxtrust.service.antlr.scimFilter.enums.ScimOperator;
import org.gluu.oxtrust.service.antlr.scimFilter.util.FilterUtil;
import org.gluu.oxtrust.service.scim2.ExtensionService;
import org.gluu.search.filter.Filter;
import org.xdi.service.cdi.util.CdiUtil;
import org.xdi.util.Pair;

import static org.gluu.oxtrust.service.antlr.scimFilter.enums.LdapFilterTemplate.*;

/**
 * Created by jgomer on 2017-12-09.
 */
public class LdapFilterListener extends ScimFilterBaseListener {

    private Logger log = LogManager.getLogger(getClass());
    private StringBuilder filter;
    private Class<? extends BaseScimResource> resourceClass;
    private String error;
    private ExtensionService extService;

    public LdapFilterListener(Class<? extends BaseScimResource> resourceClass){
        filter=new StringBuilder();
        extService=CdiUtil.bean(ExtensionService.class);
        this.resourceClass=resourceClass;
    }

    private void close(){
        filter.append(")");
    }

    @Override
    public void enterAndFilter(ScimFilterParser.AndFilterContext ctx) {
        open();
        filter.append("&");
    }

    @Override
    public void enterAttrexp(ScimFilterParser.AttrexpContext ctx) {

        if (StringUtils.isEmpty(error)) {
            log.trace("enterAttrexp.");

            String path = ctx.attrpath().getText();
            ScimFilterParser.CompvalueContext compValueCtx = ctx.compvalue();
            boolean isPrRule = compValueCtx == null && ctx.getChild(1).getText().equals("pr");

            Type attrType=null;
            Attribute attrAnnot = IntrospectUtil.getFieldAnnotation(path, resourceClass, Attribute.class);
            String ldapAttribute = null;
            boolean isNested = false;

            if (attrAnnot==null) {
                ExtensionField field=extService.getFieldOfExtendedAttribute(resourceClass, path);

                if (field==null)
                    error=String.format("Attribute path '%s' is not recognized in %s", path, resourceClass.getSimpleName());
                else {
                    attrType = field.getAttributeDefinitionType();
                    ldapAttribute = path.substring(path.lastIndexOf(":")+1);
                }
            }
            else {
                attrType = attrAnnot.type();
                Pair<String, Boolean> pair=FilterUtil.getLdapAttributeOfResourceAttribute(path, resourceClass);
                ldapAttribute=pair.getFirst();
                isNested=pair.getSecond();
            }

            if (error!=null)
                ;   //Intentionally left empty
            else
            if (attrType==null)
                error=String.format("Could not determine type of attribute path '%s' in %s", path, resourceClass.getSimpleName());
            else
            if (ldapAttribute==null)
                error=String.format("Could not determine LDAP attribute for path '%s' in %s", path, resourceClass.getSimpleName());
            else{
                String subattr=isNested ? path.substring(path.lastIndexOf(".")+1) : null;
                String subFilth;
                CompValueType type;
                ScimOperator operator;

                if (isPrRule) {
                    type=CompValueType.NULL;
                    operator=ScimOperator.NOT_EQUAL;
                }
                else {
                    type=FilterUtil.getCompValueType(compValueCtx);
                    operator=ScimOperator.getByValue(ctx.compareop().getText());
                }

                error=FilterUtil.checkFilterConsistency(path, attrType, type, operator);
                if (error==null) {
                    subFilth = getSubFilter(subattr, ldapAttribute, isPrRule ? null : compValueCtx.getText(), attrType, type, operator);

                    if (subFilth == null){
                        if (error==null)
                            error = String.format("Operator '%s' is not supported for attribute %s", operator.getValue(), path);
                    }
                    else
                        filter.append(subFilth);
                }
            }
        }
    }

    @Override
    public void enterNegatedFilter(ScimFilterParser.NegatedFilterContext ctx) {
        if (ctx.getText().startsWith("not(")) {
            open();
            filter.append("!");
        }
    }

    @Override
    public void enterOrFilter(ScimFilterParser.OrFilterContext ctx) {
        open();
        filter.append("|");
    }

    private String escapeLdapString(String string){
        //See section 4 of RFC 2254
        return string.replace("\\", "\\5c").replace("*", "\\2a")
                .replace("(", "\\28").replace(")", "\\29")
                .replace("\0", "\\00");
    }

    @Override
    public void exitAndFilter(ScimFilterParser.AndFilterContext ctx) {
        close();
    }

    @Override
    public void exitNegatedFilter(ScimFilterParser.NegatedFilterContext ctx) {
        if (ctx.getText().startsWith("not("))
            close();
    }

    @Override
    public void exitOrFilter(ScimFilterParser.OrFilterContext ctx) {
        close();
    }

    public String getError() {
        return error;
    }

    public Filter getFilter() {
        if (StringUtils.isEmpty(error)) {
            if (filter.charAt(0)!='(' || filter.charAt(filter.length()-1)!=')')
                filter.insert(0, "(").append(")");

            log.info("LDAP filter expression computed was {}", filter.toString());
            return Filter.create(filter.toString());
        }
        else
            return null;
    }

    private String getSubFilter(String subAttribute, String ldapAttribute, String compValue, Type attrType, CompValueType type, ScimOperator operator){

        String filth=null;

        if (type.equals(CompValueType.NULL)){
            if (subAttribute==null) {
                filth = NULL_NOT_EQUALS.get(ldapAttribute);
                if (operator.equals(ScimOperator.EQUAL))
                    filth= "(!" + filth + ")";
            }
            else {
                filth = NULL_EQUALS_INNER.get(ldapAttribute, subAttribute);
                if (operator.equals(ScimOperator.NOT_EQUAL))
                    filth= "(!" + filth + ")";
            }
        }
        else
        if (Type.STRING.equals(attrType) || Type.REFERENCE.equals(attrType)){
            compValue=escapeLdapString(compValue.substring(1, compValue.length()-1));     //Drop double quotes and encode
            filth=getSubFilterString(subAttribute, ldapAttribute, compValue, operator);
        }
        else
        if (Type.INTEGER.equals(attrType) || Type.DECIMAL.equals(attrType))
            filth=getSubFilterNumeric(subAttribute, ldapAttribute, compValue, operator, attrType);
        else
        if (Type.BOOLEAN.equals(attrType))
            filth=getSubFilterBoolean(subAttribute, ldapAttribute, compValue, operator, attrType);
        else
        if (Type.DATETIME.equals(attrType)){
            compValue=escapeLdapString(compValue.substring(1, compValue.length()-1));
            filth=getSubFilterDateTime(subAttribute, ldapAttribute, compValue, operator, attrType);
        }

        log.trace("getSubFilter. {}", filth);
        return filth;

    }

    private String getSubFilterBoolean(String subAttribute, String ldapAttribute, String value, ScimOperator operator, Type attrType) {

        String subfilter=null;
        log.trace("getSubFilterBoolean");

        if (operator.equals(ScimOperator.EQUAL) || operator.equals(ScimOperator.NOT_EQUAL)) {
            if (subAttribute==null)
                //should be TRUE or FALSE (uppercase) to work: boolean syntax 1.3.6.1.4.1.1466.115.121.1.7 with booleanMatch equality
                subfilter=BOOLEAN_EQUALS.get(ldapAttribute, value.toUpperCase());
            else
                //When it's inside json, the underlying attribute is not a boolean LDAP but a string so no conversion is needed
                subfilter=BOOLEAN_EQUALS_INNER.get(ldapAttribute, subAttribute, value);

            if (operator.equals(ScimOperator.NOT_EQUAL))
                subfilter= "(!" + subfilter + ")";
        }
        else
            error=FilterUtil.getOperatorInconsistencyError(operator.getValue(), attrType.toString(), subAttribute);

        return subfilter;

    }

    private String getSubFilterDateTime(String subAttribute, String ldapAttribute, String value, ScimOperator operator, Type attrType) {

        log.trace("getSubFilterDateTime");
        String generalizedStringDate= DateUtil.ISOToGeneralizedStringDate(value);
        if (generalizedStringDate==null) {
            error=String.format("Value passed for date comparison \"%s\" is not in ISO format", value);
            return null;
        }
        else
            //happily the same rules and templates apply
            return getSubFilterNumeric(subAttribute, ldapAttribute, generalizedStringDate, operator, attrType);

    }

    private String getSubFilterNumeric(String subAttribute, String ldapAttribute, String value, ScimOperator operator, Type attrType) {

        String subfilter=null;
        StringBuilder subfilter2=new StringBuilder();
        log.trace("getSubFilterNumeric");

        switch (operator){
            case EQUAL:
            case NOT_EQUAL:
                if (subAttribute==null)
                    subfilter=NUMERIC_EQUALS.get(ldapAttribute, value);
                else
                    subfilter=NUMERIC_EQUALS_INNER.get(ldapAttribute, subAttribute, value);

                if (operator.equals(ScimOperator.NOT_EQUAL))
                    subfilter= "(!" + subfilter + ")";
                break;
            case GREATER_THAN:
                if (subAttribute==null) {
                    subfilter2.append("(&")
                            .append("(!").append(NUMERIC_EQUALS.get(ldapAttribute, value)).append(")")
                            .append(NUMERIC_GREATER_THAN_OR_EQ.get(ldapAttribute, value))
                            .append(")");
                    subfilter = subfilter2.toString();
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (subAttribute==null)
                    subfilter=NUMERIC_GREATER_THAN_OR_EQ.get(ldapAttribute, value);
                break;
            case LESS_THAN:
                if (subAttribute==null) {
                    subfilter2.append("(&")
                            .append("(!").append(NUMERIC_EQUALS.get(ldapAttribute, value)).append(")")
                            .append(NUMERIC_LESS_THAN_OR_EQ.get(ldapAttribute, value))
                            .append(")");
                    subfilter = subfilter2.toString();
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (subAttribute==null)
                    subfilter=NUMERIC_LESS_THAN_OR_EQ.get(ldapAttribute, value);
                break;
            default:
                error= FilterUtil.getOperatorInconsistencyError(operator.getValue(), attrType.toString(), subAttribute);
        }
        return subfilter;

    }

    private String getSubFilterString(String subAttribute, String ldapAttribute, String value, ScimOperator operator) {

        String subfilter=null;
        StringBuilder subfilter2=new StringBuilder();
        log.trace("getSubFilterString");

        switch (operator){
            case EQUAL:
            case NOT_EQUAL:
                if (subAttribute==null)
                    subfilter=STRING_EQUALS.get(ldapAttribute, value);
                else
                    subfilter=STRING_EQUALS_INNER.get(ldapAttribute, subAttribute, value);

                if (operator.equals(ScimOperator.NOT_EQUAL))
                    subfilter= "(!" + subfilter + ")";
                break;
            case CONTAINS:
                if (subAttribute==null)
                    subfilter=STRING_CONTAINS.get(ldapAttribute, value);
                else
                    subfilter=STRING_CONTAINS_INNER.get(ldapAttribute, subAttribute, value);
                break;
            case STARTS_WITH:
                if (subAttribute==null)
                    subfilter=STRING_STARTSWITH.get(ldapAttribute, value);
                else
                    subfilter=STRING_STARTSWITH_INNER.get(ldapAttribute, subAttribute, value);
                break;
            case ENDS_WITH:
                if (subAttribute==null)
                    subfilter=STRING_ENDSWITH.get(ldapAttribute, value);
                else
                    subfilter=STRING_ENDSWITH_INNER.get(ldapAttribute, subAttribute, value);
                break;
            case GREATER_THAN:  //LDAP does not support greater than operator
                if (subAttribute==null) {
                    subfilter2.append("(&")
                            .append("(!").append(STRING_EQUALS.get(ldapAttribute, value)). append(")")
                            .append(STRING_GREATER_THAN_OR_EQ.get(ldapAttribute, value))
                            .append(")");
                    subfilter=subfilter2.toString();
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (subAttribute==null)
                    subfilter=STRING_GREATER_THAN_OR_EQ.get(ldapAttribute, value);
                break;
            case LESS_THAN:  //LDAP does not support less than operator
                if (subAttribute==null) {
                    subfilter2.append("(&")
                            .append("(!").append(STRING_EQUALS.get(ldapAttribute, value)). append(")")
                            .append(STRING_LESS_THAN_OR_EQ.get(ldapAttribute, value))
                            .append(")");
                    subfilter = subfilter2.toString();
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (subAttribute==null)
                    subfilter=STRING_LESS_THAN_OR_EQ.get(ldapAttribute, value);
                break;
        }
        return subfilter;

    }

    private void open(){
        filter.append("(");
    }

}
