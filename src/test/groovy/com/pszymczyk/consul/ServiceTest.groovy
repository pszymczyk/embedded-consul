package com.pszymczyk.consul

import spock.lang.Specification

class ServiceTest extends Specification {

    def "test default address and port"() {
        given:
        def newService = new Service("service name")

        expect:
        newService.address == null
        newService.port == null
    }
}
