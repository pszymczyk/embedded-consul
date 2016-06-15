package com.pszymczyk.consul.infrstructure

import com.pszymczyk.consul.EmbeddedConsulException

import java.util.concurrent.TimeUnit

public class ConsulWaiter {

    public static final int DEFAULT_WAITING_TIME_IN_SECONDS = 10

    private static final String NO_LEADER_ELECTED_RESPONSE = /""/;

    private final int timeoutMilis
    private final int port

    private boolean isLeaderElected

    ConsulWaiter(int port) {
        this(port, DEFAULT_WAITING_TIME_IN_SECONDS)
    }

    ConsulWaiter(int port, int timeoutInSeconds) {
        this.port = port
        this.timeoutMilis = TimeUnit.SECONDS.toMillis(timeoutInSeconds as long)
    }

    void await() {
        Long startTime = System.currentTimeMillis()

        while (!isLeaderElected(port) && !isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        if (!isLeaderElected) abnormalTerminate()
    }

    private boolean isLeaderElected(int port) {
        try {
            isLeaderElected = "http://localhost:$port/v1/status/leader".toURL().getText() != NO_LEADER_ELECTED_RESPONSE
            isLeaderElected
        } catch (def e) {
            false
        }

    }

    private boolean isTimedOut(long startTime) {
        System.currentTimeMillis() - startTime >= timeoutMilis
    }

    private void abnormalTerminate() {
        long timeoutInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeoutMilis)
        throw new EmbeddedConsulException("Could not start Consul process in $timeoutInSeconds seconds")
    }
}
