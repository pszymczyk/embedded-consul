package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.OsResolver
import com.pszymczyk.consul.infrastructure.client.ConsulClientFactory
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import com.pszymczyk.consul.starter.ConsulDownloader
import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulStarter {

    private static final Logger logger = LoggerFactory.getLogger(ConsulStarter.class)
    private static final Random random = new Random()

    private final Path dataDir
    private final Path downloadDir
    private final Path configDir
    private final CustomConfig customConfig
    private final LogLevel logLevel
    private final Logger customLogger
    private final ConsulPorts consulPorts
    private final String startJoin
    private final String advertise
    private final String client
    private final String bind
    private final String token
    private final Integer waitTimeout
    private final ConsulLogHandler logHandler
    private final ConsulDownloader consulDownloader

    private boolean started = false

    @PackageScope
    ConsulStarter(Path dataDir,
                  Path downloadDir,
                  Path configDir,
                  CustomConfig customConfig,
                  LogLevel logLevel,
                  Logger customLogger,
                  ConsulPorts.ConsulPortsBuilder ports,
                  String startJoin,
                  String advertise,
                  String client,
                  String bind,
                  String token,
                  Integer waitTimeout,
                  ConsulDownloader consulDownloader) {
        this.logLevel = logLevel
        this.customLogger = customLogger
        this.configDir = configDir
        this.customConfig = customConfig
        this.dataDir = dataDir
        this.downloadDir = downloadDir
        this.consulPorts = mergePorts(ports)
        this.startJoin = startJoin
        this.advertise = advertise
        this.client = client
        this.bind = bind
        this.token = token
        this.waitTimeout = waitTimeout

        this.logHandler = new ConsulLogHandler(customLogger)
        this.consulDownloader = consulDownloader
    }

    private ConsulPorts mergePorts(ConsulPorts.ConsulPortsBuilder ports) {
        def extraPorts = customConfig.get("ports")

        extraPorts.collect { it ->
            switch (it.key) {
                case "dns": ports = ports.withDnsPort(it.value); break
                case "http": ports = ports.withHttpPort(it.value); break
                case "serf_lan": ports = ports.withSerfLanPort(it.value); break
                case "serf_wan": ports = ports.withSerfWanPort(it.value); break
                case "server": ports = ports.withServerPort(it.value); break
                case "grpc": ports = ports.withGRpcPort(it.value); break
            }
        }
        ports.build()
    }

    ConsulProcess start() {
        logger.info("Starting new Consul process.")
        checkInitialState()

        started = true

        consulDownloader.downloadConsul()

        createBasicConfigFile(consulPorts)
        if (!customConfig.isEmpty()) {
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
                            "-advertise=$advertise",
                            "-client=$client",
                            "-log-level=$logLevel.value",
                            "-http-port=${consulPorts.httpPort}",
                            "-grpc-port=${consulPorts.grpcPort}"]

        if (bind != null) {
            command += "-bind=$bind"
        }

        if (startJoin != null) {
            command += "-join=$startJoin"
        }

        if (customConfig.get("node_id") == null) {
            command += ["-node-id=" + randomNodeId()]
        }

        if (customConfig.get("node_name") == null) {
            command += ["-node=" + randomNodeName()]
        }

        Process innerProcess = new ProcessBuilder()
            .directory(downloadDir.toFile())
            .command(command)
            .inheritIO()
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()

        logHandler.handleStream(innerProcess.getInputStream())
        SimpleConsulClient consulClient = ConsulClientFactory.newClient(advertise, consulPorts.httpPort, Optional.ofNullable(token))
        ConsulWaiter consulWaiter = new ConsulWaiter(advertise, consulPorts.httpPort, consulClient, Optional.ofNullable(waitTimeout))
        ConsulProcess process = new ConsulProcess(dataDir, consulPorts, advertise, innerProcess, consulClient, consulWaiter, logHandler)
        logger.info("Starting Consul process on port {}", consulPorts.httpPort)
        consulWaiter.awaitUntilConsulStarted()
        logger.info("Consul process started")
        return process
    }


    private void checkInitialState() {
        if (started) {
            throw new EmbeddedConsulException('This Consul Starter instance already started Consul process. Create new ConsulStarter instance')
        }
        try {
            IOGroovyMethods.withCloseable(new Socket(bind ?: "localhost", consulPorts.httpPort), {
                it-> throw new EmbeddedConsulException("Port ${consulPorts.httpPort} is not available, cannot start Consul process.")
            })
        } catch (IOException ex) {
            // socket is free - everything ok
        }
    }


    private void createBasicConfigFile(ConsulPorts consulPorts) {
        File portsConfigFile = new File(configDir.toFile(), "basic_config.json")
        logger.info("Creating ports configuration file: {}", portsConfigFile.toString())

        portsConfigFile.text = """
            {
                "ports": {
                    "dns": ${consulPorts.dnsPort},
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
        customConfigFile.text = customConfig.asString()
    }

    private static String randomNodeId() {
        return randomHex(8) + "-" + randomHex(4) + "-" + randomHex(4) + "-" + randomHex(4) + "-" + randomHex(12);
    }

    private static String randomNodeName() {
        return "node-" + randomHex(10)
    }

    private static String randomHex(int len) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < len; i++) {
            sb.append(Long.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }

    File getConsulBinary() {
        return consulDownloader.getConsulBinary()
    }
}