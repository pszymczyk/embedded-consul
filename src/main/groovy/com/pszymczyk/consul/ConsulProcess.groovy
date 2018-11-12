package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.SimpleConsulClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class);

    private final Path dataDir
    private final ConsulPorts consulPorts
    private final String address
    private final Process process
    private final SimpleConsulClient simpleConsulClient
    private final String token

    ConsulProcess(Path dataDir, ConsulPorts consulPorts, String address, String token, Process process) {
        this.dataDir = dataDir
        this.consulPorts = consulPorts
        this.address = address
        this.process = process
        this.token = token
        this.simpleConsulClient = new SimpleConsulClient(address, httpPort, token)
    }
    /**
     * Deregister all services except consul.
     */
    void reset() {
        simpleConsulClient.getServicesIds().each { it -> simpleConsulClient.deregister(it) }
        simpleConsulClient.clearKvStore()
        simpleConsulClient.destroyActiveSessions()
        simpleConsulClient.deregisterAllChecks()
    }

    @Override
    void close() {
        logger.info("Stopping Consul process")

        process.destroy()

        new ConsulWaiter(address, consulPorts.httpPort, token).awaitUntilConsulStopped() ?
                logger.info("Stopped Consul process") :
                logger.warn("Can't stop Consul process running on port {}", consulPorts.httpPort)
    }

    Path getDataDir() {
        dataDir
    }

    String getAddress() {
        return address
    }

    int getHttpPort() {
        consulPorts.httpPort
    }

    int getDnsPort() {
        consulPorts.dnsPort
    }

    int getSerfLanPort() {
        consulPorts.serfLanPort
    }

    int getSerfWanPort() {
        consulPorts.serfWanPort
    }

    int getServerPort() {
        consulPorts.serverPort
    }
}
