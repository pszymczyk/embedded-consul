package com.pszymczyk.consul

import com.pszymczyk.consul.infrastructure.ConsulWaiter
import com.pszymczyk.consul.infrastructure.OsResolver
import com.pszymczyk.consul.infrastructure.client.ConsulClientFactory
import com.pszymczyk.consul.infrastructure.client.SimpleConsulClient
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConsulStarter {

    private static final Logger logger = LoggerFactory.getLogger(ConsulStarter.class)

    private final UserInput userInput

    private ConsulLogHandler logHandler
    private ConsulStarterState consulStarterState
    private BinariesManager binariesManager
    private ConfigFilesFactory configFilesFactory
    private ConsulProcessCommandFactory consulProcessCommandFactory
    private InnerProcessFactory innerProcessFactory

    @PackageScope
    ConsulStarter(UserInput userInput) {
        this.userInput = userInput
        makeDI()
    }

    private void makeDI() {
        logHandler = new ConsulLogHandler(userInput.customLogger)
        consulStarterState = new ConsulStarterState(userInput.bind, userInput.consulPorts.httpPort)
        binariesManager = new BinariesManager(userInput.downloadDir, userInput.consulVersion)
        configFilesFactory = new ConfigFilesFactory(userInput.consulPorts, userInput.customConfig, userInput.configDir)
        consulProcessCommandFactory = new ConsulProcessCommandFactory(
                binariesManager.binaryPath,
                userInput.dataDir,
                userInput.configDir,
                userInput.advertise,
                userInput.client,
                userInput.logLevel,
                userInput.consulPorts,
                userInput.bind,
                userInput.startJoin,
                userInput.customConfig)
        innerProcessFactory = new InnerProcessFactory(userInput.downloadDir)
    }

    ConsulProcess start() {
        logger.info("Starting new Consul process.")

        consulStarterState.start()
        binariesManager.ensureConsulBinaries()
        configFilesFactory.createConfigFiles()
        String[] command = consulProcessCommandFactory.create()
        Process innerProcess = innerProcessFactory.create(command)
        logHandler.handleStream(innerProcess.getInputStream())
        SimpleConsulClient consulClient = ConsulClientFactory.newClient(userInput.advertise, userInput.consulPorts.httpPort, Optional.ofNullable(userInput.token))
        ConsulWaiter consulWaiter = new ConsulWaiter(userInput.advertise, userInput.consulPorts.httpPort, consulClient, Optional.ofNullable(userInput.waitTimeout))
        ConsulProcess process = new ConsulProcess(userInput.dataDir, userInput.consulPorts, userInput.advertise, innerProcess, consulClient, consulWaiter, logHandler)

        logger.info("Starting Consul process on port {}", userInput.consulPorts.httpPort)
        consulWaiter.awaitUntilConsulStarted()
        logger.info("Consul process started")

        userInput.services.each { consulClient.register(it) }

        return process
    }

    /**
     * It's internal ConsulStarter property - will be hidden in coming versions
     */
    @Deprecated
    File getConsulBinary() {
        String consulBinaryName = OsResolver.resolve().equals("windows") ? "consul.exe" : "consul"
        return new File(userInput.downloadDir.toString(), consulBinaryName)
    }
}
