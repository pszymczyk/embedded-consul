package com.pszymczyk.consul

import static com.pszymczyk.consul.infrastructure.StringUtils.requireNotBlank

class Service {

    final String name;
    final String address;
    final Integer port;

    Service(String name) {
        this(name, null, null)
    }

    Service(String name, String address, Integer port) {
        this.name = requireNotBlank(name, "Service name could not be null or blank!")
        this.address = address
        this.port = port
    }
}
