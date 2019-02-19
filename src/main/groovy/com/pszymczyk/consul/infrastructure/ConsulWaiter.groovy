package com.pszymczyk.consul.infrastructure

import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.EmbeddedConsulException
import org.codehaus.groovy.runtime.IOGroovyMethods

import java.util.concurrent.TimeUnit

class ConsulWaiter {

    static final int DEFAULT_WAITING_TIME_IN_SECONDS = 30

    private final SimpleConsulClient simpleConsulClient
    private final long timeoutMilis
    private final String host
    private final int port

    ConsulWaiter(String host, int port) {
        this(host, port, DEFAULT_WAITING_TIME_IN_SECONDS)
    }

    ConsulWaiter(String host, int port, int timeoutInSeconds) {
        this.timeoutMilis = TimeUnit.SECONDS.toMillis(timeoutInSeconds as long)
        this.host = host
        this.port = port
        this.simpleConsulClient = new SimpleConsulClient(host, port)
    }

    void awaitUntilConsulStarted() {
        Long startTime = System.currentTimeMillis()

        boolean elected

        while (!(elected = isLeaderElected() && allNodesRegistered()) && !isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        if (!elected) abnormalTerminate()
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

    protected void abnormalTerminate(ConsulProcess consulProcess) {
        long timeoutInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeoutMilis)
        consulProcess.close()
        throw new EmbeddedConsulException("Could not start Consul process in $timeoutInSeconds seconds")
    }
}
