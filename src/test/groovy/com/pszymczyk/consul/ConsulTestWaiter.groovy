package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.pszymczyk.consul.infrastructure.ConsulWaiter

import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await


class ConsulTestWaiter extends ConsulWaiter {

    ConsulClient consulClient

    ConsulTestWaiter(String host, int port) {
        super(host, port)
        this.consulClient = new ConsulClient(host, port)
    }

    void awaitUntilServiceRegistered(String id) {
        await().atMost(30, TimeUnit.SECONDS).until({
            consulClient.getAgentServices().getValue().values().findAll({ id == it.id }).size() == 1
        })
    }
}
