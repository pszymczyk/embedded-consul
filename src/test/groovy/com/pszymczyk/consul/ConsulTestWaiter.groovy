package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter


class ConsulTestWaiter extends ConsulWaiter {

    ConsulTestWaiter(int port) {
        super(port)
    }

    void awaitUntilServiceRegistered(String id) {
        Long startTime = System.currentTimeMillis()

        boolean found

        while ((found = isServiceRegistered(id)) == false && isTimedOut(startTime)) {
            Thread.sleep(100)
        }

        if (!found) {
            abnormalTerminate("Could not find service with id $id")
        }
    }

    private boolean isServiceRegistered(String id) {
        consulClient.getAgentServices().getValue().values().findAll({ it -> id == it.id }).size() == 1
    }
}
