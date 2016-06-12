package com.pszymczyk.embedded.consul

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
}
