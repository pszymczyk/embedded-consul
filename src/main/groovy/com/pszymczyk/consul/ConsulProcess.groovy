package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.SimpleConsulClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class);

    private final Path dataDir
    private final ConsulPorts consulPorts
    private final Process process
    private final SimpleConsulClient simpleConsulClient

    ConsulProcess(Path dataDir, ConsulPorts consulPorts, Process process) {
        this.dataDir = dataDir
        this.consulPorts = consulPorts
        this.process = process
        this.simpleConsulClient = new SimpleConsulClient(httpPort)
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
        logger.info("Stopping Consul process running on port {}", httpPort)

        process.destroy()

        new ConsulWaiter(consulPorts.httpPort).awaitUntilConsulStopped() == true ?
                logger.info("Stopped Consul process running on port {}", httpPort) :
                logger.warn("Can't stop Consul process running on port {}", httpPort)
    }

    Path getDataDir() {
        dataDir
    }

    int getHttpPort() {
        consulPorts.httpPort
    }

    int getDnsPort() {
        consulPorts.dnsPort
    }

    int getRpcPort() {
        consulPorts.rpcPort
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
