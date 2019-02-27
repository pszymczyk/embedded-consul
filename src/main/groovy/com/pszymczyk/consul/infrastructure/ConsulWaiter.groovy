package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.EmbeddedConsulException
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.util.concurrent.TimeUnit

class ConsulWaiter {

    static final int DEFAULT_WAITING_TIME_IN_SECONDS = 30

    private final SimpleConsulClient simpleConsulClient
    private final long timeoutMilis
    private final String host
    private final int port

    ConsulWaiter(String host, int port, SimpleConsulClient simpleConsulClient, Optional<Integer> timeoutInSeconds) {
        this.timeoutMilis = TimeUnit.SECONDS.toMillis(timeoutInSeconds.orElse(DEFAULT_WAITING_TIME_IN_SECONDS) as long)
        this.host = host
        this.port = port
        this.simpleConsulClient = simpleConsulClient
    }

    void awaitUntilConsulStarted() {
        Long startTime = System.currentTimeMillis()

        boolean elected

        while (!(elected = isLeaderElected() && allNodesRegistered()) && !isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        if (!elected) abnormalTerminate("Could not start Consul process")
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

    protected void abnormalTerminate(String message) {
        long timeoutInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeoutMilis)
        throw new EmbeddedConsulException("$message in $timeoutInSeconds seconds")
    }
}
