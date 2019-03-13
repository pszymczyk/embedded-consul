package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.OperationException
import com.ecwid.consul.v1.kv.model.PutParams
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static ConsulStarterBuilder.consulStarter
import static java.nio.charset.Charset.defaultCharset

class ConsulTokenTest extends Specification {

    @Shared def key = "some-dummy-consul-kv-store-key"
    @Shared def value = "some-dummy-consul-kv-store-value"
    def token = UUID.randomUUID().toString()

    @AutoCleanup ConsulProcess consul

    def "should start secured Consul with configured ACL feature"() {
        when:
        consul = consulStarter()
                .withToken(token)
                .withCustomConfig(aclConfiguration(token))
                .build()
                .start()
        ConsulTestWaiter consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)

        then:
        consulWaiter.awaitConsulServiceRegistered(Optional.of(token))
    }

    def "should not allow to start secured Consul with wrong token"() {
        given:
        def wrongToken = UUID.randomUUID().toString()
        def consulBuilder = consulStarter()
                .withToken(wrongToken)
                .withCustomConfig(aclConfiguration(token))
                .withWaitTimeout(2)
                .build()

        when:
        consul = consulBuilder.start()

        then:
        thrown EmbeddedConsulException
    }

    def "should start Consul with token without ACL configured"() {
        when:
        consul = consulStarter()
                .withToken(token)
                .build().start()

        ConsulTestWaiter consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)

        then:
        consulWaiter.awaitConsulServiceRegistered(Optional.empty())
    }

    def "should not allow to start secured Consul without token"() {
        given:
        def builder = consulStarter()
                .withWaitTimeout(2)
                .withCustomConfig(aclConfiguration(token)).build()

        when:
        builder.start()

        then:
        thrown EmbeddedConsulException
    }

    def "client calls without token to secured Consul should not be allowed"() {
        given:
        consul = consulStarter()
                .withToken(token)
                .withCustomConfig(aclConfiguration(token))
                .build()
                .start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)

        when:
        consulClient.setKVValue(key, value)

        then:
        OperationException ex = thrown OperationException
        ex.statusCode == 403
    }

    def "client should be able to call secured Consul with correct ACL"() {
        given:
        consul = consulStarter()
                .withToken(token)
                .withCustomConfig(aclConfiguration(token))
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)

        when:
        consulClient.setKVValue(key, value, token, new PutParams())

        then:
        consulClient.getKVValue(key, token).getValue().getDecodedValue(defaultCharset()) == value
    }

    def "client with token should be able to call unsecured Consul"() {
        given:
        consul = consulStarter().withToken(token).build().start()
        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)

        when:
        consulClient.setKVValue(key, value, token, new PutParams())

        then:
        consulClient.getKVValue(key, token).getValue().getDecodedValue(defaultCharset()) == value
    }


    def "client should be able to call unsecured consul with token"() {
        given:
        consul = consulStarter().build().start()
        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)

        when:
        consulClient.setKVValue(key, value, new PutParams())

        then:
        consulClient.getKVValue(key).getValue().getDecodedValue(defaultCharset()) == value
    }

    private static def aclConfiguration(String token) {
        return """{
          "acl": {
            "enabled": true,
            "default_policy": "deny",
            "down_policy": "deny",
            "tokens": {
                "agent": ${token},
                "master": ${token}
            }
        }"""
    }
}
