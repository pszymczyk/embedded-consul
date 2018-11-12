package com.pszymczyk.consul.infrastructure

import groovyx.net.http.ContentType
import groovyx.net.http.EncoderRegistry
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleConsulClient {

    private static final String NO_LEADER_ELECTED_RESPONSE = ""

    private final RESTClient http
    private final String token

    SimpleConsulClient(String host, int httpPort, String token) {
        this.http = new RESTClient("http://$host:$httpPort")

        this.token = token
        if (token != null) {
            this.http.setHeaders('X-Consul-Token': token)
        }

        def originalParser = this.http.parser.getAt(ContentType.JSON)
        this.http.parser.putAt(ContentType.JSON, { HttpResponseDecorator resp ->
            if (resp.status >= 200 && resp.status < 300 ){
                return originalParser(resp)
            } else {
                throw new GroovyRuntimeException("Unexpected response status code: ${resp.status}:" +
                        " ${resp.getEntity().getContent().getText("UTF-8")}")
            }
        })
    }

    boolean isLeaderElected() {
        HttpResponseDecorator response = http.get(path: '/v1/status/leader', contentType: ContentType.JSON)

        response.getData() != NO_LEADER_ELECTED_RESPONSE
    }

    Collection getRegisteredNodes() {
        HttpResponseDecorator response = http.get(path: '/v1/catalog/nodes', contentType: ContentType.JSON)

        response.getData()
    }

    Collection getServicesIds() {
        HttpResponseDecorator response = http.get(path: '/v1/agent/services', contentType: ContentType.JSON)

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
        HttpResponseDecorator response = http.get(path: "/v1/session/list", contentType: ContentType.JSON)

        response.getData().each {
            def id = it.ID
            http.put(path: "/v1/session/destroy/$id", contentType:  ContentType.ANY)
        }
    }

    void deregisterAllChecks() {
        HttpResponseDecorator response = http.get(path: "/v1/agent/checks", contentType: ContentType.JSON)

        response.getData().each {
            def id = it.key

            http.put(path: "/v1/agent/check/deregister/$id", contentType: ContentType.ANY)
        }
    }
}
