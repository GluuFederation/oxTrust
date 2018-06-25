package org.gluu.oxtrust.api.client.logs;

import org.gluu.oxtrust.api.client.util.AbstractClient;
import org.gluu.oxtrust.api.logs.LogFileApi;
import org.gluu.oxtrust.api.logs.LogFileDefApi;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.List;

public class LogClient extends AbstractClient<LogFileApi> {

    private static final String PATH = "/restv1/api/logs";

    public LogClient(Client client, String baseURI) {
        super(LogFileApi.class, client, baseURI, PATH);
    }

    public LogFileApi log(int id, int numberOfLines) {
        GenericType<LogFileApi> responseType = new GenericType<LogFileApi>() {
        };
        return webTarget.path("/{id}/{numberOfLines}")
                .resolveTemplate("id", id)
                .resolveTemplate("numberOfLines", numberOfLines)
                .request().get(responseType);
    }
}
