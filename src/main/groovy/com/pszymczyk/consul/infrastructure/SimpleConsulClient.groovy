package com.pszymczyk.consul.infrastructure

import groovy.json.JsonSlurper;

public class SimpleConsulClient {

    private static final String NO_LEADER_ELECTED_RESPONSE = /""/;

    int httpPort

    boolean isLeaderElected() {
        "http://localhost:$httpPort/v1/status/leader".toURL().getText() != NO_LEADER_ELECTED_RESPONSE
    }

    Collection getServicesIds() {
        new JsonSlurper().parse("http://localhost:$httpPort/v1/agent/services".toURL()).keySet().findAll({ it -> it != "consul"})
    }

    void deregister(String id) {
        "http://localhost:$httpPort/v1/agent/service/deregister/$id".toURL().getText()
    }
}
