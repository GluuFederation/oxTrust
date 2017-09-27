package org.gluu.oxtrust.ws.rs.scim2;

import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.scim2.ErrorResponse;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Base methods for SCIM web services
 *
 * @author Yuriy Movchan Date: 08/23/2013
 * Updated by jgomer on 2017-09-14.
 */
public class BaseScimWebService {

    @Inject
    Logger log;

    String endpointUrl;

    private ObjectMapper mapper=new ObjectMapper();

    private Set<String> expandAttributesPaths(String attributes, String defaulSchemaUrn, List<String> schemas, SortedSet<String> attribs) {

        Set<String> set=new HashSet<String>();

        for (String attr : attributes.split(",")) {
            String shorterName=attr.replaceAll("\\s", "");

            for (String urn : schemas){
                if (shorterName.startsWith(urn + ":")) {
                    if (urn.equals(defaulSchemaUrn))
                        shorterName = shorterName.substring(urn.length()+1);
                    else
                        shorterName = shorterName.substring(0, urn.length()) + "." + shorterName.substring(urn.length()+1);
                }
            }
            set.add(shorterName);
        }

        Set<String> extendedSet=new HashSet<String>();

        //attribs is already sorted
        for (String basicAttr : set){
            extendedSet.add(basicAttr);

            for (String elem : attribs.tailSet(basicAttr + "."))
                if (elem.startsWith(basicAttr + "."))
                    extendedSet.add(elem);
                else
                    break;
        }
        //No need for this block: containsProperty method is smart enough
        /*
        //Remove redundancies
        String attrArray[]=list.toArray(new String[]{});
        Arrays.sort(attrArray);
        list=new ArrayList<>();
        int j=0;

        for (int i=0; i<attrArray.length-1;){
            String prevAttr=attrArray[i];
            for (j=i+1;j<attrArray.length && attrArray[j].startsWith(prevAttr + ".");j++);
            i=j;
            list.add(prevAttr);
        }
           */
        return extendedSet;
    }

    private void buildIncludeSet(SortedSet<String> include, Class<? extends BaseScimResource> resourceClass,
                                 List<String> schemas, String attributes, String exclussions){

        Set<String> tempSet;
        Set<String> alwaysSet=IntrospectUtil.alwaysCoreAttrs.get(resourceClass).keySet();
        Set<String> neverSet=IntrospectUtil.neverCoreAttrs.get(resourceClass).keySet();
        Set<String> defaultSet=IntrospectUtil.defaultCoreAttrs.get(resourceClass).keySet();

        String defaultSchema=resourceClass.getAnnotation(Schema.class).id();

        if (attributes!=null) {
            log.info("Processing attributes query param (excludedAttributes ignored)");

            tempSet= expandAttributesPaths(attributes, defaultSchema, schemas, IntrospectUtil.allAttrs.get(resourceClass));
            tempSet.removeAll(neverSet);
            include.addAll(tempSet);
        }
        else
        if (exclussions!=null){
            log.info("Processing excludedAttributes query param)");

            tempSet= defaultSet;
            tempSet.removeAll(expandAttributesPaths(exclussions, defaultSchema, schemas, IntrospectUtil.allAttrs.get(resourceClass)));
            include.addAll(tempSet);
        }
        else{
            log.info("No attributes neither excludedAttributes query param were passed");
            include.addAll(defaultSet);
        }
        include.addAll(alwaysSet);

    }

    private boolean containsProperty(SortedSet<String> properties, String prefix, String key){

        String property = (prefix.length() == 0) ? key : prefix + "." + key;
        Set<String> set=properties.tailSet(property);

        boolean flag=set.contains(property);
        if (!flag){
            for (String prop : set)
                if (prop.startsWith(property + ".")) {
                    flag=true;
                    break;
                }
        }
        return flag;

    }

    private String getNewPrefix(String prefix, String key){
        return prefix + (prefix.length()==0 ? "" : ".") + key;
    }

    private Map<String, Object> smallerMap(String prefix, Map<String, Object> value, SortedSet<String> include){
        LinkedHashMap<String, Object> smallMap = new LinkedHashMap<String, Object>();
        traverse(prefix, value, smallMap, include);
        return smallMap.size()==0 ? null : smallMap;
    }

    /**
     * Section 2.5 of RFC 7643: When a resource is expressed in JSON format, unassigned attributes, although they are defined in
     * schema, MAY be omitted for compactness
     * @param prefix
     * @param map
     * @param destination
     * @param include
     */
    private void traverse(String prefix, Map<String, Object> map, LinkedHashMap<String, Object> destination, SortedSet<String> include){

        for (String key : map.keySet()){
            Object value=map.get(key);
            log.debug("key {}", key);
            if (value!=null && containsProperty(include, prefix, key)){
                log.debug("true");
                if (value instanceof Map)
                    value = smallerMap(getNewPrefix(prefix, key), (Map<String, Object>) value, include);
                else
                if (IntrospectUtil.isCollection(value.getClass())){
                    List list=new ArrayList();
                    Map<String, Object> innerMap;

                    for (Object item : (Collection) value){
                        if (item!=null)
                            if (item instanceof Map) {
                                innerMap=smallerMap(getNewPrefix(prefix, key), (Map<String, Object>) item, include);
                                if (innerMap!=null)
                                    list.add(innerMap);
                            }
                            else {
                                list.add(item);
                            }
                    }
                    value=list;
                }
                if (value!=null)
                    destination.put(key, value);
            }
        }

    }

    String serializeToJson(BaseScimResource resource, String attributes, String exclussions) throws Exception{

        SortedSet<String> include =new TreeSet<String>();
        Class<? extends BaseScimResource> resourceClass=resource.getClass();
        buildIncludeSet(include, resourceClass, resource.getSchemas(), attributes, exclussions);
        log.debug("incl {}", include);
        //Do generic serialization. This works for any POJO (not only subclasses of BaseScimResource)
        Map<String, Object> map = mapper.convertValue(resource, Map.class);
        //Using LinkedHashMap allows recursive routines to visit submaps in the same order as fields appear in java classes
        LinkedHashMap<String, Object> newMap=new LinkedHashMap<String, Object>();
        traverse("", map, newMap, include);

        return mapper.writeValueAsString(newMap);
    }

    String serializeToJson(BaseScimResource resource) throws Exception{
        return serializeToJson(resource, null, null);
    }

    public Response getErrorResponse(Response.Status status, String detail) {
        return getErrorResponse(status.getStatusCode(), null, detail);
    }

    public Response getErrorResponse(Response.Status status, ErrorScimType scimType, String detail) {
        return getErrorResponse(status.getStatusCode(), scimType, detail);
    }

    public Response getErrorResponse(int statusCode, String detail) {
        return getErrorResponse(statusCode, null, detail);
    }

    public Response getErrorResponse(int statusCode, ErrorScimType scimType, String detail) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setSchemas(Collections.singletonList(ERROR_RESPONSE_URI));
        errorResponse.setStatus(String.valueOf(statusCode));
        errorResponse.setScimType(scimType);
        errorResponse.setDetail(detail);

        return Response.status(statusCode).entity(errorResponse).build();
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

}
