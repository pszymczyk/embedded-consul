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

    static ConsulPortsBuilder consulPorts() {
        return new ConsulPortsBuilder()
    }

    static class ConsulPortsBuilder {

        private int httpPort = -1
        private int dnsPort = -1
        private int rpcPort = -1
        private int serfLanPort = -1
        private int serfWanPort = -1
        private int serverPort = -1

        private ConsulPortsBuilder() {
        }

        ConsulPorts build() {
            return new ConsulPorts(
                    randomIfNotSet(httpPort),
                    randomIfNotSet(dnsPort),
                    randomIfNotSet(rpcPort),
                    randomIfNotSet(serfLanPort),
                    randomIfNotSet(serfWanPort),
                    randomIfNotSet(serverPort)
            )
        }

        private int randomIfNotSet(int port) {
            return port > 0 ? port : Ports.nextAvailable()
        }

        ConsulPortsBuilder withHttpPort(int httpPort) {
            this.httpPort = httpPort
            return this
        }

        ConsulPortsBuilder withDnsPort(int dnsPort) {
            this.dnsPort = dnsPort
            return this
        }

        ConsulPortsBuilder withRpcPort(int rpcPort) {
            this.rpcPort = rpcPort
            return this
        }

        ConsulPortsBuilder withSerfLanPort(int serfLanPort) {
            this.serfLanPort = serfLanPort
            return this
        }

        ConsulPortsBuilder withSerfWanPort(int serfWanPort) {
            this.serfWanPort = serfWanPort
            return this
        }

        ConsulPortsBuilder withServerPort(int serverPort) {
            this.serverPort = serverPort
            return this
        }
    }
}
