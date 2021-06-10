package org.gluu.oxtrust.util;

import org.jetbrains.annotations.NotNull;

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
        if (values.length > 1) {
            url = new URL(url.getProtocol(), values[0], Integer.valueOf(values[1]), url.getFile());
        } else {
            url = new URL(url.getProtocol(), values[0], url.getPort(), url.getFile());
        }
        return url;
    }


}
