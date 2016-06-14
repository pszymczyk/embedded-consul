package com.pszymczyk.consul;

import java.nio.file.Path;

public class ConsulProcess implements AutoCloseable{

    Path dataDir;
    int httpPort;
    Process process;

    @Override
    public void close() throws Exception {
        process.destroy();
    }
}
