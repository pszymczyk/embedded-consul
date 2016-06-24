package com.pszymczyk.consul

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConsulProcess implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConsulProcess.class);

    Path dataDir;
    int httpPort;
    Process process;

    @Override
    public void close() {
        logger.info("Stopping consul process running on port {}", httpPort)

        process.destroy();
    }
}
