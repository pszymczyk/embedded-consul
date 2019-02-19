package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.infrastructure.client.ConsulClientFactory
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.util.concurrent.TimeUnit

class ConsulWaiter {

    static final int DEFAULT_WAITING_TIME_IN_SECONDS = 30

    private final SimpleConsulClient simpleConsulClient
    private final long timeoutMilis
    private final String host
    private final int port

    ConsulWaiter(String host, int port, String token) {
        this(host, port, token, DEFAULT_WAITING_TIME_IN_SECONDS)
    }

    ConsulWaiter(String host, int port, String token, int timeoutInSeconds) {
        this.timeoutMilis = TimeUnit.SECONDS.toMillis(timeoutInSeconds as long)
        this.host = host
        this.port = port
        this.simpleConsulClient = ConsulClientFactory.newClient(host, port, token)
    }

    boolean awaitUntilConsulStarted() {
        Long startTime = System.currentTimeMillis()

        boolean elected

        while ((elected = isLeaderElected() && allNodesRegistered()) == false && !isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        return elected
    }

    boolean awaitUntilConsulStopped() {
        Long startTime = System.currentTimeMillis()
        while (!isTimedOut(startTime)) {
            try {
                IOGroovyMethods.withCloseable(new Socket(host, port), {
                    it -> Thread.sleep(100)
                })
            } catch (IOException ignore) {
                return true
            }
        }

        false
    }

    private boolean isLeaderElected() {
        try {
            boolean elected = simpleConsulClient.isLeaderElected()
            elected
        } catch (def ignore) {
            false
        }

    }

    private boolean allNodesRegistered() {
        try {
            simpleConsulClient.getRegisteredNodes().first().TaggedAddresses != null
        } catch (def ignore) {
            false
        }
    }

    protected boolean isTimedOut(long startTime) {
        System.currentTimeMillis() - startTime >= timeoutMilis
    }
}
