package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.catalog.CatalogServicesRequest
import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.client.ConsulClientFactory

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await


class ConsulTestWaiter extends ConsulWaiter {

    ConsulClient consulClient

    ConsulTestWaiter(String host, int port) {
        super(host, port, ConsulClientFactory.newClient(host, port, Optional.empty()), Optional.ofNullable(2))
        this.consulClient = new ConsulClient(host, port)
    }

    void awaitUntilServiceRegistered(String id) {
        await().atMost(30, TimeUnit.SECONDS).until((Callable<Boolean>) {
            consulClient.getAgentServices().getValue().values().findAll({ id == it.id }).size() == 1
        })
    }

    void awaitConsulServiceRegistered(Optional<String> token) {
        await().atMost(30, TimeUnit.SECONDS).until((Callable<Boolean>) {
            def builder = CatalogServicesRequest.newBuilder().setQueryParams(QueryParams.DEFAULT)
            token.ifPresent({ builder.setToken(it) })
            consulClient.getCatalogServices(builder.build()).getValue().keySet().contains("consul")
        })
    }
}
