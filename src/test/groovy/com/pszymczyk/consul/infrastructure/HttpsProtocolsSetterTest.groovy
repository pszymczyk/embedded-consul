package com.pszymczyk.consul.infrastructure

import spock.lang.Specification

class HttpsProtocolsSetterTest extends Specification {

    def "should set required tls for java 1.7"() {
        given:
        HttpsProtocolsSetter.PropertiesStorage storage = Mock()
        HttpsProtocolsSetter setter = new HttpsProtocolsSetter(storage)

        when:
        setter.setRequiredTls("1.7.xxx", null)

        then:
        1 * storage.setProperty(HttpsProtocolsSetter.HTTPS_PROTOCOLS_PROP_KEY, HttpsProtocolsSetter.TLS_12)
    }

    def "should add required tls to existing tls for java 1.7"() {
        given:
        HttpsProtocolsSetter.PropertiesStorage storage = Mock()
        HttpsProtocolsSetter setter = new HttpsProtocolsSetter(storage)

        when:
        setter.setRequiredTls("1.7.xxx", "TLSv1,TLSv1.1")

        then:
        1 * storage.setProperty(HttpsProtocolsSetter.HTTPS_PROTOCOLS_PROP_KEY, "TLSv1,TLSv1.1,${HttpsProtocolsSetter.TLS_12}")
    }

    def "should not modify tls when contains required tls"() {
        given:
        HttpsProtocolsSetter.PropertiesStorage storage = Mock()
        HttpsProtocolsSetter setter = new HttpsProtocolsSetter(storage)

        when:
        setter.setRequiredTls("1.7.xxx", "omg,null,${HttpsProtocolsSetter.TLS_12},consul")

        then:
        0 * storage.setProperty(_, _)
    }

    def "should not modify tls for java 1.8"() {
        given:
        HttpsProtocolsSetter.PropertiesStorage storage = Mock()
        HttpsProtocolsSetter setter = new HttpsProtocolsSetter(storage)

        when:
        setter.setRequiredTls("1.8.xxx", null)

        then:
        0 * storage.setProperty(_, _)
    }
}
