package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class)

    final Path dataDir
    final String address

    private final ConsulPorts consulPorts
    private final Process process
    private final SimpleConsulClient simpleConsulClient
    private final ConsulWaiter consulWaiter
    private final ConsulLogHandler consulLogHandler

    @PackageScope
    ConsulProcess(Path dataDir, ConsulPorts consulPorts, String address, Process process, SimpleConsulClient simpleConsulClient, ConsulWaiter consulWaiter, ConsulLogHandler consulLogHandler) {
        this.dataDir = dataDir
        this.consulPorts = consulPorts
        this.address = address
        this.process = process
        this.simpleConsulClient = simpleConsulClient
        this.consulWaiter = consulWaiter
        this.consulLogHandler = consulLogHandler
        addShutdownHook { this.process.destroyForcibly()}
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

        consulWaiter.awaitUntilConsulStopped() ?
                logger.info("Stopped Consul process") :
                logger.warn("Can't stop Consul process running on port {}", consulPorts.httpPort)
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
