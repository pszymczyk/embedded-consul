package com.pszymczyk.consul.infrastructure.client


import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

class ConsulClientFactory {

    static SimpleConsulClient newClient(String host, int httpPort, String token) {
        RESTClient restClient = new RESTClient("http://$host:$httpPort")
        if (token != null) {
            restClient.setHeaders('X-Consul-Token': token)
            extendParser(restClient)
        }
        return new SimpleConsulClient(restClient)
    }

    private static extendParser(RESTClient restClient) {
        def originalParser = restClient.parser.getAt(ContentType.JSON)
        restClient.parser.putAt(ContentType.JSON, { HttpResponseDecorator resp ->
            if (resp.status >= 200 && resp.status < 300 ){
                return originalParser(resp)
            } else {
                throw new GroovyRuntimeException("Unexpected response status code: ${resp.status}:" +
                        " ${resp.getEntity().getContent().getText("UTF-8")}")
            }
        })
    }
}
