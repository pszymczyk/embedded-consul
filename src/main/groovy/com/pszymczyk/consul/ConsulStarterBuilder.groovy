package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.Ports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConsulStarterBuilder {

    private Path dataDir
    private Path downloadDir
    private String consulVersion = '0.6.4'
    private File portsConfigFile
    private LogLevel logLevel = LogLevel.ERR
    private ConsulPorts consulPorts

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

    ConsulStarterBuilder withConsulVersion(String consulVersion) {
        this.consulVersion = consulVersion
        this
    }

    ConsulStarterBuilder withPortsConfigFile(File file) {
        this.portsConfigFile = file
        this
    }

    ConsulStarterBuilder withHttpPort(int httpPort) {
        this.consulPorts = ConsulPorts.consulPorts().withHttpPort(httpPort).build()
        this
    }

    ConsulStarterBuilder withConsulPorts(ConsulPorts consulPorts) {
        this.consulPorts = consulPorts
        this
    }

    ConsulStarter build() {
        applyDefaults()
        return new ConsulStarter(dataDir, downloadDir, consulVersion, portsConfigFile, logLevel, consulPorts)
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

        if (consulPorts == null) {
            this.consulPorts = ConsulPorts.consulPorts().build()
        }
    }
}
