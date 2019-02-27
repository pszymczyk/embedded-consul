package com.pszymczyk.consul.infrastructure.client

import com.pszymczyk.consul.EmbeddedConsulException
import groovy.transform.PackageScope
import groovyx.net.http.HttpResponseDecorator

class ConsulClientException extends EmbeddedConsulException {

    HttpResponseDecorator originResponse

    @PackageScope
    ConsulClientException(HttpResponseDecorator originResponse) {
        this(originResponse, "Http request to Consul server failed")
    }

    protected ConsulClientException(HttpResponseDecorator originResponse, String customMessage) {
        super("$customMessage. Status code : [${originResponse.status}], response body: [$originResponse.entity.content.text], response headers $originResponse.allHeaders")
        this.originResponse = originResponse
    }
}
