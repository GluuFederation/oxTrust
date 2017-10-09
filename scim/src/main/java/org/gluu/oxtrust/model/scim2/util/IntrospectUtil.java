package org.gluu.oxtrust.model.scim2.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.gluu.oxtrust.model.scim2.annotations.Validator;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.provider.ServiceProviderConfig;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

import javax.lang.model.type.NullType;
import javax.ws.rs.HeaderParam;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by jgomer on 2017-09-14.
 *
 * Provides miscelaneous routines to query classes/objects properties using reflection mechanisms. Additionally, this
 * class exposes some public members that contain useful information about SCIM resources that is gather upon class loading
 */
public class IntrospectUtil {

    private static Logger log = LogManager.getLogger(IntrospectUtil.class);

    /**
     * From a bidimensional Array of Annotations (usually obtained by a call to a Method's getParameterAnnotations), it
     * searches for an annotation pertaining to class HeaderParam and whose value equals "Authorization"
     * @param annotations A two-dimensional array of annotations
     * @return The index (on the first dimension) where the annotation is found if any, otherwise returns -1
     */
    public static int indexOfAuthzHeader(Annotation annotations[][]){

        int j=-1;

        for (int i = 0; i<annotations.length && j<0; i++){
            //Iterate over annotations found at every parameter
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType().equals(HeaderParam.class)) {
                    //Verifies this is an authz header
                    if (((HeaderParam)annotation).value().equals("Authorization")) {
                        j=i;
                        break;
                    }
                }
            }
        }
        return j;

    }

    /**
     * This method will find a java Field with a particular name.  If  needed, this method will search through
     * super classes.  The field does not need to be public.
     * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/utils/SchemaUtils.java
     *
     * @param cls the java Class to search.
     * @param fieldName the name of the field to find.
     * @return A Field object, or null if no field was found
     */
    public static Field findField(final Class<?> cls, final String fieldName){

        Class<?> currentClass = cls;

        while(currentClass != null){
            Field fields[] = currentClass.getDeclaredFields();
            for (Field field : fields){
                if(field.getName().equals(fieldName))
                    return field;
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;

    }

    public static Field findFieldFromPath(Class<?> initcls, String path){

        Class cls=initcls;
        Field f=null;

        for (String prop : path.split("\\.")) {
            f=findField(cls, prop);

            if (f!=null) {
                cls = f.getType();
                if (isCollection(cls)) {
                    Attribute attrAnnot = f.getAnnotation(Attribute.class);
                    if (attrAnnot != null)
                        cls = attrAnnot.multiValueClass();
                }
            }
            else
                break;
        }
        return f;

    }

    public static Attribute getAttrAnnotationForField(String fieldName, Class<?> clazz){

        Attribute attr=null;
        try {/*
            PropertyDescriptor[] props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            FeatureDescriptor match=null;
            for (FeatureDescriptor fd : props)
                if (fd.getName().equals(fieldName))
                    match=fd;
            if (match!=null){
            }
            */
            Field field=findField(clazz, fieldName);
            if (field==null){
                log.warn("No field with name {} found in class {}", fieldName, clazz.getName());
            }
            else{
                attr=field.getAnnotation(Attribute.class);
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return attr;
    }

    public static Method getGetter(String fieldName, Class clazz) throws Exception{
        PropertyDescriptor[] props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        for (PropertyDescriptor p : props)
            if (p.getName().equals(fieldName))
                return p.getReadMethod();
        return null;
    }

    public static boolean isCollection(Class clazz){
        return Collection.class.isAssignableFrom(clazz);    // clazz.equals(List.class);
    }

    public static List<String> getPathsInExtension(Extension extension){

        List<String> list=new ArrayList<String>();
        for (String attr : extension.getFields().keySet())
            list.add(extension.getUrn() + "." + attr);
        return list;

    }

    public static Map<String, String> getPathsLdapAnnotationsMapping(Class<? extends BaseScimResource> cls){

        Map<String, String> map=new HashMap<String, String>();
        for (String attrib: IntrospectUtil.allAttrs.get(cls)){
            Field field=IntrospectUtil.findFieldFromPath(cls, attrib);
            if (field!=null){
                LdapAttribute ldapAnnot=field.getAnnotation(LdapAttribute.class);
                if (ldapAnnot!=null)
                    map.put(attrib, ldapAnnot.name());
            }
        }
        return map;

    }

    private static List<String> requiredAttrsNames;
    private static List<String> defaultAttrsNames;
    private static List<String> alwaysAttrsNames;
    private static List<String> neverAttrsNames;
    private static List<String> validableAttrsNames;
    private static List<String> canonicalizedAttrsNames;

    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> requiredCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> defaultCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> alwaysCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> neverCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> validableCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> canonicalCoreAttrs;

    public static Map<Class <?extends BaseScimResource>, SortedSet<String>> allAttrs;

    private static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> newEmptyMap(){
        return new HashMap<Class<? extends BaseScimResource>, Map<String, List<Method>>>();
    }

    private static void resetAttrNames(){
        requiredAttrsNames=new ArrayList<String>();
        defaultAttrsNames=new ArrayList<String>();
        validableAttrsNames=new ArrayList<String>();
        canonicalizedAttrsNames=new ArrayList<String>();
        alwaysAttrsNames=new ArrayList<String>();
        neverAttrsNames=new ArrayList<String>();
    }

    private static void resetMaps(){
        requiredCoreAttrs=newEmptyMap();
        defaultCoreAttrs=newEmptyMap();
        alwaysCoreAttrs=newEmptyMap();
        neverCoreAttrs=newEmptyMap();
        validableCoreAttrs=newEmptyMap();
        canonicalCoreAttrs=newEmptyMap();

        allAttrs=new HashMap<Class<? extends BaseScimResource>, SortedSet<String>>();
    }

    private static void traverseClassForNames(Class clazz, String prefix, List<Field> extraFields) throws Exception{

        List<Field> fields=new ArrayList<Field>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(extraFields);

        for (Field f : fields){
            Attribute attrAnnot=f.getAnnotation(Attribute.class);
            if (attrAnnot!=null){

                String name=f.getName();
                if (prefix.length()>0)
                    name=prefix + "." + name;

                switch (attrAnnot.returned()){
                    case ALWAYS:
                        alwaysAttrsNames.add(name);
                        break;
                    case DEFAULT:
                        defaultAttrsNames.add(name);
                        break;
                    case NEVER:
                        neverAttrsNames.add(name);
                        break;
                }

                if (attrAnnot.isRequired())
                    requiredAttrsNames.add(name);

                if (attrAnnot.canonicalValues().length>0)
                    canonicalizedAttrsNames.add(name);

                Validator vAnnot=f.getAnnotation(Validator.class);
                if (vAnnot!=null)
                    validableAttrsNames.add(name);

                Class cls=attrAnnot.multiValueClass();  //Use class parameter of Collection
                traverseClassForNames(cls.equals(NullType.class) ? f.getType() : cls, name, new ArrayList<Field>());
            }
        }
    }

    private static Map<String, List<Method>> computeGettersMap(List<String> attrNames, Class baseClass) throws Exception{

        Map<String, List<Method>> map=new HashMap<String, List<Method>>();

        for (String attrName : attrNames) {
            List<Method> list =new ArrayList<Method>();
            Class clazz=baseClass;

            for (String prop : attrName.split("\\.")) {
                Method method=IntrospectUtil.getGetter(prop, clazz);
                list.add(method);

                if (isCollection(method.getReturnType())) {  //Use class of parameter in collection
                    Field f=IntrospectUtil.findField(clazz, prop);
                    Attribute attrAnnot=f.getAnnotation(Attribute.class);
                    if (attrAnnot!=null)
                        clazz=attrAnnot.multiValueClass();
                }
                else
                    clazz=method.getReturnType();
            }
            map.put(attrName, list);
        }
        return map;

    }

    static{
        try {
            List<Field> basicFields=Arrays.asList(BaseScimResource.class.getDeclaredFields());
            resetMaps();

            resetAttrNames();
            Class aClass = UserResource.class;
            traverseClassForNames(aClass, "", basicFields);
            requiredCoreAttrs.put(aClass, computeGettersMap(requiredAttrsNames, aClass));
            defaultCoreAttrs.put(aClass, computeGettersMap(defaultAttrsNames, aClass));
            alwaysCoreAttrs.put(aClass, computeGettersMap(alwaysAttrsNames, aClass));
            neverCoreAttrs.put(aClass, computeGettersMap(neverAttrsNames, aClass));
            validableCoreAttrs.put(aClass, computeGettersMap(validableAttrsNames, aClass));
            canonicalCoreAttrs.put(aClass, computeGettersMap(canonicalizedAttrsNames, aClass));

            allAttrs.put(aClass, new TreeSet<String>());
            allAttrs.get(aClass).addAll(alwaysAttrsNames);
            allAttrs.get(aClass).addAll(defaultAttrsNames);
            allAttrs.get(aClass).addAll(neverAttrsNames);

            resetAttrNames();
            aClass = GroupResource.class;
            traverseClassForNames(aClass, "", basicFields);
            requiredCoreAttrs.put(aClass, computeGettersMap(requiredAttrsNames, aClass));
            defaultCoreAttrs.put(aClass, computeGettersMap(defaultAttrsNames, aClass));
            alwaysCoreAttrs.put(aClass, computeGettersMap(alwaysAttrsNames, aClass));
            neverCoreAttrs.put(aClass, computeGettersMap(neverAttrsNames, aClass));
            validableCoreAttrs.put(aClass, computeGettersMap(validableAttrsNames, aClass));
            canonicalCoreAttrs.put(aClass, computeGettersMap(canonicalizedAttrsNames, aClass));

            allAttrs.put(aClass, new TreeSet<String>());
            allAttrs.get(aClass).addAll(alwaysAttrsNames);
            allAttrs.get(aClass).addAll(defaultAttrsNames);
            allAttrs.get(aClass).addAll(neverAttrsNames);

            resetAttrNames();
            aClass = ServiceProviderConfig.class;
            traverseClassForNames(aClass, "", basicFields);
            requiredCoreAttrs.put(aClass, computeGettersMap(requiredAttrsNames, aClass));
            defaultCoreAttrs.put(aClass, computeGettersMap(defaultAttrsNames, aClass));
            alwaysCoreAttrs.put(aClass, computeGettersMap(alwaysAttrsNames, aClass));
            neverCoreAttrs.put(aClass, computeGettersMap(neverAttrsNames, aClass));
            validableCoreAttrs.put(aClass, computeGettersMap(validableAttrsNames, aClass));
            canonicalCoreAttrs.put(aClass, computeGettersMap(canonicalizedAttrsNames, aClass));

            allAttrs.put(aClass, new TreeSet<String>());
            allAttrs.get(aClass).addAll(alwaysAttrsNames);
            allAttrs.get(aClass).addAll(defaultAttrsNames);
            allAttrs.get(aClass).addAll(neverAttrsNames);

            resetAttrNames();
            aClass = ResourceType.class;
            traverseClassForNames(aClass, "", basicFields);
            requiredCoreAttrs.put(aClass, computeGettersMap(requiredAttrsNames, aClass));
            defaultCoreAttrs.put(aClass, computeGettersMap(defaultAttrsNames, aClass));
            alwaysCoreAttrs.put(aClass, computeGettersMap(alwaysAttrsNames, aClass));
            neverCoreAttrs.put(aClass, computeGettersMap(neverAttrsNames, aClass));
            validableCoreAttrs.put(aClass, computeGettersMap(validableAttrsNames, aClass));
            canonicalCoreAttrs.put(aClass, computeGettersMap(canonicalizedAttrsNames, aClass));

            allAttrs.put(aClass, new TreeSet<String>());
            allAttrs.get(aClass).addAll(alwaysAttrsNames);
            allAttrs.get(aClass).addAll(defaultAttrsNames);
            allAttrs.get(aClass).addAll(neverAttrsNames);

/*
            log.debug("requiredAttrsNames {}", requiredAttrsNames);
            log.debug("defaultAttrsNames {}", defaultAttrsNames);
            log.debug("alwaysAttrsNames {}", alwaysAttrsNames);
            log.debug("validableAttrsNames {}", validableAttrsNames);
            log.debug("canonicalizedAttrsNames {}", canonicalizedAttrsNames);
*/
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}