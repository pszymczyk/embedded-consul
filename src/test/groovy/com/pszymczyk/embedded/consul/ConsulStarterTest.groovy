package com.pszymczyk.embedded.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.agent.model.NewService
import okhttp3.OkHttpClient
import okhttp3.Request
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await

class ConsulStarterTest extends Specification {

    @Shared
    OkHttpClient client = new OkHttpClient()

    ConsulProcess consul

    def setup() {
        consul = ConsulStarterBuilder.consulStarter().build().start()
    }

    def cleanup() {
        consul.close()
    }

    def "should start consul"() {
        expect:
        await().atMost(30, TimeUnit.SECONDS).until({
            Request request = new Request.Builder()
                    .url("http://localhost:$consul.httpPort/v1/agent/self")
                    .build();

            client.newCall(request).execute().code() == 200
        })
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
}
