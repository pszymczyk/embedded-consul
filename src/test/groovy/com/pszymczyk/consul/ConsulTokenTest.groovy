package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.OperationException
import com.ecwid.consul.v1.kv.model.PutParams
import spock.lang.Specification

import java.nio.charset.Charset

class ConsulTokenTest extends Specification {


    /**
     * TODO: Config for ACL will be changed since 1.4.0, so we will have to change these tests,
     * once we set default consul version to 1.4.0 or higher
     */
    def "should start secured consul with configured token"() {
        when:
        def uuid = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_agent_master_token": ${uuid},
          "acl_master_token": ${uuid},
          "acl_agent_token": ${uuid},
          "acl_enable_key_list_policy": true,
          "acl_down_policy": "deny"
        }"""

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(uuid)
                .withCustomConfig(conf)
                .build().start()

        ConsulTestWaiter consulWaiter = new ConsulTestWaiter('localhost', consul.httpPort)

        then:
        consulWaiter.awaitConsulServiceRegistered()
        noExceptionThrown()

        cleanup:
        consul.close()
    }

    def "unable to start secured consul with wrong token"() {
        when:
        def token = UUID.randomUUID().toString()
        def wrongToken = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_master_token": ${token},
          "acl_enable_key_list_policy": true,
          "acl_down_policy": "deny"
        }"""

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
        def conf = """{
          "datacenter": "us",
        }"""

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .withCustomConfig(conf)
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
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_master_token": ${token},
          "acl_enable_key_list_policy": true,
          "acl_down_policy": "deny"
        }"""

        ConsulStarterBuilder.consulStarter()
                .withCustomConfig(conf)
                .build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }

    def "client should not call secured consul without ACL"() {
        when:
        def uuid = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_master_token": ${uuid},
          "acl_agent_master_token": ${uuid},
          "acl_agent_token": ${uuid}
        }"""

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(uuid)
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
        def uuid = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_master_token": ${uuid},
          "acl_agent_master_token": ${uuid},
          "acl_agent_token": ${uuid}
        }"""

        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(uuid)
                .withCustomConfig(conf)
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue(key, value, uuid, new PutParams())
        String actualValue = consulClient.getKVValue(key, uuid).getValue().getDecodedValue(Charset.defaultCharset())

        then:
        noExceptionThrown()
        actualValue == value

        cleanup:
        consul.close()
    }

    def "client should be able to call unsecured consul without token"() {
        when:
        def uuid = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us"
        }"""

        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(uuid)
                .withCustomConfig(conf)
                .build().start()

        ConsulClient consulClient = new ConsulClient("localhost", consul.httpPort)
        consulClient.setKVValue(key, value, uuid, new PutParams())
        String actualValue = consulClient.getKVValue(key, uuid).getValue().getDecodedValue(Charset.defaultCharset())

        then:
        noExceptionThrown()
        actualValue == value

        cleanup:
        consul.close()
    }


    def "client should be able to call unsecured consul with token"() {
        when:
        def conf = """{
          "datacenter": "us"
        }"""

        final String key = "key"
        final String value = "value"

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withCustomConfig(conf)
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

}
