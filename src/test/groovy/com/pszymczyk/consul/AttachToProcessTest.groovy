package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.jayway.awaitility.groovy.AwaitilityTrait
import spock.lang.Specification

class AttachToProcessTest extends Specification implements AwaitilityTrait {

    def "should attach to other process"() {
        when:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().buildAndStart()
        ConsulClient consulClient = new ConsulClient(consul1.address, consul1.httpPort)
        int clusterSize1 = consulClient.agentMembers.value.size()
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().withAttachedTo(consul1).buildAndStart()
        int clusterSize2 = consulClient.agentMembers.value.size()

        then:
        clusterSize1 == 1
        clusterSize2 == 2

        cleanup:
        consul1.close()
        consul2.close()
    }
}
