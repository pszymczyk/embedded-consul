package com.pszymczyk.consul

import spock.lang.Specification


class ConsulPortsTest extends Specification {

    def "should generate random ports"() {
        when:
        ConsulPorts consulPorts = ConsulPorts.consulPorts().build()

        then:
        consulPorts.dnsPort
        consulPorts.rpcPort
        consulPorts.serfLanPort
        consulPorts.serfWanPort
        consulPorts.serverPort
    }

    def "should return passed http port"() {
        when:
        ConsulPorts consulPorts = ConsulPorts.consulPorts().withHttpPort(8500).build()

        then:
        consulPorts.httpPort == 8500
    }
}
