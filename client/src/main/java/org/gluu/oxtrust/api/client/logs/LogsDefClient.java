package org.gluu.oxtrust.api.client.logs;

import org.gluu.oxtrust.api.client.util.AbstractClient;
import org.gluu.oxtrust.api.logs.LogFileDefApi;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.List;

public class LogsDefClient extends AbstractClient<LogFileDefApi> {

    private static final String PATH = "/restv1/api/logs";

    public LogsDefClient(Client client, String baseURI) {
        super(LogFileDefApi.class, client, baseURI, PATH);
    }

    public List<LogFileDefApi> list() {
        GenericType<List<LogFileDefApi>> responseType = new GenericType<List<LogFileDefApi>>() {
        };
        return webTarget.request().get(responseType);
    }
}
