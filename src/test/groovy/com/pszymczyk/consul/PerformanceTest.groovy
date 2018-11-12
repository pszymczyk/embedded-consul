package com.pszymczyk.consul

import spock.lang.Ignore
import spock.lang.Specification

class PerformanceTest extends Specification {

    @Ignore
    def "should time starting Consul process"() {
        given:
        def start = System.currentTimeMillis()

        when:
        (1..10).each {
            ConsulStarterBuilder.consulStarter().build().start()
        }

        then:
        def total = System.currentTimeMillis() - start
        println total / 1000
    }
}
