package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.agent.model.NewCheck
import com.ecwid.consul.v1.agent.model.NewService
import com.ecwid.consul.v1.session.model.NewSession
import spock.lang.Shared
import spock.lang.Specification

class ConsulStarterTest extends Specification {

    static TEST_SERVICE = new Service("test_service", "127.0.0.1", 8000)
    static ANOTHER_TEST_SERVICE = new Service("another_test_service", "127.0.0.1", 8000)

    @Shared
    ConsulProcess consul
    @Shared
    ConsulTestWaiter consulWaiter
    @Shared
    ConsulClient consulClient

    def setupSpec() {
        def conf = """{
          "datacenter": "test-dc",
          "ports": {
            "dns": 12121
          }
        }"""
        consul = ConsulStarterBuilder.consulStarter()
                .withCustomConfig(conf)
                .withService(TEST_SERVICE, ANOTHER_TEST_SERVICE)
                .buildAndStart()
        consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)
        consulClient = new ConsulClient('localhost', consul.httpPort)
    }

    def "should start consul"() {
        expect:
        consulClient.statusLeader.value.startsWith("127.0.0.1:")
        consulClient.catalogDatacenters.value.contains("test-dc")
        consul.dnsPort == 12121
        !consulClient.getCatalogNodes(QueryParams.DEFAULT).getValue().isEmpty()
    }

    def "should register service"() {
        given:
        consulWaiter.awaitUntilServiceRegistered(TEST_SERVICE.name)
        consulWaiter.awaitUntilServiceRegistered(ANOTHER_TEST_SERVICE.name)

        when:
        def services = consulClient.agentServices.value

        then:
        services[TEST_SERVICE.name].address == TEST_SERVICE.address
        services[TEST_SERVICE.name].port == TEST_SERVICE.port

        and:
        services[ANOTHER_TEST_SERVICE.name].address == ANOTHER_TEST_SERVICE.address
        services[ANOTHER_TEST_SERVICE.name].port == ANOTHER_TEST_SERVICE.port
    }

    def "should throw exception when try to run Consul on busy port"() {
        when:
        ConsulStarterBuilder.consulStarter().withHttpPort(consul.httpPort).buildAndStart()

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
        consulClient.agentServices.value.size() == 0
    }

    def "should remove all data from kv store when reset Consul process"() {
        given:
        consulClient.setKVBinaryValue("foo", "bar".getBytes())

        when:
        consul.reset()

        then:
        consulClient.getKVBinaryValue("foo").getValue() == null
    }

    def "should destroy all sessions when reset Consul process"() {
        given:
        NewSession session = new NewSession()
        consulClient.sessionCreate(session, QueryParams.DEFAULT)
        assert !consulClient.getSessionList(QueryParams.DEFAULT).value.isEmpty()

        when:
        consul.reset()

        then:
        consulClient.getSessionList(QueryParams.DEFAULT).value.isEmpty()
    }

    def "should deregister all checks when reset Consul process"() {
        given:
        NewCheck newCheck = new NewCheck()
        newCheck.name = "test-check"
        newCheck.interval = "10s"
        newCheck.http = "http://example.com"

        consulClient.agentCheckRegister(newCheck)
        assert consulClient.getAgentChecks().value.size() == 1

        when:
        consul.reset()

        then:
        consulClient.agentChecks.value.isEmpty()
    }
}
