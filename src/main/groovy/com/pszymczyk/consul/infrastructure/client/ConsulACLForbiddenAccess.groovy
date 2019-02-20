package com.pszymczyk.consul.infrastructure.client

import groovy.transform.PackageScope
import groovyx.net.http.HttpResponseDecorator


class ConsulACLForbiddenAccess extends ConsulClientException {

    @PackageScope
    ConsulACLForbiddenAccess(HttpResponseDecorator originResponse) {
        super(originResponse, "Forbidden access to requested resource")
    }
}
