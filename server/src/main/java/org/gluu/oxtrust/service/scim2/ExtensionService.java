package org.gluu.oxtrust.service.scim2;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;
import org.xdi.model.OxMultivalued;
import org.xdi.model.ScimCustomAtribute;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.USER_EXT_SCHEMA_ID;

/**
 * Created by jgomer on 2017-09-29.
 */
@Stateless
@Named
public class ExtensionService {

    @Inject
    private Logger log;

    @Inject
    private AttributeService attrService;

    public List<Extension> getResourceExtensions(Class<? extends BaseScimResource> cls){

        List<Extension> list=new ArrayList<Extension>();
        try {
            //Currently support one extension only for User Resource
            if (cls.equals(UserResource.class)) {

                Map<String, ExtensionField> fields=new HashMap<String, ExtensionField>();

                for (GluuAttribute attribute : attrService.getSCIMRelatedAttributes()) {
                    if (attribute.getOxSCIMCustomAttribute().equals(ScimCustomAtribute.TRUE)) {
                        //first non-null check is needed because certain entries do not have the multivalue attribute set
                        boolean multi=attribute.getOxMultivaluedAttribute()!=null && attribute.getOxMultivaluedAttribute().equals(OxMultivalued.TRUE);
                        fields.put(attribute.getName(), new ExtensionField(attribute.getDataType(), multi));
                    }
                }

                Extension ext=new Extension(USER_EXT_SCHEMA_ID);
                ext.setFields(fields);

                list.add(ext);
            }
        }
        catch (Exception e){
            log.error("An error ocurred when building extension for {}", cls.getName());
            log.error(e.getMessage(), e);
        }
        return list;

    }

    /**
     * Builds up a list of strings with the values associated to the field passed. The strings are created according to
     * the type asociated to the field: for STRING the value is left as is; for DATE the value is converted to a String
     * following the generalized date format; for NUMERIC the value is converted to a String
     * @param field An ExtensionField instance
     * @param valuesHolder A non-null value object (may be a collection)
     * @return List with values represented as Strings
     */
    public List<String> getStringAttributeValues(ExtensionField field, Object valuesHolder){

        Collection collection=field.isMultiValued() ? (Collection)valuesHolder : Collections.singletonList(valuesHolder);
        List<String> values=new ArrayList<String>();

        for (Object elem : collection) {
            //Despite valuesHolder is not null, it can be a collection with null elements...
            if (elem!=null)
                values.add(ExtensionField.stringValueOf(field, elem));
        }
        return values;

    }

    /**
     * Builds a list of objects based on the supplied String values passed and the extension field passed. The strings are
     * converted according to the type asociated to the field: for STRING the value is left as is; for DATE the value is
     * converted to a String following the ISO date format; for NUMERIC an Integer is created from the value supplied.
     * @param field An ExtensionField
     * @param strValues A non-empty String array with the values associated to the field passed. This values are coming
     *                  from LDAP
     * @return List of opaque values
     */
    public List<Object> convertValues(ExtensionField field, String strValues[]){

        List<Object> values=new ArrayList<Object>();

        for (String val : strValues) {
            //In practice, there should not be nulls in strValues
            if (val!=null) {
                Object value = ExtensionField.valueFromString(field, val);

                //won't happen either (value being null) because calls to this method occurs after lots of validations have taken place
                if (value != null) {
                    values.add(value);
                    log.debug("convertValues. Added value '{}'", value.toString());
                }
            }
        }
        return values;

    }

}
