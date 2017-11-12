package org.gluu.oxtrust.model.scim2.util;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;

import javax.management.InvalidAttributeValueException;
import java.lang.reflect.Field;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.AttributeDefinition.Mutability.*;

/**
 * Created by jgomer on 2017-09-25.
 *
 * Helper class to traverse a SCIM resource object recursively
 */
class traversalClass {

    private Logger log = LogManager.getLogger(getClass());

    String error;
    private Class base;

    traversalClass(Class baseClass){
        base=baseClass;
    }

    private String getNewPrefix(String prefix, String key){
        return prefix + (prefix.length()==0 ? "" : ".") + key;
    }

    private Map<String, Object> smallerMap(String prefix, Map<String, Object> source, Object destination, boolean replacing){
        Map<String, Object> smallMap = (destination==null) ? new HashMap<String, Object>() : (Map<String, Object>) destination;
        traverse(prefix, source, smallMap, replacing);
        return smallMap.size()==0 ? null : smallMap;
    }

    void traverse(String prefix, Map<String, Object> source, Map<String, Object> destination, boolean replacing){

        for (String key : source.keySet()) {
            Object value = source.get(key);
            Object destValue = destination.get(key);

            if (value!=null && error==null) {
                Field f = IntrospectUtil.findFieldFromPath(base, getNewPrefix(prefix, key));

                if (f!=null) {  //Atributes related to extensions evaluate null here
                    Attribute attrAnnot = f.getAnnotation(Attribute.class);
                    if (attrAnnot != null && !attrAnnot.mutability().equals(READ_ONLY)) {
                        if (value instanceof Map)
                            value = smallerMap(getNewPrefix(prefix, key), (Map<String, Object>) value, destValue, replacing);
                        else
                        if (attrAnnot.mutability().equals(IMMUTABLE) && destValue != null && !value.equals(destValue)) {
                            //provokes no more traversals
                            error = "Invalid value passed for immutable attribute " + key;
                            value = null;
                        }

                        if (value != null) {

                            if (IntrospectUtil.isCollection(value.getClass())) {
                                Collection col=(Collection) value;

                                if (!replacing) {    //we need to add to the existing collection
                                    if (destValue!=null) {

                                        if (!IntrospectUtil.isCollection(destValue.getClass()))
                                            log.warn("Value {} was expected to be a collection", destValue);
                                        else
                                            col.addAll((Collection) destValue);
                                    }
                                }
                                value = col.size()==0 ? null : col;
                            }
                            destination.put(key, value);
                        }
                    }
                }
            }
        }
    }

    void traverseDelete(Map<String, Object> source, String path){

        int i=path.indexOf(".");
        Object value=null;

        if (i==-1)
            source.remove(path);
        else {
            String key = path.substring(0, i);
            value = source.get(key);
            path=path.substring(i+1);
        }

        if (value!=null)
            try {
                //If it's a map we must recurse
                traverseDelete((Map<String, Object>) value, path);
            }
            catch (Exception e){
                if (IntrospectUtil.isCollection(value.getClass())){
                    Collection col=(Collection) value;
                    for (Object item : col) {
                        if (item instanceof Map)
                            traverseDelete((Map<String, Object>) item, path);
                    }
                }
            }

    }

}

/**
 * Created by jgomer on 2017-09-25.
 *
 * This class contains methods to facilitate transformation, and manipulation of data inside SCIM resource objects
 */
public class ScimResourceUtil {

    private static Logger log = LogManager.getLogger(ScimResourceUtil.class);

    private static ObjectMapper mapper=new ObjectMapper();

    private static void attachExtensionInfo(Map<String, Object> source, Map<String, Object> destination, List<Extension> extensions, boolean replacing){

        log.debug("attachExtensionInfo");

        for (Extension extension : extensions){
            String urn=extension.getUrn();
            Object extendedAttrsObj=source.get(urn);

            if (extendedAttrsObj!=null){

                Map<String, Object> extendedAttrs=(Map<String, Object>) extendedAttrsObj;
                Map<String, ExtensionField> fields=extension.getFields();

                Map<String, Object> destMap = destination.get(urn)==null ? new HashMap<String, Object>() : (Map<String, Object>) destination.get(urn);

                for (String attr : fields.keySet()){
                    Object value=extendedAttrs.get(attr);

                    if (value!=null) {

                        if (IntrospectUtil.isCollection(value.getClass())) {
                            Collection col = (Collection) value;

                            if (!replacing){
                                Object destValue=destMap.get(attr);
                                if (destValue != null) {

                                    if (!IntrospectUtil.isCollection(destValue.getClass()))
                                        log.warn("Value {} was expected to be a collection", destValue);
                                    else
                                        col.addAll((Collection) destMap.get(attr));
                                }
                            }
                            value = col.size()==0 ? null : col;
                        }
                        destMap.put(attr, value);
                    }
                }
                destination.put(urn, destMap);
            }
        }

    }

    private static void deleteCustomAttribute(Map<String, Object> source, String path, List<Extension> extensions){

        //All custom attributes are non-complex so we must search for the last dot
        int i=path.lastIndexOf(".");
        if (i==-1)
            log.warn("Path not recognized {}", path);
        else {
            String key = path.substring(i+1);
            path=path.substring(0,i);

            for (Extension ext : extensions)
                if (ext.getUrn().equals(path)){
                    Map<String, Object> submap=(Map<String, Object>) source.get(path);
                    submap.remove(key);
                }

        }
    }

