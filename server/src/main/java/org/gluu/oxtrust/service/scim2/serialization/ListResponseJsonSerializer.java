package org.gluu.oxtrust.service.scim2.serialization;

import org.apache.logging.log4j.LogManager;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.gluu.oxtrust.model.scim2.Constants.LIST_RESPONSE_SCHEMA_ID;

/**
 * Created by jgomer on 2017-10-01.
 */
public class ListResponseJsonSerializer extends JsonSerializer<ListResponse> {

    private Logger log = LogManager.getLogger(getClass());
    private ScimResourceSerializer resourceSerializer;
    private ObjectMapper mapper = new ObjectMapper();

    private String attributes;
    private String excludeAttributes;
    private List<String> jsonResources;

    //why not to inject the resource serializer instead of passing it as parameter? weld simply does not like it!
    public ListResponseJsonSerializer(ScimResourceSerializer serializer){
        resourceSerializer=serializer;
    }

    public ListResponseJsonSerializer(ScimResourceSerializer serializer, String attributes, String excludeAttributes){
        resourceSerializer=serializer;
        this.attributes=attributes;
        this.excludeAttributes=excludeAttributes;
    }

    @Override
    public void serialize(ListResponse listResponse, JsonGenerator jGen, SerializerProvider provider) throws IOException {

        try {
            jGen.writeStartObject();
            jGen.writeNumberField("totalResults", listResponse.getTotalResults());
            if (listResponse.getTotalResults()>0) {
                jGen.writeNumberField("startIndex", listResponse.getStartIndex());
                jGen.writeNumberField("itemsPerPage", listResponse.getItemsPerPage());
            }

            jGen.writeArrayFieldStart("schemas");
            jGen.writeString(LIST_RESPONSE_SCHEMA_ID);
            jGen.writeEndArray();

            if (listResponse.getResources()!=null) {
                jGen.writeArrayFieldStart("Resources");

                for (BaseScimResource resource : listResponse.getResources()) {
                    JsonNode jsonResource = mapper.readTree(resourceSerializer.serialize(resource, attributes, excludeAttributes));
                    jGen.writeTree(jsonResource);
                }
                jGen.writeEndArray();
            }
        }
        catch (Exception e) {
            throw new IOException(e);
        }

        jGen.writeEndObject();

    }

}
