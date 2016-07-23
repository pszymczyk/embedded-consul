package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.agent.model.NewService
import com.jayway.awaitility.groovy.AwaitilityTrait
import spock.lang.Specification

class MultipleProcessesTest extends Specification implements AwaitilityTrait {

    def "should run multiple Consul processes simultaneously"() {
        given:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient1 = new ConsulClient("localhost", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient2 = new ConsulClient("localhost", consul2.httpPort)
        ConsulTestWaiter consulWaiter1 = new ConsulTestWaiter(consul1.httpPort)
        ConsulTestWaiter consulWaiter2 = new ConsulTestWaiter(consul2.httpPort)

        when:
        consulClient1.agentServiceRegister(new NewService(id: "consulClient1", name: "consulClient1", address: "localhost", port: 8080))
        consulClient2.agentServiceRegister(new NewService(id: "consulClient2", name: "consulClient2", address: "localhost", port: 8080))

        then:
        consulWaiter1.awaitUntilServiceRegistered("consulClient1")
        consulWaiter2.awaitUntilServiceRegistered("consulClient2")

        cleanup:
        consul1.close()
        consul2.close()
    }

    def "reset should not remove data from another process"() {
        given:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient1 = new ConsulClient("localhost", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().build().start()
        ConsulClient consulClient2 = new ConsulClient("localhost", consul2.httpPort)
        ConsulTestWaiter consulWaiter1 = new ConsulTestWaiter(consul1.httpPort)
        ConsulTestWaiter consulWaiter2 = new ConsulTestWaiter(consul2.httpPort)

        when:
        consulClient1.agentServiceRegister(new NewService(id: "consulClient1", name: "consulClient1", address: "localhost", port: 8080))
        consulClient2.agentServiceRegister(new NewService(id: "consulClient2", name: "consulClient2", address: "localhost", port: 8080))

        then:
        consulWaiter1.awaitUntilServiceRegistered("consulClient1")
        consulWaiter2.awaitUntilServiceRegistered("consulClient2")

        when:
        consul1.reset()

        then:
        Thread.sleep(1000)
        consulClient1.getCatalogServices(QueryParams.DEFAULT).getValue().size() == 1
        !consulClient2.getCatalogServices(QueryParams.DEFAULT).getValue().containsKey("consulClient1")
        consulClient2.getCatalogServices(QueryParams.DEFAULT).getValue().size() == 2
        consulClient2.getCatalogServices(QueryParams.DEFAULT).getValue().containsKey("consulClient2")

        cleanup:
        consul1.close()
        consul2.close()
    }
}
