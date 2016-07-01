package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.Ports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConsulStarterBuilder {

    private Path dataDir
    private Path downloadDir
    private File portsConfigFile
    private LogLevel logLevel = LogLevel.ERR
    private Integer httpPort

    private ConsulStarterBuilder() {

    }

    static ConsulStarterBuilder consulStarter() {
        return new ConsulStarterBuilder()
    }

    ConsulStarterBuilder withLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel
        this
    }

    ConsulStarterBuilder withDataDirectory(Path dataDir) {
        this.dataDir = dataDir
        this
    }

    ConsulStarterBuilder withConsulBinaryDownloadDirectory(Path downloadDir) {
        this.downloadDir = downloadDir
        this
    }

    ConsulStarterBuilder withPortsConfigFile(File file) {
        this.portsConfigFile = file
        this
    }

    ConsulStarterBuilder withHttpPort(int httpPort) {
        this.httpPort = httpPort
        this
    }

    ConsulStarter build() {
        applyDefaults()
        return new ConsulStarter(dataDir, downloadDir, portsConfigFile, logLevel, httpPort)
    }

    private void applyDefaults() {
        if (downloadDir == null) {
            downloadDir = Paths.get(Files.createTempDirectory('').parent.toString(), 'embedded-consul')
            Files.createDirectories(downloadDir)
        }

        if (dataDir == null) {
            dataDir = Files.createTempDirectory("embedded-consul-data-dir")
        }

        if (portsConfigFile == null) {
            portsConfigFile = new File(dataDir.toFile(), "config.json")
        }

        if (httpPort == null) {
            this.httpPort = Ports.nextAvailable()
        }
    }
}
