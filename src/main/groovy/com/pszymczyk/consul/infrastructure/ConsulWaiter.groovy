package com.pszymczyk.consul.infrastructure

import com.ecwid.consul.v1.ConsulClient
import com.pszymczyk.consul.EmbeddedConsulException

import java.util.concurrent.TimeUnit

public class ConsulWaiter {

    public static final int DEFAULT_WAITING_TIME_IN_SECONDS = 10

    static final String NO_LEADER_ELECTED_RESPONSE = "";

    protected ConsulClient consulClient

    private final int timeoutMilis

    ConsulWaiter(int port) {
        this(port, DEFAULT_WAITING_TIME_IN_SECONDS)
    }

    ConsulWaiter(int port, int timeoutInSeconds) {
        this.consulClient = new ConsulClient("localhost", port)
        this.timeoutMilis = TimeUnit.SECONDS.toMillis(timeoutInSeconds as long)
    }

    void awaitUntilConsulStarted() {
        Long startTime = System.currentTimeMillis()

        boolean elected

        while ((elected = isLeaderElected()) == false && !isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        if (!elected) abnormalTerminate("Could not start Consul process")
    }

    private boolean isLeaderElected() {
        try {
            consulClient.getStatusLeader().getValue() != NO_LEADER_ELECTED_RESPONSE
        } catch (def e) {
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
