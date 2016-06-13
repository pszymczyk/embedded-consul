package com.pszymczyk.embedded.consul.infrstructure

import org.codehaus.groovy.runtime.IOGroovyMethods

class Ports {
    static int nextAvailable() {
        IOGroovyMethods.withCloseable(new ServerSocket(0), { it ->
            it.getLocalPort()
        })
    }

    static int[] nextAvailable(int length) {
        int[] ports = new int[length]

        for (int i = 0; i<length; i++) {
            ports[i] = nextAvailable()
        }

        ports
    }
}
