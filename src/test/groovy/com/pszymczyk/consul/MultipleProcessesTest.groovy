package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.agent.model.NewService
import spock.lang.Requires
import spock.lang.Specification

class MultipleProcessesTest extends Specification {

    def "should run multiple Consul processes simultaneously"() {
        given:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().buildAndStart()
        ConsulClient consulClient1 = new ConsulClient("localhost", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().buildAndStart()
        ConsulClient consulClient2 = new ConsulClient("localhost", consul2.httpPort)
        ConsulTestWaiter consulWaiter1 = new ConsulTestWaiter("localhost", consul1.httpPort)
        ConsulTestWaiter consulWaiter2 = new ConsulTestWaiter("localhost", consul2.httpPort)

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

    @Requires({ os.linux })
    def "should run multiple Consul processes with same port simultaneously"() {
        when:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter()
                .withBind("127.0.0.1")
                .withClient("127.0.0.1")
                .withAdvertise("127.0.0.1")
                .buildAndStart()
        ConsulClient consulClient1 = new ConsulClient("127.0.0.1", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter()
                .withHttpPort(consul1.httpPort)
                .withBind("127.0.0.10")
                .withClient("127.0.0.10")
                .withAdvertise("127.0.0.10")
                .buildAndStart()
        ConsulClient consulClient2 = new ConsulClient("127.0.0.10", consul2.httpPort)
        ConsulProcess consul3 = ConsulStarterBuilder.consulStarter()
                .withHttpPort(consul2.httpPort)
                .withBind("127.0.0.11")
                .withClient("127.0.0.11")
                .withAdvertise("127.0.0.11")
                .buildAndStart()
        ConsulClient consulClient3 = new ConsulClient("127.0.0.11", consul3.httpPort)

        then:
        consulClient1.getStatusPeers().getValue().get(0).startsWith("127.0.0.1:")
        consulClient2.getStatusPeers().getValue().get(0).startsWith("127.0.0.10:")
        consulClient3.getStatusPeers().getValue().get(0).startsWith("127.0.0.11:")

        cleanup:
        consul1.close()
        consul2.close()
        consul3.close()
    }

    def "reset should not remove data from another process"() {
        given:
        ConsulProcess consul1 = ConsulStarterBuilder.consulStarter().buildAndStart()
        ConsulClient consulClient1 = new ConsulClient("localhost", consul1.httpPort)
        ConsulProcess consul2 = ConsulStarterBuilder.consulStarter().buildAndStart()
        ConsulClient consulClient2 = new ConsulClient("localhost", consul2.httpPort)
        ConsulTestWaiter consulWaiter1 = new ConsulTestWaiter("localhost", consul1.httpPort)
        ConsulTestWaiter consulWaiter2 = new ConsulTestWaiter("localhost", consul2.httpPort)

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
