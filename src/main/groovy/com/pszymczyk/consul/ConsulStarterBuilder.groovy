package com.pszymczyk.consul

import org.slf4j.Logger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConsulStarterBuilder {

    private Path dataDir
    private Path downloadDir
    private Path configDir
    private CustomConfig customConfig = CustomConfig.empty()
    private String consulVersion = '1.4.2'
    private LogLevel logLevel = LogLevel.ERR
    private Logger customLogger
    private ConsulPorts.ConsulPortsBuilder consulPortsBuilder = ConsulPorts.consulPorts()
    private String startJoin
    private String bind
    private String advertise = "127.0.0.1"
    private String client = "127.0.0.1"
    private String token
    private Integer waitTimeout

    private ConsulStarterBuilder() {

    }

    static ConsulStarterBuilder consulStarter() {
        return new ConsulStarterBuilder()
    }

    ConsulStarterBuilder withLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel
        this
    }

    ConsulStarterBuilder withLogger(Logger customLogger) {
        this.customLogger = customLogger
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

    ConsulStarterBuilder withConfigDir(Path configDir) {
        this.configDir = configDir
        this
    }

    ConsulStarterBuilder withCustomConfig(String customConfig) {
        this.customConfig = new CustomConfig(customConfig)
        this
    }

    ConsulStarterBuilder withHttpPort(int httpPort) {
        this.consulPortsBuilder.withHttpPort(httpPort)
        this
    }

    ConsulStarterBuilder withConsulPorts(ConsulPorts consulPorts) {
        this.consulPortsBuilder.fromConsulPorts(consulPorts)
        this
    }

    ConsulStarterBuilder withAttachedTo(ConsulProcess otherProcess) {
        this.startJoin = otherProcess == null ? null : "${otherProcess.address}:${otherProcess.serfLanPort}"
        this
    }

    ConsulStarterBuilder withAdvertise(String advertise) {
        this.advertise = advertise
        this
    }

    ConsulStarterBuilder withClient(String client) {
        this.client = client
        this
    }

    ConsulStarterBuilder withBind(String bind) {
        this.bind = bind
        this
    }

    ConsulStarterBuilder withToken(String token) {
        this.token = token
        this
    }

    ConsulStarterBuilder withWaitTimeout(long timeoutSeconds) {
        this.waitTimeout = timeoutSeconds
        this
    }


    ConsulStarter build() {
        applyDefaults()
        return new ConsulStarter(dataDir,
                downloadDir,
                configDir,
                consulVersion,
                customConfig,
                logLevel,
                customLogger,
                consulPortsBuilder,
                startJoin,
                advertise,
                client,
                bind,
                token,
                waitTimeout)
    }

    private void applyDefaults() {
        if (downloadDir == null) {
            downloadDir = Paths.get(Files.createTempDirectory('').parent.toString(), "embedded-consul-$consulVersion")
            Files.createDirectories(downloadDir)
        }

        if (dataDir == null) {
            dataDir = Files.createTempDirectory("embedded-consul-data-dir")
        }

        if (configDir == null) {
            configDir = Files.createTempDirectory("embedded-consul-config-dir")
        }
    }
}
