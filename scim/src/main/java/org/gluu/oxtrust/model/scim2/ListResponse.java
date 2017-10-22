package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.LIST_RESPONSE_SCHEMA_ID;

/**
 * @author Rahat Ali Date: 05.08.2015
 * Udpated by jgomer on 2017-10-01.
 */
public class ListResponse {

    private List<String> schemas;
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;

    @JsonProperty("Resources")
    private List<BaseScimResource> resources;

    public ListResponse(){
        initSchemas();
    }

    public ListResponse(int sindex, int ippage, int total){
        initSchemas();
        totalResults=total;
        startIndex=sindex;
        itemsPerPage=ippage;
        resources =new ArrayList<BaseScimResource>();
    }

    private void initSchemas(){
        schemas=new ArrayList<String>();
        schemas.add(LIST_RESPONSE_SCHEMA_ID);
    }

    public void addResource(BaseScimResource resource){
        resources.add(resource);
    }

    public int getTotalResults() {
        return totalResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public List<BaseScimResource> getResources() {
        return resources;
    }

    public void setResources(List<BaseScimResource> resources) {
        this.resources = resources;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

}
