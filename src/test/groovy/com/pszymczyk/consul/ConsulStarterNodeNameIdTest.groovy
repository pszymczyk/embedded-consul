package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import spock.lang.Specification

public class ConsulStarterNodeNameIdTest extends Specification {

    def "should generate random id and name by default"() {
        when:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        String name1 = new ConsulClient("localhost", consul1.httpPort)
                .agentSelf.value.config.nodeName;
        String id1 = new ConsulClient("localhost", consul1.httpPort)
                .agentSelf.value.config.nodeId

        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().build().start()
        String name2 = new ConsulClient("localhost", consul2.httpPort)
                .agentSelf.value.config.nodeName;
        String id2 = new ConsulClient("localhost", consul2.httpPort)
                .agentSelf.value.config.nodeId

        then:
        name1 != name2
        id1 != id2

        cleanup:
        consul1.close()
        consul2.close()

    }

    def "should use user defined node id"() {
        when:
        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withCustomConfig("{\"node_id\":\"01234567-890a-bcde-f012-34567890abcd\"}")
                .build().start()
        String id = new ConsulClient("localhost", consul.httpPort)
                .agentSelf.value.config.nodeId

        then:
        id == "01234567-890a-bcde-f012-34567890abcd"

        cleanup:
        consul.close()
    }

    def "should use user defined node name"() {
        when:
        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withCustomConfig("{\"node_name\":\"custom_node_name\"}")
                .build().start()
        String name = new ConsulClient("localhost", consul.httpPort)
                .agentSelf.value.config.nodeName;

        then:
        name == "custom_node_name"

        cleanup:
        consul.close()
    }
}
