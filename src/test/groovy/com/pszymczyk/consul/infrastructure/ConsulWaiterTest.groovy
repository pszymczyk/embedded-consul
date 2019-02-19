package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.EmbeddedConsulException
import spock.lang.Specification


class ConsulWaiterTest extends Specification {

    def "should throw exception when timed out"() {
        when:
        new ConsulWaiter("localhost", 0, 1).awaitUntilConsulStarted(Stub(ConsulProcess))

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }
}
