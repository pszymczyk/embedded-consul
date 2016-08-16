package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.Ports

class ConsulPorts {

    final int httpPort
    final int dnsPort
    final int rpcPort
    final int serfLanPort
    final int serfWanPort
    final int serverPort

    private ConsulPorts(int httpPort, int dnsPort, int rpcPort, int serfLanPort, int serfWanPort, int serverPort) {
        this.httpPort = httpPort
        this.dnsPort = dnsPort
        this.rpcPort = rpcPort
        this.serfLanPort = serfLanPort
        this.serfWanPort = serfWanPort
        this.serverPort = serverPort
    }

    static ConsulPorts create() {
        return new ConsulPorts(
                Ports.nextAvailable(),
                Ports.nextAvailable(),
                Ports.nextAvailable(),
                Ports.nextAvailable(),
                Ports.nextAvailable(),
                Ports.nextAvailable()
        )
    }

    ConsulPorts withHttpPort(int httpPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

    ConsulPorts withDnsPort(int dnsPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

    ConsulPorts withRpcPort(int rpcPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

    ConsulPorts withSerfLanPort(int serfLanPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

    ConsulPorts withSerfWanPort(int serfWanPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

    ConsulPorts withServerPort(int serverPort) {
        new ConsulPorts(httpPort, dnsPort, rpcPort, serfLanPort, serfWanPort, serverPort)
    }

}
