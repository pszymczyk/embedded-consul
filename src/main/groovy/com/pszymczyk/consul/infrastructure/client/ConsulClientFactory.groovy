package com.pszymczyk.consul.infrastructure.client


import groovyx.net.http.RESTClient

class ConsulClientFactory {

    static SimpleConsulClient newClient(String host, int httpPort, String token) {
        RESTClient restClient = new RESTClient("http://$host:$httpPort")
        if (token != null) {
            restClient.setHeaders('X-Consul-Token': token)
        }
        return new SimpleConsulClient(restClient)
    }
}
