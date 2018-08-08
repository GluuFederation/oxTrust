package org.gluu.oxtrust.api.authorization.casprotocol;

public enum SessionStorageType {
    DEFAULT_STORAGE_SERVICE("shibboleth.StorageService"),
    MEMCACHED_STORE_SERVICE("shibboleth.MemcachedStorageService");

    private final String name;

    SessionStorageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SessionStorageType from(String name) {
        if (DEFAULT_STORAGE_SERVICE.getName().equals(name)) {
            return DEFAULT_STORAGE_SERVICE;
        } else if (MEMCACHED_STORE_SERVICE.getName().equals(name)) {
            return MEMCACHED_STORE_SERVICE;
        }
        throw new IllegalArgumentException(name + " not supported!");
    }

}