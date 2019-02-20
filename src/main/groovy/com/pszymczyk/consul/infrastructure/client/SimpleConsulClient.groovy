package com.pszymczyk.consul.infrastructure.client

import groovy.transform.PackageScope
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

class SimpleConsulClient {

    private static final String NO_LEADER_ELECTED_RESPONSE = ""

    private final RESTClient http

    @PackageScope
    SimpleConsulClient(RESTClient http) {
        this.http = http
    }

    boolean isLeaderElected() {
        HttpResponseDecorator response = http.get(path: '/v1/status/leader')

        response.getData() != NO_LEADER_ELECTED_RESPONSE
    }

    Collection getRegisteredNodes() {
        HttpResponseDecorator response = http.get(path: '/v1/catalog/nodes')

        response.getData()
    }

    Collection<String> getServicesIds() {
        HttpResponseDecorator response = http.get(path: '/v1/agent/services')

        response.getData()
                .keySet()
                .findAll({ it -> it != 'consul' })
    }

    void deregister(String id) {
        http.put(path: "/v1/agent/service/deregister/$id", contentType: ContentType.ANY)
    }

    void clearKvStore() {
        http.delete(path: "/v1/kv/", query: [recurse: true], contentType: ContentType.ANY)
    }

    void destroyActiveSessions() {
        HttpResponseDecorator response = http.get(path: "/v1/session/list")

        response.getData().each {
            def id = it.ID
            http.put(path: "/v1/session/destroy/$id", contentType:  ContentType.ANY)
        }
    }

    void deregisterAllChecks() {
        HttpResponseDecorator response = http.get(path: "/v1/agent/checks")

        response.getData().each {
            def id = it.key

            http.put(path: "/v1/agent/check/deregister/$id")
        }
    }
}
