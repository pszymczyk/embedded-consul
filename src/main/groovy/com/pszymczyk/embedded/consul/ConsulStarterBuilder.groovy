package com.pszymczyk.embedded.consul

import com.pszymczyk.embedded.consul.infrstructure.Ports

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

    public static ConsulStarterBuilder consulStarter() {
        return new ConsulStarterBuilder()
    }

    public ConsulStarterBuilder withLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel
        this
    }

    public ConsulStarterBuilder withDataDirectory(Path dataDir) {
        this.dataDir = dataDir
        this
    }

    public ConsulStarterBuilder withConsulBinaryDownloadDirectory(Path downloadDir) {
        this.downloadDir = downloadDir
        this
    }

    public ConsulStarterBuilder withPortsConfigFile(File file) {
        this.portsConfigFile = file
        this
    }

    public ConsulStarterBuilder withHttpPort(int httpPort) {
        this.httpPort = httpPort
        this
    }

    public ConsulStarter build() {
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
