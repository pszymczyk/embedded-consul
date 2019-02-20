package com.pszymczyk.consul.infrastructure

import org.codehaus.groovy.runtime.IOGroovyMethods

class Ports {
    static int nextAvailable() {
        IOGroovyMethods.withCloseable(new ServerSocket(0), { it ->
            it.getLocalPort()
        })
    }
}
