/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxauth.client.validation;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;
import org.gluu.oxauth.client.authentication.SimplePrincipal;

public class CustomHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customParameters = new HashMap<String, String>();
    
    private String username;
    private Principal userPrincipal;
    
    public CustomHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void addCustomParameter(String name, String value) {
        customParameters.put(name, value);
    }

    @Override
    public String getParameter(String name) {
        String originalParameter = super.getParameter(name);

        if(originalParameter != null) {
                return originalParameter;
        } else {
                return customParameters.get(name);
        }
    }

    @Override
    public Principal getUserPrincipal() {
        Principal principal = super.getUserPrincipal();
        if (principal != null)
            return principal;
        else
            return userPrincipal; 
    }

    @Override
    public String getRemoteUser() {
        String user = super.getRemoteUser();
        if (user != null && !user.isEmpty())
            return user;
        else
            return username; 
    }
    
    public void setRemoteUser(String username) {
        this.username = username;
        userPrincipal = new SimplePrincipal(username);
    }
}
