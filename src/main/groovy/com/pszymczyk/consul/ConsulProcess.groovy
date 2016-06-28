package com.pszymczyk.consul

import com.ecwid.consul.v1.ConsulClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class);

    ConsulClient consulClient
    Path dataDir
    int httpPort
    Process process

    /**
     * deregister all services except consul
     */
    public void reset() {
        consulClient.getAgentServices().getValue()
                .values()
                .findAll({ it -> it.getService() != 'consul'})
                .each { it -> consulClient.agentServiceDeregister(it.getId()) }
    }

    @Override
    public void close() {
        logger.info("Stopping consul process running on port {}", httpPort)

        process.destroy()
    }
}
