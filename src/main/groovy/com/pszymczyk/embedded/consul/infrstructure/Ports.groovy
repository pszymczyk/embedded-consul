package com.pszymczyk.embedded.consul.infrstructure

import org.codehaus.groovy.runtime.IOGroovyMethods

class Ports {
    static int nextAvailable() {
        IOGroovyMethods.withCloseable(new ServerSocket(0), { it ->
            it.getLocalPort()
        })
    }
}
