package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.agent.model.NewService
import com.pszymczyk.consul.infrastructure.SimpleConsulClient
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

class ConsulStarterTest extends Specification {

    ConsulProcess consul
    ConsulTestWaiter consulWaiter

    def setup() {
        consul = ConsulStarterBuilder.consulStarter().build().start()
        consulWaiter = new ConsulTestWaiter(consul.httpPort)
    }

    def cleanup() {
        consul.close()
    }

    def "should start consul"() {
        expect:
        new ConsulClient("localhost", consul.httpPort).getStatusLeader() != SimpleConsulClient.NO_LEADER_ELECTED_RESPONSE
    }

    def "should run multiple Consul processes simultaneously"() {
        given:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient1 = new ConsulClient("localhost", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient2 = new ConsulClient("localhost", consul2.httpPort)

        when:
        consulClient1.agentServiceRegister(new NewService(id: "1", name: "consulClient1", address: "localhost", port: 8080))
        consulClient2.agentServiceRegister(new NewService(id: "1", name: "consulClient2", address: "localhost", port: 8080))

        then:
        await().atMost(30, TimeUnit.SECONDS).until({
            try {
                consulClient1.getCatalogServices(QueryParams.DEFAULT).getValue().size() == 1
                consulClient1.getCatalogServices(QueryParams.DEFAULT).getValue().name == "consulClient1"
            } catch (Exception e) {
                false
            }
        })

        await().atMost(30, TimeUnit.SECONDS).until({
            try {
                consulClient2.getCatalogServices(QueryParams.DEFAULT).getValue().size() == 1
                consulClient2.getCatalogServices(QueryParams.DEFAULT).getValue().name == "consulClient2"
            } catch (Exception e) {
                false
            }
        })

        cleanup:
        consul1.close()
        consul2.close()
    }

    def "should throw exception when try to run Consul on busy port"() {
        when:
        ConsulStarterBuilder.consulStarter().withHttpPort(consul.httpPort).build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Port $consul.httpPort is not available"
    }

    def "should reset Consul process"() {
        given:
        ConsulClient consulClient = new ConsulClient('localhost', consul.httpPort)
        consulClient.agentServiceRegister(new NewService(id: '1', name: 'test-service-one'))
        consulClient.agentServiceRegister(new NewService(id: '2', name: 'test-service-two'))

        consulWaiter.awaitUntilServiceRegistered('1')
        consulWaiter.awaitUntilServiceRegistered('2')

        when:
        Thread.sleep(1000)
        consul.reset()

        then:
        consulClient.getAgentServices().value.size() == 1
        consulClient.getAgentServices().value.values().with {
            it.size() == 1
            it.first().service == 'consul'
        }
    }
}
