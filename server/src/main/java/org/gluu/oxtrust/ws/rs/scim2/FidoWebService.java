package org.gluu.oxtrust.ws.rs.scim2;

import com.unboundid.ldap.sdk.Filter;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.IFidoDeviceService;
import org.gluu.oxtrust.ldap.service.IPersonService;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.fido.GluuCustomFidoDevice;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.ErrorScimType;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.SearchRequest;
import org.gluu.oxtrust.model.scim2.fido.FidoDeviceResource;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.util.DateUtil;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;
import org.gluu.oxtrust.service.antlr.scimFilter.ScimFilterParserService;
import org.gluu.oxtrust.service.antlr.scimFilter.visitor.scim2.FidoFilterVisitor;
import org.gluu.oxtrust.service.scim2.interceptor.Protected;
import org.gluu.oxtrust.service.scim2.interceptor.RefAdjusted;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.joda.time.format.ISODateTimeFormat;
import org.xdi.ldap.model.SortOrder;
import org.xdi.ldap.model.VirtualListViewResponse;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.InvalidAttributeValueException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.*;

/**
 * Implementation of /FidoDevices endpoint. Methods here are intercepted and/or decorated. Class org.gluu.oxtrust.service.scim2.interceptor.FidoServiceDecorator
 * is used to apply pre-validations on data. Interceptor org.gluu.oxtrust.service.scim2.interceptor.ServiceInterceptor
 * secures invocations
 *
 * @author Val Pecaoco
 * Updated by jgomer on 2017-10-09.
 */
@Named("scim2FidoDeviceEndpoint")
@Path("/scim/v2/FidoDevices")
public class FidoWebService extends BaseScimWebService implements FidoDeviceService {

    @Inject
    private IFidoDeviceService fidoDeviceService;

    @Inject
    private ScimFilterParserService scimFilterParserService;

    @Inject
    private IPersonService personService;

    @Inject
    private LdapEntryManager ldapEntryManager;

    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Create device", response = FidoDeviceResource.class)
    public Response createDevice() {
        return getErrorResponse(Response.Status.NOT_IMPLEMENTED, "Not implemented; device registration only happens via the FIDO API.");
    }

