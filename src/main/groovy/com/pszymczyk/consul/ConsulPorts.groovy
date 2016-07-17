package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.Ports

class ConsulPorts {

    int httpPort
    int dnsPort
    int rpcPort
    int serfLanPort
    int serfWanPort
    int serverPort

    ConsulPorts(int httpPort) {
        this.httpPort = httpPort
        int[] ports = Ports.nextAvailable(5)
        this.dnsPort = ports[0]
        this.rpcPort = ports[1]
        this.serfLanPort = ports[2]
        this.serfWanPort = ports[3]
        this.serverPort = ports[4]
    }
}
