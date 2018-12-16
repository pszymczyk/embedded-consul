package com.pszymczyk.consul.infrastructure


import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarterBuilder
import com.pszymczyk.consul.ConsulTestWaiter
import com.pszymczyk.consul.EmbeddedConsulException
import spock.lang.Specification

class ConsulWaiterTest extends Specification {

    def "should return false when timed out"() {
        when:
        boolean started = new ConsulWaiter("localhost", 0, null,1).awaitUntilConsulStarted()

        then:
        !started
    }
}