    @Path("{id}")
    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Find device by id", notes = "Returns a device by id as path param", response = FidoDeviceResource.class)
    public Response getDeviceById(@PathParam("id") String id,
                           @QueryParam("userId") String userId,
                           @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
                           @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
                           @HeaderParam("Authorization") String authorization){

        Response response;
        try{
            FidoDeviceResource fidoResource=new FidoDeviceResource();

            GluuCustomFidoDevice device=fidoDeviceService.getGluuCustomFidoDeviceById(userId, id);
            if (device==null)
                throw new SCIMException("Resource " + id + " not found");

            transferAttributesToFidoResource(device, fidoResource, endpointUrl, userId);

            String json=resourceSerializer.serialize(fidoResource, attrsList, excludedAttrsList);
            response=Response.ok(new URI(fidoResource.getMeta().getLocation())).entity(json).build();
        }
        catch (SCIMException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at getDeviceById method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path("{id}")
    @PUT
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Update device", response = FidoDeviceResource.class)
    public Response updateDevice(
            FidoDeviceResource fidoDeviceResource,
            @PathParam("id") String id,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization){

        Response response;
        try {
            String userId=fidoDeviceResource.getUserId();
            GluuCustomFidoDevice device = fidoDeviceService.getGluuCustomFidoDeviceById(userId, id);
            if (device == null)
                throw new SCIMException("Resource " + id + " not found");

            FidoDeviceResource updatedResource=new FidoDeviceResource();
            transferAttributesToFidoResource(device, updatedResource, endpointUrl, userId);

            long now=new Date().getTime();
            updatedResource.getMeta().setLastModified(ISODateTimeFormat.dateTime().withZoneUTC().print(now));

            updatedResource=(FidoDeviceResource) ScimResourceUtil.transferToResourceReplace(fidoDeviceResource, updatedResource, extService.getResourceExtensions(updatedResource.getClass()));
            transferAttributesToDevice(updatedResource, device);

            fidoDeviceService.updateGluuCustomFidoDevice(device);

            String json=resourceSerializer.serialize(updatedResource, attrsList, excludedAttrsList);
            response=Response.ok(new URI(updatedResource.getMeta().getLocation())).entity(json).build();
        }
        catch (SCIMException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, e.getMessage());
        }
        catch (InvalidAttributeValueException e){
            log.error(e.getMessage());
            response=getErrorResponse(Response.Status.CONFLICT, ErrorScimType.MUTABILITY, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at updateDevice method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path("{id}")
    @DELETE
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected
    @ApiOperation(value = "Delete device")
    public Response deleteDevice(
            @PathParam("id") String id,
            @HeaderParam("Authorization") String authorization){

        Response response;
        try {
            //No need to check id being non-null. FidoDeviceService will give null if null is provided
            GluuCustomFidoDevice device = fidoDeviceService.getGluuCustomFidoDeviceById(null, id);
            if (device != null) {
                fidoDeviceService.removeGluuCustomFidoDevice(device);
                response = Response.noContent().build();
            }
            else
                response = getErrorResponse(Response.Status.NOT_FOUND, ErrorScimType.INVALID_VALUE, "Resource " + id + " not found");
        }
        catch (Exception e){
            log.error("Failure at deleteDevice method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @GET
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Search devices", notes = "Returns a list of devices", response = ListResponse.class)
    public Response searchDevices(
            @QueryParam(QUERY_PARAM_FILTER) String filter,
            @QueryParam(QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(QUERY_PARAM_SORT_ORDER) String sortOrder,
            @QueryParam(QUERY_PARAM_START_INDEX) Integer startIndex,
            @QueryParam(QUERY_PARAM_COUNT) Integer count,
            @QueryParam(QUERY_PARAM_ATTRIBUTES) String attrsList,
            @QueryParam(QUERY_PARAM_EXCLUDED_ATTRS) String excludedAttrsList,
            @HeaderParam("Authorization") String authorization){

        Response response;
        try {
            VirtualListViewResponse vlv = new VirtualListViewResponse();
            List<BaseScimResource> resources = searchDevices(filter, sortBy, SortOrder.getByValue(sortOrder), startIndex, count, vlv, endpointUrl);

            String json = getListResponseSerialized(vlv.getTotalResults(), startIndex, resources, attrsList, excludedAttrsList, count==0);
            response=Response.ok(json).location(new URI(endpointUrl)).build();
        }
        catch (SCIMException e){
            log.error(e.getMessage(), e);
            response=getErrorResponse(Response.Status.BAD_REQUEST, ErrorScimType.INVALID_FILTER, e.getMessage());
        }
        catch (Exception e){
            log.error("Failure at searchDevices method", e);
            response=getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
        return response;

    }

    @Path(SEARCH_SUFFIX)
    @POST
    @Consumes({MEDIA_TYPE_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces({MEDIA_TYPE_SCIM_JSON + UTF8_CHARSET_FRAGMENT, MediaType.APPLICATION_JSON + UTF8_CHARSET_FRAGMENT})
    @HeaderParam("Accept") @DefaultValue(MEDIA_TYPE_SCIM_JSON)
    @Protected @RefAdjusted
    @ApiOperation(value = "Search devices POST /.search", notes = "Returns a list of fido devices", response = ListResponse.class)
    public Response searchDevicesPost(
            SearchRequest searchRequest,
            @HeaderParam("Authorization") String authorization){

        URI uri=null;
        Response response = searchDevices(searchRequest.getFilter(), searchRequest.getSortBy(), searchRequest.getSortOrder(),
                searchRequest.getStartIndex(), searchRequest.getCount(), searchRequest.getAttributes(),
                searchRequest.getExcludedAttributes(), authorization);

        try {
            uri = new URI(endpointUrl + "/" + SEARCH_SUFFIX);
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return Response.fromResponse(response).location(uri).build();

    }

    private void transferAttributesToFidoResource(GluuCustomFidoDevice fidoDevice, FidoDeviceResource res, String url, String userId) {

        res.setId(fidoDevice.getId());

        Meta meta=new Meta();
        meta.setResourceType(ScimResourceUtil.getType(res.getClass()));
        meta.setCreated(DateUtil.generalizedToISOStringDate(fidoDevice.getCreationDate()));
        meta.setLastModified(DateUtil.generalizedToISOStringDate(fidoDevice.getMetaLastModified()));
        meta.setLocation(fidoDevice.getMetaLocation());
        if (meta.getLocation()==null)
            meta.setLocation(url + "/" + fidoDevice.getId());

        res.setMeta(meta);

        //Set values in order of appearance in FidoDeviceResource class
        res.setUserId(userId);
        res.setApplication(fidoDevice.getApplication());
        res.setCounter(fidoDevice.getCounter());

        res.setDeviceData(fidoDevice.getDeviceData());
        res.setDeviceHashCode(fidoDevice.getDeviceHashCode());
        res.setDeviceKeyHandle(fidoDevice.getDeviceKeyHandle());
        res.setDeviceRegistrationConf(fidoDevice.getDeviceRegistrationConf());

        res.setLastAccessTime(DateUtil.generalizedToISOStringDate(fidoDevice.getLastAccessTime()));
        res.setStatus(fidoDevice.getStatus());
        res.setDisplayName(fidoDevice.getDisplayName());
        res.setDescription(fidoDevice.getDescription());
        res.setNickname(fidoDevice.getNickname());

    }

    /**
     * In practice, this transference of values will not modify original values in device...
     * @param res
     * @param device
     */
    private void transferAttributesToDevice(FidoDeviceResource res, GluuCustomFidoDevice device){

        //Set values trying to follow the order found in GluuCustomFidoDevice class
        device.setId(res.getId());
        device.setCreationDate(DateUtil.ISOToGeneralizedStringDate(res.getMeta().getCreated()));
        device.setApplication(res.getApplication());
        device.setCounter(res.getCounter());

        device.setDeviceData(res.getDeviceData());
        device.setDeviceHashCode(res.getDeviceHashCode());
        device.setDeviceKeyHandle(res.getDeviceKeyHandle());
        device.setDeviceRegistrationConf(res.getDeviceRegistrationConf());

        device.setLastAccessTime(DateUtil.ISOToGeneralizedStringDate(res.getLastAccessTime()));
        device.setStatus(res.getStatus());
        device.setDisplayName(res.getDisplayName());
        device.setDescription(res.getDescription());
        device.setNickname(res.getNickname());

        device.setMetaLastModified(DateUtil.ISOToGeneralizedStringDate(res.getMeta().getLastModified()));
        device.setMetaLocation(res.getMeta().getLocation());
        device.setMetaVersion(res.getMeta().getVersion());

    }

    private String getUserInumFromDN(String deviceDn){
        String baseDn=personService.getDnForPerson(null).replaceAll("\\s*","");
        deviceDn=deviceDn.replaceAll("\\s*","").replaceAll("," + baseDn, "");
        return deviceDn.substring(deviceDn.indexOf("inum=")+5);
    }

    private Filter getFilter(String filterString) throws SCIMException {

        Filter filter;
        try {
            if (StringUtils.isEmpty(filterString))
                filter = Filter.create("oxId=*");
            else
                filter = scimFilterParserService.createFilter(filterString, FidoDeviceResource.class);
        }
        catch (Exception e){
            throw new SCIMException("An error occurred parsing the filter expression (" + e.getMessage() + ")", e);
        }
        return filter;

    }

    public List<BaseScimResource> searchDevices(String filter, String sortBy, SortOrder sortOrder, int startIndex,
                                                    int count, VirtualListViewResponse vlvResponse, String url) throws Exception {
        Filter ldapFilter=getFilter(filter);
        //Transform scim attribute to LDAP attribute
        sortBy = FidoFilterVisitor.getLdapAttributeName(sortBy, FidoDeviceResource.class);

        log.info("Executing search for fido devices using: ldapfilter '{}', sortBy '{}', sortOrder '{}', startIndex '{}', count '{}'",
                ldapFilter.toString(), sortBy, sortOrder.getValue(), startIndex, count);
        List<GluuCustomFidoDevice> list=ldapEntryManager.findEntriesSearchSearchResult(fidoDeviceService.getDnForFidoDevice(null, null),
                GluuCustomFidoDevice.class, ldapFilter, startIndex, count, MAX_COUNT, sortBy, sortOrder, vlvResponse, null);

        List<BaseScimResource> resources=new ArrayList<BaseScimResource>();

        for (GluuCustomFidoDevice device : list){
            FidoDeviceResource scimDev=new FidoDeviceResource();
            transferAttributesToFidoResource(device, scimDev, url, getUserInumFromDN(device.getDn()));
            resources.add(scimDev);
        }
        log.info ("Found {} matching entries - returning {}", vlvResponse.getTotalResults(), list.size());
        return resources;

    }

    @PostConstruct
    public void setup(){
        //Do not use getClass() here... a typical weld issue...
        endpointUrl=appConfiguration.getBaseEndpoint() + FidoWebService.class.getAnnotation(Path.class).value();
    }

}
