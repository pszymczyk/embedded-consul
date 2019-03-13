package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.OsResolver
import com.pszymczyk.consul.infrastructure.client.ConsulClientFactory
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

class ConsulStarter {

    private static final Logger logger = LoggerFactory.getLogger(ConsulStarter.class)

    private final Path dataDir
    private final Path downloadDir
    private final Path configDir
    private final CustomConfig customConfig
    private final Logger customLogger
    private final ConsulPorts consulPorts
    private final String advertise
    private final String bind
    private final String token
    private final Integer waitTimeout
    private final ConsulLogHandler logHandler
    private final ConsulDownloader consulDownloader
    private final ConsulProcessCommandFactory consulProcessCommandFactory

    private boolean started = false

    @PackageScope
    ConsulStarter(Path dataDir,
                  Path downloadDir,
                  Path configDir,
                  CustomConfig customConfig,
                  Logger customLogger,
                  ConsulPorts.ConsulPortsBuilder ports,
                  String advertise,
                  String bind,
                  String token,
                  Integer waitTimeout,
                  ConsulDownloader consulDownloader, ConsulProcessCommandFactory consulProcessCommandFactory) {
        this.customLogger = customLogger
        this.configDir = configDir
        this.customConfig = customConfig
        this.dataDir = dataDir
        this.downloadDir = downloadDir
        this.consulPorts = mergePorts(ports)
        this.advertise = advertise
        this.bind = bind
        this.token = token
        this.waitTimeout = waitTimeout

        this.logHandler = new ConsulLogHandler(customLogger)
        this.consulDownloader = consulDownloader
        this.consulProcessCommandFactory = consulProcessCommandFactory
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

        String[] command = consulProcessCommandFactory.createConsulProcessCommand(pathToConsul, consulPorts)

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


    File getConsulBinary() {
        return consulDownloader.getConsulBinary()
    }
}