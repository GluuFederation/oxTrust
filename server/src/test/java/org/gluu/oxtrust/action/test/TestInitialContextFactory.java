/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.test;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Mock Initila context
 * 
 * @author Yuriy Movchan
 * Date: 05/23/2016
 */
public class TestInitialContextFactory implements InitialContextFactory {

    private static Context context;

    static {
        try {
            context = new InitialContext(true) {
                Map<String, Object> bindings = new HashMap<String, Object>();

                @Override
                public void bind(String name, Object obj)
                        throws NamingException {
                    bindings.put(name, obj);
                }

                @Override
                public Object lookup(String name) throws NamingException {
                    return bindings.get(name);
                }

                @Override
                protected Context getDefaultInitCtx() throws NamingException{
                	return context;
                }
            };
        } catch (NamingException e) { // can't happen.
            throw new RuntimeException(e);
        }
    }

    public Context getInitialContext(Hashtable<?, ?> environment)
            throws NamingException {
        return context;
    }

    public static void bind(String name, Object obj) {
        try {
            context.bind(name, obj);
        } catch (NamingException e) { // can't happen.
            throw new RuntimeException(e);
        }
    }
}