package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.OperationException
import com.ecwid.consul.v1.kv.model.PutParams
import spock.lang.Specification

import java.nio.charset.Charset

class ConsulTokenTest extends Specification {

    def "should start secured consul with configured token"() {
        when:
        def token = UUID.randomUUID().toString()
        def conf = consulWithACL(token)

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .withCustomConfig(conf)
                .build()
                .start()

        ConsulTestWaiter consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)

        then:
        consulWaiter.awaitConsulServiceRegistered(token)
        noExceptionThrown()

        cleanup:
        consul.close()
    }

    def "unable to start secured consul with wrong token"() {
        when:
        def token = UUID.randomUUID().toString()
        def wrongToken = UUID.randomUUID().toString()
        def conf = consulWithACL(token)

        ConsulStarterBuilder.consulStarter()
                .withToken(wrongToken)
                .withCustomConfig(conf)
                .withWaitTimeout(3)
                .build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"

    }

    def "should start consul without token"() {
        when:
        def token = UUID.randomUUID().toString()

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .build().start()

        ConsulTestWaiter consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)

        then:
        consulWaiter.awaitConsulServiceRegistered()
        noExceptionThrown()

        cleanup:
        consul.close()
    }

    def "unable to start secured consul without token"() {
        when:
        def token = UUID.randomUUID().toString()
        def conf = consulWithACL(token)


        ConsulStarterBuilder.consulStarter()
                .withCustomConfig(conf)
                .build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }

    def "client should not call secured consul without ACL"() {
        when:
        def token = UUID.randomUUID().toString()
        def conf = consulWithACL(token)

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .withCustomConfig(conf)
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue("key", "value")

        consulClient.getKVValue("key")

        then:
        OperationException ex = thrown OperationException
        ex.statusCode == 403

        cleanup:
        consul.close()
    }

    def "client should be able to call secured consul with correct ACL"() {
        when:
        def token = UUID.randomUUID().toString()
        def conf = consulWithACL(token)

        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .withCustomConfig(conf)
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue(key, value, token, new PutParams())
        String actualValue = consulClient.getKVValue(key, token).getValue().getDecodedValue(Charset.defaultCharset())

        then:
        noExceptionThrown()
        actualValue == value

        cleanup:
        consul.close()
    }

    def "client should be able to call unsecured consul without token"() {
        when:
        def token = UUID.randomUUID().toString()

        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue(key, value, token, new PutParams())
        String actualValue = consulClient.getKVValue(key, token).getValue().getDecodedValue(Charset.defaultCharset())

        then:
        noExceptionThrown()
        actualValue == value

        cleanup:
        consul.close()
    }


    def "client should be able to call unsecured consul with token"() {
        when:
        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue(key, value, new PutParams())
        String actualValue = consulClient.getKVValue(key).getValue().getDecodedValue(Charset.defaultCharset())

        then:
        noExceptionThrown()
        actualValue == value

        cleanup:
        consul.close()
    }

    private GString consulWithACL(token) {
        return """{
          "acl": {
            "enabled": true,
            "default_policy": "deny",
            "down_policy": "deny",
            "tokens": {
                "master": ${token}
            }
        }"""
    }
}
