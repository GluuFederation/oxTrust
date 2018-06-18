package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.logs.LogFileApi;
import org.gluu.oxtrust.api.logs.LogFileDefApi;

import java.util.List;

public class LogsTestCase {

    private final OxTrustClient client;

    public LogsTestCase(OxTrustClient client) {
        this.client = client;
    }

    public void run() throws APITestException {
        List<LogFileDefApi> list = client.getLogsDefClient().list();
        if (list.isEmpty()) {
            throw new APITestException("list all logs failed!");
        }

        LogFileApi log = client.getLogClient().log(list.get(0).getId(), 100);
        if (log == null) {
            throw new APITestException("get log data failed!");
        }

        if (log.getId() != list.get(0).getId()) {
            throw new APITestException("id mismatch");
        }

    }
}
