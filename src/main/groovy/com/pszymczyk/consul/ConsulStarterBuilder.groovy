package com.pszymczyk.consul

import org.slf4j.Logger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static com.pszymczyk.consul.infrastructure.StringUtils.requireNotBlank
import static java.util.Objects.requireNonNull


class ConsulStarterBuilder {

    private final Set<Service> services = new LinkedHashSet<>()

    private Path dataDir
    private Path downloadDir
    private Path configDir
    private CustomConfig customConfig = CustomConfig.empty()
    private String consulVersion = '1.8.4'
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
        this.logLevel = requireNonNull(logLevel, "Given log level cannot be null.")
        this
    }

    ConsulStarterBuilder withLogger(Logger customLogger) {
        this.customLogger = requireNonNull(customLogger, "Given logger cannot be null.")
        this
    }

    ConsulStarterBuilder withDataDirectory(Path dataDir) {
        this.dataDir = requireNonNull(dataDir, "Given data directory cannot be null.")
        this
    }

    ConsulStarterBuilder withConsulBinaryDownloadDirectory(Path downloadDir) {
        this.downloadDir = requireNonNull(downloadDir, "Given download directory cannot be null.")
        this
    }

    ConsulStarterBuilder withConsulVersion(String consulVersion) {
        this.consulVersion = requireNotBlank(consulVersion, "Given Consul version cannot be null or blank.")
        this
    }

    ConsulStarterBuilder withConfigDir(Path configDir) {
        this.configDir = requireNonNull(configDir, "Given configuration directory cannot be null.")
        this
    }

    ConsulStarterBuilder withCustomConfig(String customConfig) {
        this.customConfig = new CustomConfig(requireNotBlank(customConfig, "Given custom configuration cannot bu null or blank."))
        this
    }

    ConsulStarterBuilder withHttpPort(int httpPort) {
        this.consulPortsBuilder.withHttpPort(httpPort)
        this
    }

    ConsulStarterBuilder withConsulPorts(ConsulPorts consulPorts) {
        this.consulPortsBuilder.fromConsulPorts(requireNonNull(consulPorts, "Given Consul ports cannot be null."))
        this
    }

    ConsulStarterBuilder withAttachedTo(ConsulProcess otherProcess) {
        requireNonNull(otherProcess, "Given process to join cannot be null.")
        this.startJoin = "${otherProcess.address}:${otherProcess.serfLanPort}"
        this
    }

    ConsulStarterBuilder withAdvertise(String advertise) {
        this.advertise = requireNotBlank(advertise, "Given advertise address cannot be null or blank.")
        this
    }

    ConsulStarterBuilder withClient(String client) {
        this.client = requireNotBlank(client, "Given client cannot be null or blank.")
        this
    }

    ConsulStarterBuilder withBind(String bind) {
        this.bind = requireNotBlank(bind, "Given bind address cannot be null or blank.")
        this
    }

    ConsulStarterBuilder withToken(String token) {
        this.token = requireNotBlank(token, "Given token cannot be null or blank")
        this
    }

    ConsulStarterBuilder withWaitTimeout(int timeoutSeconds) {
        this.waitTimeout = timeoutSeconds
        this
    }

    ConsulStarterBuilder withService(Service... serviceName) {
        this.services.addAll(serviceName)
        this
    }

    ConsulProcess buildAndStart() {
        build().start()
    }

    ConsulStarter build() {
        applyDefaults()
        return new ConsulStarter(new UserInput(dataDir,
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
                waitTimeout,
                services))
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
