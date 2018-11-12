package com.pszymczyk.consul.infrastructure

import com.ecwid.consul.v1.ConsulClient
import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarter
import com.pszymczyk.consul.ConsulStarterBuilder
import com.pszymczyk.consul.EmbeddedConsulException
import spock.lang.Specification


class ConsulWaiterTest extends Specification {

    def "should throw exception when timed out"() {
        when:
        new ConsulWaiter("localhost", 0, 1).awaitUntilConsulStarted()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"
    }


    def "should use token to get list of registered nodes"() {
        when:
        def uuid = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
          "acl_datacenter": "us",
          "acl_default_policy": "deny",
          "acl_agent_master_token": ${uuid},
          "acl_master_token": ${uuid},
          "acl_agent_token": ${uuid},
          "acl_token": ${uuid},
          "acl_enable_key_list_policy": true,
          "acl_down_policy": "deny"
        }"""

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(uuid)
                .withCustomConfig(conf)
                .withConsulVersion("1.0.0")
                .build().start()

        then:
        noExceptionThrown()

        cleanup:
        consul.close()
    }

    def "unable to get registered nodes with wrong token"() {
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
                .withConsulVersion("1.0.0")
                .withWaitTimeout(3)
                .build().start()

        then:
        def ex = thrown EmbeddedConsulException
        ex.message =~ "Could not start Consul*"

    }

    def "should get registered nodes with token for consul without token"() {
        when:
        def token = UUID.randomUUID().toString()
        def conf = """{
          "datacenter": "us",
        }"""

        ConsulProcess consul = ConsulStarterBuilder.consulStarter()
                .withToken(token)
                .withCustomConfig(conf)
                .withConsulVersion("1.0.0")
                .build().start()

        then:
        noExceptionThrown()

        cleanup:
        consul.close()
    }

}
