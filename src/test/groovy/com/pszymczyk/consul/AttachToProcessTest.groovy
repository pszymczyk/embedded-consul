package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.jayway.awaitility.groovy.AwaitilityTrait
import spock.lang.Specification

class AttachToProcessTest extends Specification implements AwaitilityTrait {

    def "should attach to other process"() {
        when:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient = new ConsulClient(consul1.address, consul1.httpPort)

        then:
        consulClient.agentMembers.value.size() == 1

        when:
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().withAttachedTo(consul1).build().start()

        then:
        consulClient.agentMembers.value.size() == 2

        cleanup:
        consul1.close()
        consul2.close()
    }
}
