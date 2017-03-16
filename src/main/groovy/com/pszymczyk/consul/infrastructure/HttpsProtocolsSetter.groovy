package com.pszymczyk.consul.infrastructure

import groovy.transform.PackageScope;

@PackageScope
class HttpsProtocolsSetter {

    static final String TLS_12 = "TLSv1.2"
    static final String HTTPS_PROTOCOLS_PROP_KEY = "https.protocols"

    private PropertiesStorage propertiesStorage

    interface PropertiesStorage {
        void setProperty(String key, String value)
    }

    HttpsProtocolsSetter(PropertiesStorage propertiesStorage) {
        this.propertiesStorage = propertiesStorage
    }

    void setRequiredTls(String javaVersion, String protocols) {
        javaVersion = javaVersion == null ? "" : javaVersion
        protocols = protocols == null ? "" : protocols

        if (javaVersion.startsWith("1.7") && !protocols.contains(TLS_12)) {
            def desiredProtocols = protocols.size() > 0 ? "$protocols,$TLS_12" : TLS_12

            propertiesStorage.setProperty(HTTPS_PROTOCOLS_PROP_KEY, desiredProtocols)
        }
    }

    static class SystemPropertiesStorage implements PropertiesStorage {

        @Override
        void setProperty(String key, String value) {
            System.setProperty(key, value)
        }
    }
}
