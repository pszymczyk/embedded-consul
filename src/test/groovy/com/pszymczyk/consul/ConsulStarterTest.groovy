package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import spock.lang.Shared
import spock.lang.Specification

class ConsulStarterTest extends Specification {

    @Shared
    ConsulProcess consul
    @Shared
    ConsulTestWaiter consulWaiter
    @Shared
    ConsulClient consulClient

    def setupSpec() {
        consul = ConsulStarterBuilder.consulStarter().build().start()
        consulWaiter = new ConsulTestWaiter(consul.httpPort)
        consulClient = new ConsulClient('localhost', consul.httpPort)
    }

    def cleanupSpec() {
        consul.close()
    }

    def "should start consul"() {
        expect:
        consulClient.getStatusLeader().getValue().startsWith("127.0.0.1:")
    }

    def "should throw exception when try to run Consul on busy port"() {
        when:
        ConsulStarterBuilder.consulStarter().withHttpPort(consul.httpPort).build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Port $consul.httpPort is not available"
    }

    def "should remove all services when reset Consul process"() {
        given:
        consulClient.agentServiceRegister(new NewService(id: '1', name: 'test-service-one'))
        consulClient.agentServiceRegister(new NewService(id: '2', name: 'test-service-two'))

        consulWaiter.awaitUntilServiceRegistered('1')
        consulWaiter.awaitUntilServiceRegistered('2')

        when:
        consul.reset()

        then:
        consulClient.getAgentServices().value.size() == 1
        consulClient.getAgentServices().value.values().with {
            it.size() == 1
            it.first().service == 'consul'
        }
    }

    def "should remove all data from kv store when reset Consul process"() {
        given:
        consulClient.setKVBinaryValue("foo", "bar".getBytes())

        when:
        consul.reset()

        then:
        consulClient.getKVBinaryValue("foo").getValue() == null
    }
}
