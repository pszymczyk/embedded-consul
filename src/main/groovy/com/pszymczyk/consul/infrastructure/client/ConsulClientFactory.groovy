package com.pszymczyk.consul.infrastructure.client


import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

class ConsulClientFactory {

    static SimpleConsulClient newClient(String host, int httpPort, Optional<String> token) {
        RESTClient restClient = new RESTClient("http://$host:$httpPort", ContentType.JSON)
        handleForbiddenAccess(restClient)
        handleGenericFailure(restClient)
        addTokenIfNecessary(restClient, token)
        return new SimpleConsulClient(restClient)
    }

    private static void handleForbiddenAccess(RESTClient restClient) {
        restClient.handler.'403' = { HttpResponseDecorator resp ->
            throw new ConsulACLForbiddenAccess(resp)
        }
    }

    private static void handleGenericFailure(RESTClient restClient) {
        restClient.handler.failure = { HttpResponseDecorator resp ->
            throw new ConsulClientException(resp)
        }
    }

    private static void addTokenIfNecessary(RESTClient restClient, Optional<String> token) {
        token.ifPresent({ restClient.setHeaders('X-Consul-Token': it) })
    }
}
