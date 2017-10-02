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

import static org.gluu.oxtrust.model.scim2.Constants.LIST_RESPONSE_SCHEMA_ID;

/**
 * Created by jgomer on 2017-10-01.
 */
public class ListResponseJsonSerializer extends JsonSerializer<ListResponse> {

    private Logger log = LogManager.getLogger(getClass());
    private ScimResourceSerializer resourceSerializer;
    private ObjectMapper mapper = new ObjectMapper();

    public ListResponseJsonSerializer(ScimResourceSerializer serializer){
        resourceSerializer=serializer;
    }

    @Override
    public void serialize(ListResponse listResponse, JsonGenerator jGen, SerializerProvider provider) throws IOException {

        jGen.writeStartObject();
        jGen.writeNumberField("totalResults", listResponse.getTotalResults());
        jGen.writeNumberField("startIndex", listResponse.getStartIndex());
        jGen.writeNumberField("itemsPerPage", listResponse.getItemsPerPage());

        jGen.writeArrayFieldStart("schemas");
        jGen.writeString(LIST_RESPONSE_SCHEMA_ID);
        jGen.writeEndArray();

        jGen.writeArrayFieldStart("Resources");
        for (BaseScimResource resource : listResponse.getResources()){
            try {
                log.debug("oh margoth");
                JsonNode jsonResource=mapper.readTree(resourceSerializer.serialize(resource));
                jGen.writeTree(jsonResource);
                //jGen.writeString(resourceSerializer.serialize(resource));
            }
            catch (Exception e){
                throw new IOException(e);
            }
        }
        jGen.writeEndArray();

        jGen.writeEndObject();

    }

}
