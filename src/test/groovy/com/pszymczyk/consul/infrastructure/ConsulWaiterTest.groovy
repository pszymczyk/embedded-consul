package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.EmbeddedConsulException
import spock.lang.Specification


class ConsulWaiterTest extends Specification {

    def "should throw exception when timed out"() {
        when:
        new ConsulWaiter(0, 1).await()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }
}
