package com.pszymczyk.embedded.consul

import com.pszymczyk.embedded.consul.infrstructure.Ports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConsulStarterBuilder {

    private Path dataDir
    private Path downloadDir
    private Integer httpPort

    private ConsulStarterBuilder() {

    }

    public static ConsulStarterBuilder consulStarter() {
        return new ConsulStarterBuilder()
    }

    public ConsulStarterBuilder withDataDirectory(Path dataDir) {
        this.dataDir = dataDir
        this
    }

    public ConsulStarterBuilder withConsulBinaryDownloadDirectory(Path downloadDir) {
        this.downloadDir = downloadDir
        this
    }

    public ConsulStarterBuilder withHttpPort(int httpPort) {
        this.httpPort = httpPort
        this
    }

    public ConsulStarter build() {
        applyDefaults()
        return new ConsulStarter(dataDir, downloadDir, httpPort)
    }

    void applyDefaults() {
        if (downloadDir == null) {
            downloadDir = Files.createTempDirectory('embedded-consul')
        }

        if (dataDir == null) {
            dataDir = Paths.get(downloadDir.toAbsolutePath().toString(), "data-dir")
        }

        if (httpPort == null) {
            this.httpPort = Ports.nextAvailable()
        }
    }
}