    private static BaseScimResource transferToResource(BaseScimResource origin, final BaseScimResource destination,
                                                      List<Extension> extensions, boolean replacing) throws InvalidAttributeValueException{

        log.debug("transferToResource. Processing {} operation", replacing ? "replace" : "add");

        Map<String, Object> fromMap = mapper.convertValue(origin, new TypeReference<Map<String,Object>>(){});
        Map<String, Object> toMap = mapper.convertValue(destination, new TypeReference<Map<String,Object>>(){});

        log.debug("transferToResource. Recursive traversal of resource is taking place");
        traversalClass tclass=new traversalClass(origin.getClass());
        tclass.traverse("", fromMap, toMap, replacing);
        attachExtensionInfo(fromMap, toMap, extensions, replacing);

        if (tclass.error==null)
            return mapper.convertValue(toMap, origin.getClass());
        else
            throw new InvalidAttributeValueException(tclass.error);

    }

    /**
     * Incorporates the information from an origin into a destination object doing replacement (suitable for the replace
     * operation via PUT, for instance, or for a PATCH with op="replace"), following these rules:
     * - Ignores changes in readonly attrs
     * - Ignores null values (for single-valued attrs)
     * - Nullifies multi-valued attribute when empty array is passed
     * - Immutable attrs must match in origin and destination or else exception is thrown
     * - When a multi-valued attribute is passed in the origin object, no existing data in the destination object is retained,
     *   that is, the replacement is not partial but thorough: it's not an item-by-item replacement
     * @param origin Object with the information to be copied/replaced into the destination object
     * @param destination Object that receives the new information (only non-null attributes in the origin object) end up
     *                    being changed in this object
     * @param extensions
     * @return A new object that contains the result: the final state of destination object
     * @throws InvalidAttributeValueException When recursive traversal fails or if the rule of immutable attribute was not fulfilled
     */
    public static BaseScimResource transferToResourceReplace(BaseScimResource origin, final BaseScimResource destination,
                                                            List<Extension> extensions) throws InvalidAttributeValueException{
        return transferToResource(origin, destination, extensions, true);
    }

    public static BaseScimResource transferToResourceAdd(BaseScimResource origin, final BaseScimResource destination,
                                                            List<Extension> extensions) throws InvalidAttributeValueException{
        return transferToResource(origin, destination, extensions, false);
    }

    public static BaseScimResource deleteFromResource(BaseScimResource origin, String path, List<Extension> extensions) throws InvalidAttributeValueException{

        Field f=IntrospectUtil.findFieldFromPath(origin.getClass(), path);
        if (f!=null){
            Attribute attrAnnot = f.getAnnotation(Attribute.class);
            if (attrAnnot != null && (attrAnnot.mutability().equals(READ_ONLY) || attrAnnot.isRequired()))
                throw new InvalidAttributeValueException("Cannot remove read-only or required attribute from resource");
        }

        Map<String, Object> map = mapper.convertValue(origin, new TypeReference<Map<String,Object>>(){});
        traversalClass tclass=new traversalClass(origin.getClass());

        if (f==null)    //Extensions stuff
            deleteCustomAttribute(map, path, extensions);
        else
            tclass.traverseDelete(map, path);

        return mapper.convertValue(map, origin.getClass());

    }

    public static Schema getSchemaAnnotation(Class<? extends BaseScimResource> cls){
        return cls.getAnnotation(Schema.class);
    }

    public static String getDefaultSchemaUrn(Class<? extends BaseScimResource> cls){
        Schema schema=getSchemaAnnotation(cls);
        return schema==null ? null : schema.id();
    }

    public static String stripDefaultSchema(Class<? extends BaseScimResource> cls, String attribute){

        String val=attribute;
        String defaultSchema=getDefaultSchemaUrn(cls);
        if (StringUtils.isNotEmpty(attribute) && StringUtils.isNotEmpty(defaultSchema)) {
            if (attribute.startsWith(defaultSchema + ":"))
                val = attribute.substring(defaultSchema.length() +1);
        }
        return val;

    }

    public static String getType(Class<? extends BaseScimResource> cls){
        Schema annot=ScimResourceUtil.getSchemaAnnotation(cls);
        return annot==null ? null : annot.name();
    }

    public static String adjustNotationInPath(String path, String defaultUrn, List<String> schemas){

        for (String urn : schemas){
            if (path.startsWith(urn + ":")) {
                if (urn.equals(defaultUrn))
                    path = path.substring(urn.length()+1);
                else
                    path = path.substring(0, urn.length()) + "." + path.substring(urn.length()+1);
            }
        }
        return path;

    }

    public static String[] splitPath(String path, List<String> urns){

        String prefix="";
        for (String urn : urns)
            if (path.startsWith(urn)){
                prefix=urn;
                break;
            }

        if (prefix.length()>0) {

            List<String> pieces=new ArrayList<String>();
            pieces.add(prefix);

            path=path.substring(prefix.length());

            if (path.length()>0) {
                String subpath=path.substring(path.startsWith(".") ? 1 : 0);
                pieces.addAll(Arrays.asList(subpath.split("\\.")));
            }
            return pieces.toArray(new String[]{});
        }
        else
            return path.split("\\.");

    }

}
