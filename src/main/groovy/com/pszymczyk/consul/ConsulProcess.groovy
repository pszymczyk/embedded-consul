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
    private final ConsulLogHandler consulLogHandler
    private final SimpleConsulClient simpleConsulClient

    ConsulProcess(Path dataDir, ConsulPorts consulPorts, String address, Process process, ConsulLogHandler consulLogHandler) {
        this.dataDir = dataDir
        this.consulPorts = consulPorts
        this.address = address
        this.process = process
        this.consulLogHandler = consulLogHandler
        this.simpleConsulClient = new SimpleConsulClient(address, httpPort)

        addShutdownHook { this.process.destroyForcibly() }
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
        consulLogHandler.close()

        new ConsulWaiter(address, consulPorts.httpPort).awaitUntilConsulStopped() ?
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
