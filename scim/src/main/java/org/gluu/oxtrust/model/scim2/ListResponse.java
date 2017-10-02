package org.gluu.oxtrust.model.scim2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rahat Ali Date: 05.08.2015
 * Udpated by jgomer on 2017-10-01.
 */
public class ListResponse {

    private int totalResults;
    private int startIndex;
    private int itemsPerPage;

    private List<BaseScimResource> resources;

    public ListResponse(int sindex, int ippage){
        totalResults=0;
        startIndex=sindex;
        itemsPerPage=ippage;
        resources =new ArrayList<BaseScimResource>();
    }

    public void addResource(BaseScimResource resource){
        resources.add(resource);
        totalResults++;
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

}
