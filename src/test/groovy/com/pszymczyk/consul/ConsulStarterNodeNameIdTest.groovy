package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import spock.lang.Specification

class ConsulStarterNodeNameIdTest extends Specification {

    def "should generate random name by default"() {
        when:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        String name1 = new ConsulClient("localhost", consul1.httpPort)
                .agentSelf.value.config.nodeName

        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().build().start()
        String name2 = new ConsulClient("localhost", consul2.httpPort)
                .agentSelf.value.config.nodeName

        then:
        name1 != name2

        cleanup:
        consul1.close()
        consul2.close()

    }

    def "should use user defined node name"() {
        when:
        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withCustomConfig("{\"node_name\":\"custom_node_name\"}")
                .build().start()
        String name = new ConsulClient("localhost", consul.httpPort)
                .agentSelf.value.config.nodeName

        then:
        name == "custom_node_name"

        cleanup:
        consul.close()
    }
}
