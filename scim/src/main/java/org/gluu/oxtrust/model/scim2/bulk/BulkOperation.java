package org.gluu.oxtrust.model.scim2.bulk;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;

/**
 * @author Rahat Ali Date: 05.08.2015
 *
 * Updated by jgomer on 2017-11-21.
 */
public class BulkOperation {

    private static ObjectMapper mapper=new ObjectMapper();

    private String method;
    private String bulkId;
    //private String version;
    private String path;
    private String data;
    private String location;
    private Object response;
    private String status;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBulkId() {
        return bulkId;
    }

    public void setBulkId(String bulkId) {
        this.bulkId = bulkId;
    }
/*
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
*/

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
    public String getData() {
        return data;
    }

    public void setData(Map<String, Object> map) {

        try {
            data = mapper.writeValueAsString(map);
        }
        catch (Exception e){
            data=null;
        }

    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
