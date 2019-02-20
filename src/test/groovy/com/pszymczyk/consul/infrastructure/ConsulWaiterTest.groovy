package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.EmbeddedConsulException
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import spock.lang.Specification


class ConsulWaiterTest extends Specification {

    def "should throw exception when timed out"() {
        when:
        new ConsulWaiter("localhost", 0, Stub(SimpleConsulClient), Optional.empty()).awaitUntilConsulStarted()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }
}
