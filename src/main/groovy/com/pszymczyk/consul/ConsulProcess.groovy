package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.SimpleConsulClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class);

    private Path dataDir
    private int httpPort
    private Process process
    private SimpleConsulClient simpleConsulClient

    ConsulProcess(Path dataDir, int httpPort, Process process) {
        this.dataDir = dataDir
        this.httpPort = httpPort
        this.process = process
        this.simpleConsulClient = new SimpleConsulClient(httpPort: httpPort)
    }
    /**
     * deregister all services except consul
     */
    void reset() {
        simpleConsulClient.getServicesIds().each { it -> simpleConsulClient.deregister(it) }
    }

    @Override
    void close() {
        logger.info("Stopping consul process running on port {}", httpPort)

        process.destroy()
    }
}
