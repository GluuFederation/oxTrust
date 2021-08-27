package org.gluu.oxtrust.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class CloudEditionUtil {
    public final static String CN_OXAUTH_HOST = "CN_OXAUTH_HOST";
    public final static  String CN_IDP_HOST = "CN_IDP_HOST";

    private CloudEditionUtil(){
    }
   public static Optional<String> getOxAuthHost(){
       return Optional.ofNullable(System.getProperty(CN_OXAUTH_HOST));
   }

    public static Optional<String> getIdpHost(){
        return Optional.ofNullable(System.getProperty(CN_IDP_HOST));
    }

    public static  URL getOxAuthUrl(URL url, String oxauth_Env) throws MalformedURLException {
        String[] values = oxauth_Env.split(":");
        if (values.length == 2) {
            url = new URL(url.getProtocol(), values[0], Integer.valueOf(values[1]), url.getFile());
        }
        if (values.length == 3) {
            url = new URL(values[0], values[1].substring(2), Integer.valueOf(values[2]), url.getFile());
        }
        else {
            url = new URL(url.getProtocol(), values[0], url.getPort(), url.getFile());
        }
        return url;
    }


}
