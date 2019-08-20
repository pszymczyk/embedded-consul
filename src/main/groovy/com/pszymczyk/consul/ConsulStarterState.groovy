package com.pszymczyk.consul

import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.IOGroovyMethods

@PackageScope
class ConsulStarterState {

    private final String bindAddress
    private final int httpPort

    private boolean started = false

    ConsulStarterState(String bindAddress, int httpPort) {
        this.bindAddress = bindAddress
        this.httpPort = httpPort
    }

    def start() {
        checkInitialState()
        started = true
    }

    private void checkInitialState() {
        if (started) {
            throw new EmbeddedConsulException('This Consul Starter instance already started Consul process. Create new ConsulStarter instance')
        }
        try {
            IOGroovyMethods.withCloseable(new Socket(bindAddress ?: "localhost", httpPort), {
                it-> throw new EmbeddedConsulException("Port ${httpPort} is not available, cannot start Consul process.")
            })
        } catch (IOException ex) {
            // socket is free - everything ok
        }
    }
}
