package com.pszymczyk.consul

import spock.lang.Specification


class ConsulPortsTest extends Specification {

    def "should generate random ports"() {
        when:
        ConsulPorts consulPorts = ConsulPorts.create()

        then:
        consulPorts.dnsPort
        consulPorts.rpcPort
        consulPorts.serfLanPort
        consulPorts.serfWanPort
        consulPorts.serverPort
    }

    def "should return passed http port"() {
        when:
        ConsulPorts consulPorts = ConsulPorts.create().withHttpPort(8500)

        then:
        consulPorts.httpPort == 8500
    }
}
