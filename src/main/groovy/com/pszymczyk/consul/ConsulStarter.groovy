package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.AntUnzip
import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.HttpBinaryRepository
import com.pszymczyk.consul.infrastructure.OsResolver
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulStarter {

    private static final Logger logger = LoggerFactory.getLogger(ConsulStarter.class);

    private final Path dataDir
    private final Path downloadDir
    private final Path configDir
    private String customConfig;
    private final String consulVersion
    private final LogLevel logLevel
    private final ConsulPorts consulPorts

    private boolean started = false

    private HttpBinaryRepository binaryRepository
    private AntUnzip unzip

    @PackageScope
    ConsulStarter(Path dataDir, Path downloadDir, Path configDir, String consulVersion, String customConfig, LogLevel logLevel, ConsulPorts.ConsulPortsBuilder ports) {
        this.logLevel = logLevel
        this.configDir = configDir
        this.customConfig = customConfig
        this.dataDir = dataDir
        this.downloadDir = downloadDir
        this.consulVersion = consulVersion
        this.consulPorts = mergePorts(ports, customConfig)
        makeDI()
    }

    private static ConsulPorts mergePorts(ConsulPorts.ConsulPortsBuilder ports, String customConfig) {
        def extraPorts = parseCustomConfig(customConfig)

        extraPorts.collect { it ->
            switch (it.key) {
                case "dns": ports = ports.withDnsPort(it.value); break
                case "http": ports = ports.withHttpPort(it.value); break
                case "rpc": ports = ports.withRpcPort(it.value); break
                case "serf_lan": ports = ports.withSerfLanPort(it.value); break
                case "serf_wan": ports = ports.withSerfWanPort(it.value); break
                case "server": ports = ports.withServerPort(it.value); break
            }
        }
        ports.build()
    }

    private static def parseCustomConfig(String customConfig) {
        if (customConfig == null || customConfig.isEmpty()) {
            return [:]
        }

        def parser = new JsonSlurper().setType(JsonParserType.LAX)
        return parser.parseText(customConfig)["ports"]
    }

    private void makeDI() {
        binaryRepository = new HttpBinaryRepository()
        unzip = new AntUnzip()
    }

    ConsulProcess start() {
        logger.info("Starting new Consul process.")
        checkInitialState()

        started = true

        if (!isBinaryDownloaded()) {
            downloadAndUnpackBinary()
        }

        createBasicConfigFile(consulPorts)
        if (customConfig != null) {
            createExtraConfigFile()
        }

        String downloadDirAsString = downloadDir.toAbsolutePath().toString()

        String pathToConsul = "$downloadDirAsString/consul"
        if ('windows' == OsResolver.resolve()) {
            pathToConsul = "$downloadDirAsString/consul.exe"
        }

        String[] command = [pathToConsul,
                            "agent",
                            "-data-dir=$dataDir",
                            "-dev",
                            "-config-dir=$configDir",
                            "-advertise=127.0.0.1",
                            "-log-level=$logLevel.value",
                            "-http-port=${consulPorts.httpPort}"]

        ConsulProcess process = new ConsulProcess(dataDir, consulPorts,
                new ProcessBuilder()
                        .directory(downloadDir.toFile())
                        .command(command)
                        .inheritIO()
                        .start())

        logger.info("Starting Consul process on port {}", consulPorts.httpPort)
        new ConsulWaiter(consulPorts.httpPort).awaitUntilConsulStarted()
        logger.info("Consul process started")

        return process
    }

    private void checkInitialState() {
        if (started) {
            throw new EmbeddedConsulException('This Consul Starter instance already started Consul process. Create new ConsulStarter instance')
        }
        try {
            IOGroovyMethods.withCloseable(new Socket("localhost", consulPorts.httpPort), {
                it-> throw new EmbeddedConsulException("Port ${consulPorts.httpPort} is not available, cannot start Consul process.")
            })
        } catch (IOException ex) {
            // socket is free - everything ok
        }
    }

    private void downloadAndUnpackBinary() {
        File file = new File(downloadDir.toAbsolutePath().toString(), 'consul.zip')
        logger.info("Downloading archives into: {}", file.toString())

        File archive = binaryRepository.getConsulBinaryArchive(consulVersion, file)

        logger.info("Unzipping binaries into: {}", downloadDir.toString())
        unzip.unzip(archive, downloadDir.toFile())
    }

    private void createBasicConfigFile(ConsulPorts consulPorts) {
        File portsConfigFile = new File(configDir.toFile(), "basic_config.json")
        logger.info("Creating ports configuration file: {}", portsConfigFile.toString())

        portsConfigFile << """
            {
                "ports": {
                    "dns": ${consulPorts.dnsPort},
                    "rpc": ${consulPorts.rpcPort},
                    "serf_lan": ${consulPorts.serfLanPort},
                    "serf_wan": ${consulPorts.serfWanPort},
                    "server": ${consulPorts.serverPort}
                },
                "disable_update_check": true,
                "performance": {
                    "raft_multiplier": 1
                }
            }
        """
    }

    private void createExtraConfigFile() {
        File customConfigFile = new File(configDir.toFile(), "extra_config.json")
        logger.info("Creating custom configuration file: {}", customConfigFile.toString())
        customConfigFile << customConfig
    }

    private boolean isBinaryDownloaded() {
        return new File(downloadDir.toString(), "consul").exists()
    }
}